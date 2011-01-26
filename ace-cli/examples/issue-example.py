import hashlib
import binascii
from suds.client import Client

filename='test2.py'

digFile = open(filename,'rb')
hashAlg = hashlib.sha256()
hashAlg.update(digFile.read())
binarydigest = hashAlg.digest()
filedigest = binascii.b2a_hex(binarydigest)

url='http://ims.umiacs.umd.edu:8080/ace-ims/IMSWebService?wsdl'
client = Client(url)

print '---File to secure:'
print filename, ' ', filedigest


print '\n---Token Response from IMS'
request = client.factory.create('tokenRequest')
request.hashValue = filedigest
request.name = filename
token = client.service.requestTokensImmediate('SHA-256-0',request)
print 'Round:',  token[0].roundId, ' Date:', token[0].timestamp
print token[0].proofElements


print '\n---Computing proof'

level = 0
prevhash = binarydigest
for element in token[0].proofElements:
    i = 0
    hashAlg = hashlib.sha256()
    # create level by converting hashes to bytes and inserting 
    # previous level where necessary, first level uses file hash
    for strhash in element.hashes:
        if i == element.index:
            hashAlg.update(prevhash)
        hashAlg.update(binascii.a2b_hex(strhash))
        i = i + 1

    # in case previous level is to be inserted at end
    if i == element.index:
        hashAlg.update(prevhash)
    prevhash = hashAlg.digest()
    print 'Level:',level, '( index:',element.index,') ', binascii.b2a_hex(prevhash)
    level = level + 1

print '\n---Requesting Round Hash for',token[0].roundId
rounds = client.service.getRoundSummaries(token[0].roundId)
print 'Round hash:', rounds[0].hashValue

print '\n---Comparing Round Hash to computed proof hash'
print rounds[0].hashValue
print binascii.b2a_hex(prevhash)
print 'Equal:',binascii.b2a_hex(prevhash) == rounds[0].hashValue
