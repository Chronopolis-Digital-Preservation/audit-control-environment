<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<div class="srb">

<table>
<tr><td>Server</td><td><input type=text name="server" value="${driver.settings.server}"></td></tr>
<tr><td>Port</td><td><input type=text name="port" value="${driver.settings.port}"></td></tr>
<tr><td>Username</td><td><input type=text name="username" value="${driver.settings.username}"></td></tr>
<tr><td>Domain</td><td><input type=text name="domain" value="${driver.settings.domain}"></td></tr>
<tr><td>Password</td><td><input type=Password name="password" value="${driver.settings.password}"></td></tr>
<tr><td>Zone</td><td><input type=text name="zone" value="${driver.settings.zone}"></td></tr>
</table>
        
</div>
