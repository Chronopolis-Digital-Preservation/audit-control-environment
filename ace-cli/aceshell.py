#!/fs/narahomes/shake/built/bin/python

import requests
import getopt, getpass, sys, os
import acestore, binascii
import readline
import re
from suds.client import Client

class AceShell:
  """Ace Shell Information"""
  def __init__(self, user=None, passwd=None, wsdl=None, dhost=None,  
               space=None, verbose=False):
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
    print 'Duracloud Username: ', self.user

  def ready(self):
    return (self.dhost!= None and self.spaceID != None and 
            self.user != None and self.passwd != None)

def shellHelp(shell):
  print """Shell Commands: 
    help                Show available commands
    exit                Quit the shell
    info                Show the shells parameters
    set                 Set a parameter
      wsdl    [host]    The wsdl host
      dhost   [host]    The duracloud host
      spaceID [id]      The duracloud space
      user    [user]    The duracloud user and password to
                        connect with
    put 
      file [contentID]  Upload a file to the duracloud host
      directory         Upload the contents of a directory to
                        the duracloud host
    update
      file [contendID]  The file to update, and the contentID (optional)

    get
      content [contentID] Get the content of the space, or 
                          content if specified
      props [contentID]   Get the parameters of the space, or
                          content if specified
    validate
      local  [filename]   Validate a local file 
      remote [contentID]  Validate the remote file with the specified
                          contentID
  """

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

  def complete(self, text, state):
    buf = readline.get_line_buffer()
    line = readline.get_line_buffer().split()

    COMMANDS = ['help', 'exit', 'set', 'put', 'update', 'get', 'validate']
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
      if len(args) > 1:
        return 
      return (impl(args)+[None])[state]
    results = [c + ' ' for c in COMMANDS if c.startswith(cmd)] + [None]
    return results[state]

def createHeaders(filename):
  pre = 'x-dura-meta-'
  headers = {}
  reqList = [(filename, binascii.b2a_hex(acestore.digestFile(filename)))]
  prooflist, clientinfo = acestore.getProof(reqList)
  headers[pre+'roundId'] = clientinfo['roundId']
  headers[pre+'tokenClass'] = clientinfo['tokenClass']
  headers[pre+'digestService'] = clientinfo['digestService']
  proof = prooflist[filename]
  for line in proof:
    headers[pre+"proof-"+str(proof.index(line))] = line
  return headers

def processDir(args, directory, files):
  origin, method, shell = args
  req = None
  for f in files:
    _, base = os.path.split(origin)
    _, _, dirs = directory.partition(base)
    contentID = base+dirs+'/'+f 
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

def doPut(shell, arg, filename, contentID=None):
  if not shell.ready():
    print "Cannot perform put without proper information set first"
    return
  
  if os.path.isdir(filename):
    if filename.endswith('/'):
      filename = filename.rstrip('/')
    os.path.walk(filename, processDir, (filename, 'put', shell))
    return

  headers = createHeaders(filename)
  if contentID == None:
    _, contentID = os.path.split(filename)
  url = 'https://'+shell.dhost+'/durastore/'+shell.spaceID+'/'+contentID
  req = requests.put(url, auth=(shell.user, shell.passwd), 
                     headers=headers, data=open(filename, 'rb'))
  print req.status_code


def update(shell, arg, filename, contentID=None):
  if not shell.ready():
    print "Cannot perform update without proper information set first"
    return

  if not os.path.isfile(filename) and not os.path.isdir(filename):
    print "File/Directory",filename,"does not exist"
    return

  if os.path.isdir(filename):
    print "Updating directory recursively is not yet supported"
    if filename.endswith('/'):
      filename = filename.rstrip('/')
    os.path.walk(filename, processDir, (filename, 'post', shell))
    return

  headers = createHeaders(filename) 
  if contentID == None:
    _, contentID = os.path.split(filename) 
  url = 'https://'+shell.dhost+'/durastore'+shell.spaceID+'/'+contentID
  req = requests.post(url, auth=(shell.user, shell.passwd), 
                      headers=headers)

def doGet(shell, type, contentID=None):
  if not shell.ready():
    print "Cannot perform get without proper information set first"
    return

  req = None
  url = "https://"+shell.dhost+"/durastore/"+shell.spaceID
  if contentID != None:
    url += "/"+contentID

  if type == 'content':
    req = requests.get(url, auth=(shell.user, shell.passwd))
  elif type == 'props':
    req = requests.head(url, auth=(shell.user, shell.passwd))

  print "Status: \n", req.status_code
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
      shell.verbose = False
    else:
      print 'Turning on verbose mode.'
      shell.verbose = True
  else:
    print "Unknown set parameter."

def doValidate(shell, arg, file):
  pre = 'x-dura-meta-'
  if os.path.isdir(file): 
    print 'Validating directories is not yet supported'
    return
 
  if arg == 'remote':
    print 'Validating remote files will be supported soon'
    return

  url = "https://"+shell.dhost+"/durastore/"+shell.spaceID+"/"+file
  req = requests.head(url, auth=(shell.user, shell.passwd))
  headers = req.headers
  keys = headers.keys()
  keys.sort()
  proof = [headers.get(x) for x in keys 
                          if x.startswith(pre+'proof')]
  digest = headers.get(pre+'digestservice')
  rhash = getRoundHash(headers.get(pre+'roundid'))
  csi = acestore.calculateCSI(file, proof, digest) 

  if shell.verbose: 
    print 'Round ID:',headers.get(pre+'roundid')
    print 'Token Class:',headers.get(pre+'tokenclass')
    print 'Round Hash from IMS:', rhash
    print 'Calculated csi:',csi

  print rhash == csi


def getRoundHash(round, url=None):
  url = 'http://ims.umiacs.umd.edu:8080/ace-ims/IMSWebService?wsdl'
  client = Client(url)
  response = client.service.getRoundSummaries(round)
  return response.__getitem__(0).hashValue


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
         'info': printInfo, 'update': update,
         'get':  doGet, 'set':  doSet,
         'put': doPut, 'validate': doValidate}
  args = cmd.split()
  if args[0] not in fns:
    print args[0], "is not a valid command"
    return
  func = fns[args[0]]
  func(shell, *args[1:])

# For running while coding 
# def repl():
#   global acestore
#   acestore = reload(acestore)

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

  comp = Completer()
  readline.set_completer_delims(' \t\n;')
  readline.parse_and_bind('tab: complete')
  readline.set_completer(comp.complete)
  while shell.run:
    sys.stdout.write(prompt),
    cmd = raw_input(prompt) # sys.stdin.readline().strip('\n') # raw_input()
    sys.stdout.write('\033[0m')
    process(cmd, shell)

if __name__ == "__main__":
  main()
