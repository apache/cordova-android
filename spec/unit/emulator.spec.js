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
var cc = require("cordova-common");
var emu = require("../../bin/templates/cordova/lib/emulator");
var Q = require("q");
var fs = require("fs");
var path = require("path");
var shelljs = require("shelljs");

describe("emulator", function () {
    describe("list_images_using_avdmanager", function() {
        it("should properly parse details of SDK Tools 25.3.1 `avdmanager` output", function(done) {
            var deferred = Q.defer();
            spyOn(cc.superspawn, "spawn").and.returnValue(deferred.promise);
            deferred.resolve(fs.readFileSync(path.join("spec", "fixtures", "avdmanager_list_avd.txt"), "utf-8"));
            return emu.list_images_using_avdmanager()
            .then(function(list) {
                expect(list).toBeDefined();
                expect(list[0].name).toEqual("nexus5-5.1");
                expect(list[0].target).toEqual("Android 5.1 (API level 22)");
                expect(list[1].device).toEqual("pixel (Google)");
                expect(list[2].abi).toEqual("default/x86_64");
            }).fail(function(err) {
                expect(err).toBeUndefined();
            }).fin(function() {
                done();
            });
        });
    });
    describe("list_images_using_android", function() {
        it("should properly parse details of SDK Tools pre-25.3.1 `android list avd` output", function(done) {
            var deferred = Q.defer();
            spyOn(cc.superspawn, "spawn").and.returnValue(deferred.promise);
            deferred.resolve(fs.readFileSync(path.join("spec", "fixtures", "android_list_avd.txt"), "utf-8"));
            return emu.list_images_using_android()
            .then(function(list) {
                expect(list).toBeDefined();
                expect(list[0].name).toEqual("QWR");
                expect(list[0].device).toEqual("Nexus 5 (Google)");
                expect(list[0].path).toEqual("/Users/shazron/.android/avd/QWR.avd");
                expect(list[0].target).toEqual("Android 7.1.1 (API level 25)");
                expect(list[0].abi).toEqual("google_apis/x86_64");
                expect(list[0].skin).toEqual("1080x1920");
            }).fail(function(err) {
                expect(err).toBeUndefined();
            }).fin(function() {
                done();
            });
        });
    });
    describe("list_images", function() {
        beforeEach(function() {
            spyOn(fs, "realpathSync").and.callFake(function(cmd) {
                return cmd;
            });
        });
        it("should try to parse AVD information using `android`", function() {
            spyOn(shelljs, "which").and.callFake(function(cmd) {
                if (cmd == "android") {
                    return true;
                } else {
                    return false;
                }
            });
            var android_spy = spyOn(emu, "list_images_using_android").and.returnValue({catch:function(){}});
            emu.list_images();
            expect(android_spy).toHaveBeenCalled();
        });
        it("should catch if `android` exits with non-zero code and specific stdout, and delegate to `avdmanager` if it can find it", function() {
            spyOn(shelljs, "which").and.callFake(function(cmd) {
                if (cmd == "android") {
                    return true;
                } else {
                    return false;
                }
            });
            var avdmanager_spy = spyOn(emu, "list_images_using_avdmanager");
            // Fake out the old promise to feign a failed `android` command
            spyOn(emu, "list_images_using_android").and.returnValue({
                catch:function(cb) {
                    cb({
                        code: 1,
                        stdout: ["The android command is no longer available.",
                                "For manual SDK and AVD management, please use Android Studio.",
                                "For command-line tools, use tools/bin/sdkmanager and tools/bin/avdmanager"].join("\n")
                    });
                }
            });
            emu.list_images();
            expect(avdmanager_spy).toHaveBeenCalled();
        });
        it("should throw an error if neither `avdmanager` nor `android` are able to be found", function(done) {
            spyOn(shelljs, "which").and.returnValue(false);
            return emu.list_images()
            .catch(function(err) {
                expect(err).toBeDefined();
                expect(err.message).toContain("Could not find either `android` or `avdmanager`");
                done();
            });
        });
    });
});
