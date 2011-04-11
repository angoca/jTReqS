<html>
<head>
<link rel="icon" href="images/eyes.ico" type="image/ico" />
<title>jTReqS Monitoring - Queues</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<style type="text/css">
@IMPORT url("../resources_old/css/fancystyle.css");
</style>
</head>
<body>

<?php
ini_set('error_reporting', E_ALL);
error_reporting(E_ALL | E_STRICT);
ini_set('display_errors', true);

require_once "configuration.inc";
require_once "queries.inc";

include "../resources_old/menu.php";

mysql_connect($dbServer, $dbUser, $dbPasswd);
@ mysql_select_db($dbName) or die("Unable to connect to the database '"
.$dbName."' at '".$dbServer."'");

?>
  <div>
    <img src="images/center.png" />
  </div>

  <h1>Queues per tape</h1>
  <?php
  $result = mysql_query($SQL_QUEUES_BY_ID); ?>
  <table id="gradient" summary="Current queues by user">
    <thead>
      <tr>
        <th>Tape</th>
        <th>Created</th>
        <th>Activated</th>
        <th>Suspended</th>
      </tr>
    </thead>
    <tbody>
    <?php
    $sumCreated = 0;
    $sumActivated = 0;
    $sumSuspended = 0;
    $i = 0;
    $num = mysql_numrows($result);
    while ($i < $num) {
      $name = mysql_result($result, $i, "name"); ?>
      <tr>
        <td><div>
        <?php echo $name; ?>
          </div>
        </td>
        <?php
        $media = mysql_result($result, $i, "media");
        $id = mysql_result($result, $i, "id");
        $creationTime = mysql_result($result, $i, "creation_time");
        $owner = mysql_result($result, $i, "owner");
        $size = round(mysql_result($result, $i, "byte_size")/1024/1024, 0);
        $reqs = mysql_result($result, $i, "nb_reqs");
        $waiting = mysql_result($result, $i, "waiting");
        $i++; ?>
        <td><?php
        if (isset($creationTime)) {
          $sumCreated++;?> <a href="queuebyid.php?queue=<?php echo $id; ?>"><div>
              <span> Id: <b><?php echo $id; ?> </b> </span> <span> Media: <b><?php echo $media; ?>
              </b> </span> <span> Owner: <b><?php echo $owner; ?> </b> </span>
              <span> Size: <b><?php echo $size; ?> MB</b> </span><span> Total:
                <b><?php echo $reqs; ?> </b> </span>
            </div>
            <div>
              Creation: <b><?php echo $creationTime; ?> </b>
            </div>
            <div>
              <span> Waiting <b><?php echo $waiting; ?> </b> </span>
            </div>
        </a> <?php
        }?></td>
        <?php
        $media = mysql_result($result, $i, "media");
        $id = mysql_result($result, $i, "id");
        $creationTime = mysql_result($result, $i, "creation_time");
        $activationTime = mysql_result($result, $i, "activation_time");
        $owner = mysql_result($result, $i, "owner");
        $size = round(mysql_result($result, $i, "byte_size")/1024/1024, 0);
        $reqs = mysql_result($result, $i, "nb_reqs");
        $done = mysql_result($result, $i, "nb_reqs_done");
        $failed = mysql_result($result, $i, "nb_reqs_failed");
        $waited = mysql_result($result, $i, "waited");
        $duration = mysql_result($result, $i, "duration");
        $i++; ?>
        <td><?php
        if (isset($creationTime)) {
          $sumActivated++;
          ?> <a href="queuebyid.php?queue=<?php echo $id; ?>"><div>
              <span>Id: <b><?php echo $id; ?> </b> </span><span>Media: <b><?php echo $media; ?>
              </b> </span><span>Owner: <b><?php echo $owner; ?> </b> </span> <span>
                Size: <b><?php echo $size; ?> MB</b> </span><span>
                Total/done/failed: <b><?php echo $reqs."/".$done."/";
                if ($failed > 0) {
                  echo "<span style=\"color:red\">".$failed."</span>";
                } else {
                  echo $failed;
                } ?></b> </span>
            </div>
            <div>
              Creation: <b><?php echo $creationTime; ?> </b>
            </div>
            <div>
              Activation: <b><?php echo $activationTime; ?> </b>
            </div>
            <div>
              <span> Waited <b><?php echo $waited; ?> </b> </span> <span>
                Duration <b><?php echo $duration; ?> </b> </span><span>Left: <b><?php echo $reqs - ($done + $failed); ?>
              </b> </span>
            </div> </a> <?php
        }?></td>
        <?php
        $media = mysql_result($result, $i, "media");
        $id = mysql_result($result, $i, "id");
        $creationTime = mysql_result($result, $i, "creation_time");
        $suspensionTime = mysql_result($result, $i, "suspension_time");
        $owner = mysql_result($result, $i, "owner");
        $size = round(mysql_result($result, $i, "byte_size")/1024/1024, 0);
        $reqs = mysql_result($result, $i, "nb_reqs");
        $waiting = mysql_result($result, $i, "waiting");
        $i++;
        ?>
        <td><?php
        if (isset($creationTime)) {
          $sumSuspended++;?> <a href="queuebyid.php?queue=<?php echo $id; ?>"><div>
              <span> Id: <b><?php echo $id; ?> </b> </span><span> Media: <b><?php echo $media; ?>
              </b> </span> <span> Owner: <b><?php echo $owner; ?> </b> </span>
              <span> Size: <b><?php echo $size; ?> MB</b> </span><span> Total:
                <b><?php echo $reqs; ?> </b> </span>
            </div>
            <div>
              Creation: <b> <?php echo $creationTime; ?> </b>
            </div>
            <div>
              Suspended: <b><?php echo $suspensionTime; ?> </b>
            </div>
            <div>
              <span> Waiting <b><?php echo $waiting; ?> </b> </span>
            </div> </a> <?php
        }?></td>
      </tr>
      <?php
    } ?>

    </tbody>
    <tfoot>
      <tr>
        <td>Total</td>
        <td><?php echo $sumCreated; ?></td>
        <td><?php echo $sumActivated; ?></td>
        <td><?php echo $sumSuspended; ?></td>
      </tr>
    </tfoot>
  </table>


</body>
</html>
