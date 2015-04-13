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
/* jshint laxcomma:true */

require("promise-matchers");

var create = require("../bin/lib/create");

describe("create", function () {
  describe("validatePackageName", function() {
    var valid = [
        "org.apache.mobilespec"
      , "com.example"
      , "com.floors42.package"
      , "ball8.ball8.ball8ball"
    ];
    var invalid = [
        ""
      , "com.class.is.bad"
      , "0com.example.mobilespec"
      , "c-m.e@a!p%e.mobilespec"
      , "notenoughdots"
      , ".starts.with.a.dot"
      , "ends.with.a.dot."
      , "_underscore.anything"
      , "underscore._something"
      , "_underscore._all._the._things"
      , "8.ball"
      , "8ball.ball"
      , "ball8.8ball"
      , "ball8.com.8ball"
    ];

    valid.forEach(function(package_name) {
      it("should accept " + package_name, function(done) {
        expect(create.validatePackageName(package_name)).toHaveBeenResolved(done);
      });
    });

    invalid.forEach(function(package_name) {
      it("should reject " + package_name, function(done) {
        expect(create.validatePackageName(package_name)).toHaveBeenRejected(done);
      });
    });
  });
  describe("validateProjectName", function() {
    var valid = [
        "mobilespec"
      , "package_name"
      , "PackageName"
      , "CordovaLib"
    ];
    var invalid = [
        ""
      , "0startswithdigit"
      , "CordovaActivity"
    ];

    valid.forEach(function(project_name) {
      it("should accept " + project_name, function(done) {
        expect(create.validateProjectName(project_name)).toHaveBeenResolved(done);
      });
    });

    invalid.forEach(function(project_name) {
      it("should reject " + project_name, function(done) {
        expect(create.validateProjectName(project_name)).toHaveBeenRejected(done);
      });
    });
  });
});
