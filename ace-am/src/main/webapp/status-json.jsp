<%@taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@page contentType="application/json" pageEncoding="UTF-8"%>
<json:object>
    <json:property name="startup_complete" value="${startup_complete}"/>
    <json:property name="paused" value="${pause.paused}"/>
    <json:array name="collections" items="${collections}" var="item">
        <json:object>
            <json:property name="id" value="${item.collection.id}"/>
            <json:property name="name" value="${item.collection.name}"/>
            <json:property name="group" value="${item.collection.group}"/>
            <json:property name="directory" value="${item.collection.directory}"/>
            <json:property name="lastSync" value="${item.collection.lastSync}"/>
            <json:property name="storage" value="${item.collection.storage}"/>
            <json:property name="checkPeriod" value="${item.collection.settings['audit.period']}"/>
            <json:property name="proxyData" value="${item.collection.settings['proxy.data']}"/>
            <json:property name="auditTokens" value="${item.collection.settings['audit.tokens']}"/>
            <json:property name="state" value="${item.collection.state}"/>
            <json:property name="totalFiles" value="${item.totalFiles}"/>
            <json:property name="totalSize" value="${item.totalSize}"/>
            <json:property name="fileAuditRunning" value="${item.fileAuditRunning}"/>
            <json:property name="tokenAuditRunning" value="${item.tokenAuditRunning}"/>
            <json:property name="totalErrors" value="${item.totalErrors < 0 ? 0 : item.totalErrors}"/>
            <json:property name="missingTokens" value="${item.missingTokens < 0 ? 0 : item.missingTokens}"/>
            <json:property name="missingFiles" value="${item.missingFiles < 0 ? 0 : item.missingFiles}"/>
            <json:property name="activeFiles"  value="${item.activeFiles < 0 ? 0 : item.activeFiles}"/>
            <json:property name="corruptFiles"  value="${item.corruptFiles < 0 ? 0 : item.corruptFiles}"/>
            <json:property name="invalidDigests"  value="${item.invalidDigests < 0 ? 0 : item.invalidDigests}"/>
            <json:property name="remoteMissing"  value="${item.remoteMissing < 0 ? 0 : item.remoteMissing}"/>
            <json:property name="remoteCorrupt" value="${item.remoteCorrupt < 0 ? 0 : item.remoteCorrupt}"/>
            <c:if test="${item.fileAuditRunning}">
                <json:object name="fileAudit">
                    <json:property name="totalErrors" value="${item.fileAuditThread.totalErrors}"/>
                    <json:property name="newFilesFound" value="${item.fileAuditThread.newFilesFound}"/>
                    <json:property name="filesSeen" value="${item.fileAuditThread.filesSeen}"/>
                    <json:property name="lastFileSeen" value="${item.fileAuditThread.lastFileSeen}"/>
                    <json:property name="tokensAdded" value="${item.fileAuditThread.tokensAdded}"/>
                </json:object>
            </c:if>
            <c:if test="${item.tokenAuditRunning}">
                <json:object name="tokenAudit">
                    <json:property name="totalErrors" value="${item.tokenAuditThread.totalErrors}"/>
                    <json:property name="tokensSeen" value="${item.tokenAuditThread.tokensSeen}"/>
                    <json:property name="validTokens" value="${item.tokenAuditThread.validTokens}"/>
                </json:object>
            </c:if>
        </json:object>
    </json:array>
</json:object>