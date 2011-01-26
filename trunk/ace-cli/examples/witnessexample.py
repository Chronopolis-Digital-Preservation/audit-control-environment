import hashlib
import binascii
from suds.client import Client
#
# Witness value retrieved from:
# http://groups.google.com/group/ace-ims-witness
#
roundid = 2855147
trustedwitnessvalue='d85e36d6af2246d76c9a4fa0ef22eb10a5215eae5747504241b92b18f2c22467'

url='http://ims.umiacs.umd.edu:8080/ace-ims/IMSWebService?wsdl'
client = Client(url)

print '\n---Requesting Round Hash for',roundid
rounds = client.service.getRoundSummaries(roundid)
print 'Round hash:', rounds[0].hashValue

print '\n---Requesting proof to witness for round',roundid
witnessProof = client.service.createWitnessProofForRound(roundid)
print 'Witness ID:', witnessProof[0].witnessId, 'Timestamp:',witnessProof[0].roundTimestamp, witnessProof[0].tokenClassName, witnessProof[0].digestService
print witnessProof[0].proofElements

print '\n---Calculating round to witness proof',roundid
level = 0
prevhash = binascii.a2b_hex(rounds[0].hashValue)
for element in witnessProof[0].proofElements:
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

imswitnessvalue = binascii.b2a_hex(prevhash)
print '\n---Comparing trusted value to IMS proof result'
print 'calculated',imswitnessvalue
print 'trusted   ',trustedwitnessvalue
print 'Equal:',imswitnessvalue == trustedwitnessvalue
