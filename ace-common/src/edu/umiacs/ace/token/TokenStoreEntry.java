package edu.umiacs.ace.token;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author toaster
 */
public class TokenStoreEntry {

    private AceToken token;
    private List<String> identifiers;

    TokenStoreEntry() {
    }

    void setToken( AceToken token ) {
        this.token = token;
    }

    void setIdentifiers( List<String> identifiers ) {
        this.identifiers = Collections.unmodifiableList(identifiers);
    }

    public AceToken getToken() {
        return token;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

    @Override
    public String toString() {
        return "Token: " + token + " " + identifiers;
    }


}
