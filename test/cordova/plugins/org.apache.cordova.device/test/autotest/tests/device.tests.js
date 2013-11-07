/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

describe('Device Information (window.device)', function () {
	it("should exist", function() {
        expect(window.device).toBeDefined();
	});

	it("should contain a platform specification that is a string", function() {
        expect(window.device.platform).toBeDefined();
		expect((new String(window.device.platform)).length > 0).toBe(true);
	});

	it("should contain a version specification that is a string", function() {
        expect(window.device.version).toBeDefined();
		expect((new String(window.device.version)).length > 0).toBe(true);
	});

	it("should contain a UUID specification that is a string or a number", function() {
        expect(window.device.uuid).toBeDefined();
		if (typeof window.device.uuid == 'string' || typeof window.device.uuid == 'object') {
		    expect((new String(window.device.uuid)).length > 0).toBe(true);
		} else {
			expect(window.device.uuid > 0).toBe(true);
		}
	});

	it("should contain a Cordova specification that is a string", function() {
        expect(window.device.cordova).toBeDefined();
		expect((new String(window.device.cordova)).length > 0).toBe(true);
	});

    it("should depend on the precense of cordova.version string", function() {
            expect(window.cordova.version).toBeDefined();
            expect((new String(window.cordova.version)).length > 0).toBe(true);
    });

    it("should contain device.cordova equal to cordova.version", function() {
             expect(window.device.cordova).toBe(window.cordova.version);
    });

	it("should contain a model specification that is a string", function() {
        expect(window.device.model).toBeDefined();
		expect((new String(window.device.model)).length > 0).toBe(true);
	});
});
