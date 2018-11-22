/**
 * Copyright (c) 2016, David Voiss <davidvoiss@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 * OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
*/

/* jshint node: true */
"use strict";

/**
 * A module to get Android versions by API level, NDK level, semantic version, or version name.
 *
 * Versions are referenced from here:
 * {@link https://source.android.com/source/build-numbers.html#platform-code-names-versions-api-levels-and-ndk-releases}
 * {@link https://github.com/android/platform_frameworks_base/blob/master/core/java/android/os/Build.java}
 *
 * The version for "Current Development Build" ("CUR_DEVELOPMENT") is not included.
 *
 * @module android-versions
 */

var VERSIONS = {
  BASE:                   { api: 1,     ndk: 0, semver: "1.0",               name: "(no code name)",     },
  BASE_1_1:               { api: 2,     ndk: 0, semver: "1.1",               name: "(no code name)",     },
  CUPCAKE:                { api: 3,     ndk: 1, semver: "1.5",               name: "Cupcake",            },
  DONUT:                  { api: 4,     ndk: 2, semver: "1.6",               name: "Donut",              },
  ECLAIR:                 { api: 5,     ndk: 2, semver: "2.0",               name: "Eclair",             },
  ECLAIR_0_1:             { api: 6,     ndk: 2, semver: "2.0.1",             name: "Eclair",             },
  ECLAIR_MR1:             { api: 7,     ndk: 3, semver: "2.1",               name: "Eclair",             },
  FROYO:                  { api: 8,     ndk: 4, semver: "2.2.x",             name: "Froyo",              },
  GINGERBREAD:            { api: 9,     ndk: 5, semver: "2.3.0 - 2.3.2",     name: "Gingerbread",        },
  GINGERBREAD_MR1:        { api: 10,    ndk: 5, semver: "2.3.3 - 2.3.7",     name: "Gingerbread",        },
  HONEYCOMB:              { api: 11,    ndk: 5, semver: "3.0",               name: "Honeycomb",          },
  HONEYCOMB_MR1:          { api: 12,    ndk: 6, semver: "3.1",               name: "Honeycomb",          },
  HONEYCOMB_MR2:          { api: 13,    ndk: 6, semver: "3.2.x",             name: "Honeycomb",          },
  ICE_CREAM_SANDWICH:     { api: 14,    ndk: 7, semver: "4.0.1 - 4.0.2",     name: "Ice Cream Sandwich", },
  ICE_CREAM_SANDWICH_MR1: { api: 15,    ndk: 8, semver: "4.0.3 - 4.0.4",     name: "Ice Cream Sandwich", },
  JELLY_BEAN:             { api: 16,    ndk: 8, semver: "4.1.x",             name: "Jellybean",          },
  JELLY_BEAN_MR1:         { api: 17,    ndk: 8, semver: "4.2.x",             name: "Jellybean",          },
  JELLY_BEAN_MR2:         { api: 18,    ndk: 8, semver: "4.3.x",             name: "Jellybean",          },
  KITKAT:                 { api: 19,    ndk: 8, semver: "4.4.0 - 4.4.4",     name: "KitKat",             },
  KITKAT_WATCH:           { api: 20,    ndk: 8, semver: "4.4",               name: "KitKat Watch",       },
  LOLLIPOP:               { api: 21,    ndk: 8, semver: "5.0",               name: "Lollipop",           },
  LOLLIPOP_MR1:           { api: 22,    ndk: 8, semver: "5.1",               name: "Lollipop",           },
  M:                      { api: 23,    ndk: 8, semver: "6.0",               name: "Marshmallow",        },
  N:                      { api: 24,    ndk: 8, semver: "7.0",               name: "Nougat",             },
  N_MR1:                  { api: 25,    ndk: 8, semver: "7.1",               name: "Nougat",             },
  O:                      { api: 26,    ndk: 8, semver: "8.0.0",             name: "Oreo",               },
  O_MR1:                  { api: 27,    ndk: 8, semver: "8.1.0",             name: "Oreo",               },
  P:                      { api: 28,    ndk: 8, semver: "9",                 name: "Pie",                }
}

// Add a key to each version of Android for the "versionCode".
// This is the same key we use in the VERSIONS map above.
Object.keys(VERSIONS).forEach(function(version) {
  VERSIONS[version].versionCode = version
})

var semver = require('semver');

// semver format requires <major>.<minor>.<patch> but we allow just <major>.<minor> format.
// Coerce <major>.<minor> to <major>.<minor>.0
function formatSemver(semver) {
  if (semver.match(/^\d+.\d+$/)) {
    return semver + '.0'
  } else {
    return semver
  }
}

// The default predicate compares against API level, semver, name, or code.
function getFromDefaultPredicate(arg) {
  // Coerce arg to string for comparisons below.
  arg = arg.toString()

  return getFromPredicate(function(version) {
    // Check API level before all else.
    if (arg === version.api.toString()) {
      return true
    }

    var argSemver = formatSemver(arg)
    var versionSemver = formatSemver(version.semver)

    if (semver.valid(argSemver) && semver.satisfies(argSemver, versionSemver)) {
      return true
    }

    // Compare version name and code.
    return arg === version.name || arg === version.versionCode
  })
}

// The function to allow passing a predicate.
function getFromPredicate(predicate) {
  if (predicate === null) {
    return null
  }

  return Object.keys(VERSIONS).filter(function(version) {
    return predicate(VERSIONS[version])
  }).map(function(key) { return VERSIONS[key] })
}

/**
 * The Android version codes available as keys for easier look-up.
 */
Object.keys(VERSIONS).forEach(function(name) {
  exports[name] = VERSIONS[name]
})

/**
 * The complete reference of Android versions for easier look-up.
 */
exports.VERSIONS = VERSIONS

/**
 * Retrieve a single Android version.
 *
 * @param {object | Function} arg - The value or predicate to use to retrieve values.
 *
 * @return {object} An object representing the version found or null if none found.
 */
exports.get = function(arg) {
  var result = exports.getAll(arg)

  if (result === null || result.length === 0) {
    return null
  }

  return result[0]
}

/**
 * Retrieve all Android versions that meet the criteria of the argument.
 *
 * @param {object | Function} arg - The value or predicate to use to retrieve values.
 *
 * @return {object} An object representing the version found or null if none found.
 */
exports.getAll = function(arg) {
  if (arg === null) {
    return null
  }

  if (typeof arg === "function") {
    return getFromPredicate(arg)
  } else {
    return getFromDefaultPredicate(arg)
  }
}