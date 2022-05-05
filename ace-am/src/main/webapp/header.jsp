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

<ul class="nav justify-content-center">
    <um:Auth role="Status" showUnauthenticated="true">
        <li class="nav-item">
        <a class="nav-link" href="Status">Status</a>
        </li>
    </um:Auth>

    <um:Auth role="Log" showUnauthenticated="true">
        <li class="nav-item">
        <a class="nav-link" href="EventLog?clear=1">Event Log</a>
        </li>
    </um:Auth>

    <c:if test="${authmanagement}">
        <um:Auth role="Users">
            <li class="nav-item">
            <a class="nav-link" href="Users">Accounts</a>
            </li>
        </um:Auth>
        <um:Auth role="!Users">
            <li class="nav-item">
            <a class="nav-link" href="passwordchange.jsp">Change Password</a>
            </li>
        </um:Auth>
    </c:if>
    <um:Auth role="Status">
        <li class="nav-item">
        <a class="nav-link" href="UpdateSettings">System Settings</a>

        </li>
    </um:Auth>

    <um:Auth role="Status">
        <li class="nav-item">
        <a class="nav-link" href="Statistics">Reporting</a>
            </li>
    </um:Auth>
    <um:Auth role="Status" showUnauthenticated="true">
        <li class="nav-item">
        <a class="nav-link" target="_blank" href="https://wiki.umiacs.umd.edu/adapt/index.php/Ace:Audit_Manager_User_Guide">Help</a>
        </li>
    </um:Auth>
    <um:Auth role="Status">
        <li class="nav-item">
        	<a class="nav-link" href="Logout">Logout</a>
        </li>
    </um:Auth>
</ul>
<div class="header">
    <img src="images/title.jpg" alt="ACE Audit Manager"><BR><div style="font-size: large; color: red">${globalMessage}</div>
</div>
<jsp:include page="breadcrumbs.jsp" />