#!/usr/bin/python2
#
# Classes which the ace shell depends on
#
import readline
import re
import os

class AceShell(object):
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
    self.commands = None
    self.helper = None
    self.completer = None

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

  def createCommands(self):
    ## May add on help in children later on
    self.commands = { 
      'get': ['content', 'props'],
      'info': [],
      'pull': ['file', 'directory'],
      'put': ['file'],
      'set': ['dhost', 'spaceID', 'user', 'validate', 'verbose', 'wsdl'],
      'update': ['file'],
      'validate': ['local', 'remote'],
      'import': [],
      'help': ['get', 'import', 'info', 'pull', 
               'put', 'set', 'update', 'validate']
    }

    ## While we're here...
    ## Helper and Tab Complete objects
    self.helper = Helper(self.commands)
    self.completer = Completer(self.commands)
    readline.set_completer_delims(' \t\n;')
    readline.parse_and_bind('tab: complete')
    readline.set_completer(self.completer.complete)

  def help(self, line):
    self.helper.help(line)

  def getCommands(self):
    return self.commands


class Helper(object):
  """For all the help options that need to be printed"""

  def __init__(self, commands):
    self.commands = commands 

  def print_unknown(self, cmd, children, bad):
    print 'Unknown option %s for %s' % (bad, cmd) 
    print 'Possible help command:'
    for c in children:
      print '\thelp %s %s' % (cmd, c)

  def help_get(self, args):
    children = self.commands.get('get')
    if args[0] not in children:
      self.print_unknown('get', children, args)
      return

    print 'get %s <file> [contentID]' % args[0]
    if args[0] == 'content':
      print '\tPrint the content of the file,', 
      print 'optionally specified by the contentID'
    elif args[0] == 'props':
      print '\tPrint the properties of the file,',
      print 'optionally specified by the contentID'

  def help_import(self, args):
    if args:
      self.print_unknown('import', [''], args)
      return

    print """import [tokenstore]
    Import the token store to matching duraspace content
    """

  def help_info(self, args):
    if args:
      self.print_unknown('info', [''], args)
      return

    print """info [param]
      Print the shell information matching optional parameter""" 

  def help_pull(self, args):
    children = self.commands.get('pull')
    if not args or args[0] not in children:
      self.print_unknown('pull', children, args)
      return

    print 'pull %s <file> [contentID]' % args[0]
    if args[0] == 'file':
      print '\tPull a file from durastore, optionally specified by contentID'
    elif args[0] == 'directory':
      print '\tPull a directory from durastore, optionally specified by contentID'

  def help_put(self, args):
    children = self.commands.get('put')
    if not args or args[0] not in children:
      self.print_unknown('put', children, args)
      return

    print 'put %s <file>' % args[0] 
    print '\tUpload a file or directory to durastore'

  def help_set(self, args):
    children = self.commands.get('set')
    if not args or args[0] not in children:
      self.print_unknown('set', children, args)
      return

    ## There's probably a better way to do this... for now this works
    if args[0] == 'wsdl':
      print 'set %s [hostname]' % args[0]
      print '\tSet the wsdl host, with optional hostname'
    elif args[0] == 'dhost':
      print 'set %s [hostname]' % args[0]
      print '\tSet the durastore host, with optional hostname'
    elif args[0] == 'spaceID':
      print 'set %s [id]' % args[0]
      print '\tSet the durastore space ID, with optional ID'
    elif args[0] == 'user':
      print 'set %s [user]' % args[0]
      print '\tSet the durastore user, with optional username'
    elif args[0] == 'validate':
      print 'set %s' % args[0]
      print '\tSet validation of files when pulling on or off'
    elif args[0] == 'verbose':
      print 'set %s' % args[0]
      print '\tSet verbose mode on or off'

  def help_update(self, args):
    children = self.commands.get('update')
    if not args or args[0] not in children:
      self.print_unknown('update', children, args)
      return

    print 'update %s'  % args[0]
    print '\tUpdate the proofs for the file or directory file', 

  def help_validate(self, args):
    children = self.commands.get('validate')
    if not args or args[0] not in children:
      self.print_unknown('validate', children, args)
      return

    if args[0] == 'local':
      print 'validate %s <file> [contentID]' % args[0]
      print '\tValidate a local file against it\'s proof in durastore, optional',
      print 'contentID'
    elif args[0] == 'remote':
      print 'validate %s <contentID>' % args[0]
      print '\tValidate the remote copy held in durastore specified by the', 
      print 'contentID'

  def help_all(self, cmd):
    print """Unknown command: """,cmd,""" 
    Possible help commands: 
      exit
      help info
      help set                 
      help get
      help put 
      help update
      help validate
    """

  def help(self, text):
    line = list(text)
    if not line:
      self.help_all('')
      return
    
    cmd = line[0].strip()
    if cmd in self.commands:
      impl = getattr(self, 'help_%s' % cmd)
      children = line[1:]
      impl(children)
      return

    self.help_all(cmd)

## TODO: Bug when there is a space after a sub command, will match and add to
##       the end of the line

class Completer(object):
  def __init__(self, commands):
    self.commands = commands

  def complete_update(self, args):
    cmds = self.commands.get('update')
    ## If a whole command is matched, return nozing
    return [c + ' ' for c in cmds if not args or 
           (c.startswith(args[0]) and not args[0] in cmds)]
  
  def complete_put(self, args):
    cmds = self.commands.get('put')
    return [c + ' ' for c in cmds if not args or 
           (c.startswith(args[0]) and not args[0] in cmds)]

  def complete_set(self, args):
    cmds = self.commands.get('set')
    return [c + ' ' for c in cmds if not args or
           (c.startswith(args[0]) and not args[0] in cmds)]

  def complete_get(self, args):
    cmds = self.commands.get('get')
    return [c + ' ' for c in cmds if not args or
           (c.startswith(args[0]) and not args[0] in cmds)]

  def complete_validate(self, args):
    cmds = self.commands.get('validate')
    return [c + ' ' for c in cmds if not args or
           (c.startswith(args[0]) and not args[0] in cmds)]

  def complete_pull(self, args):
    cmds = self.commands.get('pull')
    return [c + ' ' for c in cmds if not args or 
           (c.startswith(args[0]) and not args[0] in cmds)]

  def complete_help(self, args):
    cmds = self.commands.get('help')
    if not args:
      return [c + ' ' for c in cmds]

    subcmd = args[0].strip()
    
    if subcmd not in cmds:
      return [c + ' ' for c in cmds if c.startswith(subcmd)]

    sargs = args[1:]
    impl = getattr(self, 'complete_%s' % subcmd)
    return impl(sargs)+[None]

  def complete(self, text, state):
    buf = readline.get_line_buffer()
    line = readline.get_line_buffer().split()

    ## Really need to centralize these
    # COMMANDS = ['help', 'exit', 'set', 'put', 'update', 'get', 'validate',
    #              'pull']
    COMMANDS = self.commands.keys()

    if not line:
      return [c + ' ' for c in COMMANDS][state]

    regex = re.compile('.*\s+$', re.M)
    if regex.match(buf):
      line.append('')

    cmd = line[0].strip()
    if cmd in COMMANDS:
      impl = getattr(self, 'complete_%s' % cmd)
      args = filter(lambda x: x != '',line[1:])
      return (impl(args)+[None])[state]
    results = [c + ' ' for c in COMMANDS if c.startswith(cmd)] + [None]
    return results[state]

