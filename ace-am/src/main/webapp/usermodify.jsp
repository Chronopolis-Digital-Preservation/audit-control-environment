<%-- 
Document   : modifyresource
Created on : Nov 2, 2007, 10:55:07 AM
Author     : toaster
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Create/Update Users</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
        <style type="text/css">
            #error {
                color: #ff0000;
            }

            #modifytitle {
                font-weight: bold;  
            }

            #usertablehead {
                padding: 10px;

            }
        </style>
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <script type="text/javascript">document.getElementById('users').style.backgroundColor = '#ccccff';</script>
        <div class="standardBody">

            <table id="usertable">
                <th><tr><td id="usertablehead" >Click username to edit permissions or change password</td></tr></th>
                <c:forEach var="u" items="${userList}">
                    <tr><td><a href="Users?id=${u.key.id}">${u.key.username}</a></td></tr>
                </c:forEach>
            </table>
            <BR><BR><BR>
            <form action="Users" method="post">
                <fieldset>

                    <c:choose>
                        <c:when test='${user.id > 0}' >
                            <legend id="modifytitle">Modify Settings for ${user.username}</legend>
                            <input type="hidden" name="id" value="${user.id}" />

                        </c:when>
                        <c:otherwise>
                            <legend id="modifytitle">Create New User</legend>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${error != null}">
                        <span id='error'>${error}</span>
                        <br>
                    </c:if>

                    <table border="0">

                        <tbody></tbody>
                        <tr>
                            <td>Username</td>
                            <td>
                                <c:choose>
                                    <c:when test='${user.id > 0}' >
                                        ${user.username}
                                    </c:when>
                                    <c:otherwise>
                                        <input type="text" size="10" name="username" />
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        <tr>
                            <td>Password</td>
                            <td><input type="password" size="10" value="${user.password}" name="password"></td>
                        </tr>
                        <tr>
                            <td>Roles</td>
                        </tr>
                        <tr>
                            <td>
                                <input type="checkbox" name="role" value="Status" ${roles['Status']} />Status
                            </td><td>
                                <input type="checkbox" name="role" value="Collection Modify" ${roles['Collection Modify']} />Modify Collections
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input type="checkbox" name="role" value="Browse" ${roles['Browse']} />Browse
                            </td>
                            <td>
                                <input type="checkbox" name="role" value="Log" ${roles['Log']} />View Log Entries
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input type="checkbox" name="role" value="Audit" ${roles['Audit']} />Start or Stop Audits
                            </td>
                            <td>
                                <input type="checkbox" name="role" value="Remove Item" ${roles['Remove Item']} />Remove monitored files/directories
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input type="checkbox" name="role" value="Users" ${roles['Users']} />Manage Users
                            </td>
                            <td>
                                <input type="checkbox" name="role" value="Report" ${roles['Report']} />View Collection Reports
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input type="checkbox" name="role" value="Download Token" ${roles['Download Token']} />Download Tokens
                            </td>
                            <td>                            
                                <input type="checkbox" name="role" value="Show Duplicates" ${roles['Show Duplicates']} />Show Duplicates
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input type="checkbox" name="role" value="Summary" ${roles['Summary']} />Download Collection Summaries

                            </td>
                            <td>
                                <input type="checkbox" name="role" value="Compare" ${roles['Compare']} />Compare collections, this is resource intensive for large collections

                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input type="checkbox" name="role" value="View Audit Summaries" ${roles['View Audit Summaries']} />View Activity Reports

                            </td>
                            <td>
                                <input type="checkbox" name="role" value="Modify Activity Reporting" ${roles['Modify Activity Reporting']} />Configure automated activity reporting

                            </td>
                        </tr>
                        <tr>
                            <td>                            
                                <input type="checkbox" name="role" value="Modify Partner Sites" ${roles['Modify Partner Sites']} />Modify Partner Sites
                            </td>
                            <td>
                                <input type="checkbox" name="role" value="Download Item" ${roles['Download Item']} />Retrieve items from collections
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <input type="checkbox" name="role" value="Modify System Settings" ${roles['Modify System Settings']} />Modify System Settings
                            </td>
                        </tr>

                    </table>

                    <input type="submit" name="commit" value="Save" class="submitLink" />
                    <a href="Users">Clear</a>
                </fieldset>
            </form>

        </div>

        <jsp:include page="footer.jsp" />
    </body>
</html>
