util = require 'util'
exec = require('child_process').exec
path = require 'path'

exports['default example project is generated'] = (test) ->
  test.expect 1
  exec './bin/create', (error, stdout, stderr) ->
    test.ok true, "this assertion should pass" unless error?
    test.done()

exports['default example project has a ./.phonegap folder'] = (test) ->
  test.expect 1
  path.exists './example/.phonegap', (exists) ->
    test.ok exists, 'the phonegap folder exists'
    test.done()

exports['default example project has a /phonegap folder'] = (test) ->
  test.expect 1
  path.exists './example/phonegap', (exists) ->
    test.ok exists, 'the other phonegap folder exists'
    test.done()
