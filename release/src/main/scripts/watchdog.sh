#!/bin/sh
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


# This script checks if jtreqs is still running by two three criteria:
# - Administrative status file.
# - Process id.
# - Heartbeat in the database (which is updated by the application.)
#
# @author Jonathan Schaeffer
# @author Andres Gomez
# @author Pierre-Emmanuel Brinette


LOCK_FILE="/var/lock/subsys/jtreqs.id"
JTREQS_INIT=/etc/init.d/jtreqsd

# Properties to check the last heart beat.
OK='ok'
BAD='bad'
# Quantity of minutes to consider a heartbeat as old.
INTERVAL=3
SQL_SELECT="SELECT IF (last_time > DATE_SUB(NOW(), INTERVAL ${INTERVAL} MINUTE), \"${OK}\", \"${BAD}\") FROM heart_beat ;"
DBUSER=jtreqsMon
DBNAME=jtreqsDB
DBPASSWD=jtreqsPassWD

rc=0
check_db=no
verbose=0

# Processes the arguments.
while [ "${1#-}" != "$1" -a $# -gt 0 ] ; do
    case $1 in
        -h|-help)   help_msg 1 ;;
        -db) check_db=yes;;
        -v) verbose=1;;
    esac
    shift 1
done

# Checks if the file exists (Administrative status)
if [ -e  ${LOCK_FILE} ] ; then
    if [ ${verbose} -eq 1 ] ; then
        echo - The service should be active.
    fi

    # Checks the jTReqS status
    status=`${JTREQS_INIT} status`
    RETVAL=$?

# TODO PIDOF?? or not?
# TODO /sbin/service ou comment?
    if [ "${RETVAL}" -ne 0 ] ; then
        if [ ${verbose} -eq 1 ] ; then
            echo - The service is not started.
        fi

        # The service should be running but it is not,
        # then restart it.
        ${JTREQS_INIT} restart

        rc=1
    elif [ ${check_db} = "yes" ] ; then
        if [ ${verbose} -eq 1 ] ; then
            echo - The service is started.
        fi

        # The service is started, then checks the heart beat.
        result=`echo ${SQL_SELECT} | mysql -u $DBUSER -p$DBPASSWD $DBNAME | tail -1`

        # Checks if the service is fine.
        if [ "${result}" != "${OK}" ] ; then
            if [ ${verbose} -eq 1 ] ; then
                echo - Restarting the service because the heartbeat is too old.
            fi
            # The service has an old heartbeat
            ${JTREQS_INIT} restart

            rc=1
        else
            if [ ${verbose} -eq 1 ] ; then
                echo - The service has a good heartbeat.
            fi
        fi
    elif [ ${verbose} -eq 1 ] ; then
                echo - The service is running.
    fi
else
    # The file does not exist, then do nothing.
    if [ ${verbose} -eq 1 ] ; then
        echo - The service is not active.
    fi
fi

exit ${rc}
