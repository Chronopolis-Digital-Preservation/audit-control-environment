<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<div class="menuheader">
    <table>
        <tr>  
            <td align="center">
                <div class="menucell">
                    <a href="Status">Status</a><BR> 
                </div>
            </td>
            <td align="center">
                <div class="menucell">
                    <a href="EventLog?clear=1">Event Log</a>
                </div>
            </td>

            <td align="center">
                <div class="menucell">
                    <c:if test="${authmanagement}">
                        <a href="Users">Accounts</a><BR>
                    </c:if>
                </div>
            </td>

        </tr>
    </table>
</div>
<div class="header">

    <img src="images/title.jpg" alt="ACE Audit Manager">
</div>
<!--
<div class="menubar">

    <table>
        <tr>  
            <td  align="center">
                <div class="menucell" id="status">
                    <a href="Status">Status</a><BR> 
                </div>
            </td>
            <td  align="center">
                <div class="menucell" id="log">
                    <a href="EventLog">Event Log</a>
                </div>
            </td>
            <td  align="center">
                <div class="menucell" id="users">
                    <a href="Users">Accounts</a><BR>
                </div>
            </td>
        </tr>
    </table>

</div>
-->
