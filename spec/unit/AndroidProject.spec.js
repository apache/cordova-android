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

var path = require('path');
var AndroidProject = require('../../bin/templates/cordova/lib/AndroidProject');
var android_project = path.join(__dirname, '../fixtures/android_project');

describe('AndroidProject class', function() {
    describe('getPackageName method', function() {
        it('Test#001 : should return an android project\'s proper package name', function() {
            expect(AndroidProject.getProjectFile(android_project).getPackageName())
                .toEqual('com.alunny.childapp');
        });
    });
});
