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

