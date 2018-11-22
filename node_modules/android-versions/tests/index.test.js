"use strict";

const test = require('tape')
const android = require('..')

test('get specific version by API level', (t) => {
  t.plan(1)
  t.equal(android.get(24).name, "Nougat")
})

test('getAll versions by API level', (t) => {
  t.plan(1)
  t.equal(android.getAll(24)[0].name, "Nougat")
})

test('get specific version by predicate', (t) => {
  t.plan(2)

  let actual = android.get((version) => {
    return version.name.indexOf("on") !== -1
  })
  t.equal(actual.name, "Donut")

  actual = android.get((version) => {
    return version.ndk > 5 && version.api < 15
  })
  t.equal(actual.versionCode, "HONEYCOMB_MR1")
})

test('getAll versions by predicate', (t) => {
  t.plan(3)

  let actual = android.getAll((version) => {
    return version.name.indexOf("on") !== -1
  }).map((version) => version.name)
  t.deepEqual(actual, ["Donut", "Honeycomb", "Honeycomb", "Honeycomb"])

  actual = android.getAll((version) => {
    return version.ndk > 5 && version.api < 15
  }).map((version) => version.versionCode)
  t.deepEqual(actual, ["HONEYCOMB_MR1", "HONEYCOMB_MR2", "ICE_CREAM_SANDWICH"])

  actual = android.getAll((version) => {
    return version.api > 22
  }).map((version) => version.versionCode)
  t.deepEqual(actual, ["M", "N", "N_MR1", "O", "O_MR1", "P"])
})

test('get version by semantic version', (t) => {
  t.plan(4)
  t.equal(android.get("6.0").versionCode, android.M.versionCode)
  t.equal(android.get("6.0.0").versionCode, android.M.versionCode)
  t.equal(android.get("2.3").versionCode, android.GINGERBREAD.versionCode)
  t.equal(android.get("2.3.3").versionCode, android.GINGERBREAD_MR1.versionCode)
})

test('support major version only', (t) => {
  t.plan(2)
  t.equal(android.get("9.0").versionCode, android.P.versionCode)
  t.equal(android.get("9.0.0").versionCode, android.P.versionCode)
})

test('support version ranges', (t) => {
  t.plan(7)
  let tests = [ "4.4", "4.4.0", "4.4.1", "4.4.2", "4.4.3", "4.4.4" ]
  tests.forEach((versionCode) => {
    t.equal(android.get(versionCode).versionCode, android.KITKAT.versionCode)
  })
  t.equal(android.get("4.4.5"), null)
})

test('support x-ranges', (t) => {
  t.plan(12)
  let tests = [
    "4.1", "4.1.0", "4.1.1", "4.1.2", "4.1.3", "4.1.4",
    "4.1.5", "4.1.6", "4.1.7", "4.1.8", "4.1.9", "4.1.10"
  ]
  tests.forEach((versionCode) => {
    t.equal(android.get(versionCode).versionCode, android.JELLY_BEAN.versionCode)
  })
})

test('access version codes object', (t) => {
  t.plan(1)
  t.ok(android.VERSIONS)
})

test('access specific versions directly', (t) => {
  t.plan(28)
  t.ok(android.BASE)
  t.ok(android.BASE_1_1)
  t.ok(android.CUPCAKE)
  t.ok(android.DONUT)
  t.ok(android.ECLAIR)
  t.ok(android.ECLAIR_0_1)
  t.ok(android.ECLAIR_MR1)
  t.ok(android.FROYO)
  t.ok(android.GINGERBREAD)
  t.ok(android.GINGERBREAD_MR1)
  t.ok(android.HONEYCOMB)
  t.ok(android.HONEYCOMB_MR1)
  t.ok(android.HONEYCOMB_MR2)
  t.ok(android.ICE_CREAM_SANDWICH)
  t.ok(android.ICE_CREAM_SANDWICH_MR1)
  t.ok(android.JELLY_BEAN)
  t.ok(android.JELLY_BEAN_MR1)
  t.ok(android.JELLY_BEAN_MR2)
  t.ok(android.KITKAT)
  t.ok(android.KITKAT_WATCH)
  t.ok(android.LOLLIPOP)
  t.ok(android.LOLLIPOP_MR1)
  t.ok(android.M)
  t.ok(android.N)
  t.ok(android.N_MR1)
  t.ok(android.O)
  t.ok(android.O_MR1)
  t.ok(android.P)
})
