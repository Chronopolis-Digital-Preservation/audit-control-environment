
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
        <title>Manage Partner Sites</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
        <style type="text/css">
            #partnerfieldset {
                margin-top: 15px;
            }
        </style>
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <div class="standardBody">
            <h3 class="standardHeader" id="partnerSiteHeader">Registered Partner Sites</h3>
            Click link to edit site details
            <ul>
                <c:forEach var="item" items="${partnerList.sites}">
                    <li>
                        <a href="PartnerSite?partnerid=${item.id}">Edit</a> <a href="PartnerSite?partnerid=${item.id}&remove=1">Remove</a> ${item.remoteURL}
                    </li>
                </c:forEach>
            </ul>
            <a href="compare_form.jsp">Back to Collection Comparison</a>
            <fieldset id="partnerfieldset">
                <legend>
                    <c:choose>
                        <c:when test="${partner != null}">Edit - ${partner.remoteURL}</c:when>
                        <c:otherwise>Add New Partner</c:otherwise>
                    </c:choose>
                </legend>
                <form method="post" action="PartnerSite">
                    <input type=hidden name="partnerid" value="${partner.id}"/>
                    <table>
                        <tr><td colspan=2><span style="color: red">${error.msg}</span></td></tr>
                        <tr><td>Site URL:</td><td> <input type="text" size="50" name="url" value="${partner.remoteURL}"/></td></tr>
                        <tr><td>Username:</td><td> <input type="text" size="10" name="user" value="${partner.user}"/></td></tr>
                        <tr><td>Password</td><td> <input type="text" size="10" name="pass" value="${partner.pass}"/></td></tr>
                        <tr><td><input type="submit" name="commit" value="Save" class="submitLink" /></td><td><a href="PartnerSite">New</a></td></tr>
                    </table>
                    
                </form>
            </fieldset>
        </div>
        
        <jsp:include page="footer.jsp" />
    </body>
</html>
