package com.phonegap.api;

public class CommandResult {
	private final int status;
	private final String result;
	
	public CommandResult(Status status, String result) {
		this.status = status.ordinal();
		this.result = result;
	}

	public int getStatus() {
		return status;
	}

	public String getResult() {
		return result;
	}
	
	public String toSuccessCallbackString(String callbackId) {
		return "javascript:PhoneGap.callbackSuccess('"+callbackId+"', " + this.getResult()+ ");";
	}
	
	public String toErrorCallbackString(String callbackId) {
		return "javascript:PhoneGap.callbackError('"+callbackId+"', " + this.getResult()+ ");";
	}
	
	public enum Status {
		OK,
		CLASSNOTFOUNDEXCEPTION,
		ILLEGALACCESSEXCEPTION,
		INSTANTIATIONEXCEPTION,
		MALFORMEDURLEXCEPTION,
		IOEXCEPTION,
		INVALIDACTION,
		JSONEXCEPTION
	}
}
