/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

var ansi = require('ansi');
var EventEmitter = require('events').EventEmitter;
var CordovaError = require('./CordovaError/CordovaError');
var EOL = require('os').EOL;

var INSTANCE;

/**
 * @class CordovaLogger
 *
 * Implements logging facility that anybody could use. Should not be
 *   instantiated directly, `CordovaLogger.get()` method should be used instead
 *   to acquire logger instance
 */
function CordovaLogger () {
    this.levels = {};
    this.colors = {};
    this.stdout = process.stdout;
    this.stderr = process.stderr;

    this.stdoutCursor = ansi(this.stdout);
    this.stderrCursor = ansi(this.stderr);

    this.addLevel('verbose', 1000, 'grey');
    this.addLevel('normal' , 2000);
    this.addLevel('warn'   , 2000, 'yellow');
    this.addLevel('info'   , 3000, 'blue');
    this.addLevel('error'  , 5000, 'red');
    this.addLevel('results' , 10000);

    this.setLevel('normal');
}

/**
 * Static method to create new or acquire existing instance.
 *
 * @return  {CordovaLogger}  Logger instance
 */
CordovaLogger.get = function () {
    return INSTANCE || (INSTANCE = new CordovaLogger());
};

CordovaLogger.VERBOSE = 'verbose';
CordovaLogger.NORMAL = 'normal';
CordovaLogger.WARN = 'warn';
CordovaLogger.INFO = 'info';
CordovaLogger.ERROR = 'error';
CordovaLogger.RESULTS = 'results';

/**
 * Emits log message to process' stdout/stderr depending on message's severity
 *   and current log level. If severity is less than current logger's level,
 *   then the message is ignored.
 *
 * @param   {String}  logLevel  The message's log level. The logger should have
 *   corresponding level added (via logger.addLevel), otherwise
 *   `CordovaLogger.NORMAL` level will be used.
 * @param   {String}  message   The message, that should be logged to process'
 *   stdio
 *
 * @return  {CordovaLogger}     Current instance, to allow calls chaining.
 */
CordovaLogger.prototype.log = function (logLevel, message) {
    // if there is no such logLevel defined, or provided level has
    // less severity than active level, then just ignore this call and return
    if (!this.levels[logLevel] || this.levels[logLevel] < this.levels[this.logLevel])
        // return instance to allow to chain calls
        return this;

    var isVerbose = this.logLevel === 'verbose';
    var cursor = this.stdoutCursor;

    if (message instanceof Error || logLevel === CordovaLogger.ERROR) {
        message = formatError(message, isVerbose);
        cursor = this.stderrCursor;
    }

    var color = this.colors[logLevel];
    if (color) {
        cursor.bold().fg[color]();
    }

    cursor.write(message).reset().write(EOL);

    return this;
};

/**
 * Adds a new level to logger instance. This method also creates a shortcut
 *   method to log events with the level provided (i.e. after adding new level
 *   'debug', the method `debug(message)`, equal to logger.log('debug', message),
 *   will be added to logger instance)
 *
 * @param  {String}  level     A log level name. The levels with the following
 *   names added by default to every instance: 'verbose', 'normal', 'warn',
 *   'info', 'error', 'results'
 * @param  {Number}  severity  A number that represents level's severity.
 * @param  {String}  color     A valid color name, that will be used to log
 *   messages with this level. Any CSS color code or RGB value is allowed
 *   (according to ansi documentation:
 *   https://github.com/TooTallNate/ansi.js#features)
 *
 * @return  {CordovaLogger}     Current instance, to allow calls chaining.
 */
CordovaLogger.prototype.addLevel = function (level, severity, color) {

    this.levels[level] = severity;

    if (color) {
        this.colors[level] = color;
    }

    // Define own method with corresponding name
    if (!this[level]) {
        this[level] = this.log.bind(this, level);
    }

    return this;
};

/**
 * Sets the current logger level to provided value. If logger doesn't have level
 *   with this name, `CordovaLogger.NORMAL` will be used.
 *
 * @param  {String}  logLevel  Level name. The level with this name should be
 *   added to logger before.
 *
 * @return  {CordovaLogger}     Current instance, to allow calls chaining.
 */
CordovaLogger.prototype.setLevel = function (logLevel) {
    this.logLevel = this.levels[logLevel] ? logLevel : CordovaLogger.NORMAL;

    return this;
};

/**
 * Adjusts the current logger level according to the passed options.
 *
 * @param   {Object|Array}  opts  An object or args array with options
 *
 * @return  {CordovaLogger}     Current instance, to allow calls chaining.
 */
CordovaLogger.prototype.adjustLevel = function (opts) {
    if (opts.verbose || (Array.isArray(opts) && opts.indexOf('--verbose') !== -1)) {
        this.setLevel('verbose');
    } else if (opts.silent || (Array.isArray(opts) && opts.indexOf('--silent') !== -1)) {
        this.setLevel('error');
    }

    return this;
};

/**
 * Attaches logger to EventEmitter instance provided.
 *
 * @param   {EventEmitter}  eventEmitter  An EventEmitter instance to attach
 *   logger to.
 *
 * @return  {CordovaLogger}     Current instance, to allow calls chaining.
 */
CordovaLogger.prototype.subscribe = function (eventEmitter) {

    if (!(eventEmitter instanceof EventEmitter))
        throw new Error('Subscribe method only accepts an EventEmitter instance as argument');

    eventEmitter.on('verbose', this.verbose)
        .on('log', this.normal)
        .on('info', this.info)
        .on('warn', this.warn)
        .on('warning', this.warn)
        // Set up event handlers for logging and results emitted as events.
        .on('results', this.results);

    return this;
};

function formatError(error, isVerbose) {
    var message = '';

    if (error instanceof CordovaError) {
        message = error.toString(isVerbose);
    } else if (error instanceof Error) {
        if (isVerbose) {
            message = error.stack;
        } else {
            message = error.message;
        }
    } else {
        // Plain text error message
        message = error;
    }

    if (typeof message === 'string' && message.toUpperCase().indexOf('ERROR:') !== 0) {
        // Needed for backward compatibility with external tools
        message = 'Error: ' + message;
    }

    return message;
}

module.exports = CordovaLogger;
