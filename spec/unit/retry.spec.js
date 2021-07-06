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

const retry = require('../../lib/retry');

describe('retry', () => {
    describe('retryPromise method', () => {
        let promiseFn;

        beforeEach(() => {
            promiseFn = jasmine.createSpy().and.returnValue(Promise.resolve());
        });

        it('should pass all extra arguments to the promise', () => {
            const args = ['test1', 'test2', 'test3'];

            retry.retryPromise(0, promiseFn, ...args);
            expect(promiseFn).toHaveBeenCalledWith(...args);
        });

        it('should retry the function up to specified number of times', () => {
            const attempts = 3;
            promiseFn.and.returnValue(Promise.reject());

            return retry.retryPromise(attempts, promiseFn).then(
                () => fail('Unexpectedly resolved'),
                () => expect(promiseFn).toHaveBeenCalledTimes(attempts)
            );
        });

        it('should not call the function again if it succeeds', () => {
            return retry.retryPromise(42, promiseFn).then(() => {
                expect(promiseFn).toHaveBeenCalledTimes(1);
            });
        });
    });
});
