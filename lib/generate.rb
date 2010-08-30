# ProjectName
# |
# |-tmp ......... Temporary directory for generated projects to launch into emulators or devices. Ignore.
# | '-android ... A generated Android project. 
# |
# |-opt ......... Optional platform specific code. Plugins install native code here.
# | |-android ... Java files
# | '-iphone .... Objective C
# |
# |-bin ......... Generated applications. 
# | '-android ... project.apk
# |
# '-www ......... html, css and javascript (optional config.xml for additional properties)
#
class Generate
  def initialize(name)
    if name.nil?
      puts "You need to supply a name to generate a project. Try this:\n\ndroidgap gen MyApp\n\n"
      return
    end
    from = File.join ROOT, "example"
    to = File.join FileUtils.pwd, name
    FileUtils.cp_r from, to
  end
end