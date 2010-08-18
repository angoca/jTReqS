#!/bin/sh
# File:  treqs_watchdog.sh
#
# Copyright      Jonathan Schaeffer 2009-2010, 
#                CC-IN2P3, CNRS <jonathan.schaeffer@cc.in2p3.fr>
# Contributors : Andres Gomez, 
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


# This script checks if treqs is still running
# If not, restart it and print a message
# The heartbeat table is updated accordingly

DBUSER="treqshb"
DBNAME="treqsconfig"
SQL_HB_OK="UPDATE heartbeat SET last_time=CURRENT_TIMESTAMP WHERE pid=\${PID};"
SQL_HB_NOK="INSERT INTO heartbeat \(pid,start_time\) VALUES \(\${PID},CURRENT_TIMESTAMP\);"

PID=$(/sbin/pidof treqs)

if [ -z  "${PID}" ] ; then
  # TReqS has died
  DATE=$(date +"%D %T")
  echo ":: $DATE TReqS has died. Core file should be available."
  echo " Restarting ..."
  /sbin/service treqsd start
  if [ $? -ne 0 ] ; then
    # Dead again ? This is problematic ...
    echo " Can't restart the server."
  else
    sleep 1
    PID=$(/sbin/pidof treqs)
    eval echo $SQL_HB_NOK | mysql -u $DBUSER $DBNAME  
  fi
else
  eval echo $SQL_HB_OK | mysql -u $DBUSER $DBNAME
fi
