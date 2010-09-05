# Create
# 
# Generates an Android project from a valid WWW directory and puts it in ../[PROJECT NAME]_android
#
class Create < Classic
  def initialize(path)
    guess_paths(path)
    read_config
    build
  end
  
  def guess_paths(path)
    # if no path is supplied uses current directory for project
    path = FileUtils.pwd if path.nil?
    
    # if a www is found use it for the project
    path = File.join(path, 'www') if File.exists? File.join(path, 'www')
    
    # defaults
    @name             = path.split("/").last.gsub('-','').gsub(' ','') # no dashses nor spaces
    @path             = File.join(path, '..', "#{ @name }_android")
    @www              = path 
    @pkg              = "com.phonegap.#{ @name }" 
    @android_sdk_path = Dir.getwd[0,1] != "/" ? `android-sdk-path.bat android.bat`.gsub('\\tools','').gsub('\\', '\\\\\\\\') : `which android`.gsub('/tools/android','')
    @android_dir      = File.expand_path(File.dirname(__FILE__).gsub('lib',''))
    @framework_dir    = File.join(@android_dir, "framework")
    @icon             = File.join(@www, 'icon.png')
    @app_js_dir       = ''
    @content          = 'index.html'
    
    # stop executation on errors
    raise 'No index.html found!' unless File.exists? File.join(path, 'index.html')    
    raise 'Could not find android in your path!' if @android_sdk_path.empty?
  end

  # reads in a config.xml file
  def read_config
    config_file = File.join(@www, 'config.xml')
    
    if File.exists?(config_file)
      require 'rexml/document'
      f                 = File.new config_file
      doc               = REXML::Document.new(f)  
      @config           = {}  
      @config[:id]      = doc.root.attributes["id"]
      @config[:version] = doc.root.attributes["version"]
      
      doc.root.elements.each do |n|
        @config[:name]        = n.text if n.name == 'name'
        @config[:description] = n.text if n.name == 'description'
        @config[:icon]        = n.attributes["src"] if n.name == 'icon'
        @config[:content]     = n.attributes["src"] if n.name == 'content'  
        
        if n.name == "preference" && n.attributes["name"] == 'javascript_folder'
          @config[:js_dir] = n.attributes["value"]
        end 
      end 
      
      # extract android specific stuff
      @config[:versionCode] = doc.elements["//android:versionCode"] ? doc.elements["//android:versionCode"].text : 3
      @config[:minSdkVersion] = doc.elements["//android:minSdkVersion"] ? doc.elements["//android:minSdkVersion"].text : 1
      # will change the name from the directory to the name element text
      @name = @config[:name] if @config[:name]
      # set the icon from the config
      @icon = File.join(@www, @config[:icon])
      # sets the app js dir where phonegap.js gets copied
      @app_js_dir = @config[:js_dir] ? @config[:js_dir] : ''
      # sets the start page
      @content = @config[:content] ? @config[:content] : 'index.html'
    end     
  end 
end
