#!/usr/local/bin/perl
#
########################################
#
# Simple perl client for treqs V1.5
#
#
# 2011-05-30  pbrinett   Initial version
# 2011-09-20  pbrinett   Add rfcp retries when rfio server
#                        return ECONNREFUSED
#

use strict;
use English;
use Getopt::Std;
use File::Basename;
use Log::Log4perl qw(:easy);
use DBI;
use Socket;
use Sys::Hostname;
use Data::Dumper; 
use Errno;

# version of this client 
our $VERSION=0.2;

### Constant and default variable

# Default parameters
my %TreqsParams = (

#   TEST SYSTEM
#	TreqsDBHost => 'tresqstest',
#	TreqsDBName => 'jtreqs' ,
#	TreqsDBUser => 'treqsc',
#	TreqsDBPassword => '' ,

#   PRODUCTION SYSTEM 
	TreqsDBHost => 'treqsprod',
	TreqsDBName => 'treqsjobs' ,
	TreqsDBUser => 'treqsc',
	TreqsDBPassword => '' ,

	
	TreqsClientVersion => "treqs perl v$VERSION" ,
	
	RequestTable => 'jrequests',
	
	# The first poll after request submission (s)
	FirstPoll => 30 ,
	# Poll Intervall (s)
	PoolInterval => 60 ,
 	# Timeout (s) 6h
 	Timeout => 21600 ,
 	
 	# Transfert options with RFIO
 	TransfertCommand  => '/usr/local/bin/rfcp' ,
 	DefaultRfioServer => 'ccrfiotreqs'
	
);

# Request status for new schema
# got from fr.in2p3.cc.storage.treqs.model.RequestStatus.java

my %RequestStatus = (
  
    # A brand new request.
    CREATED => 100,
    # File reading failed.
    FAILED => 160 ,
    # File is submitted to HPSS for staging.
    QUEUED => 120,
    # File is staged on HPSS disks.
    STAGED => 140,
    # File is registered in a queue.
    SUBMITTED => 110,
    # The file is already on disk.
    ON_DISK =>  150
);

# Create the reverse status
my %RequestStatusById = ();
foreach my $k (keys %RequestStatus)
{
	my $v=$RequestStatus{$k};
	$RequestStatusById{$v}=$k;
}


# Log4perl level
my $Log4PerlLayout = '%d [%P] %p> %m';
#

my $database; # global database name
my $dbhost;   # global database hostname
my $dbuser;   # global database username
my $dbpasswd; # global database password
my $dbh; # Global database handler

my $username; # username (default logname)
my $hostname; # hostname where the client is run
my $email;    # email (defaut username)

my $hsmfilename; # file to stage
my $localpath;   # destination file/directory
my $rfioserver;  # rfioserver to use

# Prestage mode
my $prestage_only=0;
# rfio transfert mode
my $do_transfert=0;


# RFCP max tries
my $rfcp_max_tries=3;
my $rfcp_retry_count=0;
# sleep 55-65 s
my $rfcp_sleep_delay=55+int(rand(10));

# timeout for staging 
my $timeout = $TreqsParams{'Timeout'} ;
my $request_insertion_time;  # counter for timeout

my %opts;	# command line options

##########################################

#
# Display usage
#
sub usage()
{

	my $progname=basename $0;
	
	print <<EOFUSAGE
	
Simple client for Treqs Server > v1.5.
Client version: $VERSION

Usage:
\$ $progname [ -p filelist.txt ] [ -t duration ]
            [ -h ] [ -v | -d ]  [ -u username ] [ -e mail ]
            [ [rfioserv:]/hsm/path/to/file [ /local/path ] ]
         
Options:
    [ -h ] Print this help
    [ -v ] Verbose mode (default: only warning messages are displayed)
    [ -p filelist.txt ] Prestage a list of files (don't wait for reply)
    [ -t duration ] Timout in second (default $TreqsParams{'Timeout'} s)
    [ -u username ] Specify an username (default: logname)
    [ -e email ] Specify an email address (default: username)
    [ -d ] Display debug messages 

    The HSM filename is not used in case of prestaging mode
	
Environment:

  These environment variable are used to select the Treqs Server  
  
  	TreqsDBHost     (Default: $TreqsParams{'TreqsDBHost'} )
	TreqsDBName     (Default: $TreqsParams{'TreqsDBName'} )
	TreqsDBUser     (Default: $TreqsParams{'TreqsDBUser'} )
	TreqsDBPassword (hidden)

Example:
  - Stage a single file within a limit of 1h
  \$ $progname -t 3600 /hsm/path/to/file
  
  - Stage and transfert a file using the default rfio server ($TreqsParams{'DefaultRfioServer'})
  \$ $progname /hsm/path/to/file /scratch
  
  - Stage and transfert a file using a scpecific rfio server 
  \$ $progname cchpssdcache:/hsm/path/to/file /scratch
  
  - Submit a list of file to prestage as user 'lebrun'
  \$ $progname -p filelist.txt -u lebrun
  
EOFUSAGE
;	
	
}

#
# get the local IP adress
#
# RETURN:
#			(string) IP adress
#			undef if error
#

sub getHostIp()
{
	my $ip_address="";
	my $host = hostname();
	my $packed_ip = gethostbyname($host);
	
	if (defined $packed_ip) {
		$ip_address = inet_ntoa($packed_ip);
	 }
	 
	 DEBUG "Resolved IP for hostname: $host -> $ip_address\n";
	 
	return $ip_address; 
}

#
# Insert a single file Request into the Treqs database
#
# This function use the global $dbh DB handler
# to access to the database
#
# IN:
#			file =>    (string)   : hsm filename
# 			user =>    (string)   : username
#			email =>   (string)   : email adress (optional)
#
#
# RETURN:
#			request_id (int)
#			0 : Failed
#
sub insertFileRequest()
{
	my %args =(@_);
	my $email = 'NULL';
	
	if (! defined $args{'file'})
	{
		ERROR "filename not defined \n";
		return 0;
	}
	my $file=$args{'file'};
	
	if (! defined $args{'user'})
	{
		WARN "user not defined \n";
		$args{'user'}="undef";
	}
	my $user=$args{'user'} ;
	
	if ( defined $args{'email'})
	{
		$email="'". $args{'email'} . "'";
	}
	
	my $client=&getHostIp();
	my $version=$TreqsParams{'TreqsClientVersion'};
	
	
	my $request = "
		INSERT INTO $database.$TreqsParams{'RequestTable'}
			(file,creation_time,user,client,email,version,status)
		VALUES
			('$file' , NOW(), '$user', '$client', $email, '$version', $RequestStatus{'CREATED'})  
		;
	";

	$request =~ s/\n//g;
	DEBUG "Insert file request: $request\n";
	
	my $sth = $dbh->prepare( $request);
	if (! defined $sth)
	{
		FATAL "Unable to prepare insert request : ($DBI::err) $DBI::errstr \n";
		# FATAL "Program terminating ...\n";
		$dbh->disconnect();
		exit 1;
	}
	my $rc = $sth->execute();
	if (! defined $rc)
	{
		FATAL "Execution failed : ($DBI::err) $DBI::errstr \n";
		# FATAL "Program terminating ...\n";
		$dbh->disconnect();
		exit 1;
	}
	
	my $request_id= $dbh->last_insert_id(undef,undef,undef,undef);
	INFO "Request for file $file succefully inserted, request id is ($request_id)\n"; 
	
	$sth->finish();
	return $request_id;
	
}

#
# Poll the status of a previously inserted file request
#
# This function use the global $dbh DB handler
# to access to the database
#
# IN:
#			request_id (int)
#
# RETURN:
#			request_status (int)
#			0 : Failed
#

sub pollFileRequest()
{
	my %args =(@_);
	my $status;
	my $errorcode;
	my $message;
	
	if (! defined $args{'request_id'})
	{
		ERROR "request_id not defined \n";
		return 0;
	}
	
	my $request_id=$args{'request_id'};
	my $request = "select * from $database.$TreqsParams{'RequestTable'} where id = $request_id";
   
    DEBUG "Pool file request: $request\n";	 

	my $sth = $dbh->prepare( $request);
	if (! defined $sth)
	{
		FATAL "Unable to prepare pool request : ($DBI::err) $DBI::errstr \n";
		# FATAL "Program terminating ...\n";
		$dbh->disconnect();
		exit 1;
	}
	my $rc = $sth->execute();
	if (! defined $rc)
	{
		FATAL "Execution failed : ($DBI::err) $DBI::errstr \n";
		# FATAL "Program terminating ...\n";
		$dbh->disconnect();
		exit 1;
	}
	# get the rown
	my $row_hash = $sth->fetchrow_hashref();
	$sth->finish();
	
	if ($row_hash)
	{
		# DEBUG (Dumper($row_hash));
		$status=$row_hash->{'status'};
		$errorcode=$row_hash->{'errorcode'};
		$message=$row_hash->{'message'};
		
	}
	else
	{
		WARN "Request id ($request_id) not found !\n";
		return 0;	
	}
	
	INFO "Request id ($request_id) status : $status ($RequestStatusById{$status})\n";
	return ($status,$errorcode,$message);
	
}

#
# Run the rfcp command to transfert
# the file
#
# IN:
#			rfio_server  (string)
#			hsm_filename (string)
#			local_dest   (string)
#
# RETURN:
#			return code of rfcp command
#			
sub doRfcp()
{
	my %args =(@_);
	my $rfio_cmd=$TreqsParams{'TransfertCommand'};
	my $cmdline;	
	my $rc=0;

	# check max_tries limit

	
	if (! defined $args{'rfio_server'})
	{
		ERROR "rfio_server not defined \n";
		return 0;
	}
	my $rfio_server=$args{'rfio_server'};
	
	if (! defined $args{'hsm_filename'})
	{
		ERROR "hsm_filename not defined \n";
		return 0;
	}
	my $hsm_filename=$args{'hsm_filename'} ;
	
	if (! defined $args{'local_dest'})
	{
		ERROR "local_dest not defined \n";
		return 0;
	}
	my $local_dest=$args{'local_dest'} ;

	$cmdline="$rfio_cmd $rfio_server:$hsm_filename $local_dest 2>&1";
	
	INFO "Will run $cmdline \n";	
	
	system($cmdline);
	$rc=$? >> 8;

	# RFIO server temporary overloaded 
	# sleep ~ 60 s and retries until rfcp_max_tries 
	# limit reached 
	if ($rc == Errno::ECONNREFUSED)
	{
		if ($rfcp_max_tries == 0)
		{
			WARN "Max tries limit reached, exiting ...\n";
			return Errno::ECONNREFUSED;
		}
		$rfcp_max_tries=$rfcp_max_tries-1;

		$rfcp_retry_count=$rfcp_retry_count+1;
		my $sleep_delay=$rfcp_sleep_delay*$rfcp_retry_count;
		
		INFO "RFIO server temporary overloaded, sleep $sleep_delay s, $rfcp_max_tries tries left\n";
		sleep ($sleep_delay);
		return (&doRfcp(@_));

	}
	
	return $rc;
	
}


#
# Connect to the database
# 
# This function use global variables :
#
#		$database
#		$dbhost
#		$dbuser
#		$dbpasswd
#
# On success, $dbh variable is updated
#
# In case of failure, exit the program
#
 
sub dbConnect()
{
	# connect to the database
	my $dsn = "DBI:mysql:database=$database;host=$dbhost";
	$dbh = DBI->connect($dsn, $dbuser, $dbpasswd ,
		 { PrintError => 0 }
		);
	
	# check error
	if (! defined $dbh)
	{
		FATAL "Unable to connect to DB($dsn) : ($DBI::err) $DBI::errstr \n";
		# FATAL "Program terminating ...\n";
		exit 1;
	}
	DEBUG "Connection to Database ($dsn) succeed\n";
}

sub dbDisconnect()
{
	$dbh->disconnect();
	DEBUG "Database disconnected\n";
}


##########################################



#
# MAIN
#

# Get command line parameters
if (! getopts('hdvp:t:u:e:', \%opts) || defined $opts{'h'}) 
{
        usage();
        exit 1;
}

#
# check log level and initialise Log4Perl
#
if (defined $opts{'d'})	   # DEBUG
{
	Log::Log4perl->easy_init(
		{
		level => $DEBUG,
		layout => $Log4PerlLayout
		} 
	);
}
elsif (defined $opts{'v'}) # VERBOSE
{
	Log::Log4perl->easy_init(
	{
		level => $INFO,
		layout => $Log4PerlLayout
	} 
	);
	
}
else # Default : WARN
{
	Log::Log4perl->easy_init(
	{
		level => $WARN,
		layout => $Log4PerlLayout
	} 
	);
}


# Display version 
INFO ("Treqs Client v$VERSION \n");


# check prestage mode
if (defined $opts{'p'})
{
	$prestage_only=1;
	if ( ! -T $opts{'p'} || -z $opts{'p'} )
	{
		ERROR "The file $opts{'p'} is not a text file !\n";
		usage();
		exit 1; 
	}
	INFO "Prestaging file list : $opts{'p'} \n";
}


# check timout option
if (defined $opts{'t'})
{
	$timeout=$opts{'t'};
	INFO "Set timout to ($timeout s) \n";
}

# set the username
# getlogin failed sometime on Solaris
$username =  (getpwuid($<))[0] || getlogin() || $ENV{'USER'} || "undef" ;

if ($opts{'u'})
{
	$username=$opts{'u'};
	DEBUG "username set to $username \n";
}

# set the email
$email = $username;
if ($opts{'e'})
{
	$email =$opts{'e'};
	DEBUG "email set to $email\n";
}

#
# Check command line argument
#
if (! defined ($ARGV[0]) && ! $prestage_only)
{
	ERROR "File to stage not found";
	usage();
	exit 1; 
}
else
{
	#
	# Check if 1 argument use rfio link syntax:
	# i.e : rfioserver:/path/to/file
	
	my ($rfio_l,$file_l)=split (":",$ARGV[0],2);
	
	# not rfio: like
	if ($file_l eq "")
	{
		$hsmfilename=$ARGV[0];
		$rfioserver = $TreqsParams{'DefaultRfioServer'};
	}
	else
	{
		$hsmfilename=$file_l;
		$rfioserver=$rfio_l;
	}
	
	DEBUG "File to stage: $hsmfilename \n";
}

#
# Check if destination path is specified
#
if ( defined ($ARGV[1]) )
{
	$localpath=$ARGV[1];
	$do_transfert=1;
	
	DEBUG "localpath is $localpath, transfert is enable with RFIO on $rfioserver\n"; 
}



####
####  Initialize database connections
####


# Use environment variable if exist, else the default values
$database=$ENV{'TreqsDBName'}     || $TreqsParams{'TreqsDBName'};
$dbhost=$ENV{'TreqsDBHost'}       || $TreqsParams{'TreqsDBHost'};
$dbuser=$ENV{'TreqsDBUser'}       || $TreqsParams{'TreqsDBUser'};
$dbpasswd=$ENV{'TreqsDBPassword'} || $TreqsParams{'TreqsDBPassword'};

DEBUG "Client IP adress is: " . &getHostIp() . " \n";
DEBUG "Username is: " . $username . " \n";
INFO  "Connect to TREQS SERVER ($dbhost) on DB ($database) as $dbuser\n";
INFO  "Timout set to ($timeout s) \n";
INFO  "First pool interval is $TreqsParams{'FirstPoll'}s , Pool interval is $TreqsParams{'PoolInterval'} \n";


# connect to the database
&dbConnect();

#### Prestage mode 
#### process the list of files and exit


my $request_id =0;

if ($prestage_only)
{
	my $request_count=0;
	# procees the file list
	open PRESTAGE, $opts{'p'};
	while (<PRESTAGE>)
	{
		chomp;		# no newline     
		s/#.*//;	# no comments     
		s/^\s+//;	# no leading white
		s/\s+$//;	# no trailing white     
		next unless length;     # anything left?
			
		$hsmfilename=$_;
		$request_id  = &insertFileRequest( file => $hsmfilename , user => $username , email => "$email");
		if ($request_id == 0){
			FATAL "Request insertion failed !\n";
			&dbDisconnect();
			# FATAL "Program terminating\n";
			exit 1;	
		}
		$request_count++;	     
	} 
	WARN "$request_count prestage requests has been inserted for user $username\n";
	&dbDisconnect();
	WARN "Program succefully ended\n";
	exit 0; # exit after insertion 
	
}

#
# Insert requests
#

$request_id = &insertFileRequest( file => $hsmfilename , user => $username , email => "$email");

if ($request_id == 0){
	FATAL "Request insertion failed !\n";
	&dbDisconnect();
	# FATAL "Program terminating\n";
	exit 1;	
}

# store the insertion time to compute the timeout 
$request_insertion_time=time;
my $now=time;
# sleep firstpool
DEBUG "Sleep $TreqsParams{'FirstPoll'} s before first pool\n";
sleep $TreqsParams{'FirstPoll'};
my ($status,$errorcode,$message) = &pollFileRequest( request_id  => $request_id );
&dbDisconnect();

#
# Main poll loop
#
$now=time;
while ( (
		$status != $RequestStatus{'FAILED'}   && 
		$status != $RequestStatus{'ON_DISK'}  &&
		$status != $RequestStatus{'STAGED'}   
		) && 
		($now - $request_insertion_time) < $timeout
	)
{
	DEBUG "status is $status, sleep $TreqsParams{'PoolInterval'} s\n";
	sleep $TreqsParams{'PoolInterval'};
	&dbConnect();
	($status,$errorcode,$message) = &pollFileRequest( request_id  => $request_id );
	&dbDisconnect();
	$now=time;	
}


#
# check status
# 
my $timeout_exceed=1;
if (($now - $request_insertion_time) < $timeout)
{
	$timeout_exceed=0;
}

DEBUG "Loop ended, status is : $status ($RequestStatusById{$status}) Timout exceed : $timeout_exceed \n";

#
# Process request status
#
my $rc=0;
if ($status == $RequestStatus{'FAILED'})
{
	ERROR "Staging of file $hsmfilename failed with error $errorcode ($message)\n";
	$rc=1;
}
elsif ($status == $RequestStatus{'STAGED'})
{
	INFO "File $hsmfilename succefully staged\n";
	$rc=&doRfcp(rfio_server => $rfioserver , hsm_filename => $hsmfilename, local_dest => $localpath ) if ($do_transfert);
}
elsif ($status == $RequestStatus{'ON_DISK'})
{
	INFO "File $hsmfilename allready on disk\n";
	$rc=&doRfcp(rfio_server => $rfioserver , hsm_filename => $hsmfilename, local_dest => $localpath ) if ($do_transfert);
}
elsif ($timeout_exceed)
{
	WARN "Timout (${timeout}s) exceed for staging $hsmfilename\n";
	$rc=&doRfcp(rfio_server => $rfioserver , hsm_filename => $hsmfilename, local_dest => $localpath ) if ($do_transfert);
}
else ### Unknown status
{
	ERROR "Unknown status for staging file $hsmfilename: ($status,$errorcode,$message)\n";
	$rc=1;
}

if ($rc !=0)
{
	ERROR "Operation failed: return code is ($rc)\n";
}
INFO "Program terminating: return code is ($rc)...\n";
exit $rc;


