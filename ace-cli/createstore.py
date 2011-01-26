#!/usr/bin/python
#
#
#
import acestore
import os
import sys
import binascii
import argparse

class TrollSettings:
    outfile = sys.stdout
    requestlist = []

def processDir(settings,directory,files):
    for f in files:
        fullPath = os.path.join(directory,f)
        if os.path.isfile(fullPath):
            settings.requestlist.append((fullPath,binascii.b2a_hex(acestore.digestFile(fullPath))))
        if len(settings.requestlist) > 1000:
           acestore.createTokens(settings.requestlist,settings.outfile)
           del(settings.requestlist[:])


def main():
    parser = argparse.ArgumentParser(description='Validate Files using a token store.')
    parser.add_argument('files',nargs='+',help='Files or directories to scan');
    parser.add_argument('-v','--verbose',action='store_true')
    parser.add_argument('-r','--recurse',action='store_true',help='recurse into any listed directories')
    parser.add_argument('-f','--file',nargs='?', type=argparse.FileType('w'), default=sys.stdout,help='File to write token store into, default std out')        
    parser.add_argument('-d','--digest',nargs='?',default='SHA-256',help='Digest algorithm to use (default SHA-256)')

    args = parser.parse_args()
    if acestore.getAlgorithm(args.digest) is None:
        print 'Invalid digest algorithm ' + args.digest
        parser.print_help()
        sys.exit(2)

    settings = TrollSettings()

    settings.outfile = args.file
    
    for file in args.files:
        if not (os.path.isfile(file) or args.recurse and os.path.isdir(file)):
           if not args.recurse and os.path.isdir(file):
               print file + " is a directory and -r, or --recurse has not been specified"
           elif not os.path.isfile(file):
               print file + " does not exist"
           parser.print_help()
           sys.exit(2)

    for file in args.files:
        if os.path.isdir(file):
            os.path.walk(file,processDir, settings)
        else:
            settings.requestlist.append((file,binascii.b2a_hex(acestore.digestFile(file))))

    if len(settings.requestlist) > 0:
        acestore.createTokens(settings.requestlist,settings.outfile)

    settings.outfile.close()

if __name__ == "__main__":
    main()

    


    

