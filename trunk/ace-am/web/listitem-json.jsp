<%@taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@page contentType="text/plain" pageEncoding="UTF-8"%>
<json:object>
    <json:object name="parent">
        <json:property name="id" value="${parent.id}"/>
        <json:property name="state" value="${parent.state}"/>
        <json:property name="directory" value="${parent.directory}"/>
        <json:property name="path" value="${parent.path}"/>
        <json:property name="parentPath" value="${parent.parentPath}"/>
        <json:property name="lastSeen" value="${parent.lastSeen}"/>
        <json:property name="stateChange" value="${parent.stateChange}"/>
        <json:property name="lastVisited" value="${parent.lastVisited}"/>
        <json:property name="size" value="${parent.size}"/>
    </json:object>
    <json:array name="children" items="${children}" var="item">
            <json:object>
                <json:property name="id" value="${item.id}"/>
                <json:property name="state" value="${item.state}"/>
                <json:property name="directory" value="${item.directory}"/>
                <json:property name="path" value="${item.path}"/>
                <json:property name="parentPath" value="${item.parentPath}"/>
                <json:property name="lastSeen" value="${item.lastSeen}"/>
                <json:property name="stateChange" value="${item.stateChange}"/>
                <json:property name="lastVisited" value="${item.lastVisited}"/>
                <json:property name="size" value="${parent.size}"/>
            </json:object>
    </json:array>
</json:object>