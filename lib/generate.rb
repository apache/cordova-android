# Creates a new PhoneGap/Android project from ./example
class Generate
  def initialize(name)
    if name.nil?
      puts "You need to supply a name to generate a project. Try this:\n\ndroidgap gen MyApp\n\n"
      return
    end
    from = File.join ROOT, "example"
    to = File.join FileUtils.pwd, name
    FileUtils.cp_r from, to
    puts "Generated #{ to }"
  end
end