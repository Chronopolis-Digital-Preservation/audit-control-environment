from pysimplesoap.client import SoapClient
from pysimplesoap.simplexml import SimpleXMLElement
import acestore

acestore.retrieveRoundSummaries([56478,67489])

client = SoapClient(wsdl="http://ims.umiacs.umd.edu:8080/ace-ims/IMSWebService?WSDL",trace=True,ns='tns')
print 'namespace ' + client.namespace
response = client.getRoundSummaries(rounds=[2855142,67564])
print response[0]['return']
#print result
