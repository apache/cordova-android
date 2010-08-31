#
# Run
# ---
#
# A handy machine that does the following:
#
# - packages www to a valid android project in tmp/android
# - builds tmp/android project into an apk
# - installs apk onto first device found
# - if there is no device attached it will start an emulator with the first avd found
# - TODO install apk into now running emulator... need to find way to wait for it to have started
# - TODO if no avds present it will attempt to create one
#
class Run
  # if no path is supplied uses current directory for project
  def initialize(path)
    puts 'packaging www as phonegap/android project in ./tmp/android...'
    path = FileUtils.pwd if path.nil? || path == ""
    @pkg = Package.new(path)
    @apk = File.join(@pkg.path, "bin", "#{ @pkg.name.gsub(' ','') }-debug.apk")
    build_apk
    first_device.nil? ? start_emulator : install_to_device
  end
  
  def build_apk
    puts 'building apk...'
    Dir.chdir(@pkg.path)
    `ant debug;`
  end
  
  def install_to_device
    puts 'installing to device...'
    Dir.chdir(@pkg.path)
    `ant install;`
  end 
  
  def start_emulator
    puts "No devices attached. Starting emulator w/ first avd...\n"
    $stdout.sync = true
    avd = first_avd
    if (avd.nil? || avd == "")
      puts "No Android Virtual Device (AVD) could be found. Please create one with the Android SDK."
      return
    end
    IO.popen("emulator -avd #{ avd } -logcat all") do |f|
      until f.eof?
        puts f.gets
        if f.gets.include? 'Boot is finished'
          #IO.popen("cd #{ @pkg.path }; ant install;") do |f|
          #  puts f.gets
          #end 
          puts "\n\nEMULATOR IS NOW RUNNING!\n\n"
          puts "install your app by running: "
          puts "cd #{ @pkg.path }; ant install;"
        end 
      end
    end
  end 
  
  # helpers
  def first_device
    fd = `adb devices`.split("\n").pop()
    if fd == 'List of devices attached '
      nil
    else
      fd.gsub('device','')
    end 
  end
  
  def first_avd
    `android list avd | grep "Name: "`.gsub('Name: ','').strip
  end

  #
end