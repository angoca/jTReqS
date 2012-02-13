<!-- Copyright Jonathan Schaeffer 2009-2010, CC-IN2P3, CNRS <jonathan.schaeffer@cc.in2p3.fr> -->

<!-- Contributors Andres Gomez, CC-IN2P3, CNRS <andres.gomez@cc.in2p3.fr> -->

<!-- This software is a computer program whose purpose is to schedule, sort
    and submit file requests to the hierarchical storage system HPSS. -->

<!-- This software is governed by the CeCILL license under French law and
    abiding by the rules of distribution of free software. You can use, modify
    and/or redistribute the software under the terms of the CeCILL license as
    circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info". -->

<!-- As a counterpart to the access to the source code and rights to copy,
    modify and redistribute granted by the license, users are provided only with
    a limited warranty and the software's author, the holder of the economic
    rights, and the successive licensors have only limited liability. -->

<!-- In this respect, the user's attention is drawn to the risks associated
    with loading, using, modifying and/or developing or reproducing the software
    by the user in light of its specific status of free software, that may mean
    that it is complicated to manipulate, and that also therefore means that
    it is reserved for developers and experienced professionals having in-depth
    computer knowledge. Users are therefore encouraged to load and test the software's
    suitability as regards their requirements in conditions enabling the security
    of their systems and/or data to be ensured and, more generally, to use and
    operate it in the same conditions as regards security. -->

<!-- The fact that you are presently reading this means that you have had
    knowledge of the CeCILL license and that you accept its terms. -->
<html>
<head>
<style type="text/css">
@IMPORT url("css/fancystyle.css");
</style>
</head>
<body>

<?php
ini_set('error_reporting', E_ALL);
error_reporting(E_ALL | E_STRICT);
ini_set('display_errors', true);

require_once "configuration.inc";
require_once "queries.inc";

mysql_connect($dbServer, $dbUser, $dbPasswd);
@ mysql_select_db($dbName) or die("Unable to connect to the database '"
.$dbName."' at '".$dbServer."'"); ?>

  <table>
    <tr>
      <td>

        <h1>General Information</h1> <?php
        $result = mysql_query($SQL_INFORMATION_GET); ?>
        <table id="gradient" summary="General Information">
          <thead>
            <tr>
              <th>Component</th>
              <th>Description</th>
            </tr>
          </thead>
          <tbody>
          <?php $i = 0;
          $num = mysql_numrows($result);
          while ($i < $num) {?>
            <tr>
              <td><?php echo mysql_result($result, $i, "name");?></td>
              <td><?php echo mysql_result($result, $i, "value");?></td>
            </tr>
            <?php
            $i++;
          }?>
          </tbody>
        </table>
      </td>
      <td>
        <h1>Heartbeat</h1> <?php
        $result = mysql_query($SQL_HEARTBEAT_LAST); ?>
        <table id="gradient" summary="Heartbeat">
          <thead>
            <tr>
              <th>PID</th>
              <th>Start time</th>
              <th>Last beat</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><?php echo mysql_result($result, 0, "pid"); ?></td>
              <td><?php echo mysql_result($result, 0, "start_time"); ?></td>
              <td><?php
              $last = mysql_result($result, 0, "hb");
              if ($last > $MAX_LAST_BEAT) {
                echo '<span style="color:red">'.$last.'</span>';
              } else {
                echo $last;
              } ?>
              </td>
            </tr>
          </tbody>
        </table></td>
    </tr>
  </table>
  <table>
    <tr>
      <td>
        <h1>
          <a href="queues.php">Queues</a>
        </h1> <?php
        $result = mysql_query($SQL_QUEUES_STATUS_BY_OWNER); ?>
        <table id="gradient" summary="Current queues by user">
          <thead>
            <tr>
              <th>User</th>
              <th>Created</th>
              <th>Activated</th>
              <th>Ended</th>
              <th>Aborted</th>
              <th>Temporarily Suspended</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
          <?php
          $sumCreated = 0;
          $sumActivated = 0;
          $sumEnded = 0;
          $sumAborted = 0;
          $sumSuspended = 0;
          $i = 0;
          $num = mysql_numrows($result);
          while ($i < $num) {
            $owner = mysql_result($result, $i, "owner");
            $created = mysql_result($result, $i++, "count"); //200
            $activated = mysql_result($result, $i++, "count"); //210
            $suspended = mysql_result($result, $i++, "count"); //220
            $ended = mysql_result($result, $i++, "count"); //230
            $aborted = mysql_result($result, $i++, "count"); //240
            $total = $created + $activated + $suspended + $ended + $aborted; ?>
            <tr>
              <td><a href="queuesbyowner.php?owner=<?php echo $owner; ?>"><?php echo $owner; ?>
              </a></td>
              <td><a href="queuesbyowner.php?owner=<?php echo $owner; ?>"><?php echo $created; ?>
              </a></td>
              <td><a href="queuesbyowner.php?owner=<?php echo $owner; ?>"><?php echo $activated; ?>
              </a></td>
              <td><a href="queuesbyowner.php?owner=<?php echo $owner; ?>"><?php echo $ended; ?>
              </a></td>
              <td><a href="queuesbyowner.php?owner=<?php echo $owner; ?>"><?php echo $aborted; ?>
              </a></td>
              <td><a href="queuesbyowner.php?owner=<?php echo $owner; ?>"><?php echo $suspended; ?>
              </a></td>
              <td><a href="queuesbyowner.php?owner=<?php echo $owner; ?>"><?php echo $total; ?>
              </a>
              </td>
            </tr>
            <?php
            $sumCreated += $created;
            $sumActivated += $activated;
            $sumEnded += $ended;
            $sumAborted += $aborted;
            $sumSuspended += $suspended;
          } ?>
          </tbody>
          <tfoot>
            <tr>
              <td>Total</td>
              <td><?php echo $sumCreated; ?>
              </td>
              <td><?php echo $sumActivated; ?>
              </td>
              <td><?php echo $sumEnded; ?>
              </td>
              <td><?php echo $sumAborted; ?>
              </td>
              <td><?php echo $sumSuspended; ?>
              </td>
              <td><?php echo $sumCreated + $sumActivated + $sumEnded + $sumAborted + $sumSuspended; ?>
              </td>
            </tr>
          </tfoot>
        </table></td>
      <td>

        <h1>Requests</h1> <?php
        $result = mysql_query($SQL_REQUESTS_STATUS_BY_USER); ?>
        <table id="gradient" summary="Current requests by user">
          <thead>
            <tr>
              <th>User</th>
              <th>Created</th>
              <th>On disk</th>
              <th>Submitted</th>
              <th>Queued</th>
              <th>Staged</th>
              <th>Failed</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
          <?php
          $sumCreated = 0;
          $sumSubmitted = 0;
          $sumQueued = 0;
          $sumStaged = 0;
          $sumOnDisk = 0;
          $sumFailed = 0;
          $i = 0;
          $num = mysql_numrows($result);
          while ($i < $num) {
            $user = mysql_result($result, $i, "user");
            $created = mysql_result($result, $i++, "count"); //100
            $submitted = mysql_result($result, $i++, "count"); //110
            $queued = mysql_result($result, $i++, "count"); //120
            $staged = mysql_result($result, $i++, "count"); //140
            $onDisk = mysql_result($result, $i++, "count"); //150
            $failed = mysql_result($result, $i++, "count"); //160
            $total = $created + $submitted + $queued + $staged + $onDisk + $failed; ?>
            <tr>
              <td><a href="requestsbyuser.php?user=<?php echo $user; ?>"><?php echo $user; ?>
              </a>
              </td>
              <td><a href="requestsbyuser.php?user=<?php echo $user; ?>"><?php echo $created; ?>
              </a>
              </td>
              <td><a href="requestsbyuser.php?user=<?php echo $user; ?>"><?php echo $onDisk; ?>
              </a>
              </td>
              <td><a href="requestsbyuser.php?user=<?php echo $user; ?>"><?php echo $submitted; ?>
              </a>
              </td>
              <td><a href="requestsbyuser.php?user=<?php echo $user; ?>"><?php echo $queued; ?>
              </a>
              </td>
              <td><a href="requestsbyuser.php?user=<?php echo $user; ?>"><?php echo $staged; ?>
              </a>
              </td>
              <td><a href="requestsbyuser.php?user=<?php echo $user; ?>"><?php echo $failed; ?>
              </a>
              </td>
              <td><a href="requestsbyuser.php?user=<?php echo $user; ?>"><?php echo $total; ?>
              </a></td>
            </tr>
            <?php
            $sumCreated += $created;
            $sumSubmitted += $submitted;
            $sumQueued += $queued;
            $sumStaged += $staged;
            $sumOnDisk += $onDisk;
            $sumFailed += $failed;
          } ?>
          </tbody>
          <tfoot>
            <tr>
              <td>Total</td>
              <td><?php echo $sumCreated; ?>
              </td>
              <td><?php echo $sumOnDisk; ?>
              </td>
              <td><?php echo $sumSubmitted; ?>
              </td>
              <td><?php echo $sumQueued; ?>
              </td>
              <td><?php echo $sumStaged; ?>
              </td>
              <td><?php echo $sumFailed; ?>
              </td>
              <td><?php echo $sumCreated + $sumOnDisk + $sumSubmitted + $sumQueued + $sumStaged + $sumFailed; ?>
              </td>
            </tr>
          </tfoot>
        </table>
      </td>
    </tr>
  </table>
  <table>
    <tr>
      <td>

        <h1>Fair Share</h1> <?php
        $result = mysql_query($SQL_ALLOCATIONS_REPARTITION);
        $num = mysql_numrows($result);
        $resource_array = array();
        $drives_alloc = array();
        $i = 0;
        while ($i < $num) {
          $medianame = mysql_result($result, $i, "name");
          $drives = mysql_result($result, $i, "drives");
          $user = mysql_result($result, $i, "user");
          $share = mysql_result($result, $i, "share");
          $i++;

          if (!isset($resource_array[$medianame])) {
            $shares_array = array();
            $shares_array[$user] = $share;
            $resource_array[$medianame] = $shares_array;
          } else {
            $resource_array[$medianame][$user] = $share;
          }
          $drives_alloc[$medianame] = $drives;
        }

        // First get the largest booking.
        $maxbook = -1;
        foreach ($resource_array as $medianame => $shares_array) {
          $book = -1;
          foreach ($shares_array as $user => $share) {
            $book += $share;
          }
          if ($book > $maxbook) {
            $maxbook = $book;
          }
        }
        ?>
        <table summary="Share on resource type" class=share>
          <thead class="gradient">
            <tr>
              <th class="resource">Resource</th>
              <th class="resource">Drives</th>
              <th class="resource" colspan="100">Repartition</th>
              <?php
              if ($maxbook > 0) { ?>
              <th class="resource">Overbooking</th>
              <?php
              }?>
            </tr>
          </thead>
          <tr>
            <td class="resource" colspan="2" />
            <td class="booking" text-align="right" colspan="100">100%</td>
            <?php
            // Checks if there is overbooking.
            if ($maxbook > 0) { ?>
            <td class="overbooking" colspan="<?php echo $maxbook * 100; ?>">OverBooking</td>
            <?php
            } ?>
          </tr>
          <?php
          foreach ($resource_array as $medianame => $shares_array) {
            $share_sum = 1; ?>
          <tr>
            <td class="resource" rowspan="2"><?php echo $medianame; ?>
            </td>
            <td class="resource" rowspan="2"><?php echo $drives_alloc[$medianame]; ?>
            </td>
            <?php
            foreach ($shares_array as $user => $share) {
              // For each row, prints a line and spans columns as percentage.
              if ($share > 0) { ?>
            <td class="share" colspan="<?php echo $share * 100; ?>"><span
              class="bubble"><?php echo "User '".$user."' has ".($share * 100)."%"; ?>
            </span> <?php echo $user; ?>
            </td>
            <?php
            $share_sum -= $share;
              }
            }
            if ($share_sum <= 0) {
              // Resource is overbooked. Do nothing.
            } else if ($share_sum > 0) { ?>
            <td class="share_free" colspan="<?php echo $share_sum * 100; ?>"
              rowspan="2">Free: <?php echo $share_sum * 100; ?>%</td>
              <?php
            } ?>
          </tr>
          <tr>
            <td class="share" colspan="<?php echo (1 - $share_sum) * 100; ?>">Used:
            <?php echo (1 - $share_sum) * 100; ?>%</td>
          </tr>
          <?php
          }
          ?>
          <tfoot>
            <tr>
              <td colspan="2" />
              <td class="resourceQty" colspan="10">10%</td>
              <td class="resourceQty" colspan="10">10%</td>
              <td class="resourceQty" colspan="10">10%</td>
              <td class="resourceQty" colspan="10">10%</td>
              <td class="resourceQty" colspan="10">10%</td>
              <td class="resourceQty" colspan="10">10%</td>
              <td class="resourceQty" colspan="10">10%</td>
              <td class="resourceQty" colspan="10">10%</td>
              <td class="resourceQty" colspan="10">10%</td>
              <td class="resourceQty" colspan="10">10%</td>
            </tr>
          </tfoot>
        </table></td>
      <td>
        <h1>Drives Distribution</h1> <?php

        $used_drive = array();
        $result = mysql_query($SQL_USED_DRIVES);
        $num = mysql_numrows($result);
        $i = 0;
        while ($i < $num) {
          $user = mysql_result($result, $i, "user");
          $media = mysql_result($result, $i, "media");
          $drives = mysql_result($result, $i, "drives");
          if (isset($used_drive["$media/$user"]) == false){
            $used_drive["$media/$user"] = 0;
          }
          $used_drive["$media/$user"]++;
          $i++;
        }
        $result = mysql_query($SQL_ALLOCATIONS_REPARTITION);
        $num = mysql_numrows($result);
        ?>
        <table id="gradient" class="sortable"
          summary="Clients configuration">
          <thead>
            <tr>
              <th>Media Type</th>
              <th>Drives</th>
              <th>User</th>
              <th>Allocated Drives</th>
              <th>Used Drives</th>
            </tr>
          </thead>
          <tbody>
          <?php
          $i = 0;
          while ($i < $num) {
            $name = mysql_result($result, $i, "name");
            $drives = mysql_result($result, $i, "drives");
            $share = mysql_result($result, $i, "share");
            $mediaId = mysql_result($result, $i, "id");
            $user = mysql_result($result, $i, "user");
            $i++;

            if (isset($used_drive["$mediaId/$user"]) == false){
              $used_drive["$mediaId/$user"] = 0;
            }
            ?>

            <tr>
              <td><?php echo $name; ?></td>
              <td><?php echo $drives; ?></td>
              <td><?php echo $user; ?></td>
              <td><?php echo $share * $drives; ?></td>
              <td><?php echo $used_drive["$mediaId/$user"]; ?></td>
            </tr>
            <?php
            unset($used_drive["$mediaId/$user"]);
          }
          foreach ($used_drive as $key => $val){
            $values = preg_split('/\//', $key, -1, PREG_SPLIT_NO_EMPTY);
            $media = $values[0];
            $user = $values[1];
            $result = mysql_query($SQL_USAGE_DRIVES_FOR_MEDIA.$media);
            $name = mysql_result($result, 0, "name");
            $drives = mysql_result($result, 0, "drives");?>
            <tr>
              <td><?php echo $name; ?></td>
              <td><?php echo $drives; ?></td>
              <td><?php echo $user; ?></td>
              <td>0</td>
              <td><?php echo $val; ?></td>
            </tr>
            <?php
          }
          ?>
          </tbody>
        </table></td>
    </tr>
  </table>
  <?php mysql_close();?>
  </body>

</html>
