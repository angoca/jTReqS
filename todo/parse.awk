# This script permits to parse the logs to see in the stagers were executed in
# a round robin fashion, showing the start time and the end time.
# In order to make it work, it is necessary to change the tape name and the
# initialized stagers.
# In order to know the stager of a queue, you can execute the next command
# and then replace the given values in this code.
#
# awk '/-JT848800-/ {print $3}' jtreqs-trace.log | sort | uniq
#
# In order to execute the command, you have to give the right arguments:
# t = tapename
# s1 = stager 1
# s2 = stager 2
# s3 = stager 3
# The stagers are the id of the thread
#
# awk -v t="JT8488" -v s1="8847" -v s2="9850" -v s3="0852" -f parse.awk jtreqs-trace.log
#
# Author: Andres Gomez
# Date: 1.1 2011-03-19

BEGIN {
  tape=t
  stager1=s1
  stager2=s2
  stager3=s3
}
{

  if ($0 ~ tape) {
    if (/ Staging\./) {
      if ($0 ~ stager1) {
        print $2,$3,"\t1-s",$12;
      } else if ($0 ~ stager2) {
        print $2,$3,"\t\t2-s",$12;
      } else if ($0 ~ stager3) {
        print $2,$3,"\t\t\t3-s",$12;
      }
    } else if (/ Succesfully staged/) {
      if ($0 ~ stager1) {
        print $2,$3,"\t1-e",$12;
      } else if ($0 ~ stager2) {
        print $2,$3,"\t\t2-e",$12;
      } else if ($0 ~ stager3) {
        print $2,$3,"\t\t\t3-e",$12;
      }
    } else if (/: starting\./) {
      print $2,$3,"\t\t\t\t",$9
    } else if (/Staging process finished/) {
      print $2,$3,"\t\t\t\t",$9
    } else if (/ activated./) {
      print $2,$3,"\t\t\t\t\t\t\t",$9
    } else if (/ ended$/) {
      print $2,$3,"\t\t\t\t\t",$9
    }
  }
}
