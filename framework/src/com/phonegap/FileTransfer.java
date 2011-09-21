/*
 * PhoneGap is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 * 
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */
package com.phonegap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;
import android.webkit.CookieManager;

import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;

public class FileTransfer extends Plugin {

	private static final String LOG_TAG = "FileUploader";
	private static final String LINE_START = "--";
	private static final String LINE_END = "\r\n";
	private static final String BOUNDRY =  "*****";

	public static int FILE_NOT_FOUND_ERR = 1;
    public static int INVALID_URL_ERR = 2;
    public static int CONNECTION_ERR = 3;
    
    private SSLSocketFactory defaultSSLSocketFactory = null;
    private HostnameVerifier defaultHostnameVerifier = null;

	/* (non-Javadoc)
	* @see com.phonegap.api.Plugin#execute(java.lang.String, org.json.JSONArray, java.lang.String)
	*/
	@Override
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		String file = null;
		String server = null;
		try {
			file = args.getString(0);
			server = args.getString(1);
		}
		catch (JSONException e) {
			Log.d(LOG_TAG, "Missing filename or server name");
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION, "Missing filename or server name");
		}
		
		// Setup the options
		String fileKey = null;
		String fileName = null;
		String mimeType = null;		
		
		fileKey = getArgument(args, 2, "file");		
		fileName = getArgument(args, 3, "image.jpg");
		mimeType = getArgument(args, 4, "image/jpeg");

		try {
			JSONObject params = args.optJSONObject(5);
			boolean trustEveryone = args.optBoolean(6);
			boolean chunkedMode = args.getBoolean(7);

			if (action.equals("upload")) {
				FileUploadResult r = upload(file, server, fileKey, fileName, mimeType, params, trustEveryone, chunkedMode);
				Log.d(LOG_TAG, "****** About to return a result from upload");
				return new PluginResult(PluginResult.Status.OK, r.toJSONObject());
			} else {
	            return new PluginResult(PluginResult.Status.INVALID_ACTION);
			}
		} catch (FileNotFoundException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			JSONObject error = createFileUploadError(FILE_NOT_FOUND_ERR);
            return new PluginResult(PluginResult.Status.IO_EXCEPTION, error);
        } catch (IllegalArgumentException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			JSONObject error = createFileUploadError(INVALID_URL_ERR);
            return new PluginResult(PluginResult.Status.IO_EXCEPTION, error);
		} catch (SSLException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			Log.d(LOG_TAG, "Got my ssl exception!!!");
			JSONObject error = createFileUploadError(CONNECTION_ERR);
            return new PluginResult(PluginResult.Status.IO_EXCEPTION, error);
        } catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			JSONObject error = createFileUploadError(CONNECTION_ERR);
            return new PluginResult(PluginResult.Status.IO_EXCEPTION, error);
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
	}

	// always verify the host - don't check for certificate
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
        }
	};

	/**
	 * This function will install a trust manager that will blindly trust all SSL 
	 * certificates.  The reason this code is being added is to enable developers 
	 * to do development using self signed SSL certificates on their web server.
	 * 
	 * The standard HttpsURLConnection class will throw an exception on self 
	 * signed certificates if this code is not run.
	 */
	private void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            	return new java.security.cert.X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                            String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                            String authType) throws CertificateException {
            }
        } };

        // Install the all-trusting trust manager
        try {
        	// Backup the current SSL socket factory
        	defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        	// Install our all trusting manager
        	SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
	}

	/**
	 * Create an error object based on the passed in errorCode
	 * @param errorCode 	the error 
	 * @return JSONObject containing the error
	 */
	private JSONObject createFileUploadError(int errorCode) {
		JSONObject error = null;
		try {
			error = new JSONObject();
			error.put("code", errorCode);
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		}
		return error;
	}

	/**
	 * Convenience method to read a parameter from the list of JSON args.
	 * @param args			the args passed to the Plugin
	 * @param position		the position to retrieve the arg from
	 * @param defaultString the default to be used if the arg does not exist
	 * @return String with the retrieved value
	 */
	private String getArgument(JSONArray args, int position, String defaultString) {
		String arg = defaultString;
		if(args.length() >= position) {
			arg = args.optString(position);
			if (arg == null || "null".equals(arg)) {
				arg = defaultString;
			}
		}
		return arg;
	}
	
    /**
     * Uploads the specified file to the server URL provided using an HTTP 
     * multipart request. 
     * @param file      Full path of the file on the file system
     * @param server        URL of the server to receive the file
     * @param fileKey       Name of file request parameter
     * @param fileName      File name to be used on server
     * @param mimeType      Describes file content type
     * @param params        key:value pairs of user-defined parameters
     * @return FileUploadResult containing result of upload request
     */
	public FileUploadResult upload(String file, String server, final String fileKey, final String fileName, 
			final String mimeType, JSONObject params, boolean trustEveryone, boolean chunkedMode) throws IOException, SSLException {
		// Create return object
		FileUploadResult result = new FileUploadResult();
		
		// Get a input stream of the file on the phone
		InputStream fileInputStream = getPathFromUri(file);

		HttpURLConnection conn = null;
		DataOutputStream dos = null;

		int bytesRead, bytesAvailable, bufferSize;
		long totalBytes;
		byte[] buffer;
		int maxBufferSize = 8096;

		//------------------ CLIENT REQUEST
		// open a URL connection to the server 
		URL url = new URL(server);
		
		// Open a HTTP connection to the URL based on protocol 
        if (url.getProtocol().toLowerCase().equals("https")) {
    		// Using standard HTTPS connection. Will not allow self signed certificate
        	if (!trustEveryone) {
        		conn = (HttpsURLConnection) url.openConnection();
        	}
        	// Use our HTTPS connection that blindly trusts everyone.
        	// This should only be used in debug environments
        	else {
        		// Setup the HTTPS connection class to trust everyone
	            trustAllHosts();
	            HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
	            // Save the current hostnameVerifier
	            defaultHostnameVerifier = https.getHostnameVerifier();
	            // Setup the connection not to verify hostnames 
	            https.setHostnameVerifier(DO_NOT_VERIFY);
	        	conn = https;
        	}
        } 
        // Return a standard HTTP connection
        else {
        	conn = (HttpURLConnection) url.openConnection();
        }
		
		// Allow Inputs
		conn.setDoInput(true);
		
		// Allow Outputs
		conn.setDoOutput(true);
		
		// Don't use a cached copy.
		conn.setUseCaches(false);
		
		// Use a post method.
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+BOUNDRY);

		// Set the cookies on the response
		String cookie = CookieManager.getInstance().getCookie(server);
		if (cookie != null) {
			conn.setRequestProperty("Cookie", cookie);
		}
		
		// Should set this up as an option
		if (chunkedMode) {
		    conn.setChunkedStreamingMode(maxBufferSize);
		}
		    
		dos = new DataOutputStream( conn.getOutputStream() );

		// Send any extra parameters
		try {
			for (Iterator iter = params.keys(); iter.hasNext();) {
				Object key = iter.next();
				dos.writeBytes(LINE_START + BOUNDRY + LINE_END); 
				dos.writeBytes("Content-Disposition: form-data; name=\"" +  key.toString() + "\";");
				dos.writeBytes(LINE_END + LINE_END); 
				dos.write(params.getString(key.toString()).getBytes());
				dos.writeBytes(LINE_END); 
			}
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage(), e); 
		}
		
		dos.writeBytes(LINE_START + BOUNDRY + LINE_END);
		dos.writeBytes("Content-Disposition: form-data; name=\"" + fileKey + "\";" + " filename=\"" + fileName +"\"" + LINE_END);
		dos.writeBytes("Content-Type: " + mimeType + LINE_END); 
		dos.writeBytes(LINE_END);
		
		// create a buffer of maximum size
		bytesAvailable = fileInputStream.available();
		bufferSize = Math.min(bytesAvailable, maxBufferSize);
		buffer = new byte[bufferSize];
		
		// read file and write it into form...
		bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		totalBytes = 0;

		while (bytesRead > 0) {
			totalBytes += bytesRead;
			result.setBytesSent(totalBytes);
			dos.write(buffer, 0, bufferSize);
			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		}

		// send multipart form data necesssary after file data...
		dos.writeBytes(LINE_END);
		dos.writeBytes(LINE_START + BOUNDRY + LINE_START + LINE_END);
		
		// close streams
		fileInputStream.close();
		dos.flush();
		dos.close();

		//------------------ read the SERVER RESPONSE
		StringBuffer responseString = new StringBuffer("");
		DataInputStream inStream;
		try {
			inStream = new DataInputStream ( conn.getInputStream() );
		} catch(FileNotFoundException e) {
			throw new IOException("Received error from server");
		}
		
		String line;
		while (( line = inStream.readLine()) != null) {
			responseString.append(line);
		}
		Log.d(LOG_TAG, "got response from server");
		Log.d(LOG_TAG, responseString.toString());

		// send request and retrieve response
        result.setResponseCode(conn.getResponseCode());
        result.setResponse(responseString.toString());

        inStream.close();
        conn.disconnect();
        
        // Revert back to the proper verifier and socket factories
        if (trustEveryone && url.getProtocol().toLowerCase().equals("https")) {
        	((HttpsURLConnection)conn).setHostnameVerifier(defaultHostnameVerifier);
        	HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSocketFactory);
        }        
        
		return result;
	}

	/**
     * Get an input stream based on file path or content:// uri
     * 
     * @param path
     * @return an input stream
     * @throws FileNotFoundException 
     */
    private InputStream getPathFromUri(String path) throws FileNotFoundException {  	  
    	if (path.startsWith("content:")) {
    		Uri uri = Uri.parse(path);
    		return ctx.getContentResolver().openInputStream(uri);
    	}
    	else if (path.startsWith("file://")) {
    	    return new FileInputStream(path.substring(6)); 
    	}
    	else {
    		return new FileInputStream(path);
    	}
    }  

}