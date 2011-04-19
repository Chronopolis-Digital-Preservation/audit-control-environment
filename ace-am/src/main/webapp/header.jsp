<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@taglib uri="/WEB-INF/tlds/monitor" prefix="um"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<div class="menuheader">
    <table>
        <tr>  
            <td align="center">
                <div class="menucell">
                    <um:Auth role="Status" showUnauthenticated="true">
                        <a href="Status">Status</a>
                    </um:Auth>
                    <BR>
                </div>
            </td>

            <td align="center">
                <um:Auth role="Log">
                    <div class="menucell">
                        <a href="EventLog?clear=1">Event Log</a>
                        <BR>
                    </div>
                </um:Auth>
            </td>

            <td align="center">
                <div class="menucell">
                    <c:if test="${authmanagement}">
                        <um:Auth role="Users">
                            <a href="Users">Accounts</a>
                        </um:Auth>
                        <um:Auth role="!Users">
                            <a href="passwordchange.jsp">Change Password</a>
                        </um:Auth>
                    </c:if>
                    <BR>
                </div>
            </td>

        </tr>
    </table>
</div>
<div class="header">
    <img src="images/title.jpg" alt="ACE Audit Manager"><BR><div style="font-size: large; color: red">${globalMessage}</div>
</div>