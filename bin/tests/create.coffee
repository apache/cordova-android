util = require('util')
exec = require('child_process').exec


exports['default example project is generated'] = (test) ->
  test.expect 1
  exec './bin/create', (error, stdout, stderr) ->
    test.ok true, "this assertion should pass" unless error?
    test.done()

