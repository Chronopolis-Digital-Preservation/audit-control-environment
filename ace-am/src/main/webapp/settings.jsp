<%--
    Document   : settings
    Created on : Jul 30, 2012, 4:07:16 PM
    Author     : shake
    TODO: The Settings should come from a bean or smth so we can loop similar to the custom settings
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<jsp:useBean id="currSettings" scope="request" type="java.util.Map"/>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>${pageHeader}</title>
    <link rel="stylesheet" type="text/css" href="style.css"/>
    <style type="text/css">
        body {
            width: 980px !important;
            margin-top: 8px !important;
            padding: 0px !important;
        }
    </style>  
</head>
<body>
<jsp:include page="header.jsp"/>
<h1 class="page_header">${pageHeader}</h1>

<FORM name="settingsform" METHOD="POST" ENCTYPE="multipart/form-data" ACTION="UpdateSettings">
  <div align="center">
    <fieldset id="settingsTable">
        <div class="tabs">
            <ul>
                <li class="is-active" aria-controls="general-settings"><a>General</a></li>
                <li aria-controls="audit-settings"><a>Audit</a></li>
                <li aria-controls="ims-settings"><a>IMS</a></li>
                <li aria-controls="logging-settings"><a>Logging</a></li>
                <li aria-controls="custom-settings"><a>Custom</a></li>
            </ul>
        </div>


        <div id="general-settings">
            <div class="form-field">
                <label class="form-label" for="auth.management">User Management</label>
                <span class="form-help">
                Set this to true to disable internal user management. This should only be used in
                conjunction with changes to the Authentication realm listed above.
            </span>
                <input class="form-input-settings" type="text" id="auth.management"
                       name="auth.management" value="${currSettings['auth.management']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="mail.server">
                    Mail Server
                </label>
                <span class="form-help">
                        Mail server to use when mailing reports.
                        You will need to set this if you want reports to be mailed properly.
            </span>
                <input class="form-input-settings" type="text" id="mail.server"
                       name="mail.server" value="${currSettings['mail.server']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="mail.from">Mail From</label>
                <span class="form-help">
                Set this e-mail address to an address e-mail should originate from
            </span>
                <input class="form-input-settings" type="text" id="mail.from" name="mail.from"
                       value="${currSettings['mail.from']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="mail.to">Mail To</label>
                <span class="form-help">
                Set this e-mail address to an admin e-mail that should be sent on audit error
            </span>
                <input class="form-input-settings" type="text" id="mail.to" name="mail.to"
                       value="${currSettings['mail.to']}"/>
            </div>
        </div>

        <div class="tab-block" id="audit-settings">
            <div class="form-field">
                <label class="form-label" for="throttle.maxaudit">Max Audit</label>
                <span class="form-help">Maximum number of running audits</span>
                <input class="form-input-settings" type="text" id="throttle.maxaudit"
                       name="throttle.maxaudit" value="${currSettings['throttle.maxaudit']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="throttle.wait">Audit Wait Time</label>
                <span class="form-help">Minimum time between file reads in milliseconds</span>
                <input class="form-input-settings" type="text" id="throttle.wait"
                       name="throttle.wait" value="${currSettings['throttle.wait']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="throttle.bps">Audit BPS</label>
                <span class="form-help">
                Maximum bytes per second per running audit: default = 0 = unlimited
            </span>
                <input class="form-input-settings" type="text" id="throttle.bps"
                       name="throttle.bps" value="${currSettings['throttle.bps']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="audit.blocking">IMS Audit Blocking</label>
                <span class="form-help">Block audits when connectivity to the IMS is lost</span>
                <input class="form-input-settings" type="text" id="audit.blocking"
                       name="audit.blocking" value="${currSettings['audit.blocking']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="ims.max.retry">IMS Audit Max Retry Attempts</label>
                <span class="form-help">Maximum amount of retries when IMS failure occur</span>
                <input class="form-input-settings" type="text" id="ims.max.retry"
                       name="ims.max.retry" value="${currSettings['ims.max.retry']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="ims.reset.timeout">IMS Audit Reset Timeout</label>
                <span class="form-help">
                Amount of time to wait in milliseconds before retrying after an IMS failure
             </span>
                <input class="form-input-settings" type="text" id="ims.reset.timeout"
                       name="ims.reset.timeout" value="${currSettings['ims.reset.timeout']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="auto.audit.enable">Enable Auto Audit</label>
                <span class="form-help">Enable automated auditing</span>
                <input class="form-input-settings" type="text" id="auto.audit.enable"
                       name="auto.audit.enable" value="${currSettings['auto.audit.enable']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="audit.only">Audit Only Mode</label>
                <span class="form-help">
                If true, do not attempt to register new items or contact the IMS during auditing.
                Default value of false is recommended.
            </span>
                <input class="form-input-settings" type="text" id="audit.only"
                       name="audit.only" value="${currSettings['audit.only']}"/>
            </div>
        </div>

        <div class="tab-block" id="ims-settings">
            <div class="form-field">
                <label class="form-label" for="ims">IMS Host</label>
                <span class="form-help">
                IMS hostname to use. Unless you deployed your own IMS,
                this should probably not be changed.
             </span>
                <input class="form-input-settings" type="text" id="ims"
                       name="ims" value="${currSettings['ims']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="ims.port">IMS Port</label>
                <span class="form-help">
                Port the IMS Host runs on.
                Typically 80 for http or 443 for https.
            </span>
                <input class="form-input-settings" type="text" id="ims.port"
                       name="ims.port" value="${currSettings['ims.port']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="ims.ssl">IMS SSL</label>
                <span class="form-help">Use SSL when connecting to the IMS</span>
                <input class="form-input-settings" type="text" id="ims.ssl"

                       name="ims.ssl" value="${currSettings['ims.ssl']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="ims.tokenclass">IMS Token Class</label>
                <span class="form-help">Token class for the IMS host to use</span>
                <input class="form-input-settings" type="text" id="ims.tokenclass"
                       name="ims.tokenclass" value="${currSettings['ims.tokenclass']}"/>
            </div>
        </div>


        <div class="tab-block" id="logging-settings">
            <div class="form-field">
                <label class="form-label" for="log.location">Log Location</label>
                <span class="form-help">The location of your logfile</span>
                <input class="form-input-settings" type="text" id="log.location"
                       name="log4j.appender.A1.File"
                       value="${currSettings['log4j.appender.A1.File']}"/>
            </div>
            <c:choose>
                <c:when test="${fileAppender}">
                    <div class="form-field">
                        <label class="form-label" for="log.single">Log Type</label>
                        <span class="form-help">
                        Log everything to one file, will grow to infinity
                    </span>
                        <input class="form-input-settings" type="text" id="log.single"
                               name="log4j.appender.A1"
                               value="${currSettings['log4j.appender.A1']}"/>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="form-field">
                        <label class="form-label" for="log.rolling">Log Type</label>
                        <span class="form-help">
                        Use a rolling log file
                    </span>
                        <input class="form-input-settings" type="text" name="log4j.appender.A1"
                               id="log.rolling" value="${currSettings['log4j.appender.A1']}"/>
                    </div>
                    <div class="form-field">
                        <label class="form-label" for="log.size">Log File Size</label>
                        <span class="form-help">
                        Maximum file size allowed for your log
                    </span>
                        <input class="form-input-settings" type="text" id="log.size"
                               name="log4j.appender.A1.maxFileSize"
                               value="${currSettings['log4j.appender.A1.maxFileSize']}"/>
                    </div>
                    <div class="form-field">
                        <label class="form-label" for="log.index">Log Backup Index</label>
                        <span class="form-help">
                        Maximum backup index allowed for your log
                    </span>
                        <input class="form-input-settings" type="text" id="log.index"
                               name="log4j.appender.A1.maxBackupIndex"
                               value="${currSettings['log4j.appender.A1.maxBackupIndex']}"/>
                    </div>
                </c:otherwise>
            </c:choose>
            <br>
            <div class="form-field">
                <div>Generic logging properties, change only if you know what you are doing</div>
            </div>
            <div class="form-field">
                <label class="form-label" for="log.root">Root Logger</label>
                <input class="form-input-settings" type="text" id="log.root"
                       name="log4j.rootLogger" value="${currSettings['log4j.rootLogger']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="log.layout">Pattern Layout</label>
                <input class="form-input-settings" type="text" id="log.layout"
                       name="log4j.appender.A1.layout"
                       value="${currSettings['log4j.appender.A1.layout']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="log.conversion">Conversion Pattern</label>
                <input class="form-input-settings" type="text" id="log.conversion"
                       name="log4j.appender.A1.layout.ConversionPattern"
                       value="${currSettings['log4j.appender.A1.layout.ConversionPattern']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="irods.log4">IRods</label>
                <input class="form-input-settings" type="text" id="irods.log4"
                       name="log4j.logger.edu.umiacs.irods"
                       value="${currSettings['log4j.logger.edu.umiacs.irods']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="umiacs.connection">UMIACS Connection</label>
                <input class="form-input-settings" type="text" name="log4j.logger.edu.umiacs"
                       id="umiacs.connection" value="${currSettings['log4j.logger.edu.umiacs']}"/>
            </div>
            <div class="form-field">
                <label class="form-label" for="irods.connection">IRods Connection</label>
                <input class="form-input-settings" type="text" id="irods.connection"
                       name="irods.connection" value="${currSettings['irods.connection']}"/>
            </div>
        </div>

        <div class="tab-block" id="custom-settings">
            <jsp:useBean id="customSettings" scope="request" type="java.util.Map"/>
            <c:forEach var="item" items="${customSettings}">
                <div class="form-field">
                    <label class="form-label" for="${item.key}">${item.key}</label>
                    <input class="form-input-settings" type="text" id="${item.key}"
                           name="${item.key}" value="${item.value}"/>
                </div>
            </c:forEach>
            <div class="form-field">
                <div class="form-label" style="text-align: left">
                    <a href="addsetting.jsp">Add setting</a>
                </div>
                <div class="form-label" style="text-align: left">
                    <a href="DeleteSettings">Delete settings</a>
                </div>
            </div>

        </div>

    </fieldset>
    <div class="form-group">
        <input type="submit" value="Submit" name="update" class="btn" style="margin-left: 5%">
        <input type="submit" value="Set Defaults" name="default" class="btn is-secondary"
               style="width: 125px;">
    </div>
  </div>
</FORM>

<jsp:include page="footer.jsp"/>
</body>
<script type="application/javascript">
    var container = document.querySelectorAll('div.tabs ul li');

    function toggle() {
        var active = document.getElementsByClassName('is-active')[0];
        if (active != null) {
            var activeControl = active.getAttribute('aria-controls');
            if (activeControl != null) {
                var block = document.getElementById(activeControl);
                block.classList.toggle('tab-block');
            }
            active.classList.toggle('is-active');
        }

        var currentControl = this.getAttribute('aria-controls');
        if (currentControl != null) {
            var thisBlock = document.getElementById(currentControl);
            thisBlock.classList.toggle('tab-block');
        }

        this.classList.toggle('is-active');
    }

    for (var i = 0; i < container.length; i++) {
        container[i].addEventListener('click', toggle);
    }

</script>
</html>
