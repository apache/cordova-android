    var deviceInfo = function(){      
      document.getElementById("platform").innerHTML = device.platform;
      document.getElementById("version").innerHTML = device.version;
      document.getElementById("uuid").innerHTML = device.uuid;
      document.getElementById("name").innerHTML = device.name;
      document.getElementById("width").innerHTML = screen.width;
      document.getElementById("height").innerHTML = screen.height;
      document.getElementById("colorDepth").innerHTML = screen.colorDepth;
    };
    
    var getLocation = function() {
      var suc = function(p){
		    alert(p.coords.latitude + " " + p.coords.longitude);
      };
      var fail = function(){};
      navigator.geolocation.getCurrentPosition(suc,fail);
    };
    
    var beep = function(){
	    navigator.notification.beep(2);
    };
    
  	var vibrate = function(){
  	  navigator.notification.vibrate(0);
  	};

    function roundNumber(num) {
      var dec = 3;
      var result = Math.round(num*Math.pow(10,dec))/Math.pow(10,dec);
      return result;
    }
    
    var accelerationWatch = false;
    
    var toggleAccel = function() {
    	if (accelerationWatch) {
    		navigator.accelerometer.clearWatch(accelerationWatch);
    		updateAcceleration( {
    			x : "",
    			y : "",
    			z : ""
    		});
    		accelerationWatch = false;
    	} else {
    		accelerationWatch = true;
    		var options = new Object();
    		options.frequency = 1000;
    		accelerationWatch = navigator.accelerometer.watchAcceleration(
    				updateAcceleration, function(ex) {
    					navigator.accelerometer.clearWatch(accel_watch_id);
    					alert("accel fail (" + ex.name + ": " + ex.message + ")");
    				}, options);
    	}
    };

    function updateAcceleration(a) {
		document.getElementById('x').innerHTML = roundNumber(a.x);
		document.getElementById('y').innerHTML = roundNumber(a.y);
		document.getElementById('z').innerHTML = roundNumber(a.z);
    }
    
	var preventBehavior = function(e) { 
      e.preventDefault(); 
    };

    function show_pic()
    {
      var viewport = document.getElementById('viewport');
      viewport.style.display = "";
      navigator.camera.getPicture(dump_pic, fail, { quality: 50 }); 
    }

    function dump_pic(data)
    {
      var viewport = document.getElementById('viewport');
      console.log(data);
      viewport.style.display = "";
      viewport.style.position = "absolute";
      viewport.style.top = "10px";
      viewport.style.left = "10px";
      document.getElementById("test_img").src = "data:image/jpeg;base64," + data;
    }

    function close()
    {
      var viewport = document.getElementById('viewport');
      viewport.style.position = "relative";
      viewport.style.display = "none";
    }

    function fail(fail)
    {
      alert(fail);
    }

  	// This is just to do this.
  	function readFile()
  	{  	
   		navigator.file.read('/sdcard/phonegap.txt', fail , fail);
  	}

  	function writeFile()
  	{
  	  	navigator.file.write('foo.txt', "This is a test of writing to a file", fail, fail);
  	}

	function get_contacts()
	{
		var obj = new ContactFindOptions();
		obj.filter="";
		obj.multiple=true;
		obj.limit=5;
		navigator.service.contacts.find(["displayName", "phoneNumbers", "emails"], contacts_success, fail, obj);
	}

	function contacts_success(contacts)
	{		
		alert(contacts.length + ' contacts returned.' + 
				(contacts[2] ? (' Third contact is ' + contacts[2].displayName) : ''));
	}
	
  	
	function init(){
		//the next line makes it impossible to see Contacts on the HTC Evo since it doesn't have a scroll button
//		document.addEventListener("touchmove", preventBehavior, false);  
		document.addEventListener("deviceready", deviceInfo, true);		
	}	
