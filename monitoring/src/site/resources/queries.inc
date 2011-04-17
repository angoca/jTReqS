<?php

$SQL_HEARTBEAT_LAST = ""
."SELECT "
."pid, start_time, "
."TIMESTAMPDIFF(second, last_time, NOW()) as hb "
."FROM heart_beat "
."ORDER BY start_time "
."DESC LIMIT 1";

$SQL_INFORMATION_GET = ""
."SELECT name, value "
."FROM information";

$SQL_QUEUES_STATUS_BY_OWNER = ""
."SELECT "
."t1.owner, t1.status, IFNULL(t2.count,0) count "
."FROM ("
."SELECT s.id status, owner "
."FROM queue_status s, jqueues "
."GROUP BY owner, s.id"
.") AS t1 LEFT JOIN ("
."SELECT owner, status, COUNT(1) count "
."FROM jqueues "
."GROUP BY owner, status"
.") AS t2 ON ("
."t1.owner = t2.owner and t1.status = t2.status) "
."ORDER BY owner, status";

$SQL_REQUESTS_STATUS_BY_USER = ""
."SELECT "
."t1.user, t1.status, IFNULL(t2.count,0) count "
."FROM ("
."SELECT s.id status, user "
."FROM request_status s, jrequests "
."GROUP BY user, s.id"
.") AS t1 LEFT JOIN ("
."SELECT user, status, COUNT(1) count "
."FROM jrequests "
."GROUP BY user, status"
.") AS t2 ON ("
."t1.user = t2.user and t1.status = t2.status) "
."ORDER BY user, status";

$SQL_ALLOCATIONS_REPARTITION = ""
."SELECT "
."m.name, m.drives, m.id, a.user, a.share "
."FROM "
."jallocations a, jmediatypes m "
."WHERE "
."m.id = a.media_id "
."ORDER BY "
."m.name, a.share DESC";

$SQL_USED_DRIVES = ""
."SELECT "
."owner user, mediatype_id media, count(1) drives "
."FROM "
."jqueues "
."WHERE "
."status = 210 "
."GROUP BY "
."owner, mediatype_id";

$SQL_USAGE_DRIVES_FOR_MEDIA = ""
."SELECT "
."name, drives "
."FROM "
."jmediatypes "
."WHERE "
."id = ";

$SQL_QUEUES_BY_ID = ""
."SELECT "
."t1.name, t1.status, t2.id, t2.owner, t2.creation_time, m.name media, "
."t2.suspension_time, t2.nb_reqs_failed, t2.activation_time, t2.nb_reqs, "
."t2.byte_size, t2.nb_reqs_done, "
."SEC_TO_TIME(UNIX_TIMESTAMP(CURRENT_TIMESTAMP)-UNIX_TIMESTAMP(creation_time)) waiting, "
."SEC_TO_TIME(UNIX_TIMESTAMP(CURRENT_TIMESTAMP)-UNIX_TIMESTAMP(activation_time)) as duration, "
."SEC_TO_TIME(UNIX_TIMESTAMP(activation_time)-UNIX_TIMESTAMP(creation_time)) as waited "
."FROM "
."(SELECT "
."q.name name, s.id status "
."FROM "
."queue_status s,"
."(SELECT "
."name, status "
."FROM "
."jqueues "
."WHERE "
."status < 230 "
."GROUP BY name) q "
."WHERE "
."s.id < 230 "
."GROUP BY "
."q.name, s.id) AS t1 "
."LEFT JOIN "
."(SELECT "
."id, name, creation_time, mediatype_id, suspension_time, nb_reqs_failed, activation_time, end_time, status, nb_reqs, owner, byte_size, nb_reqs_done "
."FROM "
."jqueues "
."WHERE "
."status < 230) AS t2 ON (t1.name = t2.name and t1.status = t2.status) "
."LEFT JOIN "
."jmediatypes m ON (t2.mediatype_id = m.id) "
."ORDER BY "
."name, status"

?>
