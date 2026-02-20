<!--
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
-->

# Development

## Setup

1. Install the requirements defined in the [README.md](README.md#requirements)
2. Install npm dependencies.

    ```bash
    npm install
    ```

## Using a Cloned Repo as a Shared Framework

By default, when you add a platform to a Cordova project, the platform files are copied directly into your Android project. However, during development, it can be easier to use a shared framework instead.

Using the `--link` option creates symbolic links to your cloned repository instead of copying the files. This allows you to make changes to the core platform files in Android Studio and have those changes automatically reflected in your cloned development repository (and vice versa).

To add a linked platform, run:

```bash
cordova platform add --link /path/to/cordova-android
```

## Unit Testing

Our projects include unit tests, which can be run with:

```bash
npm test
```

## Linting

During development, you should run the linter to ensure the code follows our coding standards:

```bash
npm run lint
```

> [!NOTE]
> Running `npm test` will also execute the linter before running the tests.

### Fixing Lint Issues

In many cases, lint warnings can be fixed automatically with:

```bash
npm run lint:fix
```

If an issue cannot be resolved automatically, it will require manual review and correction.

## Install Nightly Build

> [!WARNING]
> Nightly builds are generated daily from the `main` branch and are considered **unstable** and **untested**. They are **not for use in production applications** and are intended only for development and testing purposes.
>
> See [Apache Cordova - Nightly Builds](https://cordova.apache.org/contribute/nightly_builds.html) for more details.

You can install the nightly build with:

```bash
cordova platform add android@nightly
```

> [!NOTE]
> Due to recent changes to npm authentication tokens, nightly builds may occasionally stop publishing.

## Building from Source

1. **Clone the repository** locally.

2. **Change to the repository directory.**

3. **Install dependencies:**

   ```bash
   npm install
   ```

   Installs all production and development dependencies required for using and developing the package.

4. **Update sub-dependencies:**

   ```bash
   npm update
   ```

   Over time, `package-lock.json` can become stale and may trigger audit warnings. `npm update` refreshes dependencies within the pinned versions.

   > [!NOTE]
   > When you install the published package from the npm registry (normal usage), it does **not** include a `package-lock.json` and automatically fetches the latest compatible dependencies. Running `npm update` locally simulates this behavior.

5. **Generate a tarball:**

   ```bash
   npm pack
   ```

   Creates a `.tgz` file that can be installed in a project via:

   ```bash
   npm install /path/to/package.tgz
   ```

   > [!NOTE]
   > Tarball will be created in the `.asf-release` directory within this repository directory.
