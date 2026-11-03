#!/usr/bin/env bash
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

# Run inside android-emulator-runner while the emulator is still available.
# GNU timeout is provided by the Ubuntu CI runner.
set -euo pipefail
cd "$(dirname "$0")/androidx"

mkdir -p build/instrumentation

collect_diagnostics() {
    adb logcat -d -v threadtime > build/instrumentation/logcat.txt 2>&1 || true
    adb shell getprop > build/instrumentation/device-properties.txt 2>&1 || true
    adb shell dumpsys webviewupdate > build/instrumentation/webview.txt 2>&1 || true
    adb shell dumpsys package com.android.chrome > build/instrumentation/chrome-package.txt 2>&1 || true
    adb shell dumpsys package com.android.webview > build/instrumentation/aosp-webview-package.txt 2>&1 || true
    adb shell dumpsys package com.google.android.webview > build/instrumentation/google-webview-package.txt 2>&1 || true
}
trap collect_diagnostics EXIT

adb logcat -c
# Bound hung tests without filtering the suite or retrying failures. Keep time
# for diagnostics and artifact upload before the workflow's overall timeout.
timeout --signal=TERM --kill-after=30s 10m \
    tools/gradlew :app:connectedDebugAndroidTest --stacktrace --console=plain \
    2>&1 | tee build/instrumentation/gradle.log
