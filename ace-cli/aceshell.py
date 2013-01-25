#!/usr/bin/python2

import requests
import getopt, getpass, sys, os
import acestore, acelib, binascii
import readline
import re
import xml.dom.minidom
from suds.client import Client

## Global for success codes
SUCCESS = (200, 201)


##
## Main functions for the shell commands
##   On all - contentID should default to filename unless specified
##

## Open question -- should we use the base directory as the filename?
## ie: filename = /fs/narahomes/shake/scripts/ base = /scripts/
## on durastore as scripts/etcetc

def doPut(shell, arg, filename, base=''):
  """Put files or directories into duraspace"""
  
  if not os.path.exists(filename):
    print 'File/Directory %s does not exist' % filename
    return 

  base_dir = ''
  if filename.startswith('/'):
    base_dir = os.path.basename(filename.rstrip('/')) + '/'
  else:
    base_die = filename.split('/')[0] +'/'

  pre_l = 'x-dura-meta-local-'
  pre_r = 'x-dura-meta-remote-'
  rllocal = createRequestList(filename, 'SHA-256', base_dir)
  rlremote = createRequestList(filename, 'MD5', base_dir)
  prooflocal, client_l = acestore.getProof(rllocal)
  proofremote, client_r = acestore.getProof(rlremote)

  for file in prooflocal: 
    headers = createHeaders(pre_l, prooflocal.get(file), client_l)
    headers.update(createHeaders(pre_r, proofremote.get(file), client_r))

    contentID = base+file

    url = 'https://'+shell.dhost+'/durastore/'+shell.spaceID+'/'+contentID
    req = requests.put(url, auth=(shell.user, shell.passwd), 
                       headers=headers, data=open(file, 'rb'))

    if req.status_code in SUCCESS:
      print 'Successfully put %s' % file 
    else:
      print 'Could not put %s into your durastore space' % file 

## TODO: This and put share many things... combine into one?

def doUpdate(shell, arg, filename, base=''):
  """Update files or directories already in duraspace"""

  if not os.path.exists(filename):
    print 'File/Directory %s does not exist' % filename
    return

  base_dir = ''
  if filename.startswith('/'):
    base_dir = os.path.basename(filename.rstrip('/')) + '/'
  else:
    base_dir = filename.split('/')[0] +'/'

  pre_l = 'x-dura-meta-local-'
  pre_r = 'x-dura-meta-remote-'
  rllocal = createRequestList(filename, 'SHA-256', base_dir)
  rlremote = createRequestList(filename, 'MD5', base_dir)
  prooflocal, client_l = acestore.getProof(rllocal)
  proofremote, client_r = acestore.getProof(rlremote)

  for file in prooflocal:
    headers = createHeaders(pre_l, prooflocal.get(file), client_l)
    headers.update(createHeaders(pre_r, proofremote.get(file), client_r))

    contentID = base+file
    url = 'https://'+shell.dhost+'/durastore/'+shell.spaceID+'/'+contentID
    req = requests.post(url, auth=(shell.user, shell.passwd), headers=headers)

    if req.status_code in SUCCESS:
      print 'Successfully updated %s' % filename
    else:
      print 'Could not update %s' % filename

# Similar to pull, but we're not saving the file... not sure what the
# point of that is, especially if the file is not text 
# TODO: Pretty print for headers

def doGet(shell, type, contentID=''):
  """Print contents or properties of files in duraspace"""

  req = None
  url = 'https://'+shell.dhost+'/durastore/'+shell.spaceID
  if contentID != None:
    url += "/"+contentID

  if type == 'content':
    req = requests.get(url, auth=(shell.user, shell.passwd))
  elif type == 'props':
    req = requests.head(url, auth=(shell.user, shell.passwd))

  status = 'success' if req.status_code in SUCCESS else 'failure'
  print "Status: \n", status 
  if shell.verbose or type =='props': 
    print "Headers: \n", req.headers
  print "Data: \n", req.text

def doImport(shell, filename):
  if not os.path.exists(filename):
    print 'Cannot open file %s' % filename
    return

  pre = 'x-dura-meta-local-'
  base = raw_input('Enter the base contentID of the duraspace collection (leave blank for none): ') 

  ts = acestore.TokenStore(open(filename, 'r'))
  for file in ts.entries:
    entry = ts.entries.get(file)
    contentID = os.path.join(base, file.lstrip('/'))
    headers = createHeaders(pre, entry.proof, entry.get_client_info())
    url = 'https://'+shell.dhost+'/durastore/'+shell.spaceID+'/'+contentID
    req = requests.post(url, auth=(shell.user, shell.passwd), headers=headers)
    if req.status_code in SUCCESS:
      print 'Successfully added ace proof to %s' % file
    else:
      print 'Could not add ace proof for %s' % file
      print 'Tried to use contentID: %s' % contentID

def doSet(shell, arg, param=None):
  """Set shell properties"""

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
  """Pull files and directories from duraspace"""

  debug = True
  stage = raw_input('Where would you like to stage the data? ')

  if arg == 'directory':
    ## Let's grab content from the space
    ## Then match against everything in the dir and pull them
    url = 'https://'+shell.dhost+'/durastore/'+shell.spaceID
    req = requests.get(url, auth=(shell.user, shell.passwd))
    dom = xml.dom.minidom.parseString(req.text)
    items = dom.getElementsByTagName('item')
    contentIDs = [item.firstChild.nodeValue for item in items if
                  item.firstChild.nodeValue.startswith(file)]
  else:
    contentIDs = [contentID]

  for contentID in contentIDs:
    ## Get the file name and make a request
    ## Not sure how this would fare for large files ( >8G or so )
    if contentID == None: 
      contentID = file 
    file = os.path.join(stage, contentID)
    print file
    url = 'https://'+shell.dhost+'/durastore/'+shell.spaceID+'/'+contentID
    req = requests.get(url, auth=(shell.user, shell.passwd))
    if req.status_code == 404:
      print 'File Not Found in your duraspace.'
      return

    ## Make the directory if it does not exist
    dirs, _ = os.path.split(file)
    if not os.path.exists(dirs):
      os.makedirs(dirs)

    ## Open the file to write in binary, and create if needed
    fnew = open(file, 'w+b')
    fnew.write(req.content)
    fnew.close()

    ## validate
    status = 'success' if req.status_code in SUCCESS else 'failure'
    valid = doValidate(shell, 'local', file, contentID)
    print 'Finished pulling data; Status: ', status 
    print 'File is valid: ', valid


def doValidate(shell, arg, file, contentID=None):
  """Validate local or remote files"""

  ## set properties on duracloud contain 'x-dura-meta', so we include it
  pre = 'x-dura-meta-'
  prevhash = None
  validateList = []

  if not checkArgs('validate', ['local', 'remote'], arg):
    return

  if os.path.isdir(file): 
    print 'Validating directories is not yet supported'
    for r, _, files in os.walk(file):
      for f in files:
        validateList.append(os.path.join(r,f))
    print validateList
    return
  else:
    validateList.append(file)
 
  if contentID == None:
    contentID = file
  url = 'https://'+shell.dhost+'/durastore/'+shell.spaceID+'/'+contentID

  req = requests.head(url, auth=(shell.user, shell.passwd))
  headers = req.headers
  keys = headers.keys()
  keys.sort()

  ## Pull the MD5 hash from duracloud if it is remote
  if arg == 'remote':
    pre += 'remote-'
    prevhash = binascii.a2b_hex(headers.get('etag'))
  elif arg == 'local':
    pre += 'local-'

  proof = [headers.get(x) for x in keys 
           if x.startswith(pre+'proof')]
  digest = headers.get(pre+'digestservice')
  rhash = getRoundHash(shell, headers.get(pre+'roundid'))
  csi = acestore.calculateCSI(file, proof, digest, prevhash) 

  if shell.verbose: 
    print 'Round ID:', headers.get(pre+'roundid')
    print 'Token Class:', headers.get(pre+'tokenclass')
    print 'Round Hash from IMS:', rhash
    print 'Calculated csi:', csi

  valid = rhash == csi
  print 'File is valid: ', valid 
  return valid

##
## Helper methods for the most part
## 

def createRequestList(filename, digest, base=''):
  """ Return a list of tuples as (filename, hash) for the ims """
  if os.path.isdir(filename):
    rl = [(os.path.join(base, r.split(base)[1], f),  
          binascii.b2a_hex(acestore.digestFile(os.path.join(r, f), 
          digest))) for r, _, files in os.walk(filename)
                    for f in files]
  else:
    rl = [(filename, binascii.b2a_hex(acestore.digestFile(filename, digest)))]
  return rl

def checkArgs(cmd, children, arg):
  """ Czech an argument for a given command """
  valid = True
  if arg not in children:
    print 'Unknown argument for %s' % cmd
    valid = False
  return valid

def getRoundHash(shell, round, url=None):
  client = Client(shell.wsdl)
  response = client.service.getRoundSummaries(round)
  return response.__getitem__(0).hashValue

def createHeaders(pre, proof, clientinfo):
  headers = {}
  for line in proof:
    headers[pre+'proof-'+str(proof.index(line))] = line
  headers[pre+'roundId'] = clientinfo['roundId']
  headers[pre+'tokenClass'] = clientinfo['tokenClass']
  headers[pre+'digestService'] = clientinfo['digestService']
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

def sHelp(shell, *args):
  shell.help(args)

def stopShell(shell):
  shell.run = False
  if shell.verbose: print "Exiting ACE Shell"

def process(cmd, shell):
  if cmd == None or len(cmd) == 0: return

  fns = {'help': sHelp, 'exit': stopShell,
         'info': printInfo, 'update': doUpdate,
         'get':  doGet, 'set':  doSet,
         'put': doPut, 'pull': doPull, 
         'validate': doValidate, 'import': doImport}

  args = cmd.split()
  if args[0] not in fns:
    print args[0], "is not a valid command"
    return

  ## Check if it's a command that requires duracloud info
  # if args[0] in ['get', 'update', 'put', 'validate', 'pull', 'import'] and not shell.ready():
  #   print 'Please set duraspace information before using',args[0]
  #   return

  func = fns[args[0]]
  func(shell, *args[1:])

##
## For running while coding (but not really cause that's another file) 
##  
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
            'esc': '\033[0m'} 

  shell = acelib.AceShell()
  shell.createCommands()
  prompt = colors.get('esc')+'>> '+colors.get('white')

  print "Ace Shell"
  print "To get more information type help, to leave type exit"

  # This gets a little buggy, should fix when the input gets fubar'd 
  while shell.run:
    cmd = raw_input(prompt) 
    sys.stdout.write('\033[0m')
    process(cmd, shell)

if __name__ == "__main__":
  main()
