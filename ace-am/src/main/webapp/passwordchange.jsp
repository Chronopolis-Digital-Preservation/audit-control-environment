
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
        <title>List Item</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
        <style type="text/css">
            #status {
                color: #ff0000;
            }
        </style>
        <script type="text/javascript">
            function verifypasswords()
            {
                var pw1 = document.passwordform.newpassword.value;
                var pw2 = document.passwordform.newpassword1.value;
                if (pw1 != pw2)
                {
                    alert("New passwords do not match");
                    return false;
                }
                return true;

            }
        </script>
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <div class="standardBody">
            <h3>Change Password</h3>
            <c:if test="${status != null}">
                <br/>
                <span id='status'>${status}</span>
                <br/>
            </c:if>
            <c:if test="${!success}">
                <form id='passwordform' onsubmit="return verifypasswords()" action="ChangePassword" method="POST">
                    <table>
                        <tr>
                            <td>Username</td>
                            <td><input type="text" name="user"/></td>
                        </tr>
                        <tr>
                            <td>Old Password</td>
                            <td><input type="password" name="oldpassword"/></td>
                        </tr>
                        <tr>
                            <td>New Password</td>
                            <td><input type="password" name="newpassword"/></td>
                        </tr>
                        <tr>
                            <td>Verify Password</td>
                            <td><input type="password" name="newpassword1"/></td>
                        </tr>
                        <tr>
                            <td></td>
                            <td><input type="submit" value="Change Password"/></td>
                        </tr>
                    </table>
                </form>
            </c:if>
        </div>

        <jsp:include page="footer.jsp" />
    </body>
</html>
