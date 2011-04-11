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
include "htmlutils.php";
include "menu.php";

$SELECT_HEARTBEAT = "SELECT pid, start_time, TIME_TO_SEC(CURRENT_TIMESTAMP)-TIME_TO_SEC(last_time) as hb " .
"FROM heartbeat " .
"ORDER BY start_time DESC LIMIT 1";

$SELECT_GENERAL_INFO = "SELECT component, value " .
"FROM generalinfo";

$SELECT_QUEUES_STATUS_BY_OWNER = "SELECT status, count(1) as count, owner " .
"FROM queues " .
"GROUP BY status, owner";

$SELECT_REQUESTS_STATUS_BY_USER = "SELECT status, count(1) as count, user " .
"FROM requests " .
"GROUP BY status, user";

$SELECT_RESOURCE_REPARTITION = "SELECT p.pvrname, p.drives as drives, b.user, b.default_share as default_share, b.share as modshare, b.default_depth " .
"FROM allocation b, mediatype p " .
"WHERE p.pvrid = b.pvrid " .
"ORDER BY p.pvrname, share DESC";

$used_drive = array ();
mysql_connect($db_server, $db_user, $db_passwd);

@ mysql_select_db($db_configdatabase) or die("Unable to select database");
$query = $SELECT_HEARTBEAT;
$result = mysql_query($query);
echo '<table id="gradient" summary="TReqS heartbeat">';
echo "<thead><tr>\n
<th>PID</th><th>Start Date</th><th>Last Heartbeat</th><th>Message</th>\n
</tr> </thead>";
echo "
  <tbody>\n";
$pid = mysql_result($result, 0, "pid");
$start = mysql_result($result, 0, "start_time");
$last = (int) (mysql_result($result, 0, "hb"));
$msg = mysql_result($result, 0, "message");

if ($last > 300) {
  $last = '<span style="color:red">' . $last . '</span>';
}
echo "<tr><td>$pid</td><td>$start</td><td>" . "$last" . "s ago</td><td>$msg</td></tr>\n";
echo "</tbody></table>";

@ mysql_select_db($db_jobdatabase) or die("Unable to select database");
echo "<br><b>Current Queues (last 25 hours)<br>";
$query = $SELECT_QUEUES_STATUS_BY_OWNER;
$result = mysql_query($query);
$num = mysql_numrows($result);

$i = 0;
$done = 0;
$wait = 0;
$count = 0;
$total = 0;
echo '<table id="gradient" class="sortable" summary="Current Queues by user">';
echo "<thead><tr>\n
<th>User</th><th>Status</th><th>Count</th>\n
</tr> </thead>";
/*echo "<tfoot>
  <tr>
  <td colspan='4'>Fetched $num rows</td>
  </tr>
  </tfoot>
 */
echo "
  <tbody>\n";
while ($i < $num) {
  $status = mysql_result($result, $i, "status");
  $owner = mysql_result($result, $i, "owner");
  $total = mysql_result($result, $i, "count");
  if ($status == 20) {
    $wait += $total;
    $strstatus = "waiting";
  } else
    if ($status == 21) {
      $count += $total;
      $strstatus = "active";
    } else {
      $done += $total;
      $strstatus = "done";
    }
  echo "<tr><td><a href=\"queues.php?user=$owner\">$owner</a></td><td>$strstatus</td><td>$total</td></tr>\n";

  $i++;
}
echo "
  <tfoot>
  <tr><td rowspan=\"3\">Total</td><td>waiting</td><td>$wait</td></tr>
  <tr><td>active</td><td>$count</td></tr>
  <tr><td>done</td><td>$done</td></tr>
  </tfoot>
  ";

echo "</tbody></table>";

echo "<hr style=\"width: 40%; height: 2px; margin-left: 0px; margin-right: auto;\">\n";
echo "<br><b>Current Requests (last 25 hours):<br>";

$query = $SELECT_REQUESTS_STATUS_BY_USER;
$result = mysql_query($query);
$num = mysql_numrows($result);

echo '<table id="gradient" class="sortable" summary="Current requests by user">';
echo "<thead><tr>\n
<th>User</th><th>Status</th><th>Count</th>\n
</tr> </thead>";

$i = 0;
$count = 0;
$error = 0;
$waitcopy = 0;
$clienterror = 0;
$done = 0;
while ($i < $num) {
  $status = mysql_result($result, $i, "status");
  $owner = mysql_result($result, $i, "user");
  $total = mysql_result($result, $i, "count");
  if ($status < 14) {
    $count += $total;
  } else
    if ($status == 16) {
      $error += $total;
    } else
      if ($status == 18) {
        $clienterror += $total;
      } else
        if (($status == 14) or ($status == 17)) {
          $done += $total;
        } else {
          $waitcopy += $total;
        }
  $i++;
  echo "<tr><td>$owner</td><td>" . $job_status[$status] . "</td><td>$total</td></tr>\n";
}
echo "
  </tbody>
  <tfoot>
  <tr><td rowspan=\"5\">Total</td><td><a href=\"errors.php\">error</a></td><td>$error</td></tr>
  <tr><td>client error</td><td>$clienterror</td></tr>
  <tr><td>queued</td><td>$count</td></tr>
  <tr><td>done</td><td>$done</td></tr>
  <tr><td>unknown status</td><td>$waitcopy</td></tr>
  </tfoot>
</table>
  ";

mysql_close();

mysql_connect($db_server, $db_user, $db_passwd);

@ mysql_select_db($db_configdatabase) or die("Unable to select database $db_configdatabase");
$query = $SELECT_GENERAL_INFO;
$result = mysql_query($query);
$num = mysql_numrows($result);
$i = 0;
echo '<table id="gradient" summary="General Information">';
echo "<thead><tr>\n
<th>Component</th><th>Description</th>\n
</tr> </thead>";
echo "<tbody>\n";
while ($i < $num) {
  $component = mysql_result($result, $i, "component");
  $value = mysql_result($result, $i, "value");

  echo "<tr><td>$component</td><td>$value</td></tr>\n";
  $i++;
}
echo "</tbody></table>";

$query = $SELECT_RESOURCE_REPARTITION;
$result = mysql_query($query);
$num = mysql_numrows($result);
$i = 0;
while ($i < $num) {

  $user = mysql_result($result, $i, "user");
  $pvr = mysql_result($result, $i, "pvrname");
  $drives = mysql_result($result, $i, "drives");
  $mod_allocdrives = mysql_result($result, $i, "modshare");
  $allocdrives = mysql_result($result, $i, "default_share");
  $depth = mysql_result($result, $i, "default_depth");
  $i++;

  if (($mod_allocdrives != "") and ($allocdrives != $mod_allocdrives)) {
    $allocdrives = $mod_allocdrives;
  }
  if (isset ($used_drive["$pvr/$user"]) == false) {
    $used_drive["$pvr/$user"] = 0;
  }
  $shares_array[$user] = $allocdrives;
  $resource_array[$pvr] = $shares_array;
  $drives_alloc[$pvr] = $drives;
}
mysql_close();
$table_width = 950;
$col1_width = 50;
// First get the largest booking
$maxbook = -1;
foreach ($resource_array as $resource => $shares_array) {
  $book = -1;
  foreach ($shares_array as $s => $v) {
    $book += $v;
  }
  if ($book > $maxbook) {
    $maxbook = $book;
  }
}
echo "<hr style=\"width: 40%; height: 2px; margin-left: 0px; margin-right: auto;\">\n";
echo "<br><b>Share on resource type:</b><br>\n";
echo '<table width="' . $table_width . '" summary="Share on resource type" class=share>' . "\n";
echo '<thead class="gradient"><tr><th class="resource">Resource</th><th class="resource">Drives</th>><th class="resource" colspan="100">Repartition</th><th class="resource"> </th></tr></thead>' . "\n";
echo '<tr><td class="resource" colspan="2" ></th></th><td class="booking" text-align="right" width="100" colspan="100">100%</td>';
if ($maxbook > 0) {
  printf('<td class="overbooking" width="%s" colspan="%s">OverBooking</td>', $maxbook * 100, $maxbook * 100);
}
echo "</tr>\n";
foreach ($resource_array as $resource => $shares_array) {
  $share_sum = 1;
  echo "<tr><td class='resource' width='$col1_width'>" . $resource . "</td>";
  echo "<td class='resource' width='$col1_width'>" . $drives_alloc[$resource] . "</td>";
  foreach ($shares_array as $s => $v) {
    if ($v != 0) {
      printf('<td class="share" width="%s%%" colspan="%s"><span class="bubble">%s: %s%%</span>%s</td>', $v * 100, $v * 100, $s, $v * 100, $s, $v * 100);
      $share_sum -= $v;
    }
  }
  if ($share_sum < 0) {
    // resource is overbooked
  } else
    if ($share_sum > 0) {
      printf('<td class="share_free" width="%s%%" colspan="%s">free:%s%%</td>', $share_sum * 100, $share_sum * 100, $share_sum * 100);
    }
  echo "</tr>";
}
echo '</table>';
?>
</html>
</body>
