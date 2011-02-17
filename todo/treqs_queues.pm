package Smurf::Client::Config::treqs_queues;

use strict;
use warnings;
no warnings 'redefine';

=head1 NAME

Smurf::Client::Config::TReqS::treqs_queues - Smurf Client probe for TReqS.
This probe counts the active queues (or waiting to be activated)
=head1 SYNOPSIS

To be used with Smurf::Client by using the helper C<smurfc>.

Format of this probe :
TREQSQUEUES/1/atlagrid => [$ts,nbactive,nbwaiting]
 
=cut

sub get {
  use DBI;
  my $pfix = "TREQSQUEUES";
  my %data = (); 
  my $ts = time;

  my $database="treqsconfig";
  my $hostname="cctreqs";
  my $user="treqsweb";
  my $sock="";#/var/spool/treqs/mysql/tmp/mysql.sock";
  my %qstate = (20,2,    # 20 becomes 2
                21,1);   # 21 becomes 1
 
  my $password='';

  # At first get the available users
  my $dsn = "DBI:mysql:database=$database;host=$hostname;mysql_socket=$sock";
  my $dbh = DBI->connect($dsn, $user, $password) or return {};

  # Get the defined users
  my @users=();
  my $rq = "SELECT user,pvrid FROM allocation";
  my $sth = $dbh->prepare( $rq );
  $sth->execute() or return {};
  while (my $rowref = $sth->fetchrow_hashref()){
    my %row = %$rowref;
    # store the users
    push @users, $row{"user"} unless exists {map { $_ => 1 } @users}->{$row{"user"}};

    ${$data{"$pfix/".$row{"pvrid"}."/all"}}[0] = $ts;
    ${$data{"$pfix/".$row{"pvrid"}."/all"}}[1] = 0;
    ${$data{"$pfix/".$row{"pvrid"}."/all"}}[2] = 0;

    ${$data{"$pfix/".$row{"pvrid"}."/".$row{"user"}}}[0] = $ts;
    ${$data{"$pfix/".$row{"pvrid"}."/".$row{"user"}}}[1] = 0;
    ${$data{"$pfix/".$row{"pvrid"}."/".$row{"user"}}}[2] = 0;

    ${$data{"$pfix/".$row{"pvrid"}."/other"}}[0] = $ts;
    ${$data{"$pfix/".$row{"pvrid"}."/other"}}[1] = 0;
    ${$data{"$pfix/".$row{"pvrid"}."/other"}}[2] = 0;
  }
  $sth->finish();

  $dbh->disconnect();

  # Now list the inactive queues
  $database="treqsjobs";
  $dsn = "DBI:mysql:database=$database;host=$hostname;mysql_socket=$sock";
  $dbh = DBI->connect($dsn, $user, $password) or return {};

  $rq = "SELECT count(1) as count,sum(nbjobs) as nbj,owner,status,master_queue FROM queues WHERE status='20' OR status='21' GROUP BY owner,status,master_queue";
  $sth = $dbh->prepare( $rq );
  $sth->execute() or return {};

  while (my $rowref = $sth->fetchrow_hashref()){
    my %row = %$rowref;
    #{ print "COUCOU\n";
    #print $row{"status"}."=>".int($qstate{$row{"status"}}) . "\n";
    my $owner = "$pfix/".$row{"master_queue"}."/";
    if(exists {map { $_ => 1 } @users}->{$row{"owner"}})
    {
      $owner = $owner.$row{"owner"};
      ${$data{$owner}}[int($qstate{$row{"status"}})] = $row{"count"};
    }
    else
    {
      $owner = $owner."other";
      ${$data{$owner}}[int($qstate{$row{"status"}})] += $row{"count"};
    }

    ${$data{"$pfix/".$row{"master_queue"}."/all"}}[int($qstate{$row{"status"}})] += $row{"count"};
  }

  $sth->finish();
  $dbh->disconnect();

  foreach(keys %data){
    if(not defined $data{$_}[1]){
      $data{$_}[1]=0;
    }
    if(not defined $data{$_}[2]){
      $data{$_}[2]=0;
    }
  }
  return \%data;
}
1;
