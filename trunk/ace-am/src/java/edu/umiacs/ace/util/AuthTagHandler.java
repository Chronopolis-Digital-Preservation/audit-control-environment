package edu.umiacs.ace.util;

import javax.servlet.jsp.JspException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

/**
 *
 * @author toaster
 */
public class AuthTagHandler extends TagSupport {

    private String role;
    private boolean showUnauthenticated = false;

    @Override
    public int doStartTag() throws JspException {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();

        if ( req.getAuthType() == null && showUnauthenticated ) {
            return EVAL_BODY_INCLUDE;
        }

        if ( req.isUserInRole(role) ) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    public void setShowUnauthenticated( boolean showUnauthenticated ) {
        this.showUnauthenticated = showUnauthenticated;
    }

    public void setRole( String role ) {
        this.role = role;
    }
}
