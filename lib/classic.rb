class Classic
  attr_reader :android_sdk_path, :name, :pkg, :www, :path

  def initialize(a)
    @android_sdk_path, @name, @pkg, @www, @path = a
    build
  end
  
  def build
    setup
    clobber
    build_jar
    create_android
    include_www
    generate_manifest
    copy_libs
    add_name_to_strings
    write_java
  end 
  
  def setup
    @android_dir    = File.expand_path(File.dirname(__FILE__).gsub('lib',''))
    @framework_dir  = File.join(@android_dir, "framework")
    @icon           = File.join(@www, 'icon.png')
    @app_js_dir     = ''
    @content        = 'index.html'
  end
  
  # replaces @path with new android project
  def clobber
    FileUtils.rm_r(@path) if File.exists? @path
    FileUtils.mkdir_p @path
  end
  
  # removes local.properties and recreates based on android_sdk_path 
  # then generates framework/phonegap.jar
  def build_jar
    %w(local.properties phonegap.js phonegap.jar).each do |f|
      FileUtils.rm File.join(@framework_dir, f) if File.exists? File.join(@framework_dir, f)
    end
    open(File.join(@framework_dir, "local.properties"), 'w') do |f|
      f.puts "sdk.dir=#{ @android_sdk_path }"
    end 
    Dir.chdir(@framework_dir)
    `ant jar`
    Dir.chdir(@android_dir)
  end

  # runs android create project
  # TODO need to allow more flexible SDK targetting via config.xml
  def create_android
    IO.popen("android list targets") { |f| 
      targets = f.readlines(nil)[0].scan(/id\:.*$/)
      if (targets.length > 0) 
        target_id = targets.last.match(/\d+/).to_a.first
        `android create project -t #{ target_id } -k #{ @pkg } -a #{ @name } -n #{ @name } -p #{ @path }`
      else
        puts "No Android targets found. Please run 'android' and install at least one SDK package."
        puts "If that makes no sense then you need to go read the Android SDK documentation."
      end
    }
  end
  
  # copies the project/www folder into tmp/android/www
  def include_www
    FileUtils.mkdir_p File.join(@path, "assets", "www")
    FileUtils.cp_r File.join(@www, "."), File.join(@path, "assets", "www")
  end

  # creates an AndroidManifest.xml for the project
  def generate_manifest
    manifest = ""
    open(File.join(@framework_dir, "AndroidManifest.xml"), 'r') do |old|
      manifest = old.read
      manifest.gsub! 'android:versionCode="5"', 'android:versionCode="1"'
      manifest.gsub! 'package="com.phonegap"', "package=\"#{ @pkg }\""
      manifest.gsub! 'android:name=".StandAlone"', "android:name=\".#{ @name.gsub(' ','') }\""
      manifest.gsub! 'android:minSdkVersion="5"', 'android:minSdkVersion="3"'
    end
    open(File.join(@path, "AndroidManifest.xml"), 'w') { |x| x.puts manifest }
  end

  # copies stuff from src directory into the android project directory (@path)
  def copy_libs
    framework_res_dir = File.join(@framework_dir, "res")
    app_res_dir = File.join(@path, "res")
    # copies in the jar
    FileUtils.mkdir_p File.join(@path, "libs")
    FileUtils.cp File.join(@framework_dir, "phonegap.jar"), File.join(@path, "libs")
    # copies in the strings.xml
    FileUtils.mkdir_p File.join(app_res_dir, "values")
    FileUtils.cp File.join(framework_res_dir, "values","strings.xml"), File.join(app_res_dir, "values", "strings.xml")
    # drops in the layout files: main.xml and preview.xml
    FileUtils.mkdir_p File.join(app_res_dir, "layout")
    %w(main.xml).each do |f|
      FileUtils.cp File.join(framework_res_dir, "layout", f), File.join(app_res_dir, "layout", f)
    end
    # icon file copy
    # if it is not in the www directory use the default one in the src dir
    @icon = File.join(framework_res_dir, "drawable", "icon.png") unless File.exists?(@icon)
    %w(drawable-hdpi drawable-ldpi drawable-mdpi).each do |e|
      FileUtils.mkdir_p(File.join(app_res_dir, e))
      FileUtils.cp(@icon, File.join(app_res_dir, e, "icon.png"))
    end
    # concat JS and put into www folder. this can be overridden in the config.xml via @app_js_dir
    js_dir = File.join(@framework_dir, "assets", "js")
    phonegapjs = IO.read(File.join(js_dir, 'phonegap.js.base'))
    Dir.new(js_dir).entries.each do |script|
      next if script[0].chr == "." or script == "phonegap.js.base"
      phonegapjs << IO.read(File.join(js_dir, script))
      phonegapjs << "\n\n"
    end
    File.open(File.join(@path, "assets", "www", @app_js_dir, "phonegap.js"), 'w') {|f| f.write(phonegapjs) }
  end
  
  # puts app name in strings
  def add_name_to_strings
    x = "<?xml version=\"1.0\" encoding=\"utf-8\"?>
    <resources>
      <string name=\"app_name\">#{ @name }</string>
      <string name=\"go\">Snap</string>
    </resources>
    "
    open(File.join(@path, "res", "values", "strings.xml"), 'w') do |f|
      f.puts x.gsub('    ','')
    end 
  end 

  # this is so fucking unholy yet oddly beautiful
  # not sure if I should thank Ruby or apologize for this abusive use of string interpolation
  def write_java
    j = "
    package #{ @pkg };

    import android.app.Activity;
    import android.os.Bundle;
    import com.phonegap.*;

    public class #{ @name.gsub(' ','') } extends DroidGap
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            super.loadUrl(\"file:///android_asset/www/#{ @content }\");
        }
    }
    "
    code_dir = File.join(@path, "src", @pkg.gsub('.', File::SEPARATOR))
    FileUtils.mkdir_p(code_dir)
    open(File.join(code_dir, "#{ @name }.java"),'w') { |f| f.puts j }
  end
  
  # friendly output for now
  def msg
    puts "Created #{ @path }"
  end
  #
end
