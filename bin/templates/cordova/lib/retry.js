#!/usr/bin/env node

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

/* jshint node: true */

"use strict";

/*
 * Retry a promise-returning function a number of times, propagating its
 * results on success or throwing its error on a failed final attempt.
 *
 * @arg {Number}   attemts_left    - The number of times to retry the passed call.
 * @arg {Function} promiseFunction - A function that returns a promise.
 * @arg {...}                      - Arguments to pass to promiseFunction.
 *
 * @returns {Promise}
 */
module.exports.retryPromise = function (attemts_left, promiseFunction) {

    // NOTE:
    //      get all trailing arguments, by skipping the first two (attemts_left and
    //      promiseFunction) because they shouldn't get passed to promiseFunction
    var promiseFunctionArguments = Array.prototype.slice.call(arguments, 2);

    return promiseFunction.apply(undefined, promiseFunctionArguments).then(

        // on success pass results through
        function onFulfilled(value) {
            return value;
        },

        // on rejection either retry, or throw the error
        function onRejected(error) {

            attemts_left -= 1;

            if (attemts_left < 1) {
                throw error;
            }

            console.log("A retried call failed. Retrying " + attemts_left + " more time(s).");

            // retry call self again with the same arguments, except attemts_left is now lower
            var fullArguments = [attemts_left, promiseFunction].concat(promiseFunctionArguments);
            return module.exports.retryPromise.apply(undefined, fullArguments);
        }
    );
};
