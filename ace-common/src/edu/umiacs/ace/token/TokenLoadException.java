package edu.umiacs.ace.token;

/**
 *
 * @author toaster
 */
public class TokenLoadException extends RuntimeException
{
    public enum ErrorType {
        IOEXCEPTION, HEADERFORMATERROR, PROOFFORMATERROR
    }
    public TokenLoadException(Exception e) {
        super(e);
    }

    public TokenLoadException(ErrorType type, String line) {
        super(type + ": " + line);
    }


}
