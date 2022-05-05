
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <title>Login</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
        <style type="text/css">
            #message {
                color: #ff0000;
            }
            .standardBody {
                margin-left: 30%;
                margin-right: 30%;
                font-size: 16px;
            }
            .btn {
                cursor: pointer;
            }
        </style>
        <script type="text/javascript">
            function validate()
            {
            
                var username = document.loginform.j_username.value.trim();
                var pwd = document.loginform.j_password.value.trim();
                
                if (username.length === 0)
                {
                    document.loginform.j_username.placeholder = 'Enter user name';
                    document.loginform.j_username.focus();
                   
                    return false;
                }
                else if (pwd.length === 0)
                {
                    document.loginform.j_password.placeholder = 'Enter password';
                    document.loginform.j_password.focus();
                    return false;
                }
                document.loginform.submit();
            }
        </script>
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <div class="standardBody">
            <c:if test="${param.auth == 0}">
                <div id="message">Invalid username or password!</div>
            </c:if>
            <h3 style="color: #336699;">Please Login:</h3>
            <form action="j_security_check" method=post id='loginform' name="loginform">
                <table>
                    <tr>
                        <td>Username</td>
                        <td><input type="text" name="j_username" class="form-input" size="25"></td>
                    </tr>
                    <tr>
                        <td>Password</td>
                        <td><input type="password" name="j_password" class="form-input" size="25"></td>
                    </tr>
                    <tr>
                        <td colspan="2" style="padding: 16px 0px; text-align: center;">
                            <input type="reset" value="Reset" class="btn is-secondary">
                            <input type="button" value="Login" onClick="javascript:validate();" class="btn">
                        </td>
                    </tr>
                </table>
            </form>
        </div>

        <jsp:include page="footer.jsp" />
    </body>
</html>
