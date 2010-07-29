package com.phonegap.api;

import android.content.Context;

public interface Command {
	/**
	 * Executes the request and returns JS code to change client state.
	 *
	 * @param action the command to execute
	 * @return a string with JavaScript code or null
	 */
	CommandResult execute(String action, String[] args);

	/**
	 * Determines if this command can process a request.
	 *
	 * @param action the command to execute
	 *
	 * @return true if this command understands the petition
	 */
	boolean accept(String action);

	void setContext(Context ctx);
}
