#!/usr/bin/python2
#
# ACE Token store tool
#
import hashlib
import binascii
import string
import getopt
import sys
import time
from urlparse import urlparse
from suds.client import Client
from suds.sax.date import DateTime

class TokenStore:
    """Ace Token Store"""
    
    def __init__(self,infile,trimid=0):
        self.entries = {}
        self.roundlist = {}
        while 1:
            header = readHeader(infile)
            if not header:
                break
            albName = header[0]
            identifiers = readIdentifiers(infile)
            proof = readProof(infile)
            entry = TokenStoreEntry(proof,header)
            for id in identifiers:
                self.entries[id[trimid:]] = entry

    def get_round(self,round):
        if round not in self.roundlist:
            url='http://ims.umiacs.umd.edu:8080/ace-ims/IMSWebService?wsdl'
            client = Client(url)
            response = client.service.getRoundSummaries(round)
            for resp in response:
                self.roundlist[resp.id] = resp.hashValue;
        return self.roundlist[round]

    def validate(self,file,identifier):
        if identifier not in self.entries:
            return None
        token = self.entries[identifier]
        roundhash = self.get_round(token.round)
        prevhash = digestFile(file)
        for proofLine in token.proof:
            prevhash = calculateLevel(prevhash,proofLine,token.algorithm)
        if (binascii.b2a_hex(prevhash) != roundhash):
            return False
        else:
            return True
        
        
class TokenStoreEntry:
    """Token Store Entry"""
    def __init__(self,proof,headerparts):
        self.proof = proof
        self.algorithm,self.server,self.service,self.round,self.date,self.length = headerparts
        self.round = int(self.round)

# Should merge this and create tokens, or at least some parts since they're pretty close
def getProof(digestlist, wsdl='http://ims.umiacs.umd.edu:8080/ace-ims/IMSWebService?wsdl'):
    urlparts = urlparse(wsdl)
    client = Client(wsdl)
    requestlist = []
    for digestpair in digestlist:
        request = client.factory.create('tokenRequest')
        request.hashValue = digestpair[1]
        request.name = digestpair[0]
        requestlist.append(request)
    response = client.service.requestTokensImmediate('SHA-256-0', requestlist)
    prooflist = {}
    clientInfo = {}
    for item in response:
        lines = []
        for proofelement in item.proofElements:
            proofelement.hashes.insert(proofelement.index,'X')
            lines.append(":".join(proofelement.hashes))
        prooflist[item.name] = lines
        clientInfo['roundId'] = str(item.roundId)
        clientInfo['tokenClass'] = item.tokenClassName
        clientInfo['digestService'] = item.digestService
    return (prooflist, clientInfo)

   
def createTokens(digestlist,outfile,wsdl='http://ims.umiacs.umd.edu:8080/ace-ims/IMSWebService?wsdl'):
    print 'start_create ' + str(time.time())
    urlparts = urlparse(wsdl)
    client = Client(wsdl)
    requestlist = []
    for digestpair in digestlist:
        request = client.factory.create('tokenRequest')
        request.hashValue = digestpair[1]
        request.name = digestpair[0]
        requestlist.append(request)
    #request._tokenClassName = "SHA-256-0"
    print 'before call' + str(time.time())
    response = client.service.requestTokensImmediate('SHA-256-0',requestlist)
    #response = portType.requestTokensImmediate(request)
    print 'after call ' + str(time.time())
    for item in response:
        lines = [item.name,'']
        for proofelement in item.proofElements:
            proofelement.hashes.insert(proofelement.index,'X')
            lines.append( ":".join(proofelement.hashes))
        lines.append('')
        lines.append('')
        result = '\n'.join(lines)
        outfile.write(item.digestService + ' ' + urlparts.hostname + ' ' + item.tokenClassName + ' ' + str(item.roundId) + ' ' +DateTime(item.timestamp).__unicode__()+ ' ' + str(len(result)) + "\n")
        outfile.write(result)
    print 'return ' + str(time.time())


def getAlgorithm(algName):
    if (algName == "SHA-256"):
       return hashlib.sha256()
    elif (algName == "SHA-512"):
       return hashlib.sha512()
    elif (algName == "SHA-384"):
       return hashlib.sha384()
    elif (algName == "MD5"):
       return hashlib.md5()
    elif (algName == "SHA1"):
       return hashlib.sha1()
    return None

def calculateCSI(file, proof, algName):
    """Calculate the CSI for a file"""
    prevhash = digestFile(file, algName)
    for proofLine in proof:
        prevhash = calculateLevel(prevhash,proofLine,algName)
    return binascii.b2a_hex(prevhash)

def calculateLevel(lowerHash,rowString, algName):
    """Calculate a level given a token store string, and the hash and index
    of the previously calculated level's (or file) hash"""
    hashAlg = getAlgorithm(algName)
    for hash in string.split(rowString,":"):
        if (hash == "X"):
            hashAlg.update(lowerHash)
        else:
            hashAlg.update(binascii.a2b_hex(hash))
    return hashAlg.digest()

def readHeader(file):
    currLine = file.readline()
    if not currLine:
        return False
    headerParts = string.split(currLine)
    if (len(headerParts) != 6):
        print "Bad header: " + currLine
        return False
    return headerParts

def readIdentifiers(infile):
    line = infile.readline().rstrip("\n")
    ids = []
    while line != "":
        ids.append(line)        
        line = infile.readline().rstrip("\n")
    return ids

def readProof(infile):
    line = infile.readline().rstrip("\n")
    proof = []
    while line != "":
        proof.append(line)        
        line = infile.readline().rstrip("\n")
    return proof

def digestFile(file,alg="SHA-256"):
    hashAlg = getAlgorithm(alg)
    with open(file,'rb') as digFile:
        bytes_read = digFile.read(1024*1024)
        while bytes_read:
            hashAlg.update(bytes_read)
            bytes_read = digFile.read(1024*1024)
    digFile.close()
    fileDigest = hashAlg.digest()
    return fileDigest 

