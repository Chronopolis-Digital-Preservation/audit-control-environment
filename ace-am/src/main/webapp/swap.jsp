<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<div class="srb">

<table>
<tr><td>Servers</td><td><input type=text name="servers" value="${driver.settings.servers}"></td></tr>
<tr><td>Port</td><td><input type=text name="port" value="${driver.settings.port}"></td></tr>
<tr><td>Username</td><td><input type=text name="username" value="${driver.settings.username}"></td></tr>
<tr><td>Password</td><td><input type=Password name="password" value="${driver.settings.password}"></td></tr>
<tr><td>Optional Prefix</td><td><input type=text name="prefix" value="${driver.settings.prefix}"></td></tr>

</table>
        
</div>
