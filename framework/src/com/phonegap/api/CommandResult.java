package com.phonegap.api;

import java.net.URLEncoder;

public class CommandResult {
	private final int status;
	private final String message;
	
	public CommandResult(Status status) {
		this.status = status.ordinal();
		this.message = CommandResult.StatusMessages[this.status];
	}
	
	public CommandResult(Status status, String message) {
		this.status = status.ordinal();
		this.message = message;
	}

	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}
	
	public String getJSONString() {
		return "{ status: " + this.getStatus() + ", message: '" + URLEncoder.encode(this.getMessage()) + "' }";
	}
	
	public String toSuccessCallbackString(String callbackId) {
		return "javascript:PhoneGap.callbackSuccess('"+callbackId+"', " + this.getJSONString() + " );";
	}
	
	public String toErrorCallbackString(String callbackId) {
		return "javascript:PhoneGap.callbackError('"+callbackId+"', " + this.getJSONString()+ ");";
	}
	
	public static String[] StatusMessages = new String[] {
		"OK",
		"Class not found",
		"Illegal access",
		"Instantiation error",
		"Malformed url",
		"IO error",
		"Invalid action",
		"JSON error"
	};
	
	public enum Status {
		OK,
		CLASS_NOT_FOUND_EXCEPTION,
		ILLEGAL_ACCESS_EXCEPTION,
		INSTANTIATION_EXCEPTION,
		MALFORMED_URL_EXCEPTION,
		IO_EXCEPTION,
		INVALID_ACTION,
		JSON_EXCEPTION
	}
}
