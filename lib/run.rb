#
# Run
# ---
#
# A handy machine that does the following:
#
# - runs ant_debug
# - if there is no device attached it will start an emulator with the first avd found
# - runs ant_install
#
class Run
  # if no path is supplied uses current directory for project
  def initialize
    @path = FileUtils.pwd
    build
    start_emulator if first_device.nil?
    install
  end
  
  def build
    Dir.chdir(@path)
    `ant debug`
  end
  
  def install
    Dir.chdir(@path)
    `ant install`
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