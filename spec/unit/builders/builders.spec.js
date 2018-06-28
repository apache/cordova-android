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
const GenericBuilder = require('../../../bin/templates/cordova/lib/builders/GenericBuilder');
const GradleBuilder = require('../../../bin/templates/cordova/lib/builders/GradleBuilder');
const StudioBuilder = require('../../../bin/templates/cordova/lib/builders/StudioBuilder');

describe('builders', () => {
    let builder;

    beforeEach(() => {
        builder = rewire('../../../bin/templates/cordova/lib/builders/builders');
    });

    describe('getBuilder', () => {
        it('should return an instance of GradleBuilder', () => {
            const newBuilder = builder.getBuilder('gradle');
            expect(newBuilder).toEqual(jasmine.any(GradleBuilder));
        });

        it('should return an instance of StudioBuilder', () => {
            const newBuilder = builder.getBuilder('studio');
            expect(newBuilder).toEqual(jasmine.any(StudioBuilder));
        });

        it('should return an instance of GenericBuilder', () => {
            const newBuilder = builder.getBuilder('none');
            expect(newBuilder).toEqual(jasmine.any(GenericBuilder));
        });

        it('should throw an error if the selected builder does not exist', () => {
            expect(() => builder.getBuilder('NonExistentBuilder')).toThrow(jasmine.any(CordovaError));
        });

        it('should throw an error if a builder cannot be instantiated', () => {
            const requireSpy = jasmine.createSpy('require').and.throwError();
            builder.__set__('require', requireSpy);

            expect(() => builder.getBuilder('gradle')).toThrow(jasmine.any(CordovaError));
        });
    });
});
