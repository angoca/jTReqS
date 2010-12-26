#!/usr/local/bin/python

# It does the process of request archiving by inserting the old requests in another database, and then, delete them in the source database.
# It does the same procedure for the queues
#
# Error code returned.
# -1 If there is a problem archiving the requests.
# -2 If there is a problem archiving the queues.

import sqlalchemy
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import Table, Column, Integer, String, DateTime
from sqlalchemy import and_

from datetime import datetime,timedelta
from sys import exit
from optparse import OptionParser

parser = OptionParser(version="1.0")
parser.add_option("--srcdb", dest="srcdb",action="store",
              help="[mandatory] Volatile database to archive")
parser.add_option("--srcuser", dest="srcuser",action="store",
              help="[mandatory] User who is authorised to read the database to archive")
parser.add_option("--srcpwd", dest="srcpwd",action="store",
              help="Password for the user")
parser.add_option("--srchost", dest="srchost", action="store", default="localhost",
              help="hostname of the volatile database (default:localhost)")
parser.add_option("--srcsock", dest="srcsock", action="store", default="/var/lib/mysql/mysql.sock",
              help="unix socket file for database connection (default: /var/lib/mysql/mysql.sock)")
parser.add_option("--arcdb", dest="arcdb",action="store",
              help="[mandatory] Archived database which receives the volatile data")
parser.add_option("--arcuser", dest="arcuser", action="store",
              help="[mandatory] User authorised to write into the archive database");
parser.add_option("--arcpwd", dest="arcpwd",action="store",
              help="Password for the user")
parser.add_option("--archost", dest="archost", action="store", default="localhost",
              help="hostname of the archive database (default:localhost)")
parser.add_option("--arcsock", dest="arcsock", action="store", default="/var/lib/mysql/mysql.sock",
              help="unix socket file for database connection (default: /var/lib/mysql/mysql.sock)")
parser.add_option("--days", dest="days", action="store", default="1",
              help="How many days should be kept in the volatile database")
parser.add_option("--maxrow", dest="maxrow", action="store", default=1000, type="int",
              help="Number of rows simultaneously copied (default 1000)")
parser.add_option("--dump", dest="dump", action="store_true", default='false',
              help="Export archived entries as an SQL dump file")
parser.add_option("--verbose", dest="verbose", action="store_true", default='false',
              help="Be more verbose")

(opt,args) = parser.parse_args()
if not (opt.srcdb or opt.srcuser or opt.arcdb or opt.arcuser) :
    parser.error("please provide the mandatory options")


# Declare the classes
Base = declarative_base()
class Request(Base):
  """A request as defined in the table
  """
  __tablename__ = 'requests'
  id = Column(Integer, primary_key=True)
  file = Column(String(length=1024))
  creation_time = Column(DateTime)
  user = Column(String(length=32))
  client = Column(String(length=32))
  version = Column(String(length=32))
  email = Column(String(length=64))
  queue_id = Column(Integer)
  tape = Column(String(length=8))
  position = Column(Integer)
  level = Column(Integer)
  size = Column(Integer)
  tries = Column(Integer)
  errorcode = Column(Integer)
  submission_time = Column(DateTime)
  queued_time = Column(DateTime)
  end_time = Column(DateTime)
  status = Column(Integer)
  message = Column(String(length=254))

  def __init__(self, id, file, creation_time, user, client, version, email, queue_id, tape, position, level, size, tries, errorcode, submission_time, queued_time, end_time, status, message):
    self.id = id
    self.file = file
    self.creation_time = creation_time
    self.user = user
    self.client = client
    self.version = version
    self.email = email
    self.queue_id = queue_id
    self.tape = tape
    self.position = position
    self.level = level
    self.size = size
    self.tries = tries
    self.errorcode = errorcode
    self.submission_time = submission_time
    self.queued_time = queued_time
    self.end_time = end_time
    self.status = status
    self.message = message

  def __repr__(self):
    return "<Request('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')" %(self.id, self.file, self.creation_time, self.user, self.client, self.version, self.email, self.queue_id, self.tape, self.position, self.level, self.size, self.tries, self.errorcode, self.submission_time, self.queued_time, self.end_time, self.status, self.message)


class Queue(Base):
  """A queue as defined in the table
  """
  __tablename__ = 'queues'
  id = Column(Integer, primary_key=True)
  name = Column(String(length=12))
  creation_time = Column(DateTime)
  mediatype_id = Column(Integer)
  nb_reqs_failed = Column(Integer)
  activation_time = Column(DateTime)
  end_time = Column(DateTime)
  status = Column(Integer)
  nb_reqs = Column(Integer)
  owner = Column(String(length=32))
  byte_size = Column(Integer)
  nb_reqs_done = Column(Integer)

  def __init__(self, id, name, creation_time, mediatype_id, nb_reqs_failed, activation_time, end_time, status, nb_reqs, owner, byte_size, nb_reqs_done):
    self.id = id
    self.name = name
    self.creation_time = creation_time
    self.mediatype_id = mediatype_id
    self.nb_reqs_failed = nb_reqs_failed
    self.activation_time = activation_time
    self.end_time = end_time
    self.status = status
    self.nb_reqs = nb_reqs
    self.owner = owner
    self.byte_size = byte_size
    self.nb_reqs_done = nb_reqs_done

  def __repr__(self):
    return "<Queue('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')" %(self.id, self.name, self.creation_time, self.mediatype_id, self.nb_reqs_failed, self.activation_time, self.end_time, self.status, self.nb_reqs, self.owner, self.byte_size, self.nb_reqs_done)

# The archived objects

class RequestArch(Base):
  """A request as describe in the archive table
  """
  __tablename__ = 'requests_history'
  id = Column(Integer, primary_key=True, autoincrement=False)
  file = Column(String(length=1024))
  creation_time = Column(DateTime)
  user = Column(String(length=32))
  client = Column(String(length=32))
  version = Column(String(length=32))
  email = Column(String(length=64))
  queue_id = Column(Integer)
  tape = Column(String(length=8))
  position = Column(Integer)
  level = Column(Integer)
  size = Column(Integer)
  tries = Column(Integer)
  errorcode = Column(Integer)
  submission_time = Column(DateTime)
  queued_time = Column(DateTime)
  end_time = Column(DateTime)
  status = Column(Integer)
  message = Column(String(length=254))

  def __init__(self, id, file, creation_time, user, client, version, email, queue_id, tape, position, level, size, tries, errorcode, submission_time, queued_time, end_time, status, message):
    self.id = id
    self.file = file
    self.creation_time = creation_time
    self.user = user
    self.client = client
    self.version = version
    self.email = email
    self.queue_id = queue_id
    self.tape = tape
    self.position = position
    self.level = level
    self.size = size
    self.tries = tries
    self.errorcode = errorcode
    self.submission_time = submission_time
    self.queued_time = queued_time
    self.end_time = end_time
    self.status = status
    self.message = message

  def __init__(self, r):
    self.id = r.id
    self.file = r.file
    self.creation_time = r.creation_time
    self.user = r.user
    self.client = r.client
    self.version = r.version
    self.email = r.email
    self.queue_id = r.queue_id
    self.tape = r.tape
    self.position = r.position
    self.size = r.size
    self.tries = r.tries
    self.level = r.level
    self.errorcode = r.errorcode
    self.submission_time = r.submission_time
    self.queued_time = r.queued_time
    self.end_time = r.end_time
    self.status = r.status
    self.message = r.message

  def __repr__(self):
    return "<Request('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')" %(self.id, self.file, self.creation_time, self.user, self.client, self.version, self.email, self.queue_id, self.tape, self.position, self.level, self.size, self.tries, self.errorcode, self.submission_time, self.queued_time, self.end_time, self.status, self.message)

class QueueArch(Base):
  """A queue as describe in the archive table
  """
  __tablename__ = 'queues_history'
  id = Column(Integer, primary_key=True, autoincrement=False)
  name = Column(String(length=12))
  creation_time = Column(DateTime)
  mediatype_id = Column(Integer)
  nb_reqs_failed = Column(Integer)
  activation_time = Column(DateTime)
  end_time = Column(DateTime)
  status = Column(Integer)
  nb_reqs = Column(Integer)
  owner = Column(String(length=32))
  byte_size = Column(Integer)
  nb_reqs_done = Column(Integer)

  def __init__(self, id, name, creation_time, mediatype_id, nb_reqs_failed, activation_time, end_time, status, nb_reqs, owner, byte_size, nb_reqs_done):
    self.id = id
    self.name = name
    self.creation_time = creation_time
    self.mediatype_id = mediatype_id
    self.nb_reqs_failed = nb_reqs_failed
    self.activation_time = activation_time
    self.end_time = end_time
    self.status = status
    self.nb_reqs = nb_reqs
    self.owner = owner
    self.byte_size = byte_size
    self.nb_reqs_done = nb_reqs_done

  def __init__(self, Queue):
    self.id = Queue.id
    self.name = Queue.name
    self.creation_time = Queue.creation_time
    self.mediatype_id = Queue.mediatype_id
    self.nb_reqs_failed = Queue.nb_reqs_failed
    self.activation_time = Queue.activation_time
    self.end_time = Queue.end_time
    self.status = Queue.status
    self.nb_reqs = Queue.nb_reqs
    self.owner = Queue.owner
    self.byte_size = Queue.byte_size
    self.nb_reqs_done = Queue.nb_reqs_done

  def __repr__(self):
    return "<Queue('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')" %(self.id, self.name, self.creation_time, self.mediatype_id, self.nb_reqs_failed, self.activation_time, self.end_time, self.status, self.nb_reqs, self.owner, self.byte_size, self.nb_reqs_done)

####
#
# End of Class declaration
#
####

def main():
  # Setting the archiving environment
  # Create a Session for archiving
  Arch_Session = sqlalchemy.orm.sessionmaker()
  # Create an Engine for archiving
  arch_engine = sqlalchemy.create_engine("mysql://%s:%s@%s/%s?unix_socket=%s"%(opt.arcuser, opt.arcpwd, opt.archost, opt.arcdb, opt.arcsock))
  # Bind the session to the engine
  Arch_Session.configure(bind=arch_engine)

  # Setting the data source environment
  # Create a Session
  Src_Session = sqlalchemy.orm.sessionmaker()
  # Create an Engine
  src_engine = sqlalchemy.create_engine("mysql://%s:%s@%s/%s?unix_socket=%s"%(opt.srcuser, opt.srcpwd, opt.srchost, opt.srcdb, opt.srcsock))
  # Bind the session to the engine
  Src_Session.configure(bind=src_engine)

  print ":: Starting archiver at %s"%(datetime.now())

  Base.metadata.create_all(arch_engine)

  ### Now we have 2 sessions (Arch_ and Src_)
  timelimit = datetime.today() - timedelta(days=int(opt.days))

  arch_reqs = []
  arch_queues = []
  # Get the data from Src
  srcsession = Src_Session()
  # 100 Created
  # 110 Submitted
  # 120 Queued
  # 140 Staged
  # 150 On disk
  # 160 Failed
  # 180 Invalid
  reqs = srcsession.query(Request).filter(and_(Request.status.in_(['140', '150', '160', '180']), Request.end_time < timelimit))
  print ":: %s requests to archive out of %s"%(reqs.count(), srcsession.query(Request).count())

  for r in reqs:
    arch_reqs.append(RequestArch(r))
  # Store the data in Arch
  archsession=Arch_Session()
  try:
    for r in arch_reqs:
      archsession.add(r)
    archsession.commit()
    archsession.close()
  except sqlalchemy.exc.IntegrityError, e:
    print e
    exit(-1)
  # Remove the archived requests from src table
  for r in reqs:
    srcsession.delete(r)
  print ":: %s requests left"%( srcsession.query(Request).count())
  srcsession.commit()

  queues = srcsession.query(Queue).filter(and_(Queue.status == 230, Queue.end_time < timelimit))
  print ":: %s queues to archive out of %s"%(queues.count(), srcsession.query(Queue).count())
  for q in queues:
    arch_queues.append(QueueArch(q))
  try:
    for q in arch_queues:
      archsession.add(q)
    archsession.commit()
    archsession.close()
  except sqlalchemy.exc.IntegrityError, e:
    print e
    exit(-2)
  # Remove the archived queues from src table
  for q in queues:
    srcsession.delete(q)
  print ":: %s queues left"%( srcsession.query(Queue).count())
  srcsession.commit()

if __name__ == "__main__":
  exit(main())




