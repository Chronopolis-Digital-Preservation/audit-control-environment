<%-- 
    Document   : ingeststatus
    Created on : Jun 11, 2012, 4:20:58 PM
    Author     : shake
--%>

<%@page pageEncoding="UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Ingest Store</title>
    <jsp:include page="imports.jsp"/>
</head>

<body>
<jsp:include page="header.jsp"/>

<%--@elvariable id="results" type="edu.umiacs.ace.monitor.register.IngestThreadPool"--%>
<%--@elvariable id="active" type="edu.umiacs.ace.monitor.register.IngestSupervisor"--%>

<div class="standardBody">
    <h2>Token Import Status</h2>
    <div class="row">
        <div class="col-3">
            <div class="nav nav-pills flex-column">
                <li class="nav-item">
                    Active Token Imports
                </li>
                <c:forEach items="${results.cache}" var="entry">
                    <c:set var="collection" value="${entry.key}"/>

                    <li class="nav-item">
                        <a class="${active != null && entry.key.id == active.collection.id
                              ? 'nav-link active' : 'nav-link'}"
                           href="TokenImportStatus?active=${entry.key.id}">
                                ${collection.group} - ${collection.name}
                        </a>
                    </li>
                </c:forEach>
            </div>
        </div>

        <!-- we want to pass the active thread here instead of looping all -->
        <div class="col-9">
            <div class="tab-content card" id="v-pills-tab-content">
                <div class="card-header">
                    <ul class="nav nav-pills card-header-pills">
                        <li class="nav-item">
                            <a class="nav-link active" id="pills-queued-tab" role="tab"
                               href="#pills-queued" data-toggle="pill"
                               aria-controls="pills-queued" aria-selected="true">Queued
                                (${supervisor.queuedSize})</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" id="pills-new-tab" role="tab"
                               href="#pills-new" data-toggle="pill"
                               aria-controls="pills-new" aria-selected="false">New
                                (${supervisor.newSize})</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" id="pills-updated-tab" role="tab"
                               href="#pills-updated" data-toggle="pill"
                               aria-controls="pills-updated" aria-selected="false">Updated
                                (${supervisor.updatedSize})</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" id="pills-match-tab" role="tab"
                               href="#pills-match" data-toggle="pill"
                               aira-controls="pills-match" aria-selected="false">
                                Matched (${supervisor.matchSize})</a>
                        </li>

                        <%-- Maybe in the future
                        <li class="nav-item ml-auto">
                            <a class="nav-link bg-danger text-white" href="#">Cancel</a>
                        </li>
                        --%>
                    </ul>
                </div>

                <div class="card-body">
                    <div class="tab-content" id="pills-content">
                        <div class="tab-pane fade show active" id="pills-queued"
                             role="tabpanel"
                             aria-labelledby="pills-queued-tab">
                            <ul class="list-group list-group-flush">
                                <c:forEach items="${supervisor.queued}" var="identifier">
                                    <li class="list-group-item">${identifier}</li>
                                </c:forEach>
                            </ul>
                        </div>
                        <div class="tab-pane fade" id="pills-new" role="tabpanel"
                             aria-labelledby="pills-new-tab">
                            <ul class="list-group list-group-flush">
                                <c:forEach items="${supervisor.newItems}" var="identifier">
                                    <li class="list-group-item">${identifier}</li>
                                </c:forEach>
                            </ul>
                        </div>
                        <div class="tab-pane fade" id="pills-updated" role="tabpanel"
                             aria-labelledby="pills-updated-tab">
                            <ul class="list-group list-group-flush">
                                <c:forEach items="${supervisor.updated}" var="identifier">
                                    <li class="list-group-item">${identifier}</li>
                                </c:forEach>
                            </ul>
                        </div>
                        <div class="tab-pane fade" id="pills-match" role="tabpanel"
                             aria-labelledby="pills-match-tab">
                            <ul class="list-group list-group-flush">
                                <c:forEach items="${supervisor.matched}" var="identifier">
                                    <li class="list-group-item">${identifier}</li>
                                </c:forEach>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<jsp:include page="footer.jsp"/>
</body>
</html>
