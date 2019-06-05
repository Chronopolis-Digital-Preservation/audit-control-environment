package edu.umiacs.ace.ims.api;

import edu.umiacs.ace.exception.StatusCode;
import edu.umiacs.ace.ims.ws.IMSFault;
import edu.umiacs.ace.ims.ws.IMSFault_Exception;

import javax.xml.ws.WebServiceException;
import java.util.Optional;

/**
 * Encapsulate the result of communicating with the IMS. If a response is successful, store it as
 * a field 'e', and in the result of failure store the exception.
 *
 * @author shake
 * @since 1.13.1
 */
public class IMSResult<E> {

    private final E e;
    private final Exception exception;

    public IMSResult(E e) {
        this.e = e;
        this.exception = null;
    }

    public IMSResult(Exception exception) {
        this.e = null;
        this.exception = exception;
    }

    public Optional<E> getResult() {
        return Optional.ofNullable(e);
    }

    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

    /**
     * Get the result of the IMS request, or throw an exception if no result exists.
     *
     * @return the response from the IMS, if successful
     * @throws IMSException on the event of failure
     */
    public E getOrThrow() {
        if (e != null) {
            return e;
        }

        throw createException();
    }

    private IMSException createException() {
        if (exception == null) {
            String message = "Invalid state; createException called with exception == null";
            throw new RuntimeException(message);
        }

        if (exception instanceof WebServiceException) {
            Throwable cause = exception.getCause();
            if (cause instanceof java.net.ConnectException ||
                    cause instanceof java.io.FileNotFoundException) {
                return new IMSConnectionException(cause.getMessage(), cause);
            }

        }

        if (exception instanceof IMSFault_Exception) {
            IMSFault fault = ((IMSFault_Exception) exception).getFaultInfo();
            return new IMSException(fault.getStatusCode(), fault.getMessage());
        }

        throw new IMSException(StatusCode.CLIENT_ERROR, "Client error: " +
                exception.getMessage(), exception);
    }

}
