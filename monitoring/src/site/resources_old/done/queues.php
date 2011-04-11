<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link rel="icon" href="../css/eyes.ico" type="image/ico"/> 
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script src="../js/sorttable.js"></script>
<title>TReqS Monitoring Page</title>
<style type="text/css">
<!--
@import url("../css/fancystyle.css");
-->
</style>
</head>
<body>
<img src="../css/treqsmon.png"/>
<?php
require_once "treqsmon.inc";
include "menu.php";

$user = $_GET['user'];

$used_drive = array();
mysql_connect($db_server, $db_user, $db_passwd);
@mysql_select_db($db_jobdatabase) or die ( "Unable to select database");
# TODO : status = dynamic
if($user){
  $query = "SELECT id,name,byte_size,nbjobs,nbdone,nbfailed,master_queue,creation_time,activation_time,owner,SEC_TO_TIME(UNIX_TIMESTAMP(CURRENT_TIMESTAMP)-UNIX_TIMESTAMP(activation_time)) as duration FROM queues WHERE status=21 and owner='$user' ORDER BY activation_time DESC";
}
else {
$query = "SELECT id,name,byte_size,nbjobs,nbdone,nbfailed,master_queue,creation_time,activation_time,owner,SEC_TO_TIME(UNIX_TIMESTAMP(CURRENT_TIMESTAMP)-UNIX_TIMESTAMP(activation_time)) as duration FROM queues WHERE status=21 ORDER BY activation_time DESC";
}
$result = mysql_query($query);
$num=mysql_numrows($result);

echo "<br><h2><a name=\"active\"/><b>Active Queues</b></h2>";
if($user){
  echo "<b>for user $user</b><br>";
}
echo " [<a href=\"archived_queues.php\">See archived queues</a>]<br>";

echo "<table id=\"gradient\" class=\"sortable\" summary=\"Active queues\">\n";
echo "<thead><tr>";
echo "<th>id</th><th>tape</th><th>master queue id</th><th>owner</th><th>requests (done/failed/total)</th><th>requests left</th><th>Size (MB)</th><th>creation time</th><th>activation time</th><th>duration from activation</th>\n";
echo "</tr></thead>";
echo "<tfoot>
  <tr>
  <td colspan='4'>Fetched $num rows</td>
  </tr>
  </tfoot>
  <tbody>\n";
$i=0;
while ($i < $num) {

  $id=mysql_result($result,$i,"id");
  $name=mysql_result($result,$i,"name");
  $jobs=mysql_result($result,$i,"nbjobs");
  $done_jobs=mysql_result($result,$i,"nbdone");
  $failed_jobs=mysql_result($result,$i,"nbfailed");
  $master_queue=mysql_result($result,$i,"master_queue");
  $creation_time=mysql_result($result,$i,"creation_time");
  $activation_time=mysql_result($result,$i,"activation_time");
  $owner=mysql_result($result,$i,"owner");
  $bs=round(mysql_result($result,$i,"byte_size")/1024/1024,0);
  $duration=mysql_result($result,$i,"duration");
  if (isset($used_drive["$master_queue/$owner"]) == false)
     $used_drive["$master_queue/$owner"]=0;
  $used_drive["$master_queue/$owner"]++;
  $i++;
  echo "<tr>";
  echo "<td><a href=\"queueinfo.php?id=$id\">$id</a></td><td><a href=\"tapeinfo.php?tape=$name\">$name</a></td><td>$pvr_id[$master_queue]</td><td>$owner</td><td>$done_jobs/";
  if($failed_jobs>0){
    echo "<span style=\"color:red\">$failed_jobs</span>";
  }
  else{
    echo "$failed_jobs";
  }
  echo "/$jobs</td><td>";
  echo $jobs-$failed_jobs-$done_jobs;
  echo "</td><td>$bs</td><td>$creation_time</td><td>$activation_time</td><td>$duration</td>\n";
  echo "</tr>\n";
}
echo "</tbody></table>";


if($user){
$query = "SELECT id,name,byte_size,nbjobs,master_queue,creation_time,owner,SEC_TO_TIME(UNIX_TIMESTAMP(CURRENT_TIMESTAMP)-UNIX_TIMESTAMP(creation_time)) as duration FROM queues WHERE status=20 and owner='$user' ORDER BY activation_time DESC";
} else {
$query = "SELECT id,name,byte_size,nbjobs,master_queue,creation_time,owner,SEC_TO_TIME(UNIX_TIMESTAMP(CURRENT_TIMESTAMP)-UNIX_TIMESTAMP(creation_time)) as duration FROM queues WHERE status=20 ORDER BY activation_time DESC";
}
$result = mysql_query($query);
$num=mysql_numrows($result);

echo "<h2><a name=\"waiting\"/>Waiting Queues</h2> ";
if($user){
  echo "<b>for user $user</b><br>";
}
echo "[<a href=\"archived_queues.php\">See archived queues</a>]<br>";
echo "<table id=\"gradient\" class=\"sortable\" summary=\"Waiting queues\">\n";
echo "<thead><tr>";
echo "<th>id</th><th>tape</th><th>master queue id</th><th>owner</th><th>file requests</th><th>Size (MB)</th><th>creation time</th><th>duration</th>\n";
echo "</tr></thead>";
echo "<tfoot>
  <tr>
  <td colspan='4'>Fetched $num rows</td>
  </tr>
  </tfoot>
  <tbody>\n";
$i=0;
while ($i < $num) {

  $id=mysql_result($result,$i,"id");
  $name=mysql_result($result,$i,"name");
  $bs=round(mysql_result($result,$i,"byte_size")/1024/1024,0);
  $jobs=mysql_result($result,$i,"nbjobs");
  $done_jobs=mysql_result($result,$i,"nbdone");
  $master_queue=mysql_result($result,$i,"master_queue");
  $creation_time=mysql_result($result,$i,"creation_time");
  $owner=mysql_result($result,$i,"owner");
  $duration=round(mysql_result($result,$i,"duration")/60,0);
  $duration_seconds = sprintf("%'02d",mysql_result($result,$i,"duration")%60);
  if (isset($used_drive["$master_queue/$owner"]) == false)
     $used_drive["$master_queue/$owner"]=0;
  $i++;
  echo "<tr>";
  echo "<td><a href=\"queueinfo.php?id=$id\">$id</a></td><td><a href=\"tapeinfo.php?tape=$name\">$name</a></td><td>$pvr_id[$master_queue]</td><td>$owner</td><td>$jobs</td><td>$bs</td><td>$creation_time</td><td>$duration:$duration_seconds</td>\n";
  echo "</tr>\n";
}
echo "</tbody></table>";

//
// SUSPENDED QUEUES
//
if($user){
$query = "SELECT id,name,byte_size,nbjobs,nbdone,nbfailed,master_queue,creation_time,activation_time,end_time,owner,status,UNIX_TIMESTAMP(end_time)-UNIX_TIMESTAMP(activation_time) as stage_duration,UNIX_TIMESTAMP(end_time)-UNIX_TIMESTAMP(creation_time) as sec_duration FROM queues WHERE status=22 and owner='$user' ORDER BY end_time";
}
else {
$query = "SELECT id,name,byte_size,nbjobs,nbdone,nbfailed,master_queue,creation_time,activation_time,end_time,owner,status,UNIX_TIMESTAMP(end_time)-UNIX_TIMESTAMP(activation_time) as stage_duration,UNIX_TIMESTAMP(end_time)-UNIX_TIMESTAMP(creation_time) as sec_duration FROM queues WHERE status=22 ORDER BY end_time";
}

$result = mysql_query($query);
$num=mysql_numrows($result);

echo "<h2><a name=\"done\"/>Suspended Queues</h2>";
if($user){
  echo "<b>for user $user</b><br>";
}
echo "<table id=\"gradient\" class=\"sortable\" summary=\"Suspended queues\">\n";
echo "<thead><tr>";
echo "<th>id</th>
      <th>tape</th>
      <th>master queue id</th>
      <th>owner</th>
      <th>file requests (done/failed/total)</th>
      <th>Size (MB)</th>
      <th>status</th>
      <th>creation time</th>
      <th>activation time</th>
      <th>end time</th>
      <th>duration</th>
      <th>stage duration</th>
      <th>speed with waiting overhead (MB/s)</th>
      <th>staging speed (MB/s)</th>\n";
echo "</tr></thead>";
echo "<tfoot>
  <tr>
  <td colspan='4'>Fetched $num rows</td>
  </tr>
  </tfoot>
  <tbody>\n";
$i=0;
while ($i < $num) {
  $id=mysql_result($result,$i,"id");
  $name=mysql_result($result,$i,"name");
  $bs=round(mysql_result($result,$i,"byte_size")/1024/1024,0);
  $jobs=mysql_result($result,$i,"nbjobs");
  $done_jobs=mysql_result($result,$i,"nbdone");
  $failed_jobs=mysql_result($result,$i,"nbfailed");
  $active=mysql_result($result,$i,"status");
  $master_queue=mysql_result($result,$i,"master_queue");
  $creation_time=mysql_result($result,$i,"creation_time");
  $activation_time=mysql_result($result,$i,"activation_time");
  $owner=mysql_result($result,$i,"owner");
  $done=mysql_result($result,$i,"done");
  $end=mysql_result($result,$i,"end_time");
  $sec_duration=round(mysql_result($result,$i,"sec_duration")/60,0);
  $sec_duration_seconds = sprintf("%'02d",mysql_result($result,$i,"sec_duration")%60);
  $stage_duration=round(mysql_result($result,$i,"stage_duration")/60,0);
  $stage_duration_seconds = sprintf("%'02d",mysql_result($result,$i,"stage_duration")%60);
  if (isset($used_drive["$master_queue/$owner"]) == false)
     $used_drive["$master_queue/$owner"]=0;
  if((mysql_result($result,$i,"stage_duration" != 0))&& ($done+$failed!=$jobs)){
    $speed = number_format($bs / mysql_result($result,$i,"stage_duration"),2);
  }
  else{
    $speed = "--";
  }
  
  if(mysql_result($result,$i,"sec_duration" != 0)){
    $totalspeed = number_format($bs / mysql_result($result,$i,"sec_duration"),2);
  }
  else{
    $totalspeed = "--";
  }
  $i++;
  echo "<tr>";
  echo "<td><a href=\"queueinfo.php?id=$id\">$id</a></td>
    <td><a href=\"tapeinfo.php?tape=$name\">$name</a></td>
    <td>$pvr_id[$master_queue]</td>
    <td>$owner</td>
    <td>$done_jobs/$failed_jobs/$jobs</td>
    <td>$bs</td>
    <td>".$queue_status[$active]."</td>
    <td>$creation_time</td>
    <td>$activation_time</td>
    <td>$end</td>
    <td>$sec_duration:$sec_duration_seconds</td>
    <td>$stage_duration:$stage_duration_seconds</td>
    <td>$totalspeed</td>
    <td>$speed</td>
    ";
  echo "</tr>\n";
}
echo "</tbody></table>";

mysql_close();

mysql_connect($db_server, $db_user, $db_passwd);
@mysql_select_db($db_configdatabase) or die ( "Unable to select database") ;
$query = "SELECT p.pvrname,p.drives as drives,p.pvrid as pvrid, b.user,b.share,b.default_share as allocated_drives FROM allocation b, mediatype p WHERE p.pvrid=b.pvrid ORDER BY p.pvrname,allocated_drives DESC";
$result = mysql_query($query);
$num=mysql_numrows($result);
echo "<hr style=\"width: 40%; height: 2px; margin-left: 0px; margin-right: auto;\">\n";
echo "<b><a name=\"drives\">Drives Usage</a></b><br><br>";
echo "<table id=\"gradient\" class=\"sortable\" summary=\"Clients configuration\">\n";
echo "<thead><tr>\n
<th>Media Type</th><th>drives</th><th>user</th><th>Allocated Drives</th><th>Used Drives</th>\n
</tr> </thead>";
echo "<tfoot>
  <tr>
  <td colspan='4'>Fetched $num rows</td>
  </tr>
  </tfoot>
  <tbody>\n";
$i=0;
while ($i < $num) {

  $user=mysql_result($result,$i,"user");
  $pvr=mysql_result($result,$i,"pvrname");
  $drives=mysql_result($result,$i,"drives");
  $allocdrives=mysql_result($result,$i,"b.share");
  $defdrives = mysql_result($result,$i,"allocated_drives");
  $pvrid = mysql_result($result,$i,"pvrid");
  $i++;

  if(($allocdrives != "") and ($defdrives != $allocdrives)){
    $defdrives = $allocdrives;
  }

  if (isset($used_drive["$pvrid/$user"]) == false){
    $used_drive["$pvrid/$user"] = 0;
  } 
  echo "<tr>";
  echo "<td>$pvr</td><td>$drives</td><td>$user</td><td>".(int)($defdrives*$drives)."</td><td>".$used_drive["$pvrid/$user"]."</td>\n";
  echo "</tr>\n";
  unset($used_drive["$pvrid/$user"]);
}
foreach ($used_drive as $key=>$val){
  list($pvrid,$user) = split('/', $key);
  $query = "SELECT pvrname,drives FROM mediatype WHERE pvrid=$pvrid";
  $result = mysql_query($query);
  $pvr=mysql_result($result,0,"pvrname");
  $drives=mysql_result($result,0,"drives");
  echo "<tr>";
  echo "<td>$pvr</td><td>$drives</td><td>$user</td><td>0</td><td>$val</td>\n";
  echo "</tr>";
}
echo "</tbody></table>";
mysql_close();
?>
</html>
</body>
