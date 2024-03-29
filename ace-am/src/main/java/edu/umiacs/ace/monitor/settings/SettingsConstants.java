package edu.umiacs.ace.monitor.settings;

/**
 * @author shake
 */
public class SettingsConstants {

    // Attributes
    public static final String PARAM_AUDIT_BLOCKING = "audit.blocking";
    public static final String PARAM_AUDIT_ONLY = "audit.only";
    public static final String PARAM_AUDIT_SAMPLE = "audit.sample";
    public static final String PARAM_IMS = "ims";
    public static final String PARAM_IMS_PORT = "ims.port";
    public static final String PARAM_IMS_TOKEN_CLASS = "ims.tokenclass";
    public static final String PARAM_IMS_MAX_RETRY = "ims.max.retry";
    public static final String PARAM_IMS_RESET_TIMEOUT = "ims.reset.timeout";
    public static final String PARAM_IMS_SSL = "ims.ssl";
    public static final String PARAM_AUTO_AUDIT_ENABLE = "auto.audit.enable";
    public static final String PARAM_THROTTLE_MAXAUDIT = "throttle.maxaudit";
    public static final String PARAM_TIME = "throttle.wait";
    public static final String PARAM_BPS = "throttle.bps";
    public static final String PARAM_SMTP_SERVER = "mail.server";
    public static final String PARAM_FROM = "mail.from";
    public static final String PARAM_USER_AUTH = "auth.management";
    public static final String PARAM_4J_FILE = "log4j.appender.A1.File";
    public static final String PARAM_4J_APPENDER = "log4j.appender.A1";
    public static final String PARAM_4J_FILE_SIZE = "log4j.appender.A1.maxFileSize";
    public static final String PARAM_4J_BACKUP_INDEX = "log4j.appender.A1.maxBackupIndex";
    public static final String PARAM_4J_ROOT_LOGGER = "log4j.rootLogger";
    public static final String PARAM_4J_LAYOUT = "log4j.appender.A1.layout";
    public static final String PARAM_4J_CONV_PAT = "log4j.appender.A1.layout.ConversionPattern";
    public static final String PARAM_4J_IRODS = "log4j.logger.edu.umiacs.irods";
    public static final String PARAM_4J_CLASS = "log4j.logger.edu.umiacs";

    // Default Values
    public static final String auditBlocking = "false";
    public static final String auditOnly = "false";
    public static final String auditSample = "false";
    public static final String mailServer = "localhost.localdomain";
    public static final String mailFrom = "acemail@localhost";
    public static final String mailTo = "acemail@localhost";
    public static final String maxAudit = "3";
    public static final String autoAudit = "true";
    public static final String ims = "ims.tdl.org";
    public static final String imsPort = "80";
    public static final String imsTokenClass = "SHA-256";
    public static final String imsMaxRetry = "3";
    public static final String imsResetTimeout = "3000";
    public static final String imsSSL = "false";
    public static final String authManagement = "true";
    public static final String throttleWait = "0";
    public static final String throttleBPS = "0";
    // Yay log4j
    public static final String log4JA1File = "/tmp/aceam.log";
    public static final String log4JA1 = "org.apache.log4j.RollingFileAppender";
    public static final String log4JA1MaxFileSize = "100000KB";
    public static final String log4JA1MaxBackupIndex = "5";
    public static final String log4JRootLogger = "FATAL, A1";
    public static final String log4JA1Layout = "org.apache.log4j.PatternLayout";
    public static final String log4JA1layoutConversationPattern =
            "%d{[dd/MMM/yyyy:HH:mm:ss]} %x%m%n";
    public static final String log4JLoggerIrods = "ERROR";
    public static final String log4JLoggerUMIACS = "TRACE";

    /**
     * @deprecated migrated to PARAM_IMS_MAX_RETRY and PARAM_IMS_RESET_TIMEOUT
     */
    @Deprecated
    public static final String PARAM_AUDIT_MAX_BLOCK_TIME = "audit.max.block.time";
    /**
     * @deprecated migrated to PARAM_IMS_MAX_RETRY and PARAM_IMS_RESET_TIMEOUT
     */
    @Deprecated
    public static final String auditMaxBlockTime = "0";

    /**
     * @deprecated no longer used
     */
    @Deprecated
    public static final String PARAM_INGEST = "ingest.maxthreads";

    /**
     * @deprecated no longer used
     */
    @Deprecated
    public static final String maxIngestThreads = "4";

}
