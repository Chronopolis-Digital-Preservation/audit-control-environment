#!/usr/bin/python2
#
# Classes which the ace shell depends on
#
import readline
import re
import os

_help = '_help_'

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
    self.commands = {
      'get':{
        'content':{_help: 'get content <file> [contentID] \n' \
            '\tPrint the content of the file, optionally specified by the ContentID'},
        'props':  {_help: 'get props <file> [contentID] \n' \
            '\tPrint the properties of the file, optionally specified bt the ContentID'},
        _help : 'Possible arguments for get: \n\thelp get content \n\thelp get props',
      },'info':{
        _help: 'info [param] \n\tPrint the shell information matching optional parameter'
      },'import':{
        _help: 'import [tokenstore] \n\tImport the token store to duraspace'
      },'pull' :{
        'file':{_help: 'pull file <file> \n' \
            '\tPull a file from durastore to local disk'},
        'directory': {_help: 'pull directory <file> \n' \
            '\tPull a directory from durastore to local disk'},
        _help : 'Possible arguments for pull: \n\thelp pull file \n\thelp pull directory',
      },'put' :{
        'file':{_help: 'put file <file> \n' \
            '\tUpload a file or directory to durastore'},
        _help: 'Possible arguements for put: \n\thelp put file',
      },'set' :{
        'dhost':{_help: 'set dhost [hostname] \n' \
            '\tSet the durastore host'},
        'spaceID':{_help: 'set spaceID [id] \n' \
            '\tSet the durastore spaceID'},
        'user':{_help: 'set user [username]\n' \
            '\tSet the durastore user'},
        'validate':{_help: 'set valdate\n' \
            '\tSet validation of files when pulling on or off'},
        'verbose':{_help: 'set verbose\n' \
            '\tSet verbose mode on or off'},
        'wsdl':{_help: 'set wsdl [hostname]\n' \
            '\tSet the wsdl hostname'},
        _help: 'Possible arguments for set: \n\thelp set dhost \n\thelp set spaceID'\
            '\n\thelp set user \n\thelp set validate \n\thelp set verose'\
            '\n\thelp set wsdl',
      },'update':{
        'file':{_help: 'update file \n' \
            '\tUpdate the proofs for the file or directory'},
        _help: 'Possible arguments for update: \n\thelp update file',
      },'validate':{
        'local':{_help: 'validate local <file> [contentID] \n' \
            '\tValidate a local file against its proof in durastore, optional ContentID'},
        'remote':{_help: 'validate remote <contentID> \n' \
            '\tValidate the remote copy held in durastore specified by the contentID'},
        _help: 'Possible arguments for validate: \n\thelp validate local' \
            '\n\thelp validate remote',
      },'help':{
        'get':None,
        'import': None,
        'info': None,
        'pull': None,
        'put': None,
        'set': None,
        'update': None,
        'validate': None,
        _help: 'Possible help commands:' \
            '\n\thelp get \n\thelp import \n\thelp info \n\thelp pull \n\thelp put' \
            '\n\thelp set \n\thelp update \n\thelp validate'
      },
    }

    ## While we're here...
    ## Tab Complete object
    self.completer = Completer(self.commands)
    readline.set_completer_delims(' \t\n;')
    readline.parse_and_bind('tab: complete')
    readline.set_completer(self.completer.complete)

  def help(self, line):
    ## The initial help is stripped off so we want to add it back
    ## if nothing is there
    if len(line) == 0:
      line.append('help')

    help = get_help(line, self.commands)
    print help 

  def getCommands(self):
    return self.commands

def get_help(tokens, cmds):
  if tokens is None: 
    return ''

  if _help in cmds and len(tokens) == 0:
    return cmds[_help]
  else:
    if tokens[0] in cmds.keys():
      help = get_help(tokens[1:], cmds[tokens[0]])
    else:
      print 'Unknown argument %s' % tokens[0] 
      help = ''

  if help == '' and _help in cmds.keys():
   help = cmds[_help] 

  return help 

## Slighly modified version of: 
## https://sites.google.com/site/xiangyangsite/home/software-development/python-readline-completions
class Completer(object):
  def __init__(self, commands):
    self.commands = commands

  def traverse(self, tokens, cmds):
    if cmds is None or len(tokens) == 0:
      return []
    
    ## Base case -- when we're at the final command
    if len(tokens) == 1:
      return [x + ' ' for x in cmds if x.startswith(tokens[0]) and x is not _help]
    else:
      ## Follow along the dictionary with subsequent tokens 
      ## And child commands
      if tokens[0] in cmds.keys():
        return self.traverse(tokens[1:], cmds[tokens[0]])

    return []

  def complete(self, text, state):
    try:
      tokens = readline.get_line_buffer().split()
      if not tokens or readline.get_line_buffer()[-1] == ' ':
        tokens.append('')
      results = self.traverse(tokens, self.commands) + [None]
      return results[state]
    except Exception, e:
      print e
