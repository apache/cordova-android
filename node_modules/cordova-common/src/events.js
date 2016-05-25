/**
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

var EventEmitter = require('events').EventEmitter;

var INSTANCE = new EventEmitter();
var EVENTS_RECEIVER;

module.exports = INSTANCE;

/**
 * Sets up current instance to forward emitted events to another EventEmitter
 *   instance.
 *
 * @param   {EventEmitter}  [eventEmitter]  The emitter instance to forward
 *   events to. Falsy value, when passed, disables forwarding.
 */
module.exports.forwardEventsTo = function (eventEmitter) {

    // If no argument is specified disable events forwarding
    if (!eventEmitter) {
        EVENTS_RECEIVER = undefined;
        return;
    }

    if (!(eventEmitter instanceof EventEmitter))
        throw new Error('Cordova events can be redirected to another EventEmitter instance only');

    // CB-10940 Skipping forwarding to self to avoid infinite recursion.
    // This is the case when the modules are npm-linked.
    if (this !== eventEmitter) {
        EVENTS_RECEIVER = eventEmitter;
    } else {
        // Reset forwarding if we are subscribing to self
        EVENTS_RECEIVER = undefined;
    }
};

var emit = INSTANCE.emit;

/**
 * This method replaces original 'emit' method to allow events forwarding.
 *
 * @return  {eventEmitter}  Current instance to allow calls chaining, as
 *   original 'emit' does
 */
module.exports.emit = function () {

    var args = Array.prototype.slice.call(arguments);

    if (EVENTS_RECEIVER) {
        EVENTS_RECEIVER.emit.apply(EVENTS_RECEIVER, args);
    }

    return emit.apply(this, args);
};
