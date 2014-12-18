
package com.util.log;

import org.acra.log.ACRALog;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

/**
 * Generic Log class
 */
public class Logger implements ACRALog {

    // ----------------------------------------------------------------
    // Constants
    /**
     * Log level DISABLED: used to speed up applications using Logging features
     */
    public static final int DISABLED = -1;

    /**
     * Log level ERROR: used to Log error messages.
     */
    public static final int ERROR = 0;

    /**
     * Log level INFO: used to Log information messages.
     */
    public static final int INFO = 1;

    /**
     * Log level DEBUG: used to Log debug messages.
     */
    public static final int DEBUG = 2;

    /**
     * Log level TRACE: used to trace the program execution.
     */
    public static final int TRACE = 3;

    public static final int VERBOSE = 4;
    public static final int WARN = 5;

    private static final int PROFILING = -2;

    private static boolean release = false;

    // ----------------------------------------------------------------
    // Variables
    /**
     * The default appender is the console
     */
    private static Appender out;

    /**
     * The default Log level is INFO
     */
    private static int level = INFO;

    /**
     * Last time stamp used to dump profiling information
     */
    private static long initialTimeStamp = -1;

    /**
     * Default Log cache size
     */
    private static final int CACHE_SIZE = 1024;

    /**
     * This is the Log cache size (by default this is CACHE_SIZE)
     */
    private static int cacheSize = CACHE_SIZE;

    /**
     * Log cache
     */
    private static Vector cache;

    /**
     * Tail pointer in the Log cache
     */
    private static int next = 0;

    /**
     * Head pointer in the Log cache
     */
    private static int first = 0;

    /**
     * Controls the context Logging feature
     */
    private static boolean contextLogging = false;

    /**
     * The client max supported Log level. This is only needed for more accurate
     * context Logging behavior and the client filters Log statements.
     */
    private static int clientMaxLogLevel = TRACE;

    private static boolean lockedLogLevel;

    private static Logger instance = null;

    // -------------------------------------------------------------
    // Constructors
    /**
     * This class is static and cannot be intantiated
     */
    private Logger() {
    }

    /**
     * The Log can be used via its static methods or as a singleton in case
     * static access is not allowed (this is the case for example on the
     * BlackBerry listeners when invoked outside of the running process)
     */
    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public static boolean isRelease() {
        return release;
    }

    public static void setRelease(boolean release) {
        Logger.release = release;
    }

    // ----------------------------------------------------------- Public
    // methods
    /**
     * Initialize Log file with a specific appender and Log level. Contextual
     * errors handling is disabled after this call.
     * 
     * @param object the appender object that write Log file
     * @param level the Log level
     */
    public static void initLog(Appender object, int level) {
        instance = getInstance();

        out = object;
        out.initLogFile();
        // Init the caching part
        cache = new Vector(cacheSize);
        first = 0;
        next = 0;
        contextLogging = false;
        lockedLogLevel = false;
        setLogLevel(level);
        if (level > Logger.DISABLED) {
            writeLogMessage(level, "INITLog", "---------");
        }
    }

    /**
     * Initialize Log file with a specific appender and Log level. Contextual
     * errors handling is enabled after this call.
     * 
     * @param object the appender object that write Log file
     * @param level the Log level
     * @param cacheSize the max number of Log messages cached before an error is
     *            dumped
     */
    public static void initLog(Appender object, int level, int cacheSize) {
        Logger.cacheSize = cacheSize;
        initLog(object, level);
        contextLogging = true;
    }

    /**
     * Ititialize Log file
     * 
     * @param object the appender object that write Log file
     */
    public static void initLog(Appender object) {
        initLog(object, INFO);
    }

    /**
     * Return a reference to the current appender
     */
    public static Appender getAppender() {
        return out;
    }

    /**
     * Enabled/disable the context Logging feature. When this feature is on, any
     * call to Log.error will trigger the dump of the error context.
     */
    public static void enableContextLogging(boolean contextLogging) {
        Logger.contextLogging = contextLogging;
    }

    /**
     * Allow clients to specify their maximum Log level. By default this value
     * is set to TRACE.
     */
    public static void setClientMaxLogLevel(int clientMaxLogLevel) {
        Logger.clientMaxLogLevel = clientMaxLogLevel;
    }

    /**
     * Delete Log file
     */
    public static void deleteLog() {
        out.deleteLogFile();
    }

    /**
     * Accessor method to define Log level the method will be ignorated in Log
     * level is locked
     * 
     * @param newlevel Log level to be set
     */
    public static void setLogLevel(int newlevel) {
        if (!lockedLogLevel) {
            level = newlevel;
            if (out != null) {
                out.setLogLevel(level);
            }
        }
    }

    /**
     * Accessor method to lock defined Log level
     * 
     * @param level Log level to be lock
     */
    public static void lockLogLevel(int levelToLock) {
        level = levelToLock;
        lockedLogLevel = true;
        if (out != null) {
            out.setLogLevel(level);
        }
    }

    /**
     * Accessor method to lock defined Log level
     */
    public static void unlockLogLevel() {
        lockedLogLevel = false;
    }

    /**
     * Accessor method to retrieve Log level:
     * 
     * @return actual Log level
     */
    public static int getLogLevel() {
        return level;
    }

    /**
     * ERROR: Error message
     * 
     * @param msg the message to be Logged
     */
    public static void error(String msg) {
        writeLogMessage(ERROR, "ERROR", msg);
    }

    /**
     * ERROR: Error message
     * 
     * @param msg the message to be Logged
     * @param obj the object that send error message
     */
    public static void error(Object obj, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(obj.getClass().getName()).append("] ").append(msg);
        writeLogMessage(ERROR, "ERROR", message.toString());
    }

    /**
     * ERROR: Error message
     * 
     * @param msg the message to be Logged
     * @param tag the tag characterizing the Log message initiator
     */
    public static void error(String tag, String msg) {
        instance.e(tag, msg);
    }

    /**
     * ERROR: Error message
     * 
     * @param msg the message to be Logged
     * @param tag the tag characterizing the Log message initiator
     * @param e the exception that caused the error
     */
    public static void error(String tag, String msg, Throwable e) {
        instance.e(tag, msg, e);
    }

    /**
     * INFO: Information message
     * 
     * @param msg the message to be Logged
     */
    public static void info(String msg) {
        writeLogMessage(INFO, "INFO", msg);
    }

    /**
     * INFO: Information message
     * 
     * @param msg the message to be Logged
     * @param obj the object that send Log message
     */
    public static void info(Object obj, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(obj.getClass().getName()).append("] ").append(msg);
        writeLogMessage(INFO, "INFO", message.toString());
    }

    /**
     * INFO: Information message
     * 
     * @param msg the message to be Logged
     * @param tag the tag characterizing the Log message initiator
     */
    public static void info(String tag, String msg) {
        instance.i(tag, msg);
    }

    public static void info(String tag, String msg, Throwable e) {
        instance.i(tag, msg, e);
    }

    /**
     * DEBUG: Debug message
     * 
     * @param msg the message to be Logged
     */
    public static void debug(String msg) {
        writeLogMessage(DEBUG, "DEBUG", msg);
    }

    /**
     * DEBUG: Information message
     * 
     * @param msg the message to be Logged
     * @param tag the tag characterizing the Log message initiator
     */
    public static void debug(String tag, String msg) {
        instance.d(tag, msg);
    }

    public static void debug(String tag, String msg, Throwable e) {
        instance.d(tag, msg, e);
    }

    /**
     * DEBUG: Information message
     * 
     * @param msg the message to be Logged
     * @param obj the object that send Log message
     */
    public static void debug(Object obj, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(obj.getClass().getName()).append("] ").append(msg);
        writeLogMessage(DEBUG, "DEBUG", message.toString());
    }

    /**
     * TRACE: Debugger mode
     */
    public static void trace(String msg) {
        writeLogMessage(TRACE, "TRACE", msg);
    }

    /**
     * TRACE: Information message
     * 
     * @param msg the message to be Logged
     * @param obj the object that send Log message
     */
    public static void trace(Object obj, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(obj.getClass().getName()).append("] ").append(msg);
        writeLogMessage(TRACE, "TRACE", message.toString());
    }

    /**
     * TRACE: Information message
     * 
     * @param msg the message to be Logged
     * @param tag the tag characterizing the Log message initiator
     */
    public static void trace(String tag, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(tag).append("] ").append(msg);
        writeLogMessage(TRACE, "TRACE", message.toString());
    }

    public static void verbose(String tag, String msg) {
        instance.v(tag, msg);
    }

    public static void verbose(String tag, String msg, Throwable e) {
        instance.v(tag, msg, e);
    }

    public static void warn(String tag, String msg) {
        instance.w(tag, msg);
    }

    public static void warn(String tag, Throwable e) {
        instance.w(tag, e);
    }

    public static void warn(String tag, String msg, Throwable e) {
        instance.w(tag, msg, e);
    }

    /**
     * Dump memory statistics at this point. Dump if level >= DEBUG.
     * 
     * @param msg message to be Logged
     */
    public static void memoryStats(String msg) {
        // Try to force a garbage collection, so we get the real amount of
        // available memory
        long available = Runtime.getRuntime().freeMemory();
        Runtime.getRuntime().gc();
        writeLogMessage(PROFILING, "PROFILING-MEMORY", msg + ":" + available + " [bytes]");
    }

    /**
     * Dump memory statistics at this point.
     * 
     * @param obj caller object
     * @param msg message to be Logged
     */
    public static void memoryStats(Object obj, String msg) {
        // Try to force a garbage collection, so we get the real amount of
        // available memory
        Runtime.getRuntime().gc();
        long available = Runtime.getRuntime().freeMemory();
        writeLogMessage(PROFILING, "PROFILING-MEMORY", obj.getClass().getName() + "::" + msg + ":"
                + available + " [bytes]");
    }

    /**
     * Dump time statistics at this point.
     * 
     * @param msg message to be Logged
     */
    public static void timeStats(String msg) {
        long time = System.currentTimeMillis();
        if (initialTimeStamp == -1) {
            writeLogMessage(PROFILING, "PROFILING-TIME", msg + ": 0 [msec]");
            initialTimeStamp = time;
        } else {
            long currentTime = time - initialTimeStamp;
            writeLogMessage(PROFILING, "PROFILING-TIME", msg + ": " + currentTime + "[msec]");
        }
    }

    /**
     * Dump time statistics at this point.
     * 
     * @param obj caller object
     * @param msg message to be Logged
     */
    public static void timeStats(Object obj, String msg) {
        // Try to force a garbage collection, so we get the real amount of
        // available memory
        long time = System.currentTimeMillis();
        if (initialTimeStamp == -1) {
            writeLogMessage(PROFILING, "PROFILING-TIME", obj.getClass().getName() + "::" + msg
                    + ": 0 [msec]");
            initialTimeStamp = time;
        } else {
            long currentTime = time - initialTimeStamp;
            writeLogMessage(PROFILING, "PROFILING-TIME", obj.getClass().getName() + "::" + msg
                    + ":" + currentTime + " [msec]");
        }
    }

    /**
     * Dump time statistics at this point.
     * 
     * @param msg message to be Logged
     */
    public static void stats(String msg) {
        memoryStats(msg);
        timeStats(msg);
    }

    /**
     * Dump time statistics at this point.
     * 
     * @param obj caller object
     * @param msg message to be Logged
     */
    public static void stats(Object obj, String msg) {
        memoryStats(obj, msg);
        timeStats(obj, msg);
    }

    /**
     * Return the current Log appender LogContent container object
     */
    public static LogContent getCurrentLogContent() throws IOException {
        return out.getLogContent();
    }

    private static synchronized void writeLogMessage(int msgLevel, String levelMsg, String msg) {
        if (contextLogging) {
            try {
                cacheMessage(msgLevel, levelMsg, msg);
            } catch (Exception e) {
                // Cannot cache Log message, just ignore the error
            }
        }

        try {
            writeLogMessageNoCache(msgLevel, levelMsg, msg);
        } catch (Exception e) {
            // Cannot write Log message, just ignore the error
        }
    }

    private static void writeLogMessageNoCache(int msgLevel, String levelMsg, String msg) {
        if (level >= msgLevel) {
            try {
                if (out != null) {
                    out.writeLogMessage(levelMsg, msg);
                }

                if (!release) {
                    Date now = new Date();
                    System.out.print(now.toString());
                    System.out.print(" [" + levelMsg + "] ");
                    System.out.println(msg);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void cacheMessage(int msgLevel, String levelMsg, String msg) throws IOException {
        // If we are already dumping at DEBUG, then the context is already
        // available
        if (cache == null || level >= clientMaxLogLevel) {
            return;
        }

        if (msgLevel == ERROR) {
            dumpAndFlushCache();
        } else {

            // Store at next
            if (next >= cache.size()) {
                cache.addElement(msg);
            } else {
                cache.setElementAt(msg, next);
            }
            // Move next
            next++;
            if (next == cacheSize) {
                next = 0;
            }

            if (next == first) {
                // Make room for the next entry
                first++;
            }
            if (first == cacheSize) {
                first = 0;
            }
        }
    }

    private static void dumpAndFlushCache() throws IOException {

        int i = first;
        if (first != next) {
            writeLogMessageNoCache(ERROR, "[Error Context]",
                    "==================================================");
        }
        while (i != next) {
            if (i == cacheSize) {
                i = 0;
            }
            writeLogMessageNoCache(ERROR, "[Error Context]", (String) cache.elementAt(i));
            ++i;
        }

        if (first != next) {
            writeLogMessageNoCache(ERROR, "[Error Context]",
                    "==================================================");
        }
        first = 0;
        next = 0;
    }

    @Override
    public int d(String tag, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(tag).append("] ").append(msg);
        writeLogMessage(DEBUG, "DEBUG", message.toString());

        return 0;
    }

    @Override
    public int d(String tag, String msg, Throwable e) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(tag).append("] ").append(msg).append("(").append(e.toString())
                .append(")");
        writeLogMessage(DEBUG, "DEBUG", message.toString());
        writeLogMessage(DEBUG, "DEBUG", instance.getStackTraceString(e));

        return 0;
    }

    @Override
    public int e(String tag, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(tag).append("] ").append(msg);
        writeLogMessage(ERROR, "ERROR", message.toString());

        return 0;
    }

    @Override
    public int e(String tag, String msg, Throwable e) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(tag).append("] ").append(msg).append("(").append(e.toString())
                .append(")");
        writeLogMessage(ERROR, "ERROR", message.toString());
        writeLogMessage(ERROR, "ERROR", instance.getStackTraceString(e));

        return 0;
    }

    @Override
    public String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    @Override
    public int i(String tag, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(tag).append("] ").append(msg);
        writeLogMessage(INFO, "INFO", message.toString());

        return 0;
    }

    @Override
    public int i(String tag, String msg, Throwable e) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(tag).append("] ").append(msg).append("(").append(e.toString())
                .append(")");
        writeLogMessage(INFO, "INFO", message.toString());
        writeLogMessage(INFO, "INFO", instance.getStackTraceString(e));

        return 0;
    }

    @Override
    public int v(String tag, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(tag).append("] ").append(msg);
        writeLogMessage(VERBOSE, "VERBOSE", message.toString());

        return 0;
    }

    @Override
    public int v(String tag, String msg, Throwable e) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(tag).append("] ").append(msg).append("(").append(e.toString())
                .append(")");
        writeLogMessage(VERBOSE, "VERBOSE", message.toString());
        writeLogMessage(VERBOSE, "VERBOSE", instance.getStackTraceString(e));

        return 0;
    }

    @Override
    public int w(String tag, String msg) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(tag).append("] ").append(msg);
        writeLogMessage(WARN, "WARN", message.toString());

        return 0;
    }

    @Override
    public int w(String tag, Throwable e) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(tag).append("] ").append("(").append(e.toString()).append(")");
        writeLogMessage(WARN, "WARN", message.toString());
        writeLogMessage(WARN, "WARN", instance.getStackTraceString(e));

        return 0;
    }

    @Override
    public int w(String tag, String msg, Throwable e) {
        StringBuffer message = new StringBuffer();
        message.append("[").append(tag).append("] ").append(msg).append("(").append(e.toString())
                .append(")");
        writeLogMessage(WARN, "WARN", message.toString());
        writeLogMessage(WARN, "WARN", instance.getStackTraceString(e));

        return 0;
    }
}
