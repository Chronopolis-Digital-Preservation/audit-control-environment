#!/usr/bin/python2

import requests
import getopt, getpass, sys, os
import acestore, binascii
import readline
import re
from suds.client import Client

## TODO:
##   Batch token creation on folders
##   Help for individual commands
##   Status Code to Human Readable

class AceShell:
  """Ace Shell Information"""
  def __init__(self, user=None, passwd=None, dhost=None, space=None, 
               wsdl='http://ims.umiacs.umd.edu:8080/ace-ims/IMSWebService?wsdl',  
               verbose=False):
    self.user = user
    self.passwd = passwd
    self.wsdl = wsdl 
    self.dhost = dhost
    self.spaceID = space
    self.verbose = verbose
    self.run = True
    self.validate = True

  def printInfo(self):
    print 'WSDL: ', self.wsdl, '\n', 
    print 'Duracloud Host: ', self.dhost, '\n',
    print 'Duracloud SpaceID: ', self.spaceID, '\n',
    print 'Duracloud Username: ', self.user, '\n',
    print 'Validate on pull: ', self.validate, '\n',
    print 'Verbose mode: ', self.verbose

  def ready(self):
    return (self.dhost!= None and self.spaceID != None and 
            self.user != None and self.passwd != None)

def shellHelp(shell):
  print """Shell Commands: 
    help                Show available commands
    exit                Quit the shell
    info                Show the shells parameters

    set                 Set a parameter
      wsdl    [host]    Set the wsdl host
      dhost   [host]    Set the duracloud host
      spaceID [id]      Set the duracloud space
      user    [user]    Set the duracloud user and password to
                        connect with
      validate [file]   Set validation of files when pulling from 
                        duracloud
      verbose           Turn on/off verbose output

    get
      content [contentID] Get the content of the space, or 
                          content if specified
      props [contentID]   Get the parameters of the space, or
                          content if specified
    put 
      file [contentID]  Upload a file to the duracloud host
      directory         Upload the contents of a directory to
                        the duracloud host
    update
      file [contendID]  The file to update, and the contentID (optional)

    validate
      local  [filename]   Validate a local file 
      remote [contentID]  Validate the remote file with the specified
                          contentID
  """

## Tab complete class

class Completer(object):
  def complete_update(self, args):
    cmds = ['file']
    return [c + ' ' for c in cmds if not args or c.startswith(args[0])]
  
  def complete_put(self, args):
    cmds = ['file', 'directory']
    return [c + ' ' for c in cmds if not args or c.startswith(args[0])]

  def complete_set(self, args):
    cmds = ['wsdl', 'dhost', 'spaceID', 'user', 'verbose']
    return [c + ' ' for c in cmds if not args or c.startswith(args[0])]

  def complete_get(self, args):
    cmds = ['content', 'props']
    return [c + ' ' for c in cmds if not args or c.startswith(args[0])]

  def complete_validate(self, args):
    cmds = ['remote', 'local']
    return [c + ' ' for c in cmds if not args or c.startswith(args[0])]

  def complete_pull(self, args):
    cmds = ['file', 'directory']
    return [c + ' ' for c in cmds if not args or c.startswith(args[0])]

  def complete(self, text, state):
    buf = readline.get_line_buffer()
    line = readline.get_line_buffer().split()

    COMMANDS = ['help', 'exit', 'set', 'put', 'update', 'get', 'validate',
                'pull']
    if not line:
      return [c + ' ' for c in COMMANDS][state]

    regex = re.compile('.*\s+$', re.M)
    if regex.match(buf):
      line.append('')

    cmd = line[0].strip()
    if cmd in COMMANDS:
      impl = getattr(self, 'complete_%s' % cmd)
      args = line[1:]
      # Currently no commands have more than 1 argument, so chop it here
      # Also, don't feel like completing path names hah
      if len(args) > 1:
        return 
      return (impl(args)+[None])[state]
    results = [c + ' ' for c in COMMANDS if c.startswith(cmd)] + [None]
    return results[state]

##
## Main functions for the shell commands
##   On all - contentID should default to filename unless specified
##   Will probably want to clean up how work on dir/files are done
##

def doPut(shell, arg, filename, contentID=None):
  if os.path.isdir(filename):
    if filename.endswith('/'):
      filename = filename.rstrip('/')
    os.path.walk(filename, processDir, (filename, 'put', shell))
    return

  headers = createHeaders(filename)
  if contentID == None:
    contentID = filename
  url = 'https://'+shell.dhost+'/durastore/'+shell.spaceID+'/'+contentID
  req = requests.put(url, auth=(shell.user, shell.passwd), 
                     headers=headers, data=open(filename, 'rb'))
  print req.status_code


def doUpdate(shell, arg, filename, contentID=None):
  if not os.path.isfile(filename) and not os.path.isdir(filename):
    print "File/Directory",filename,"does not exist"
    return

  if os.path.isdir(filename):
    if filename.endswith('/'):
      filename = filename.rstrip('/')
    os.path.walk(filename, processDir, (filename, 'post', shell))
    return

  headers = createHeaders(filename) 
  if contentID == None:
    contentID = filename
  url = 'https://'+shell.dhost+'/durastore/'+shell.spaceID+'/'+contentID
  print url
  req = requests.post(url, auth=(shell.user, shell.passwd), 
                      headers=headers)
  print req.status_code

# Similar to pull, but we're not saving the file... not sure what the
# point of that is...
# TODO: Pretty print for headers

def doGet(shell, type, contentID=None):
  req = None
  url = "https://"+shell.dhost+"/durastore/"+shell.spaceID
  if contentID != None:
    url += "/"+contentID

  if type == 'content':
    req = requests.get(url, auth=(shell.user, shell.passwd))
  elif type == 'props':
    req = requests.head(url, auth=(shell.user, shell.passwd))

  print "Status: \n", req.status_code
  if shell.verbose or type =='props': 
    print "Headers: \n", req.headers
  print "Data: \n", req.text


def doSet(shell, arg, param=None):
  if arg == 'wsdl':
    shell.wsdl = (raw_input('WSDL Host: ') 
                  if param == None else param)
  elif arg == 'dhost':
    shell.dhost = (raw_input('Duracloud host: ') 
                   if param == None else param) 

    # Clean the url of any leading/trailing info
    if shell.dhost.startswith('https://'):
      shell.dhost = shell.dhost.lstrip('https://')
    if shell.dhost.endswith('/'):
      shell.dhost = shell.dhost.rstrip('/')
  elif arg == 'spaceID':
    shell.spaceID = (raw_input('Duracloud spaceID: ') 
                     if param == None else param)
  elif arg == 'user':
    username = (raw_input('Duracloud username: ') 
                if param == None else param)
    passwd = getpass.getpass()
    shell.user = username
    shell.passwd = passwd
  elif arg == 'verbose':
    if shell.verbose:
      print 'Turning off verbose mode.'
    else:
      print 'Turning on verbose mode.'

    shell.verbose = not shell.verbose
  elif arg == 'validate':
    if shell.validate:
      print 'Turning off validate on pull.'
    else:
      print 'Turning on validate on pull.'

    shell.validate = not shell.validate
  else:
    print "Unknown set parameter."


def doPull(shell, arg, file, contentID=None):
  stage = raw_input('Where would you like to stage the data? ')

  if arg == 'directory':
    print 'I really shouldn\'t add these in until I\'ve coded them'
    return

  # Get the file name and make a request
  # Not sure how this would fare for large files ( >8G or so )
  if contentID == None: 
    contentID = file 
  file = os.path.join(stage, contentID)
  print file
  url = 'https://'+shell.dhost+'/durastore/'+shell.spaceID+'/'+contentID
  req = requests.get(url, auth=(shell.user, shell.passwd))
  if req.status_code == 404:
    print "File Not Found in your duraspace."
    return

  # Make the directory if it does not exist
  dirs, _ = os.path.split(file)
  if not os.path.exists(dirs):
    os.makedirs(dirs)

  fnew = open(file, 'w+b')
  fnew.write(req.content)
  fnew.close()

  # validate and leave
  valid = doValidate(shell, 'local', file, contentID)
  print 'Finished pulling data; Status: ', req.status_code
  print 'File is valid: ', valid


def doValidate(shell, arg, file, contentID=None):
  pre = 'x-dura-meta-'
  prevhash = None

  if arg not in ['remote', 'local']:
    print 'Unknown argument for validate'
    return

  if os.path.isdir(file): 
    print 'Validating directories is not yet supported'
    return
 
  if contentID == None:
    contentID = file
  url = "https://"+shell.dhost+"/durastore/"+shell.spaceID+"/"+contentID

  req = requests.head(url, auth=(shell.user, shell.passwd))
  headers = req.headers
  keys = headers.keys()
  keys.sort()

  if arg == 'remote':
    pre += 'remote-'
    prevhash = binascii.a2b_hex(headers.get('etag'))
  elif arg == 'local':
    pre += 'local-'

  proof = [headers.get(x) for x in keys 
                          if x.startswith(pre+'proof')]
  digest = headers.get(pre+'digestservice')
  rhash = getRoundHash(headers.get(pre+'roundid'))
  csi = acestore.calculateCSI(file, proof, digest, prevhash) 

  if shell.verbose: 
    print 'Round ID:', headers.get(pre+'roundid')
    print 'Token Class:', headers.get(pre+'tokenclass')
    print 'Round Hash from IMS:', rhash
    print 'Calculated csi:', csi

  valid = rhash == csi
  print valid 
  return valid

##
## Helper methods for the most part
## 

def getRoundHash(round, url=None):
  client = Client(shell.wsdl)
  response = client.service.getRoundSummaries(round)
  return response.__getitem__(0).hashValue

# Need to check working on a directory when given a full path
def processDir(args, directory, files):
  origin, method, shell = args
  req = None
  for f in files:
    _, base = os.path.split(origin)
    _, _, dirs = directory.partition(base)

    # This has worked so far, may opt for os.join later 
    contentID = base+dirs+'/'+f 

    # Ignore directories...  
    if (os.path.isdir(os.path.join(directory, f))):
      continue
    headers = createHeaders(os.path.join(directory,f))
    url = 'https://'+shell.dhost+'/durastore/'+shell.spaceID+'/'+contentID

    if method == 'post':
      req = requests.post(url, auth=(shell.user, shell.passwd), 
                          headers=headers)
    elif method == 'put':
      fobj = open(os.path.join(directory, f), 'rb')
      req = requests.put(url, auth=(shell.user, shell.passwd), 
                         data=fobj, headers=headers)
    print req.url, req.status_code,'\n', req.text

# Needs a better name
def headerHelp(filename, pre, digest):
  headers = {}
  reqList = [(filename, binascii.b2a_hex(acestore.digestFile(filename, digest)))]
  prooflist, clientinfo = acestore.getProof(reqList)
  headers[pre+'roundId'] = clientinfo['roundId']
  headers[pre+'tokenClass'] = clientinfo['tokenClass']
  headers[pre+'digestService'] = clientinfo['digestService']
  proof = prooflist[filename]
  for line in proof:
    headers[pre+"proof-"+str(proof.index(line))] = line
  return headers

def createHeaders(filename):
  headers = {}
  headers.update(headerHelp(filename, 'x-dura-meta-local-', 'SHA-256'))
  headers.update(headerHelp(filename, 'x-dura-meta-remote-', 'MD5'))
  return headers 

def printInfo(shell):
  shell.printInfo()

def help():
  print """ACE Shell
  Usage: aceshell.py [options]

  Arguments:
    -h, --help          Print this help
    -v, --verbose       Verbose mode
  """
  shellHelp(None)

def stopShell(shell):
  shell.run = False
  if shell.verbose: print "Exiting ACE Shell"

def process(cmd, shell):
  if cmd == None or len(cmd) == 0: return

  fns = {'help': shellHelp, 'exit': stopShell,
         'info': printInfo, 'update': doUpdate,
         'get':  doGet, 'set':  doSet,
         'put': doPut, 'pull': doPull, 
         'validate': doValidate}

  args = cmd.split()
  if args[0] not in fns:
    print args[0], "is not a valid command"
    return
  if args[0] in ['get', 'update', 'put', 'validate', 'pull'] and not shell.ready():
    print "Please set duraspace information before using",args[0]
    return

  func = fns[args[0]]
  func(shell, *args[1:])

##
## For running while coding... sort of 
##  
#  def repl():
#    global acestore
#    acestore = reload(acestore)

def main():
  try:
    opts, args = getopt.getopt(sys.argv[1:], "hv", ["help","verbose"])
  except getopt.GetoptError, err:
    print str(err)
    help()
    sys.exit(1)

  colors = {'white':'\033[1;37m',
            'esc': '\033[1;37m'} 

  shell = AceShell()
  prompt = '>>\033[1;37m '

  # Set up our tab complete object
  comp = Completer()
  readline.set_completer_delims(' \t\n;')
  readline.parse_and_bind('tab: complete')
  readline.set_completer(comp.complete)
  print "Ace Shell"
  print "To get a list of commands type help, to leave type exit"

  # This gets a little buggy, should fix when the input gets fubar'd 
  while shell.run:
    sys.stdout.write(prompt),
    cmd = raw_input(prompt) # sys.stdin.readline().strip('\n') # raw_input()
    sys.stdout.write('\033[0m')
    process(cmd, shell)

if __name__ == "__main__":
  main()
