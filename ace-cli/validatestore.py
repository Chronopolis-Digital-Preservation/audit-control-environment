import sys
import argparse
import acestore
import os

def scan_dir(dict,directory,files):
    unseenids = dict['store'].entries.keys
    for f in files:
        fullpath = os.path.join(directory,f)
        idpath = fullpath[dict['trim']:]
        #unseenids.remove(unseenids.index(idpath))
        if os.path.isfile(fullpath):
            result = dict['store'].validate(fullpath,idpath)
            if result is None:
                print idpath + ' not found in tokenstore'
            elif not result:
                print fullpath + ' not valid'
            #elif [dict['verbose']]
            #    print fullpath + ' valid ' + 
        #for id in unseenids:
        #    print id + ' not found'

def main():
    parser = argparse.ArgumentParser(description='Validate Files using a token store.')
    parser.add_argument('store',metavar='token-store', nargs='?',help='token store file name or "-" to read from stdin')
    parser.add_argument('-v','--verbose',action='store_true')
    parser.add_argument('-m','--missing',action='store_true',default=False,help='Show files which appear in token store, but not on the local filesystem')
    parser.add_argument('-f','--files',nargs=1,help="comma(,) separated list of files to validate")
    parser.add_argument('-i','--identifiers',nargs=1,help='comma(,) separated list of identifiers to match with supplied files (ie, file1=id1)')
    parser.add_argument('-d','--dir',nargs='?',default='.',help='directory to scan, default is current directory')
    parser.add_argument('--trimidentifiers',nargs='?',type=int,default=0,help='remove the first n characters from id\'s when reading a token store')
    parser.add_argument('--trimpath',nargs='?',type=int,default=0,help='remove the first n characters from file paths when looking for identifiers')

    arguments = parser.parse_args()

    storefile = sys.stdin
    if arguments.store != '-' and arguments.store is not None:
        storefile = open(arguments.store,'r')

    aceStore = acestore.TokenStore(storefile,arguments.trimidentifiers)
    
    os.path.walk(arguments.dir,scan_dir,{'store':aceStore,'trim':arguments.trimpath})

if __name__ == "__main__":
    main()
