<%-- 
    Document   : addsetting
    Created on : Aug 6, 2012, 2:41:40 PM
    Author     : shake
--%>


<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>System Settings</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
    </head>
    <body>
        <jsp:include page="header.jsp"/>
        <fieldset id="settingsTable">
            <legend>
                <h2>Delete Custom Settings</h2>
            </legend>
        <FORM METHOD="POST" ACTION="DeleteSettings">
                <c:forEach var="item" items="${customSettings}">
                    <div class="settingsRow">
                        <input type="checkbox" name="setting" value="${item.name}"/>
                        <div class="settingsName">${item.name}</div>
                    </div>
                </c:forEach>
            <input type=submit value="Delete" name="update" class="submitLink">
            <a href="UpdateSettings" style="font-size: medium; text-decoration: underline;">Cancel</a>
        </FORM>
        </fieldset>

        <jsp:include page="footer.jsp"/>
    </body>
</html>

