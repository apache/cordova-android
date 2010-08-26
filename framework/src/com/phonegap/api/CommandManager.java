package com.phonegap.api;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.webkit.WebView;

import com.phonegap.DroidGap;

/**
 * CommandManager is exposed to JavaScript in the PhoneGap WebView.
 * 
 * Calling native plugin code can be done by calling CommandManager.exec(...)
 * from JavaScript.
 * 
 * @author davejohnson
 *
 */
public final class CommandManager {	
	private Command[] commands;
	
	private final Context ctx;
	private final WebView app;
	
	public CommandManager(WebView app, Context ctx) {
		this.ctx = ctx;
		this.app = app;
	}

	/**
	 * Receives a request for execution and fulfills it by finding the appropriate
	 * Java class and calling it's execute method.
	 * 
	 * CommandManager.exec can be used either synchronously or async. In either case, a JSON encoded 
	 * string is returned that will indicate if any errors have occurred when trying to find
	 * or execute the class denoted by the clazz argument.
	 * 
	 * @param clazz String containing the fully qualified class name. e.g. com.phonegap.FooBar
	 * @param action String containt the action that the class is supposed to perform. This is
	 * passed to the plugin execute method and it is up to the plugin developer 
	 * how to deal with it.
	 * @param callbackId String containing the id of the callback that is execute in JavaScript if
	 * this is an async plugin call.
	 * @param args An Array literal string containing any arguments needed in the
	 * plugin execute method.
	 * @param async Boolean indicating whether the calling JavaScript code is expecting an
	 * immediate return value. If true, either PhoneGap.callbackSuccess(...) or PhoneGap.callbackError(...)
	 * is called once the plugin code has executed.
	 * @return JSON encoded string with a response message and status.
	 */
	public String exec(final String clazz, final String action, final String callbackId, 
			final String jsonArgs, final boolean async) {
		CommandResult cr = null;
		try {
			final JSONArray args = new JSONArray(jsonArgs);
			Class c = getClassByName(clazz);
			if (isPhoneGapCommand(c)) {
				// Create a new instance of the plugin and set the context and webview
				final Command plugin = (Command)c.newInstance();
				plugin.setContext(this.ctx);
				plugin.setView(this.app);

				if (async) {
					// Run this on a different thread so that this one can return back to JS
					Thread thread = new Thread(new Runnable() {
						public void run() {
							// Call execute on the plugin so that it can do it's thing
							CommandResult cr = plugin.execute(action, args);
							// Check the status for 0 (success) or otherwise
							if (cr.getStatus() == 0) {
								app.loadUrl(cr.toSuccessCallbackString(callbackId));
							} else {
								app.loadUrl(cr.toErrorCallbackString(callbackId));
							}
						}
					});
					thread.start();
					return "";
				} else {
					// Call execute on the plugin so that it can do it's thing
					cr = plugin.execute(action, args);
				}
			}
		} catch (ClassNotFoundException e) {
			cr = new CommandResult(CommandResult.Status.CLASS_NOT_FOUND_EXCEPTION);
		} catch (IllegalAccessException e) {
			cr = new CommandResult(CommandResult.Status.ILLEGAL_ACCESS_EXCEPTION);
		} catch (InstantiationException e) {
			cr = new CommandResult(CommandResult.Status.INSTANTIATION_EXCEPTION);
		} catch (JSONException e) {
			cr = new CommandResult(CommandResult.Status.JSON_EXCEPTION);
		}
		// if async we have already returned at this point unless there was an error...
		if (async) {
			app.loadUrl(cr.toErrorCallbackString(callbackId));
		}
		return ( cr != null ? cr.getJSONString() : "{ status: 0, message: 'all good' }" );
	}
	
	/**
	 * 
	 * 
	 * @param clazz
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Class getClassByName(final String clazz) throws ClassNotFoundException {
		return Class.forName(clazz);
	}

	/**
	 * Get the interfaces that a class implements and see if it implements the
	 * com.phonegap.api.Command interface.
	 * 
	 * @param c The class to check the interfaces of.
	 * @return Boolean indicating if the class implements com.phonegap.api.Command
	 */
	private boolean isPhoneGapCommand(Class c) {
		boolean isCommand = false;
		Class[] interfaces = c.getInterfaces();
		for (int j=0; j<interfaces.length; j++) {
			if (interfaces[j].getName().equals("com.phonegap.api.Command")) {
				isCommand = true;
				break;
			}
		}
		return isCommand;
	}
}