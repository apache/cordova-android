Tests.prototype.SystemTests = function() {
	module('System Information (navigator.system)');
	test("should exist", function() {
  		expect(1);
  		ok(navigator.system != null, "navigator.system should not be null.");
	});
	test("should contain a get function", function() {
		expect(2);
		ok(typeof navigator.system.get != 'undefined' && navigator.system.get != null, "navigator.system.get should not be null.");
		ok(typeof navigator.system.get == 'function', "navigator.system.get should be a function.");
	});
	test("should contain a has function", function() {
		expect(2);
		ok(typeof navigator.system.has != 'undefined' && navigator.system.has != null, "navigator.system.has should not be null.");
		ok(typeof navigator.system.has == 'function', "navigator.system.has should be a function.");
	});
	test("should contain a monitor function", function() {
		expect(2);
		ok(typeof navigator.system.monitor != 'undefined' && navigator.system.monitor != null, "navigator.system.monitor should not be null.");
		ok(typeof navigator.system.monitor == 'function', "navigator.system.monitor should be a function.");
	});
	module('System Information Options');
	test("should be able to define a SystemInfoOptions object", function() {
		expect(6);
		var systemInfoOptions = new SystemInfoOptions(0.0, 0.0, "a", 0, "b");
		ok(systemInfoOptions != null, "new SystemInfoOptions() should not be null.");
		ok(typeof systemInfoOptions.highThreshold != 'undefined' && systemInfoOptions.highThreshold != null && systemInfoOptions.highThreshold == 0.0, "new SystemInfoOptions() should include a 'highThreshold' property.");
		ok(typeof systemInfoOptions.lowThreshold != 'undefined' && systemInfoOptions.lowThreshold != null && systemInfoOptions.lowThreshold == 0.0, "new SystemInfoOptions() should include a 'lowThreshold' property.");
		ok(typeof systemInfoOptions.thresholdTarget != 'undefined' && systemInfoOptions.thresholdTarget != null && systemInfoOptions.thresholdTarget == "a", "new SystemInfoOptions() should include a 'thresholdTarget' property.");
		ok(typeof systemInfoOptions.timeout != 'undefined' && systemInfoOptions.timeout != null && systemInfoOptions.timeout == 0, "new SystemInfoOptions() should include a 'timeout' property.");
		ok(typeof systemInfoOptions.id != 'undefined' && systemInfoOptions.id != null && systemInfoOptions.id == "b", "new SystemInfoOptions() should include a 'id' property.");
	});	
	module('Power Property');
	test("should be able to define a Power Property object", function() {
		expect(7);
		var power = new PowerAttributes("a","b",0.0,0,true,false);
		ok(power != null, "new PowerAttributes() should not be null.");
		ok(typeof power.info != 'undefined' && power.info != null && power.info == "a", "new PowerAttributes() should include a 'info' property.");
		ok(typeof power.id != 'undefined' && power.id != null && power.id == "b", "new PowerAttributes() should include a 'id' property.");
		ok(typeof power.level != 'undefined' && power.level != null && power.level == 0.0, "new PowerAttributes() should include a 'level' property.");
		ok(typeof power.timeRemaining != 'undefined' && power.timeRemaining != null && power.timeRemaining == 0, "new PowerAttributes() should include a 'timeRemaining' property.");
		ok(typeof power.isBattery != 'undefined' && power.isBattery != null && power.isBattery == true, "new PowerAttributes() should include a 'isBattery' property.");
		ok(typeof power.isCharging != 'undefined' && power.isCharging != null && power.isCharging == false, "new PowerAttributes() should include a 'isCharging' property.");
	});	
	module('CPU Property');
	test("should be able to define a CPU Property object", function() {
		expect(4);
		var cpu = new CPUAttributes("a", "b", 0.0);
		ok(cpu  != null, "new CPUAttributes() should not be null.");
		ok(typeof cpu.info != 'undefined' && cpu.info != null && cpu.info == "a", "new CPUAttributes() should include a 'info' property.");
		ok(typeof cpu.id != 'undefined' && cpu.id != null && cpu.id == "b", "new CPUAttributes() should include a 'id' property.");
		ok(typeof cpu.usage != 'undefined' && cpu.usage != null && cpu.usage == 0.0, "new CPUAttributes() should include a 'usage' property.");
	});	
	module('Thermal Property');
	test("should be able to define a Thermal Property object", function() {
		expect(4);
		var thermal = new ThermalAttributes("a", "b", 0.0);
		ok(thermal  != null, "new ThermalAttributes() should not be null.");
		ok(typeof thermal.info != 'undefined' && thermal.info != null && thermal.info == "a", "new ThermalAttributes() should include a 'info' property.");
		ok(typeof thermal.id != 'undefined' && thermal.id != null && thermal.id == "b", "new ThermalAttributes() should include a 'id' property.");
		ok(typeof thermal.state != 'undefined' && thermal.state != null && thermal.state == 0.0, "new ThermalAttributes() should include a 'state' property.");
	});	
	module('Network Property');
	test("should be able to define a Network Property object", function() {
		expect(4);
		var network = new NetworkAttributes("a", "b", []);
		ok(network  != null, "new NetworkAttributes() should not be null.");
		ok(typeof network.info != 'undefined' && network.info != null && network.info == "a", "new NetworkAttributes() should include a 'info' property.");
		ok(typeof network.id != 'undefined' && network.id != null && network.id == "b", "new NetworkAttributes() should include a 'id' property.");
		ok(typeof network.activeConnections != 'undefined' && network.activeConnections != null, "new NetworkAttributes() should include a 'activeConnections' property.");
	});	
	module('Connection Type Property');
	test("should be able to define a display Type Property object", function() {
		expect(10);
		var connection = new ConnectionAttributes('a', 'b', ConnectionType.UNKNOWN, 0, 0, 0, 0, 0.0, false);
		ok(connection  != null, "new displayAttributes() should not be null.");
		ok(typeof connection.info != 'undefined' && connection.info != null && connection.info == "a", "new ConnectionAttributes() should include a 'info' property.");
		ok(typeof connection.id != 'undefined' && connection.id != null && connection.id == "b", "new ConnectionAttributes() should include a 'id' property.");
		ok(typeof connection.type != 'undefined' && connection.type != null && connection.type == 'unknown', "new ConnectionAttributes() should include a 'type' property.");
		ok(typeof connection.currentDownloadBandwidth != 'undefined' && connection.currentDownloadBandwidth != null && connection.currentDownloadBandwidth == 0, "new ConnectionAttributes() should include a 'currentDownloadBandwidth' property.");
		ok(typeof connection.currentUploadBandwidth != 'undefined' && connection.currentUploadBandwidth != null && connection.currentUploadBandwidth == 0, "new ConnectionAttributes() should include a 'currentUploadBandwidth' property.");
		ok(typeof connection.maxDownloadBandwidth != 'undefined' && connection.maxDownloadBandwidth != null && connection.maxDownloadBandwidth == 0, "new ConnectionAttributes() should include a 'maxDownloadBandwidth' property.");
		ok(typeof connection.maxUploadBandwidth != 'undefined' && connection.maxUploadBandwidth != null && connection.maxUploadBandwidth == 0, "new ConnectionAttributes() should include a 'maxUploadBandwidth' property.");
		ok(typeof connection.currentSignalStrength != 'undefined' && connection.currentSignalStrength != null && connection.currentSignalStrength == 0.0, "new ConnectionAttributes() should include a 'currentSignalStrength' property.");
		ok(typeof connection.roaming != 'undefined' && connection.roaming != null && connection.roaming == false, "new ConnectionAttributes() should include a 'roaming' property.");
	});	
	module('Sensor Property');
	test("should be able to define a Sensor Property object", function() {
		expect(5);
		var sensor = new SensorAttributes(0.0,0.0,0.0,0.0);
		ok(sensor  != null, "new SensorAttributes() should not be null.");
		ok(typeof sensor.value != 'undefined' && sensor.value != null && sensor.value == 0.0, "new SensorAttributes() should include a 'value' property.");
		ok(typeof sensor.min != 'undefined' && sensor.min != null && sensor.min == 0.0, "new SensorAttributes() should include a 'min' property.");
		ok(typeof sensor.max != 'undefined' && sensor.max != null && sensor.max == 0.0, "new SensorAttributes() should include a 'max' property.");
		ok(typeof sensor.normalizedValue != 'undefined' && sensor.normalizedValue != null && sensor.normalizedValue == 0.0, "new SensorAttributes() should include a 'normalizedValue' property.");
	});	
	module('AVCodecs Property');
	test("should be able to define a AVCodecs Property object", function() {
		expect(5);
		var avcodecs = new AVCodecsAttributes("a", "b", [], []);
		ok(avcodecs  != null, "new AVCodecsAttributes() should not be null.");
		ok(typeof avcodecs.info != 'undefined' && avcodecs.info != null && avcodecs.info == "a", "new AVCodecsAttributes() should include a 'info' property.");
		ok(typeof avcodecs.id != 'undefined' && avcodecs.id != null && avcodecs.id == "b", "new AVCodecsAttributes() should include a 'id' property.");
		ok(typeof avcodecs.audioCodecs != 'undefined' && avcodecs.audioCodecs != null, "new AVCodecsAttributes() should include a 'audioCodecs' property.");
		ok(typeof avcodecs.videoCodecs != 'undefined' && avcodecs.videoCodecs != null, "new AVCodecsAttributes() should include a 'videoCodecs' property.");
	});	
	module('Audio Codec Property');
	test("should be able to define a Audio Codec Property object", function() {
		expect(6);
		var codec = new AudioCodecAttributes("a", "b", 'a',true,true);
		ok(codec != null, "new AudioCodecAttributes() should not be null.");
		ok(typeof codec.info != 'undefined' && codec.info != null && codec.info == "a", "new AudioCodecAttributes() should include a 'info' property.");
		ok(typeof codec.id != 'undefined' && codec.id != null && codec.id == "b", "new AudioCodecAttributes() should include a 'id' property.");
		ok(typeof codec.compFormats != 'undefined' && codec.compFormats != null && codec.compFormats == 'a', "new AudioCodecAttributes() should include a 'compFormats' property.");
		ok(typeof codec.encode != 'undefined' && codec.encode != null && codec.encode == true, "new AudioCodecAttributes() should include a 'encode' property.");
		ok(typeof codec.decode != 'undefined' && codec.decode != null && codec.decode == true, "new AudioCodecAttributes() should include a 'decode' property.");
	});	
	module('Video Codec Property');
	test("should be able to define a Video Codec Property object", function() {
		expect(9);
		var codec = new VideoCodecAttributes("a", "b", [],[],[],[],[],[]);
		ok(codec != null, "new VideoCodecAttributes() should not be null.");
		ok(typeof codec.info != 'undefined' && codec.info != null && codec.info == "a", "new VideoCodecAttributes() should include a 'info' property.");
		ok(typeof codec.id != 'undefined' && codec.id != null && codec.id == "b", "new VideoCodecAttributes() should include a 'id' property.");
		ok(typeof codec.compFormats != 'undefined' && codec.compFormats != null, "new VideoCodecAttributes() should include a 'compFormats' property.");
		ok(typeof codec.containerFormats != 'undefined' && codec.containerFormats != null, "new VideoCodecAttributes() should include a 'containerFormats' property.");
		ok(typeof codec.hwAccel != 'undefined' && codec.hwAccel != null, "new VideoCodecAttributes() should include a 'hwAccel' property.");
		ok(typeof codec.profiles != 'undefined' && codec.profiles != null, "new VideoCodecAttributes() should include a 'profiles' property.");
		ok(typeof codec.frameTypes != 'undefined' && codec.frameTypes != null, "new VideoCodecAttributes() should include a 'frameTypes' property.");
		ok(typeof codec.rateTypes != 'undefined' && codec.rateTypes != null, "new VideoCodecAttributes() should include a 'rateTypes' property.");
	});	
	module('Storage Unit Property');
	test("should be able to define a Storage Property object", function() {
		expect(8);
		var storage = new StorageUnitAttributes('a','b',0,true,0,0,true);
		ok(storage != null, "new StorageUnitAttributes() should not be null.");
		ok(typeof storage.info != 'undefined' && storage.info != null && storage.info == "a", "new StorageUnitAttributes() should include a 'info' property.");
		ok(typeof storage.id != 'undefined' && storage.id != null && storage.id == "b", "new StorageUnitAttributes() should include a 'id' property.");
		ok(typeof storage.type != 'undefined' && storage.type != null && storage.type == 0, "new StorageUnitAttributes() should include a 'type' property.");
		ok(typeof storage.isWritable != 'undefined' && storage.isWritable != null && storage.isWritable == true, "new StorageUnitAttributes() should include a 'isWritable' property.");
		ok(typeof storage.capacity != 'undefined' && storage.capacity != null && storage.capacity == 0, "new StorageUnitAttributes() should include a 'capacity' property.");
		ok(typeof storage.availableCapacity != 'undefined' && storage.availableCapacity != null && storage.availableCapacity == 0, "new StorageUnitAttributes() should include a 'availableCapacity' property.");
		ok(typeof storage.isRemoveable != 'undefined' && storage.isRemoveable != null && storage.isRemoveable == true, "new StorageUnitAttributes() should include a 'isRemoveable' property.");
	});	
	module('Output Devices Property');
	test("should be able to define a Input Devices Property object", function() {
		expect(11);
		var output = new OutputDevicesAttributes('a','b',[],[],[],"a",[],"a",[],[]);
		ok(output != null, "new OutputDevicesAttributes() should not be null.");
		ok(typeof output.info != 'undefined' && output.info != null && output.info == "a", "new OutputDevicesAttributes() should include a 'info' property.");
		ok(typeof output.id != 'undefined' && output.id != null && output.id == "b", "new OutputDevicesAttributes() should include a 'id' property.");
		ok(typeof output.displayDevices != 'undefined' && output.displayDevices != null, "new OutputDevicesAttributes() should include a 'displayDevices' property.");
		ok(typeof output.activeDisplayDevices != 'undefined' && output.activeDisplayDevices != null, "new OutputDevicesAttributes() should include a 'activeDisplayDevices' property.");
		ok(typeof output.printingDevices != 'undefined' && output.printingDevices != null, "new OutputDevicesAttributes() should include a 'printingDevices' property.");
		ok(typeof output.activePrintingDevice != 'undefined' && output.activePrintingDevice != null && output.activePrintingDevice == "a", "new OutputDevicesAttributes() should include a 'activePrintingDevice' property.");
		ok(typeof output.brailleDevices != 'undefined' && output.brailleDevices != null, "new OutputDevicesAttributes() should include a 'brailleDevices' property.");
		ok(typeof output.activeBrailleDevice != 'undefined' && output.activeBrailleDevice != null && output.activeBrailleDevice == "a", "new OutputDevicesAttributes() should include a 'activeBrailleDevice' property.");
		ok(typeof output.audioDevices != 'undefined' && output.audioDevices != null, "new OutputDevicesAttributes() should include a 'audioDevices' property.");
		ok(typeof output.activeAudioDevices != 'undefined' && output.activeAudioDevices != null, "new OutputDevicesAttributes() should include a 'activeAudioDevices' property.");
	});	
	module('Display Device Type Property');
	test("should be able to define a Display Device Property object", function() {
		expect(10);
		var display = new DisplayDeviceAttributes(0,0.0,0.0,true,0,0,0.0,0.0,"a");
		ok(display  != null, "new DisplayDeviceAttributes() should not be null.");
		ok(typeof display.orientation != 'undefined' && display.orientation != null && display.orientation == 0, "new DisplayDeviceAttributes() should include a 'orientation' property.");
		ok(typeof display.brightness != 'undefined' && display.brightness != null && display.brightness == 0.0, "new DisplayDeviceAttributes() should include a 'brightness' property.");
		ok(typeof display.contrast != 'undefined' && display.contrast != null && display.contrast == 0.0, "new DisplayDeviceAttributes() should include a 'contrast' property.");
		ok(typeof display.blanked != 'undefined' && display.blanked != null && display.blanked == true, "new DisplayDeviceAttributes() should include a 'blanked' property.");
		ok(typeof display.dotsPerInchW != 'undefined' && display.dotsPerInchW != null && display.dotsPerInchW == 0, "new DisplayDeviceAttributes() should include a 'dotsPerInchW' property.");
		ok(typeof display.dotsPerInchH != 'undefined' && display.dotsPerInchH != null && display.dotsPerInchH == 0, "new DisplayDeviceAttributes() should include a 'dotsPerInchH' property.");
		ok(typeof display.physicalWidth != 'undefined' && display.physicalWidth != null && display.physicalWidth == 0.0, "new DisplayDeviceAttributes() should include a 'physicalWidth' property.");
		ok(typeof display.physicalHeight != 'undefined' && display.physicalHeight != null && display.physicalHeight == 0.0, "new DisplayDeviceAttributes() should include a 'physicalHeight' property.");
		ok(typeof display.info != 'undefined' && display.info != null && display.info == 'a', "new DisplayDeviceAttributes() should include a 'info' property.");
	});	
	module('Audio Device Type Property');
	test("should be able to define a Audio Device Property object", function() {
		expect(6);
		var audio = new AudioDeviceAttributes(0,0,0,0,"a");
		ok(audio  != null, "new AudioDeviceAttributes() should not be null.");
		ok(typeof audio.type != 'undefined' && audio.type != null && audio.type == 0, "new AudioDeviceAttributes() should include a 'type' property.");
		ok(typeof audio.freqRangeLow != 'undefined' && audio.freqRangeLow != null && audio.freqRangeLow == 0, "new AudioDeviceAttributes() should include a 'freqRangeLow' property.");
		ok(typeof audio.freqRangeHigh != 'undefined' && audio.freqRangeHigh != null && audio.freqRangeHigh == 0, "new AudioDeviceAttributes() should include a 'freqRangeHigh' property.");
		ok(typeof audio.volumeLevel != 'undefined' && audio.volumeLevel != null && audio.volumeLevel == 0, "new AudioDeviceAttributes() should include a 'volumeLevel' property.");
		ok(typeof audio.info != 'undefined' && audio.info != null && audio.info == "a", "new AudioDeviceAttributes() should include a 'info' property.");
	});	
	module('Printing Device Type Property');
	test("should be able to define a Printing Device Property object", function() {
		expect(5);
		var printer = new PrintingDeviceAttributes(0,0,0,"a");
		ok(printer  != null, "new PrintingDeviceAttributes() should not be null.");
		ok(typeof printer.type != 'undefined' && printer.type != null && printer.type == 0, "new PrintingDeviceAttributes() should include a 'type' property.");
		ok(typeof printer.resolution != 'undefined' && printer.resolution != null && printer.resolution == 0, "new PrintingDeviceAttributes() should include a 'resolution' property.");
		ok(typeof printer.color != 'undefined' && printer.color != null && printer.color == 0, "new PrintingDeviceAttributes() should include a 'color' property.");
		ok(typeof printer.info != 'undefined' && printer.info != null && printer.info == "a", "new PrintingDeviceAttributes() should include a 'info' property.");
	});	
	module('Braille Device Type Property');
	test("should be able to define a Printing Device Property object", function() {
		expect(3);
		var braille = new BrailleDeviceAttributes(0,"a");
		ok(braille  != null, "new BrailleDeviceAttributes() should not be null.");
		ok(typeof braille.nbCells != 'undefined' && braille.nbCells != null && braille.nbCells == 0, "new BrailleDeviceAttributes() should include a 'nbCells' property.");
		ok(typeof braille.info != 'undefined' && braille.info != null && braille.info == "a", "new BrailleDeviceAttributes() should include a 'info' property.");
	});	
	module('Input Devices Property');
	test("should be able to define a Input Devices Property object", function() {
		expect(11);
		var input = new InputDevicesAttributes('a','b',[],[],[],[],[],[],[],[]);
		ok(input != null, "new InputDevicesAttributes() should not be null.");
		ok(typeof input.info != 'undefined' && input.info != null && input.info == "a", "new InputDevicesAttributes() should include a 'info' property.");
		ok(typeof input.id != 'undefined' && input.id != null && input.id == "b", "new InputDevicesAttributes() should include a 'id' property.");
		ok(typeof input.pointingDevices != 'undefined' && input.pointingDevices != null, "new InputDevicesAttributes() should include a 'pointingDevices' property.");
		ok(typeof input.activePointingDevices != 'undefined' && input.activePointingDevices != null, "new InputDevicesAttributes() should include a 'activePointingDevices' property.");
		ok(typeof input.keyboards != 'undefined' && input.keyboards != null, "new InputDevicesAttributes() should include a 'keyboards' property.");
		ok(typeof input.activeKeyboards != 'undefined' && input.activeKeyboards != null, "new InputDevicesAttributes() should include a 'activeKeyboards' property.");
		ok(typeof input.cameras != 'undefined' && input.cameras != null, "new InputDevicesAttributes() should include a 'cameras' property.");
		ok(typeof input.activeCameras != 'undefined' && input.activeCameras != null, "new InputDevicesAttributes() should include a 'activeCameras' property.");
		ok(typeof input.microphones != 'undefined' && input.microphones != null, "new InputDevicesAttributes() should include a 'microphones' property.");
		ok(typeof input.activeMicrophones != 'undefined' && input.activeMicrophones != null, "new InputDevicesAttributes() should include a 'activeMicrophones' property.");
	});	
	module('Pointer Property');
	test("should be able to define a Pointer Property object", function() {
		expect(4);
		var pointer = new PointerAttributes(0,true,"a");
		ok(pointer  != null, "new PointerAttributes() should not be null.");
		ok(typeof pointer.type != 'undefined' && pointer.type != null && pointer.type == 0, "new PointerAttributes() should include a 'type' property.");
		ok(typeof pointer.supportsMultiTouch != 'undefined' && pointer.supportsMultiTouch != null && pointer.supportsMultiTouch == true, "new PointerAttributes() should include a 'supportsMultiTouch' property.");
		ok(typeof pointer.info != 'undefined' && pointer.info != null && pointer.info == "a", "new PointerAttributes() should include a 'info' property.");
	});	
	module('Keyboard Property');
	test("should be able to define a Keyboard Property object", function() {
		expect(4);
		var keyboard = new KeyboardAttributes(0,true,"a");
		ok(keyboard  != null, "new KeyboardAttributes() should not be null.");
		ok(typeof keyboard.type != 'undefined' && keyboard.type != null && keyboard.type == 0, "new KeyboardAttributes() should include a 'type' property.");
		ok(typeof keyboard.isHardware != 'undefined' && keyboard.isHardware != null && keyboard.isHardware == true, "new KeyboardAttributes() should include a 'isHardware' property.");
		ok(typeof keyboard.info != 'undefined' && keyboard.info != null && keyboard.info == "a", "new KeyboardAttributes() should include a 'info' property.");
	});	
	module('Camera Property');
	test("should be able to define a Camera Property object", function() {
		expect(5);
		var camera = new CameraAttributes(true,true,0,0.0);
		ok(camera  != null, "new CameraAttributes() should not be null.");
		ok(typeof camera.supportsVideo != 'undefined' && camera.supportsVideo != null && camera.supportsVideo == true, "new CameraAttributes() should include a 'supportsVideo' property.");
		ok(typeof camera.hasFlash != 'undefined' && camera.hasFlash != null && camera.hasFlash == true, "new CameraAttributes() should include a 'hasFlash' property.");
		ok(typeof camera.sensorPixels != 'undefined' && camera.sensorPixels != null && camera.sensorPixels == 0, "new CameraAttributes() should include a 'sensorPixels' property.");
		ok(typeof camera.maxZoomFactor != 'undefined' && camera.maxZoomFactor != null && camera.maxZoomFactor == 0.0, "new CameraAttributes() should include a 'maxZoomFactor' property.");
	});	
	module('Microphone Property');
	test("should be able to define a Microphone Property object", function() {
		expect(7);
		var mic = new MicrophoneAttributes(0,0,0,"a","b",[]);
		ok(mic  != null, "new MicrophoneAttributes() should not be null.");
		ok(typeof mic.type != 'undefined' && mic.type != null && mic.type == 0, "new MicrophoneAttributes() should include a 'type' property.");
		ok(typeof mic.freqRangeLow != 'undefined' && mic.freqRangeLow != null && mic.freqRangeLow == 0, "new MicrophoneAttributes() should include a 'freqRangeLow' property.");
		ok(typeof mic.freqRangeHigh != 'undefined' && mic.freqRangeHigh != null && mic.freqRangeHigh == 0, "new MicrophoneAttributes() should include a 'freqRangeHigh' property.");
		ok(typeof mic.info != 'undefined' && mic.info != null && mic.info == "a", "new MicrophoneAttributes() should include a 'info' property.");
		ok(typeof mic.name != 'undefined' && mic.name != null && mic.name == "b", "new MicrophoneAttributes() should include a 'name' property.");
		ok(typeof mic.types != 'undefined' && mic.types != null, "new MicrophoneAttributes() should include a 'types' property.");
	});	
};
