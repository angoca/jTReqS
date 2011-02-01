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


# This script checks if treqs is still running
# If not, restart it and print a message
# The heartbeat table is updated accordingly
#
# @author Jonathan Schaeffer
# @author Andres Gomez
# @author Pierre-Emmanuel Brinette


#pid_file="/var/lock/subsys/treqs.id"
pid_file="/tmp/treqs.id"
#jtreqs_init=/etc/init.d/jtreqs
jtreqs_init=echo

# Properties to check the last heart beat.
check_db=no
ok='ok'
bad='bad'
interval=3
sql_select="SELECT IF (last_time > DATE_SUB(NOW(), INTERVAL ${interval} MINUTE), \"${ok}\", \"${bad}\") FROM heart_beat ;"
dbuser=jtreqs
dbname=jtreqs
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

# Checks if the file exists.
if [ -e  ${pid_file} ] ; then
    if [ ${verbose} -eq 1 ] ; then
        echo - The service should be active
    fi

    # Checks the TReqS status
    status=`${jtreqs_init} status`

status="jTReqS is STARTED"
# TODO PIDOF?? or not?
# TODO /sbin/service ou comment?
    if [ "${status}" != "jTReqS is STARTED" ] ; then
        if [ ${verbose} -eq 1 ] ; then
            echo - The service is not started
        fi

        # The service should be running but it is not,
        # then restart it.
        $jtreqs_init restart
    elif [ ${check_db} = "yes" ] ; then
        if [ ${verbose} -eq 1 ] ; then
            echo - The service is started
        fi

        # The service is started, then checks the heart beat.
        result=`echo ${sql_select} | mysql -u $dbuser -p"jtreqs" $dbname | tail -1`

        # Checks if the service is fine.
        if [ "${result}" != "${ok}" ] ; then
            if [ ${verbose} -eq 1 ] ; then
                echo - Restarting the service because on an old heartbeat
            fi
            # The service has an old heartbeat
            $jtreqs_init restart
        else
            if [ ${verbose} -eq 1 ] ; then
                echo - The service has a good heartbeat
            fi
        fi
    fi
else
    # The file does not exist, then do nothing.
    if [ ${verbose} -eq 1 ] ; then
        echo - The service is not active.
    fi
fi

