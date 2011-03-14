#!/bin/sh
# File:  registerInfo.sh
#
# Copyright      Jonathan Schaeffer 2009-2010,
#                CC-IN2P3, CNRS <jonathan.schaeffer@cc.in2p3.fr>
# Contributors   Andres Gomez,
#                CC-IN2P3, CNRS <andres.gomez@cc.in2p3.fr>
#
# This software is a computer program whose purpose is to schedule, sort
# and submit file requests to the hierarchical storage system HPSS.
#
# This software is governed by the CeCILL license under French law and
# abiding by the rules of distribution of free software.  You can  use,
# modify and/or redistribute the software under the terms of the CeCILL
# license as circulated by CEA, CNRS and INRIA at the following URL
# "http://www.cecill.info".
#
# As a counterpart to the access to the source code and rights to copy,
# modify and redistribute granted by the license, users are provided only
# with a limited warranty  and the software's author,  the holder of the
# economic rights, and the successive licensors have only limited
# liability.
#
# In this respect, the user's attention is drawn to the risks associated
# with loading,  using,  modifying and/or developing or reproducing the
# software by the user in light of its specific status of free software,
# that may mean  that it is complicated to manipulate,  and  that  also
# therefore means  that it is reserved for developers  and  experienced
# professionals having in-depth computer knowledge. Users are therefore
# encouraged to load and test the software's suitability as regards their
# requirements in conditions enabling the security of their systems and/or
# data to be ensured and,  more generally, to use and operate it in the
# same conditions as regards security.
#
# The fact that you are presently reading this means that you have had
# knowledge of the CeCILL license and that you accept its terms.

# This script starts treqs and registers some information about the
# environment where it is run

# TODO v2.0 include this in the Starter, rather than have this in a script.

DBUSER="treqshb"
DBNAME="treqsconf"

treqs_hostname=`hostname`

hpss_conf_file=/var/hpss/etc/env.conf
hpss_hostname=`awk -F= '/^HPSS_SITE_NAME/ {print \$2}' ${hpss_conf_file}`

treqs_conf_file=/opt/treqs/etc/treqs.conf
mysql_hostname=`awk -F= '/^DBHOST/ {print \$2}' ${treqs_conf_file}`

SQL_TREQS_HOSTNAME="UPDATE generalinfo SET value = \\\""${treqs_hostname}"\\\" WHERE id = 3;"
SQL_HPSS_HOSTNAME="UPDATE generalinfo SET value = \\\""${hpss_hostname}"\\\" WHERE id = 1;"
SQL_MYSQL_HOSTNAME="UPDATE generalinfo SET value = \\\""${mysql_hostname}"\\\" WHERE id = 2;"

eval echo $SQL_TREQS_HOSTNAME | mysql -u $DBUSER $DBNAME
eval echo $SQL_HPSS_HOSTNAME | mysql -u $DBUSER $DBNAME
eval echo $SQL_MYSQL_HOSTNAME | mysql -u $DBUSER $DBNAME