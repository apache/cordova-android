require 'rubygems'
require 'nokogiri'
require 'fileutils'

class Build
  attr_reader :path
  attr_reader :name
  attr_reader :package_name
  attr_reader :www_dir

  def start(name, pkg_name, www, path)
    create_android(name, pkg_name, path)
    @www_dir = www
    generate_manifest
    copy_libs
    write_java
  end

  def create_android(name, pkg_name, path)
    @name = name
    @pkg_name = pkg_name
    @path = path
    `android create project -t 5 -k #{pkg_name} -a #{name} -n #{name} -p #{path}`
  end

  def generate_manifest  
    f = File.open('framework/AndroidManifest.xml', 'r')
    doc = Nokogiri::XML(f.read)
    manifest = doc.search('//manifest')
    manifest[0]['package'] = @pkg_name
    actions = doc.search('//activity')
    actions[0]['android:name'] = ".#{@name}"
    actions[1]['android:name'] = "com.phonegap.CameraPreview"
    f = File.open("#{@path}/AndroidManifest.xml", 'w')
    f.write(doc.to_xml)
  end

  def copy_libs
    FileUtils.cp('framework/phonegap.jar', "#{@path}/libs")
    FileUtils.cp('framework/res/values/strings.xml', "#{@path}/res/values/strings.xml")
    FileUtils.mkdir_p("#{@path}/res/drawable/")
    FileUtils.mkdir_p("#{@path}/assets")
    FileUtils.cp_r("#{@www_dir}/", "#{@path}/assets/www")
    FileUtils.cp("#{@www_dir}/icon.png", "#{@path}/res/drawable/icon.png")
  end

  def write_java
    package_path = "#{@path}/src/" + @pkg_name.gsub('.', '/')
    doc = File.open("#{package_path}/#{@name}.java", 'r')
    data = doc.read.split(/\n/)
    result = ""
    data.each do |line|
      if line.include? "android.os.Bundle"
        line += "\n\nimport com.phonegap.*;"
      end
      if line.include? "extends Activity"
        line = "public class #{@name} extends DroidGap"
      end
      if line.include? "setContentView"
        line = "        super.loadUrl(\"file:///android_asset/www/index.html\");"
      end
      result += line + "\n"
    end
    doc.close
    package_path = "#{@path}/src/" + @pkg_name.gsub('.', '/') 
    target = File.open(package_path + "/#{@name}.java", 'w')
    target.write(result);
  end

end


b = Build.new

if(ARGV.length >= 3)
  b.start(ARGV[0], ARGV[1], ARGV[2], ARGV[3])  
else
  str = "Android PhoneGap Build Tool \n Usage: build <name> <package_name> <wwwdir> <path> \n name: The name of your application \n package_name: The name of your package: i.e. com.nitobi.demo \n wwwdir: The name of your Web App \n path: Location of where you want to work on your application"
  puts str
end
