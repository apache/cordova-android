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
	
	public enum Status {
		OK,
		CLASSNOTFOUNDEXCEPTION,
		ILLEGALACCESSEXCEPTION,
		INSTANTIATIONEXCEPTION,
		MALFORMEDURLEXCEPTION,
		IOEXCEPTION,
		INVALIDACTION
	}
}
