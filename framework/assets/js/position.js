/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */

/**
 * This class contains position information.
 * @param {Object} lat
 * @param {Object} lng
 * @param {Object} acc
 * @param {Object} alt
 * @param {Object} altacc
 * @param {Object} head
 * @param {Object} vel
 * @constructor
 */
function Position(coords, timestamp) {
	this.coords = coords;
        this.timestamp = new Date().getTime();
}

function Coordinates(lat, lng, alt, acc, head, vel, altacc) {
	/**
	 * The latitude of the position.
	 */
	this.latitude = lat;
	/**
	 * The longitude of the position,
	 */
	this.longitude = lng;
	/**
	 * The accuracy of the position.
	 */
	this.accuracy = acc;
	/**
	 * The altitude of the position.
	 */
	this.altitude = alt;
	/**
	 * The direction the device is moving at the position.
	 */
	this.heading = head;
	/**
	 * The velocity with which the device is moving at the position.
	 */
	this.speed = vel;
	/**
	 * The altitude accuracy of the position.
	 */
	this.altitudeAccuracy = (altacc != 'undefined') ? altacc : null; 
}

/**
 * This class specifies the options for requesting position data.
 * @constructor
 */
function PositionOptions() {
	/**
	 * Specifies the desired position accuracy.
	 */
	this.enableHighAccuracy = true;
	/**
	 * The timeout after which if position data cannot be obtained the errorCallback
	 * is called.
	 */
	this.timeout = 10000;
}

/**
 * This class contains information about any GSP errors.
 * @constructor
 */
function PositionError() {
	this.code = null;
	this.message = "";
}

PositionError.UNKNOWN_ERROR = 0;
PositionError.PERMISSION_DENIED = 1;
PositionError.POSITION_UNAVAILABLE = 2;
PositionError.TIMEOUT = 3;
