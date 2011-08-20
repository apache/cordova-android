util = require('util')
exec = require('child_process').exec

exports['you are sane'] = (test) ->
  test.expect 1
  test.ok true, "this assertion should pass"
  test.done()

exports['default example project is generated'] = (test) ->
  test.expect 1
  exec './bin/create', (error, stdout, stderr) ->
      #console.log 'stdout: ' + stdout if stdout?
      #console.log 'stderr: ' + stderr if stderr?
      #console.log 'exec error: ' + error if error?
      test.ok true, "this assertion should pass" unless error?
      test.done()

