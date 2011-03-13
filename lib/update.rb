#!/usr/bin/env ruby
require 'fileutils'

class Update
  attr_reader :android_sdk_path, :path
  
  def initialize
    @path             = FileUtils.pwd
    @android_sdk_path = Dir.getwd[0,1] != "/" ? `android-sdk-path.bat android.bat`.gsub('\\tools','').gsub('\\', '\\\\\\\\') : `which android`.gsub('/tools/android','')
    @android_dir      = File.expand_path(File.dirname(__FILE__))
    @framework_dir    = File.join(@android_dir, "..", "framework")
    # puts "updating #{ @path } with phonegap from #{ @android_dir }"
    build_jar
    copy_libs
  end
  
  # removes local.properties and recreates based on android_sdk_path 
  # then generates framework/phonegap.jar & framework/assets/www/phonegap.js
  def build_jar
    puts "Building the JAR and combining JS files..."
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
  
  # copies stuff from framework into the project
  # TODO need to allow for www import inc icon
  def copy_libs
    puts "Copying over libraries and assets..."
    
    FileUtils.mkdir_p File.join(@path, "libs")
    FileUtils.cp File.join(@framework_dir, "phonegap.jar"), File.join(@path, "libs")

    FileUtils.mkdir_p File.join(@path, "assets", "www")
    FileUtils.cp File.join(@framework_dir, "assets", "www", "phonegap.js"), File.join(@path, "assets", "www")
  end
  #
end