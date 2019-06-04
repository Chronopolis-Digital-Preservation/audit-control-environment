package edu.umiacs.ace.rest;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Set the encoding for requests/responses to be UTF-8
 * <p>
 * From http://stackoverflow.com/questions/138948/how-to-get-utf-8-working-in-java-webapps
 *
 * @author shake
 */
public class CharsetFilter implements Filter {

    private String encoding;

    @Override
    public void init(FilterConfig config) {
        encoding = config.getInitParameter("requestEncoding");

        if (encoding == null) {
            encoding = "UTF-8";
        }
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain next)
            throws IOException, ServletException {
        // Respect the client-specified character encoding
        // (see HTTP specification section 3.4.1)
        if (null == request.getCharacterEncoding()) {
            request.setCharacterEncoding(encoding);
        }

        // Set the default response encoding
        response.setCharacterEncoding("UTF-8");
        next.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
