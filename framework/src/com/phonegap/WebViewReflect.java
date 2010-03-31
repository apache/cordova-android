package com.phonegap;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.webkit.WebSettings;

public class WebViewReflect {
	   private static Method mWebSettings_setDatabaseEnabled;
	   private static Method mWebSettings_setDatabasePath;
	   private static Method mWebSettings_setDomStorageEnabled;
	   private static Method mWebSettings_setGeolocationEnabled;
	   
	   static 
	   {
		   checkCompatibility();
	   }
	   
	   private static void setDatabaseEnabled(boolean e) throws IOException {
		   try
		   {
			   mWebSettings_setDatabaseEnabled.invoke(e);
		   }
		   catch (InvocationTargetException ite) {
	           /* unpack original exception when possible */
	           Throwable cause = ite.getCause();
	           if (cause instanceof IOException) {
	               throw (IOException) cause;
	           } else if (cause instanceof RuntimeException) {
	               throw (RuntimeException) cause;
	           } else if (cause instanceof Error) {
	               throw (Error) cause;
	           } else {
	               /* unexpected checked exception; wrap and re-throw */
	               throw new RuntimeException(ite);
	           }
	       } catch (IllegalAccessException ie) {
	           System.err.println("unexpected " + ie);
	       }	   
	   }	   
	   
	   
	   public static void checkCompatibility() {
	       try {
	           mWebSettings_setDatabaseEnabled = WebSettings.class.getMethod(
	                   "setDatabaseEnabled", new Class[] { boolean.class } );
	           mWebSettings_setDatabasePath = WebSettings.class.getMethod(
	        		   "setDatabasePath", new Class[] { String.class });
	           mWebSettings_setDomStorageEnabled = WebSettings.class.getMethod(
	        		   "setDomStorageEnabled", new Class[] { boolean.class });
	           mWebSettings_setGeolocationEnabled = WebSettings.class.getMethod(
	        		   "setGeolocationEnabled", new Class[] { boolean.class });
	           /* success, this is a newer device */
	       } catch (NoSuchMethodException nsme) {
	           /* failure, must be older device */
	       }
	   }

	   public static void setStorage(WebSettings setting, boolean enable, String path) {
	       if (mWebSettings_setDatabaseEnabled != null) {
	           /* feature is supported */
	    	   try {
				mWebSettings_setDatabaseEnabled.invoke(setting, enable);
				mWebSettings_setDatabasePath.invoke(setting, path);
	    	   } catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	       } else {
	           /* feature not supported, do something else */
	       }
	   }
	   public static void setGeolocationEnabled(WebSettings setting, boolean enable) {
		   if (mWebSettings_setGeolocationEnabled != null) {
			   /* feature is supported */
			   try {
				mWebSettings_setGeolocationEnabled.invoke(setting, enable);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   } else {
			   /* feature not supported, do something else */
	           System.out.println("Native Geolocation not supported - we're ok");
		   }
	   }
	   public static void setDomStorage(WebSettings setting)
	   {
		   if(mWebSettings_setDomStorageEnabled != null)
		   {
			     /* feature is supported */
	    	   try {
	    		   mWebSettings_setDomStorageEnabled.invoke(setting, true);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	       } else {
	           /* feature not supported, do something else */
	       }
			   
	   }
}
