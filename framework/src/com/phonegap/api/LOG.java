/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */
package com.phonegap.api;

import android.util.Log;

/**
 * Log to Android logging system.
 * 
 * Log message can be a string or a printf formatted string with arguments.
 * See http://developer.android.com/reference/java/util/Formatter.html
 */
public class LOG {
    
    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;

    // Current log level
    public static int LOGLEVEL = Log.ERROR;
    
    /**
     * Set the current log level.
     * 
     * @param logLevel
     */
    public static void setLogLevel(int logLevel) {
        LOGLEVEL = logLevel;
        Log.i("PhoneGapLog", "Changing log level to " + logLevel);
    }
    
    /**
     * Set the current log level.
     * 
     * @param logLevel
     */
    public static void setLogLevel(String logLevel) {
        if ("VERBOSE".equals(logLevel)) LOGLEVEL = VERBOSE;
        else if ("DEBUG".equals(logLevel)) LOGLEVEL = DEBUG;
        else if ("INFO".equals(logLevel)) LOGLEVEL = INFO;
        else if ("WARN".equals(logLevel)) LOGLEVEL = WARN;
        else if ("ERROR".equals(logLevel)) LOGLEVEL = ERROR;
        Log.i("PhoneGapLog", "Changing log level to " + logLevel + "(" + LOGLEVEL + ")");
    }

    /**
     * Determine if log level will be logged
     * 
     * @param logLevel
     * @return
     */
    public static boolean isLoggable(int logLevel) {
        return (logLevel >= LOGLEVEL);
    }

    /**
     * Verbose log message.
     * 
     * @param tag
     * @param s
     */
    public static void v(String tag, String s) {
        if (LOG.VERBOSE >= LOGLEVEL) Log.v(tag, s);
    }

    /**
     * Debug log message.
     * 
     * @param tag
     * @param s
     */
    public static void d(String tag, String s) {
        if (LOG.DEBUG >= LOGLEVEL) Log.d(tag, s);
    }
    
    /**
     * Info log message.
     * 
     * @param tag
     * @param s
     */
    public static void i(String tag, String s) {
        if (LOG.INFO >= LOGLEVEL) Log.i(tag, s);
    }

    /**
     * Warning log message.
     * 
     * @param tag
     * @param s
     */
    public static void w(String tag, String s) {
        if (LOG.WARN >= LOGLEVEL) Log.w(tag, s);
    }

    /**
     * Error log message.
     * 
     * @param tag
     * @param s
     */
    public static void e(String tag, String s) {
        if (LOG.ERROR >= LOGLEVEL) Log.e(tag, s);
    }

    /**
     * Verbose log message.
     * 
     * @param tag
     * @param s
     * @param e
     */
    public static void v(String tag, String s, Throwable e) {
        if (LOG.VERBOSE >= LOGLEVEL) Log.v(tag, s, e);
    }

    /**
     * Debug log message.
     * 
     * @param tag
     * @param s
     * @param e
     */
    public static void d(String tag, String s, Throwable e) {
        if (LOG.DEBUG >= LOGLEVEL) Log.d(tag, s, e);
    }
    
    /**
     * Info log message.
     * 
     * @param tag
     * @param s
     * @param e
     */
    public static void i(String tag, String s, Throwable e) {
        if (LOG.INFO >= LOGLEVEL) Log.i(tag, s, e);
    }

    /**
     * Warning log message.
     * 
     * @param tag
     * @param s
     * @param e
     */
    public static void w(String tag, String s, Throwable e) {
        if (LOG.WARN >= LOGLEVEL) Log.w(tag, s, e);
    }

    /**
     * Error log message.
     * 
     * @param tag
     * @param s
     * @param e
     */
    public static void e(String tag, String s, Throwable e) {
        if (LOG.ERROR >= LOGLEVEL) Log.e(tag, s, e);
    }

    /**
     * Verbose log message with printf formatting.
     * 
     * @param tag
     * @param s
     * @param args
     */
    public static void v(String tag, String s, Object... args) {
        if (LOG.VERBOSE >= LOGLEVEL) Log.v(tag, String.format(s, args));
    }

    /**
     * Debug log message with printf formatting.
     * 
     * @param tag
     * @param s
     * @param args
     */
    public static void d(String tag, String s, Object... args) {
        if (LOG.DEBUG >= LOGLEVEL) Log.d(tag, String.format(s, args));
    }

    /**
     * Info log message with printf formatting.
     * 
     * @param tag
     * @param s
     * @param args
     */
    public static void i(String tag, String s, Object... args) {
        if (LOG.INFO >= LOGLEVEL) Log.i(tag, String.format(s, args));
    }
    
    /**
     * Warning log message with printf formatting.
     * 
     * @param tag
     * @param s
     * @param args
     */
    public static void w(String tag, String s, Object... args) {
        if (LOG.WARN >= LOGLEVEL) Log.w(tag, String.format(s, args));
    }
    
    /**
     * Error log message with printf formatting.
     * 
     * @param tag
     * @param s
     * @param args
     */
    public static void e(String tag, String s, Object... args) {
        if (LOG.ERROR >= LOGLEVEL) Log.e(tag, String.format(s, args));
    }

}
