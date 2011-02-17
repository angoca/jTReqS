package Smurf::Client::Config::treqs_jobs;

use strict;
use warnings;
no warnings 'redefine';

#####
#
# Format of this data :
# TREQSJOBS/done/atlagrid => [$ts,nbfiles,totalsize]
# TREQSJOBS/failed/atlagrid => [$ts,nbfiles,totalsize]
# 
# Collects the amount of data staged from 0:00 till now
#
#

sub get {
  use DBI;
  my $ts = time;
  my $pfix = "TREQSJOBS";
  my @users = ();
  my %data = ("$pfix/done/all" => [$ts,0,0] ,"$pfix/failed/all" => [$ts,0,0],
              "$pfix/done/other"=>[$ts,0,0] ,"$pfix/failed/other"=>[$ts,0,0]); 

  my $database="treqsconfig";
  my $hostname="cctreqs";
  my $user="treqsweb";
  my $sock="";#/var/spool/treqs/mysql/tmp/mysql.sock";
 
  my $password='';

  # At first get the available users
  my $dsn = "DBI:mysql:database=$database;host=$hostname;mysql_socket=$sock";
  my $dbh = DBI->connect($dsn, $user, $password) or return {};

  # Get the defined users
  my $rq = "SELECT DISTINCT user FROM allocation";
  my $sth = $dbh->prepare( $rq );
  $sth->execute() or return {};
  while (my $rowref = $sth->fetchrow_hashref()){
    my %row = %$rowref;
    push @users, $row{"user"};
  }
  $sth->finish() or return {};
  $dbh->disconnect() or return {};

  # Initialize the hashes
  foreach(@users){
    $data{"$pfix/done/$_"} = [$ts,0,0];
    $data{"$pfix/failed/$_"} = [$ts,0,0];
  }

  # Count the successful requests and the size of them
  $database="treqsjobs";
  $dsn = "DBI:mysql:database=$database;host=$hostname;mysql_socket=$sock";
  $dbh = DBI->connect($dsn, $user, $password) or return {};

  $rq = "SELECT count(1) as count,sum(size) as size,user FROM requests WHERE status=? AND day(end_time) = day( CURRENT_TIMESTAMP ) GROUP BY user";
  $sth = $dbh->prepare( $rq );
  $sth->execute('14') or return {}; # 14 is the database representation for a successfully staged file

  while (my $rowref = $sth->fetchrow_hashref()){
    my %row = %$rowref;
    my $owner = "$pfix/done/";
    if (exists {map { $_ => 1 } @users}->{$row{"user"}}){
      $owner = $owner.$row{"user"};
      ${$data{$owner}}[1] = $row{"count"};
      ${$data{$owner}}[2] = $row{"size"};
    }
    else {
      $owner = $owner."other";
      ${$data{$owner}}[1] += $row{"count"};
      ${$data{$owner}}[2] += $row{"size"};
    }
    ${$data{$owner}}[0] = $ts;
    ${$data{"$pfix/done/all"}}[1]+=$row{"count"};
    ${$data{"$pfix/done/all"}}[2]+=$row{"size"};
    ${$data{"$pfix/done/all"}}[0]= $ts;
  }

  $sth->finish();

  $sth->execute('16'); # 14 is the database representation for a failed request

  while (my $rowref = $sth->fetchrow_hashref()){
    my %row = %$rowref;
    my $owner = "$pfix/failed/";
    if (exists {map { $_ => 1 } @users}->{$row{"user"}}){
      $owner = $owner.$row{"user"};
      ${$data{$owner}}[1] = $row{"count"};
      ${$data{$owner}}[2] = $row{"size"};
    }
    else {
      $owner = $owner."other";
      ${$data{$owner}}[1] += $row{"count"};
      ${$data{$owner}}[2] += $row{"size"};
    }
    ${$data{$owner}}[0] = $ts;
    ${$data{"$pfix/failed/all"}}[1]+=$row{"count"};
    ${$data{"$pfix/failed/all"}}[2]+=$row{"size"};
    ${$data{"$pfix/failed/all"}}[0]= $ts;
  }

  $sth->finish();
  $dbh->disconnect();
  return \%data;
}
1;
