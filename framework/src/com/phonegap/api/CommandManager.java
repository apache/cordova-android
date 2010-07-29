package com.phonegap.api;

import android.content.Context;
import android.webkit.WebView;

import com.phonegap.DroidGap;

/**
 * Given a execution request detects matching {@link Command} and executes it.
 */
public final class CommandManager {
	private static final String EXCEPTION_PREFIX = "[PhoneGap] *ERROR* Exception executing command [";
	private static final String EXCEPTION_SUFFIX = "]: ";
	
	private Command[] commands;
	
	private final Context ctx;
	private final WebView app;
	
	public CommandManager(WebView app, Context ctx) {
		this.ctx = ctx;
		this.app = app;
	}

	/**
	 * Receives a request for execution and fulfills it as long as one of
	 * the configured {@link Command} can understand it. Command precedence
	 * is important (just one of them will be executed).
	 *
	 * @param instruction any API command
	 * @return JS code to execute by the client or null
	 */
	public String exec(final String clazz, final String action, final String callbackId, 
			final String args, final boolean async) {
		CommandResult cr = null;
		try {
			//final WebView wv = this.app;
			final String _callbackId = callbackId;
			final String[] aargs = args.split("__PHONEGAP__");
			Class c = Class.forName(clazz);
			Class[] interfaces = c.getInterfaces();
			for (int j=0; j<interfaces.length; j++) {
				if (interfaces[j].getName().equals("com.phonegap.api.Command")) {
					final Command ci = (Command)c.newInstance();
					ci.setContext(this.ctx);
					if (async) {
						// Run this async on the UI thread
						app.post(new Runnable() {
							public void run() {
								CommandResult cr = ci.execute(action, aargs);
								if (cr.getStatus() == 0) {
									app.loadUrl("javascript:PhoneGap.callbackSuccess('"+callbackId+"', " + cr.getResult()+ ");");
								} else {
									app.loadUrl("javascript:PhoneGap.callbackFailure('"+callbackId+"', " + cr.getResult() + ");");
								}
							}
						});
						return "";
					} else {
						cr = ci.execute(action, aargs);
					}
				}
			}
		} catch (ClassNotFoundException e) {
			cr = new CommandResult(CommandResult.Status.CLASSNOTFOUNDEXCEPTION, 
					"{ message: 'ClassNotFoundException', status: "+CommandResult.Status.CLASSNOTFOUNDEXCEPTION.ordinal()+" }");
		} catch (IllegalAccessException e) {
			cr = new CommandResult(CommandResult.Status.ILLEGALACCESSEXCEPTION, 
					"{ message: 'IllegalAccessException', status: "+CommandResult.Status.ILLEGALACCESSEXCEPTION.ordinal()+" }");
		} catch (InstantiationException e) {
			cr = new CommandResult(CommandResult.Status.INSTANTIATIONEXCEPTION, 
					"{ message: 'InstantiationException', status: "+CommandResult.Status.INSTANTIATIONEXCEPTION.ordinal()+" }");
		}
		// if async we have already returned at this point unless there was an error...
		if (async) {
			app.loadUrl("javascript:PhoneGap.callbackFailure('"+callbackId+"', " + cr.getResult() + ");");
		}
		return ( cr != null ? cr.getResult() : "{ status: 0, message: 'all good' }" );
	}
}