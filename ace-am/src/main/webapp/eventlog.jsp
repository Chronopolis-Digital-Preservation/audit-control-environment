<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
<%@taglib uri="/WEB-INF/tlds/monitor" prefix="log"%> 
<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>
    
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <title>Activity Log</title>
        <script type="text/javascript">
            
            function toggleVisibility(id) {
                var t = document.getElementById("msg" + id);
                if (t.style.display == "block") {
                    t.style.display = "none";
                } else {
                t.style.display = "block";
            } 
        }
        
        function toggleSelected(logType) {
            window.location = "EventLog?start=${start}&count=${count}&toggletype=" + logType ; 
        }
        function toggleSelectedSite(siteId) {
            window.location = "EventLog?start=${start}&count=${count}&replicasite=" + siteId ; 
        }
        </script>
        
        <link rel="stylesheet" type="text/css" href="style.css" />
        <style type="text/css">
            #logtypeselection
            {
                width: 75%;
                margin-left: auto;
                margin-top: 0px;
                margin-right: auto;
            }
            
            #logtypeselection tr td
            {
                margin-left: 10px;
            }
            
            #browselinktable
            {
                width: 720px;
                margin-left: auto;
                margin-right: auto;
            }
            
            .info {   
                margin-bottom:2px;
                margin-top:2px;
                cursor: pointer;
            }
            .id {
                position:absolute;
                margin-left: 15px;
            }
            .date {
                position:absolute;
                margin-left:100px;
            }
            .type {
                position:absolute;
                margin-left:400px
            }
            .session {
                position:absolute;
                margin-left: 300px;
            }
            .msg {
                width: 100%;
                margin-top:2px;
                padding-bottom:2px;
                padding-top:2px;
                border-top:1px dotted #000000;
                display:none; 
                background: #e8e8e8;
            }
            .logItem {
                border-top: 1px solid #000000;
            }
            .log {
                margin-left: auto;
                margin-right: auto;
                width: 720px;
                border-left: 1px solid #000000;
                border-right: 1px solid #000000;
                border-bottom: 1px solid #000000;
                margin-bottom: 10px;
                margin-top:10px;
            }
            
        </style>
    </head>
    <body>
        <jsp:include page="header.jsp" />
        <script type="text/javascript">document.getElementById('log').style.backgroundColor = '#ccccff';</script>
        
        
        <table id="logtypeselection">
            
            <c:if test="${sessionId > 0}">
                <tr><td colspan="7">
                        <a href="EventLog?sessionId=0&start=1&count=${count}">x</a>&nbsp;&nbsp;Session: ${sessionId}
                </td></tr>
            </c:if>
            
            <c:if test="${replica > 0}">
                <tr><td colspan="7">
                        <a href="EventLog?replica=0&start=1&count=${count}">x</a>&nbsp;&nbsp;Replica: ${replicabean.path}
                </td></tr>
            </c:if>
            
            <c:if test="${collection > 0}">
                <tr><td colspan="7">
                        <a href="EventLog?collection=0&start=1&count=${count}">x</a>&nbsp;&nbsp;Collection: ${collectionbean.name}
                </td></tr>
            </c:if>
            
            <c:if test="${logpath != null && logpath ne ''}">
                <tr><td colspan="7">
                        <a href="EventLog?&start=1&count=${count}&logpath=">x</a>&nbsp;&nbsp;Path: '${logpath}'
                </td></tr>
            </c:if>            
            
            <tr>
                <td>Show Only:</td>
                <td><b>System Errors</b></td>
                <td><b>Monitored File Errors</b></td>
                <td><b>New Master Items</b></td> 
                <td><b>Sync Start/Stop Events</b></td>
                
            </tr>
            
            <tr>
                <td></td>
                <td align="center"><input onClick="javascript:toggleSelected('errors'); return false;" ${selects['errors']} type="checkbox" style="border:none"/></td>
                <td align="center"><input onClick="javascript:toggleSelected('missing'); return false;" ${selects['missing']} type="checkbox" style="border:none"/></td>
                <td align="center"><input onClick="javascript:toggleSelected('newmaster'); return false;" ${selects['newmaster']} type="checkbox" style="border:none"/></td>
                <td align="center"><input onClick="javascript:toggleSelected('sync'); return false;" ${selects['sync']} type="checkbox" style="border:none"/></td> 
            </tr>
            
        </table>
        
        <c:if test="${loglist != null}">
            <div class="log">
                <c:forEach var="item" items="${loglist}">
                    <div id="logEntries" class="logItem"
                         onclick='javascript:toggleVisibility(${item.id})'
                         onmouseover='javascript:this.style.background="#e8e8e8"' 
                         onmouseout='javascript:this.style.background="#ffffff"'
                         >
                        <div class="info">
                            <div class="id">${item.id}</div>
                            <div class="date">${item.date}</div>
                            <div class="session">${item.session}</div>
                            <div class="type"><log:LogType type="${item.logType}" /></div>
                        </div>
                        <br>
                        <div class="msg" id="msg${item.id}">
                            <c:if test="${item.collection != null}">
                                Collection: <a href="EventLog?collection=${item.collection.id}">${item.collection.name}</a><BR>
                            </c:if>
                            <c:if test="${item.path != null}">
                                Path: <a href="EventLog?logpath=${item.path}">${item.path}</a><BR>
                            </c:if>
                            Session: <a href="EventLog?start=1&sessionId=${item.session}">${item.session}</a><BR>
                            Event Type: <log:LogType type="${item.logType}" verbose="true" /><BR>
                            Details:<BR>
                                <pre>${item.description}</pre>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:if>
        
        <table id="browselinktable" >
            <tr>
                <td align="left">
                    <a href="EventLog?start=1&count=${count}">|&lt;</a>&nbsp;&nbsp;&nbsp;<a href="EventLog?top=${loglist[0].id}&count=${count}&start=0">&lt;&lt;</a>
                </td>
                <td align="center">
                    Show per page: <a href="EventLog?start=${loglist[0].id}&count=20">20</a> <a href="EventLog?start=${loglist[0].id}&count=50">50</a> <a href="EventLog?start=${loglist[0].id}&count=100">100</a>
                </td>
                <td align="right">
                    <a href="EventLog?start=${loglist[count - 1].id + 1}&count=${count}">&gt;&gt;</a>&nbsp;&nbsp;&nbsp;<a href="EventLog?count=${count}&start=0&top=0">&gt;|</a>
                </td>
            </tr>
        </table>
        
        <jsp:include page="footer.jsp" />        
    </body>
</html>
