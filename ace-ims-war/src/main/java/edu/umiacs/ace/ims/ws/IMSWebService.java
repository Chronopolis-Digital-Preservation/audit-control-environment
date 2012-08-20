/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.ims.ws;

import edu.umiacs.ace.exception.StatusCode;
import edu.umiacs.ace.ims.*;
import edu.umiacs.ace.ims.processor.LinkResponse;
import edu.umiacs.ace.ims.processor.RoundProcessors;
import edu.umiacs.ace.ims.processor.TokenReceipt;
import edu.umiacs.ace.ims.processor.TokenRequest;
import edu.umiacs.ace.ims.processor.TokenResponse;
import edu.umiacs.ace.ims.processor.TokenResponseWrapper;
import edu.umiacs.ace.ims.processor.WitnessValidation;
import edu.umiacs.ace.ims.round.RoundLocal;
import edu.umiacs.ace.ims.round.RoundSummary;
import edu.umiacs.ace.ims.store.IssuedToken;
import edu.umiacs.ace.ims.store.TokenStoreLocal;
import edu.umiacs.ace.ims.tokenclass.TokenClass;
import edu.umiacs.ace.ims.tokenclass.TokenClassLocal;
import edu.umiacs.ace.server.exception.InvalidParameterException;
import edu.umiacs.util.Strings;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

/**
 *
 * @author mmcgann
 */
@WebService(serviceName = "IMSWebService")
public class IMSWebService
{

    @EJB
    private TokenClassLocal tokenClassSession;
    @EJB
    private TokenStoreLocal tokenStoreSession;
//    @EJB
//    private WitnessLocal witnessSession;
    @EJB
    private RoundLocal roundSession;
    @Resource
    private WebServiceContext webServiceContext;
    private static final Logger print = Logger.getLogger(IMSWebService.class);
    private static RoundProcessors roundProcessors =
            RoundProcessors.getInstance();

    @PostConstruct
    void init()
    {
        Log.system("Web service started");
    }

//    @WebMethod
//    public TokenClass createTokenClass(
//            @WebParam(name = "tokenClass") TokenClass tokenClass)
//            throws IMSFault
//    {
//        enter();
//        try
//        {   
//            if ( tokenClass == null )
//            {
//                throw new InvalidParameterException("tokenClass");
//            }
//            
//            TokenClass entity = tokenClassSession.create(tokenClass);
//            RoundProcessors.getInstance().installProcessor(entity);
//            Log.system("Created token class: " + entity);
//            return entity;
//        }
//        catch ( Exception e )
//        {
//            throw handleException(e);
//        }
//        finally
//        {
//            exit();
//        }
//    }
    @WebMethod
    public TokenReceipt requestTokensAsync(
            @WebParam(name = "tokenClassName") String tokenClassName,
            @WebParam(name = "requests") List<TokenRequest> requests)
            throws IMSFault
    {
        ClientContext clientContext = enter();
        try
        {
            if ( Strings.isEmpty(tokenClassName) )
            {
                throw new InvalidParameterException("tokenClassName");
            }
            checkTokenRequests(requests);

            TokenClass tokenClass = tokenClassSession.findByName(tokenClassName);
            TokenReceipt receipt = roundProcessors.requestTokensAsync(
                    clientContext.getShortSessionId(), tokenClass, requests);
            Log.system("Token request async, " + requests.size() +
                    " request(s)");
            return receipt;
        }
        catch ( Exception e )
        {
            throw handleException(e);
        }
        finally
        {
            exit();
        }
    }

    @WebMethod
    public List<TokenResponse> requestTokensImmediate(
            @WebParam(name = "tokenClassName") String tokenClassName,
            @WebParam(name = "requests") List<TokenRequest> requests)
            throws IMSFault
    {
        ClientContext clientContext = enter();
        try
        {
            if ( Strings.isEmpty(tokenClassName) )
            {
                throw new InvalidParameterException("tokenClassName");
            }
            checkTokenRequests(requests);

            TokenClass tokenClass = tokenClassSession.findByName(tokenClassName);
            List<TokenResponse> responses =
                    roundProcessors.requestTokensImmediate(
                    clientContext.getShortSessionId(), tokenClass, requests);
            Log.system("Token request immediate, " + requests.size() +
                    " request(s), " + responses.size() + " response(s)");
            return responses;
        }
        catch ( Exception e )
        {
            throw handleException(e);
        }
        finally
        {
            exit();
        }
    }

    @WebMethod
    public List<RoundSummary> getRoundSummaries(
            @WebParam(name = "rounds") List<Long> rounds)
            throws IMSFault
    {
        ClientContext clientContext = enter();
        try
        {
            List<RoundSummary> summaries = new ArrayList<RoundSummary>();

            for ( long round : rounds )
            {
                Log.system("Querying for round: " + round);
                RoundSummary summary = roundSession.getRoundSummary(round);

                if ( summary == null )
                {
                    throw new InvalidParameterException(StatusCode.INVALID_CSI_ROUND,
                            "Invalid CSI Round", "No csi round for id: " + round);
                }
                Log.system("Adding round: " + summary.getTimestamp());
                summaries.add(summary);
            }

            Log.system("Requested round summaries: " + rounds.size() 
                    + " returning: " + summaries.size());

            return summaries;
        }
        catch ( Exception e )
        {
            throw handleException(e);
        }
        finally
        {
            exit();
        }
    }

    @WebMethod
    public List<RoundResponse> createWitnessProofForRound(
            @WebParam(name = "rounds") List<Long> rounds)
            throws IMSFault
    {
        ClientContext clientContext = enter();
        try
        {
            WitnessValidation validator = new WitnessValidation();
            List<RoundResponse> results = validator.createWitnessProofs(rounds);
            Log.system("Returning " + results.size() + " CSI proofs for " + rounds.size() + " requests");
            return results;
        }
        catch ( Exception e )
        {
            throw handleException(e);
        }
        finally
        {
            exit();
        }
    }
    
    @WebMethod
    public LinkResponse requestLink(
            @WebParam(name = "tokenClassName") String tokenClassName,
            @WebParam(name = "hashValue") String hashValue)
            throws IMSFault
    {
        ClientContext clientContext = enter();
        try
        {
            if ( Strings.isEmpty(tokenClassName) )
            {
                throw new InvalidParameterException("tokenClassName");
            }
            if ( Strings.isEmpty(hashValue) )
            {
                throw new InvalidParameterException("hashValue");
            }

            TokenClass tokenClass = tokenClassSession.findByName(tokenClassName);
            LinkResponse response = roundProcessors.requestLink(
                    clientContext.getShortSessionId(), tokenClass, hashValue);
            Log.system("Link request successful");
            return response;
        }
        catch ( Exception e )
        {
            throw handleException(e);
        }
        finally
        {
            exit();
        }
    }

    @WebMethod
    public List<TokenResponse> retrieveTokens(
            
            @WebParam(name = "requestNumber") long requestNumber,
            
            @WebParam(name = "sessionKey") long sessionKey)
            throws IMSFault
    {
        enter();
        try
        {
            List<IssuedToken> issuedTokens = tokenStoreSession.retreiveTokens(
                    requestNumber, sessionKey);
            List<TokenResponse> responses =
                    new ArrayList<TokenResponse>(issuedTokens.size());
            TokenResponseWrapper trWrapper = new TokenResponseWrapper();

            for ( IssuedToken issuedToken : issuedTokens )
            {
                responses.add(trWrapper.decode(issuedToken.getTokenResponse()));
            }
            Log.system("Retrieved " + responses.size() + " token(s)");
            return responses;
        }
        catch ( Exception e )
        {
            throw handleException(e);
        }
        finally
        {
            exit();
        }
    }

    private ClientContext enter()
    {
        ClientContext clientContext = new ClientContext(webServiceContext);
        NDC.push(clientContext.getRemoteAddress() + " " +
                clientContext.getShortSessionId() + ": ");
        if ( print.isDebugEnabled() )
        {
            StackTraceElement e = Thread.currentThread().getStackTrace()[2];
            if ( e != null )
            {
                print.debug("Enter: " + e.getMethodName());
            }
        }
        return clientContext;
    }

    private void exit()
    {
        if ( print.isDebugEnabled() )
        {
            StackTraceElement e = Thread.currentThread().getStackTrace()[2];
            if ( e != null )
            {
                print.debug("Exit: " + e.getMethodName());
            }
        }
        NDC.pop();
    }

    private IMSFault handleException(Exception e)
    {
        IMSFault fault = new IMSFault(e);
        if ( fault.isServerFault() )
        {
            Log.error(e.getMessage(), e);
        }
        else
        {
            Log.error(e.getMessage());
        }
        return fault;
    }

    /**
     * Check for null hashes or request list, throw InvalidParameterException if any found
     * @param requests
     */
    private void checkTokenRequests(List<TokenRequest> requests)
    {
        if ( requests == null || requests.size() < 1 )
        {
            throw new InvalidParameterException("requests");
        }

        int i = 0;
        for ( TokenRequest request : requests )
        {
            if ( Strings.isEmpty(request.getHashValue()) )
            {
                throw new InvalidParameterException("requests[" + i + "].hashValue");
            }
        }

    }
}
