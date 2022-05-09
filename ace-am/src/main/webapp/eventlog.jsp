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
        <title>${pageHeader}</title>

        <jsp:include page="imports.jsp"/>
 
        <script type="text/javascript">
            var scrollDivHeight = 420;

            var timeoutId;
            var count = '${count}';
            var logSize = '${loglist.size()}';
            var firstRecord = '${loglist[0].id}';  // reverted results
            var lastRecord = '${loglist[loglist.size() - 1].id}';
            
            var url = 'EventLog?sessionId=${sessionId}&collection=${collection}&logpath=${logpath}&toggletype=${logType}&replicasite=${siteId}&count=${count}';

            document.addEventListener("DOMContentLoaded", function(event) {
                var logElement = document.getElementById('log');
                logElement.addEventListener("wheel", function(e){
                    var st = logElement.scrollTop;

                    var scrollingParam;
                    var scrollingDistance = logElement.scrollHeight - scrollDivHeight;
                    if (st >= scrollingDistance && logSize == count) {
                        scrollingParam = 'top=' + lastRecord; // scrolling down
                        loadPage(url + '&' + scrollingParam);
                    } else if (st <= 0 && e.deltaY <= 0 && logSize == count) {
                        scrollingParam = 'start=' + firstRecord;  // scrolling up
                        loadPage(url + '&' + scrollingParam);
                    }
                }, false);
            });

            function loadPage(url) {
                if (timeoutId === null || timeoutId === undefined) {
                    timeoutId = setTimeout(() => { document.location.href = url; }, 500);
                }
            }

            function hideEventLog() {
                document.getElementById("eventLogOverlay").style.display = "none";
            }

            function toggleVisibility(id) {
                var t = document.getElementById("msg" + id);
                document.getElementById('eventLogOverlay').style.display = 'block';
                document.getElementById('eventLogDetails').innerHTML = t.innerHTML;
        } 
        
        function toggleSelected(logType) {
            window.location = "EventLog?start=${start}&count=${count}&toggletype=" + logType ; 
        }
        function toggleSelectedSite(siteId) {
            window.location = "EventLog?start=${start}&count=${count}&replicasite=" + siteId ; 
        }
        </script>

        <style type="text/css">
            #logtypeselection
            {
                width: 90%;
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
 
            .logHeader
            {
                font-weight: bold;
                color: #888;
                width: 100%;
                text-align: left;

            }
            .info {   
                margin-bottom:2px;
                margin-top:2px;
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
                width: 100%;
                cursor: pointer;
                text-align: left;
            }
            #log {
                margin-left: auto;
                margin-right: auto;
                width: 96%;
                border-left: 1px solid #000000;
                border-right: 1px solid #000000;
                border-bottom: 1px solid #000000;
                margin-bottom: 10px;
                margin-top:10px;
                height: 420px;
                overflow-y: scroll;
            }
            .col-md-padding {
                padding-left: 8px !important;
                padding-right: 8px !important;
            }
            .col-md-left-padding {
                padding-left: 4px !important;
                padding-right: 8px !important;
            }
            .col-md-right-padding {
                padding-left: 8px !important;
                padding-right: 4px !important;
            }

            /* event log overlay */
            #eventLogOverlay
            {
                display: none;
                position: fixed;
                width: 100%;
                height: 100%;
                top: 0;
                bottom: 0;
                left: 0;
                right: 0;
                background-color: rgba(0, 0, 0, 0.3);
                z-index: 1001;
            }
            .eventLogContainer
            {
                position: relative;
                width: 84%;
                top: 6%;
                left: 16%;
                transform: translateX(-8%) translateY(-3%);
                background-color: white;
            }
            .eventLogBanner
            {
                background-color: #efefef;
                height: 45px;
                text-align: left;
                padding-left: 30px;
                padding-top: 6px;
            }
            .eventLogHeader
            {
                color: #444;
                font-size: 20px;
                font-weight: bold;
            }
            #eventLogDetails
            {
                display: block;
                min-height: 360px;
                max-height: 560px;
                overflow: auto;
                padding-left: 30px;
            }

            /* scrollbar */
            ::-webkit-scrollbar
            {
                width: 16px;
            }
            ::-webkit-scrollbar-track {
                background: #f1f1f1;
                border-radius: 6px;
            }
            ::-webkit-scrollbar-thumb {
                background: #ccc;
                border-radius: 6px;
            }
            ::-webkit-scrollbar-thumb:hover
            {
                background: #999;
            }   
        </style>
    </head>
    <body>
        <div id="eventLogOverlay">
            <div class="eventLogContainer">
                <div class="eventLogBanner">
                    <span class="eventLogHeader">Event Log Details</span>
                    <a class="btn-close" onClick="javascript:hideEventLog();">Close</a>
                </div>
                <div id="eventLogDetails"></div>
            </div>
        </div> 
        <jsp:include page="header.jsp" />
        <h1 class="page_header">${pageHeader}</h1>
 
        <script type="text/javascript">if(document.getElementById('log')!=undefined){document.getElementById('log').style.backgroundColor = '#ccccff';}</script>
        
        <div align="center">
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
                <td align="center"><b>System Errors</b></td>
                <td align="center"><b>Monitored File Errors</b></td>
                <td align="center"><b>New Master Items</b></td> 
                <td align="center"><b>Sync Start/Stop Events</b></td>
                
            </tr>
            
            <tr>
                <td></td>
                <td align="center"><input onClick="javascript:toggleSelected('errors'); return false;" ${selects['errors']} type="checkbox" style="border:none"/></td>
                <td align="center"><input onClick="javascript:toggleSelected('missing'); return false;" ${selects['missing']} type="checkbox" style="border:none"/></td>
                <td align="center"><input onClick="javascript:toggleSelected('newmaster'); return false;" ${selects['newmaster']} type="checkbox" style="border:none"/></td>
                <td align="center"><input onClick="javascript:toggleSelected('sync'); return false;" ${selects['sync']} type="checkbox" style="border:none"/></td> 
            </tr>
            
          </table>
        
          <div id="log" class="container log">
            <div class="row logHeader">
                <div class="col-md-1 col-md-left-padding">ID</div>
                <div class="col-md-5 col-md-padding">
                    <div class="row">
                        <div class="col-md-8">Date</div>
                        <div class="col-md-4 col-md-left-padding">Session</div>
                    </div>
                </div>
                <div class="col-md-6 col-md-right-padding">
                    <div class="row">
                        <div class="col-md-7">Event Type</div>
                        <div class="col-md-5 col-md-right-padding">Category</div>
                    </div>
                </div>
            </div>
            <c:if test="${loglist != null}">
                <c:forEach var="item" items="${loglist}">
                    <div id="logEntries-${item.id}" class="row logItem"
                         onclick='javascript:toggleVisibility(${item.id})'
                         onmouseover='javascript:this.style.background="#e8e8e8"' 
                         onmouseout='javascript:this.style.background="#ffffff"'
                         >
                        <div class="col-md-1 col-md-left-padding">
                            <span class="info">${item.id}</span>
                        </div>
                        <div class="col-md-5 col-md-padding">
                            <div class="row">
                                <div class="col-md-8">
                                    <span class="info">${item.date}</span>
                                </div>
                                <div class="col-md-4 col-md-left-padding">
                                    <span class="info">${item.session}</span>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6 col-md-right-padding">
                            <div class="row">
                                <div class="col-md-7" style="overflow-wrap: break-word;">
                                    <span class="info"><log:LogType type="${item.logType}" /></span>
                                </div>
                                <div class="col-md-5 col-md-right-padding">
                                    <span class="info"><log:LogCategory type="${item.logType}" /></span>
                                </div>
                            </div>
                        </div>                       
                    </div>
                      
                    <div class="msg" id="msg${item.id}">
                        <div style="margin:20px;">
                            <c:if test="${item.collection != null}">
                                Collection: <a href="EventLog?collection=${item.collection.id}">${item.collection.name}</a><br/>
                            </c:if>
                            <c:if test="${item.path != null}">
                                Path: <a href="EventLog?logpath=${item.path}">${item.path}</a><br/>
                            </c:if>
                            Session: <a href="EventLog?start=1&sessionId=${item.session}">${item.session}</a><br/>
                            Event Type: <log:LogType type="${item.logType}" verbose="true" /><br/>
                            Details:<br/>
                                <pre>${item.description}</pre>
                        </div>
                    </div>
                </c:forEach>
            </c:if>
          </div>
        </div>
       
        <jsp:include page="footer.jsp" />     
    </body>
</html>
