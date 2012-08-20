/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umiacs.ace.ims.processor;

import edu.umiacs.ace.ims.store.TokenStore;

/**
 *
 * @author mmcgann
 */
public class WorkUnit 
{
    private String clientId;
    private TokenRequest tokenRequest;
    private TokenResponse tokenResponse;
    private TokenStore tokenStore = null;
    private ResponseDestination responseDestination;

    public String getClientId()
    {
        return clientId;
    }

    public void setClientId(String clientId)
    {
        this.clientId = clientId;
    }

    public TokenRequest getTokenRequest()
    {
        return tokenRequest;
    }

    public void setTokenRequest(TokenRequest tokenRequest)
    {
        this.tokenRequest = tokenRequest;
    }

    public TokenStore getTokenStore()
    {
        return tokenStore;
    }

    public void setTokenStore(TokenStore tokenStore)
    {
        this.tokenStore = tokenStore;
    }

    public ResponseDestination getResponseDestination()
    {
        return responseDestination;
    }

    public void setResponseDestination(ResponseDestination responseDestination)
    {
        this.responseDestination = responseDestination;
    }

    public TokenResponse getTokenResponse()
    {
        return tokenResponse;
    }

    public void setTokenResponse(TokenResponse tokenResponse)
    {
        this.tokenResponse = tokenResponse;
    }
}
