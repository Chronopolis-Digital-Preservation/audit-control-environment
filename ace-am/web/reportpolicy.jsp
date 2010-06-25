<%-- 
Document   : resourcefinish
Created on : Nov 2, 2007, 2:00:03 PM
Author     : toaster
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Configure Automated Reporting</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
        <script language="javascript">
            function checkAll(field)
            {
                for (i = 0; i < field.length; i++)
                    field[i].checked = true ;
            }
            function uncheckAll(field)
            {
                for (i = 0; i < field.length; i++)
                    field[i].checked = false ;
            }
            function setTitle()
            {
                newTitle = document.getElementById("titleinput").value;
                document.getElementById("reportName").innerHTML = newTitle;
            }
        </script>
        <style type="text/css">
            .selectall {
                padding-right: 5px;
                color: #003388;
            }
            #editbox {
                padding: 10px;
            }
            #editbox form a {
                padding-left: 10px;
            }
        </style>
    </head>
    <body>
        <jsp:include page="header.jsp"/>
        <div class="standardBody">
            <h3>Add/Modify an automated report</h3>
            
            <ul>
                <c:forEach var="rep" items="${reportlist}">
                    <li><a href="ReportConfiguration?reportid=${rep.id}">${rep.name}</a>
                </c:forEach>
            </ul>
            
            <fieldset id="editbox">
                <c:choose>
                    <c:when test="${report == null}">
                        <legend>Create new report</legend>
                    </c:when>
                    <c:otherwise>
                        <legend>Modify existing report: <span id="reportName">${report.name}</span></legend>
                    </c:otherwise>
                    
                </c:choose>
                <form name="modifyform" method="post">
                    <input type="hidden" name="collectionid" value="${collection.id}">
                    Report Name: <input id="titleinput" onkeypress="setTitle();" type="text" name="name" value="${report.name}" >
                    <br><BR>Select which months run report<br><br>
                    <table>
                        <tr><td colspan=2>Select <span class="selectall" onClick="checkAll(document.modifyform.month)">All</span> <span class="selectall" onclick="uncheckAll(document.modifyform.month);">None</span></tr>
                        <tr>
                            <td><input type="checkbox" name="month" ${JAN} value="JAN">January </td>
                            <td><input type="checkbox" name="month" ${FEB} value="FEB">February </td>
                            <td><input type="checkbox" name="month" ${MAR} value="MAR">March </td>
                            <td><input type="checkbox" name="month" ${APR} value="APR">April</td>
                        </tr><tr>
                            <td><input type="checkbox" name="month" ${MAY} value="MAY">May</td>
                            <td><input type="checkbox" name="month" ${JUN} value="JUN">June</td>
                            <td><input type="checkbox" name="month" ${JUL} value="JUL">July</td>
                            <td><input type="checkbox" name="month" ${AUG} value="AUG">August</td>
                        </tr><tr>
                            <td><input type="checkbox" name="month" ${SEP} value="SEP">September</td>
                            <td><input type="checkbox" name="month" ${OCT} value="OCT">October</td>
                            <td><input type="checkbox" name="month" ${NOV} value="NOV">November</td>
                            <td><input type="checkbox" name="month" ${DEC} value="DEC">December</td>
                        </tr>
                    </table>
                    <br>
                    <c:set var="currday" value="${day}"/>
                    <c:if test="${day == null}">
                        <c:set var="currday" value="1"/>
                    </c:if>
                    
                    Day of Month to run: <input type="text" name="day" size=2 value="${currday}"> - Enter L for last day of month<BR>
                    <br>E-mail addresses to notify upon completion: <input type="text" name="email" size=50 value="${report.emailList}"> - separate addreses with a space
                    <br><br>
                    <input type="submit" name="submit" value="Save" class="submitLink" /> <a href="ReportConfiguration?collectionid=${collection.id}">Clear</a> <c:if test="${report != null}"><a href="ReportConfiguration?removeid=${report.id}">Remove</a></c:if>
                </form>
            </fieldset>
        </div>
        <jsp:include page="footer.jsp"/>
    </body>
</html>
