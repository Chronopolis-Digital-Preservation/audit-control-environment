<%-- 
    Document   : settings
    Created on : Jul 30, 2012, 4:07:16 PM
    Author     : shake
    TODO: Enable sampling when we have it properly implemented
    TODO: The Settings should come from a bean or smth so we can loop similar to the custom settings
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <title>System Settings</title>
        <link rel="stylesheet" type="text/css" href="style.css" />
    </head>
    <body>
        <jsp:include page="header.jsp"/>
        <FORM name="settingsform" METHOD="POST" ENCTYPE="multipart/form-data" ACTION="UpdateSettings">
            <fieldset id="settingsTable">
                <legend>
                    <h2>System Settings</h2>
                </legend>
                <div class="settingsRow">
                    <div class="settingsName">Mail Server:</div>
                    <div class="settingsVal"><input type=text name="mail.server" value="${currSettings['mail.server']}"/></div>
                    <div class="settingsHelp"><img src="images/help.png" title="Mail server to use when mailing reports. You will need to set this if you want reports to be mailed properly."></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">Mail From:</div>
                    <div class="settingsVal"><input type=text name="mail.from" value="${currSettings['mail.from']}"/></div>
                    <div class="settingsHelp"><img src="images/help.png" title="Set this e-mail address to an address e-mail should originate from"></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">Max Audit:</div>
                    <div class="settingsVal"><input type=text name="throttle.maxaudit" value="${currSettings['throttle.maxaudit']}"/></div>
                    <div class="settingsHelp"><img src="images/help.png" title="Max number of running audits"></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">Audit IMS Blocking:</div>
                    <div class="settingsVal"><input type=text name="audit.blocking" value="${currSettings['audit.blocking']}"/></div>
                    <div class="settingsHelp"><img src="images/help.png" title="Block audits when connectivity to the IMS is lost"></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">Audit Max Block Time:</div>
                    <div class="settingsVal"><input type=text name="audit.max.block.time" value="${currSettings['audit.max.block.time']}"/></div>
                    <div class="settingsHelp"><img src="images/help.png" title="Maximum time (in minutes) to block an audit when connectivity to the IMS is lost"></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">Audit Wait Time:</div>
                    <div class="settingsVal"><input type=text name="throttle.wait" value="${currSettings['throttle.wait']}"/></div>
                    <div class="settingsHelp"><img src="images/help.png" title="Minimum time between srb file reads in milliseconds"></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">Audit BPS:</div>
                    <div class="settingsVal"><input type=text name="throttle.bps" value="${currSettings['throttle.bps']}"/></div>
                    <div class="settingsHelp"><img src="images/help.png" title="Maximum bytes per second per running audit, default = 0 = unlimited"></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">Enable Auto Audit:</div>
                    <div class="settingsVal"><input type=text name="auto.audit.enable" value="${currSettings['auto.audit.enable']}"/></div>
                    <div class="settingsHelp"><img src="images/help.png" title="Enable automated auditing"></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">Audit Only Mode:</div>
                    <div class="settingsVal"><input type=text name="audit.only" value="${currSettings['audit.only']}"/></div>
                    <div class="settingsHelp"><img src="images/help.png" title="If true, do not attempt to register new items or contact the IMS during auditing"></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">IMS Host:</div>
                    <div class="settingsVal"><input type=text name="ims" value="${currSettings['ims']}"/></div>
                    <div class="settingsHelp"><img src="images/help.png" title="IMS hostname to use. Unless you deployed your own IMS, this should probably not be changed"></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">IMS Port:</div>
                    <div class="settingsVal"><input type=text name="ims.port" value="${currSettings['ims.port']}"/></div>
                    <div class="settingsHelp"><img src="images/help.png" title="Port the IMS Host runs on."></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">IMS SSL:</div>
                    <div class="settingsVal"><input type=text name="ims.ssl" value="${currSettings['ims.ssl']}"/></div>
                    <div class="settingsHelp"><img src="images/help.png" title="Use SSL when connecting to the IMS."></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">IMS Token Class:</div>
                    <div class="settingsVal"><input type=text name="ims.tokenclass" value="${currSettings['ims.tokenclass']}"/></div>
                    <div class="settingsHelp"><img src="images/help.png" title="Token class for the IMS host to use."></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">User Management:</div>
                    <div class="settingsVal"><input type=text name="auth.management" value="${currSettings['auth.management']}"/></div>
                    <div class="settingsHelp"><img src="images/help.png" title="Set this to true to disable internal user management. This should only be used in conjunction with changes to the Authentication realm listed above."></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">Max Ingest Threads:</div>
                    <div class="settingsVal"><input type=text name="ingest.maxthreads" value="${currSettings['ingest.maxthreads']}"/></div>
                    <div class="settingsHelp"><img src="images/help.png" title="The maximum number of threads allowed for ingesting token stores."></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">Log Location:</div>
                    <div class="settingsVal"><input type=text name="log4j.appender.A1.File" value="${currSettings['log4j.appender.A1.File']}"/></div>
                    <div class="settingsHelp"><img src="images/help.png" title="The location of your logfile"></div>
                </div>
                <c:choose>
                    <c:when test="${fileAppender}">
                        <div class="settingsRow">
                            <div class="settingsName">Log Type:</div>
                            <div class="settingsVal"><input type=text name="log4j.appender.A1" value="${currSettings['log4j.appender.A1']}"/></div>
                            <div class="settingsHelp"><img src="images/help.png" title="Log everything to one file, will grow to infinity"></div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="settingsRow">
                            <div class="settingsName">Log Type:</div>
                            <div class="settingsVal"><input type=text name="log4j.appender.A1" value="${currSettings['log4j.appender.A1']}"/></div>
                            <div class="settingsHelp"><img src="images/help.png" title="Rolling log file"></div>
                        </div>
                        <div class="settingsRow">
                            <div class="settingsName">Log File Size:</div>
                            <div class="settingsVal"><input type=text name="log4j.appender.A1.maxFileSize" value="${currSettings['log4j.appender.A1.maxFileSize']}"/></div>
                            <div class="settingsHelp"><img src="images/help.png" title="File size of your log"></div>
                        </div>
                        <div class="settingsRow">
                            <div class="settingsName">Log Backup Index:</div>
                            <div class="settingsVal"><input type=text name="log4j.appender.A1.maxBackupIndex" value="${currSettings['log4j.appender.A1.maxBackupIndex']}"/></div>
                            <div class="settingsHelp"><img src="images/help.png" title="Maximum backup index"></div>
                        </div>
                    </c:otherwise>
                </c:choose>
                <br>
                <div class="settingsRow">
                    <div>Generic logging properties,
                        change only if you know what you are doing</div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">Root Logger:</div>
                    <div class="settingsVal"><input type=text name="log4j.rootLogger" value="${currSettings['log4j.rootLogger']}"/></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">Pattern Layout:</div>
                    <div class="settingsVal"><input type=text name="log4j.appender.A1.layout" value="${currSettings['log4j.appender.A1.layout']}"/></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">Conversion Pattern:</div>
                    <div class="settingsVal"><input type=text name="log4j.appender.A1.layout.ConversionPattern" value="${currSettings['log4j.appender.A1.layout.ConversionPattern']}"/></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">IRods:</div>
                    <div class="settingsVal"><input type=text name="log4j.logger.edu.umiacs.irods" value="${currSettings['log4j.logger.edu.umiacs.irods']}"/></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">UMIACS Connection:</div>
                    <div class="settingsVal"><input type=text name="log4j.logger.edu.umiacs" value="${currSettings['log4j.logger.edu.umiacs']}"/></div>
                </div>
                <div class="settingsRow">
                    <div class="settingsName">IRods Connection:</div>
                    <div class="settingsVal"><input type=text name="irods.connection" value="${currSettings['irods.connection']}"/></div>
                </div>
                <br>
                <div class="settingsRow">
                    <div>Custom Settings: </div>
                </div>
                <c:forEach var="item" items="${customSettings}">
                    <div class="settingsRow">
                        <div class="settingsName">${item.key}</div>
                        <div class="settingsVal"><input type="text" name="${item.key}" value="${item.value}"/></div>
                    </div>
                </c:forEach>
                <div class="settingsRow">
                    <div class="settingsName"><a href="addsetting.jsp">Add setting</a></div>
                    <div class="settingsName"><a href="DeleteSettings">Delete settings</a></div>
                </div>
            </fieldset>
            <br>
            <input type=submit value="Submit" name="update" class="submitLink" style="margin-left: 5%">
            <input type=submit value="Default" name="default" class="submitLink">
            <a href="Status" style="font-size: medium; text-decoration: underline;">Cancel</a>
        </FORM>

        <jsp:include page="footer.jsp"/>
    </body>
</html>
