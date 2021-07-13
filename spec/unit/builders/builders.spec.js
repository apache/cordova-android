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

const rewire = require('rewire');

const CordovaError = require('cordova-common').CordovaError;
const ProjectBuilder = require('../../../lib/builders/ProjectBuilder');

describe('builders', () => {
    let builders;

    beforeEach(() => {
        builders = rewire('../../../lib/builders/builders');
    });

    describe('getBuilder', () => {
        it('should return an instance of ProjectBuilder when gradle is requested', () => {
            const root = 'FakeProjectRoot';
            const newBuilder = builders.getBuilder(root);
            expect(newBuilder).toEqual(jasmine.any(ProjectBuilder));
            expect(newBuilder.root).toBe(root);
        });

        it('should throw an error if a builder cannot be instantiated', () => {
            const requireSpy = jasmine.createSpy('require').and.throwError();
            builders.__set__('require', requireSpy);

            expect(() => builders.getBuilder()).toThrow(jasmine.any(CordovaError));
        });
    });
});
