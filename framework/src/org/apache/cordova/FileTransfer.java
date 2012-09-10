/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;

public class FileTransfer extends Plugin {

    private static final String LOG_TAG = "FileTransfer";
    private static final String LINE_START = "--";
    private static final String LINE_END = "\r\n";
    private static final String BOUNDARY =  "*****";

    public static int FILE_NOT_FOUND_ERR = 1;
    public static int INVALID_URL_ERR = 2;
    public static int CONNECTION_ERR = 3;

    private SSLSocketFactory defaultSSLSocketFactory = null;
    private HostnameVerifier defaultHostnameVerifier = null;

    /* (non-Javadoc)
    * @see org.apache.cordova.api.Plugin#execute(java.lang.String, org.json.JSONArray, java.lang.String)
    */
    @Override
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        String source = null;
        String target = null;
        try {
            source = args.getString(0);
            target = args.getString(1);
        } catch (JSONException e) {
            Log.d(LOG_TAG, "Missing source or target");
            return new PluginResult(PluginResult.Status.JSON_EXCEPTION, "Missing source or target");
        }

        if (action.equals("upload")) {
            return upload(URLDecoder.decode(source), target, args);
        } else if (action.equals("download")) {
            return download(source, target);
        } else {
            return new PluginResult(PluginResult.Status.INVALID_ACTION);
        }
    }

    /**
     * Uploads the specified file to the server URL provided using an HTTP multipart request.
     * @param source        Full path of the file on the file system
     * @param target        URL of the server to receive the file
     * @param args          JSON Array of args
     *
     * args[2] fileKey       Name of file request parameter
     * args[3] fileName      File name to be used on server
     * args[4] mimeType      Describes file content type
     * args[5] params        key:value pairs of user-defined parameters
     * @return FileUploadResult containing result of upload request
     */
    private PluginResult upload(String source, String target, JSONArray args) {
        Log.d(LOG_TAG, "upload " + source + " to " +  target);

        HttpURLConnection conn = null;
        try {
            // Setup the options
            String fileKey = getArgument(args, 2, "file");
            String fileName = getArgument(args, 3, "image.jpg");
            String mimeType = getArgument(args, 4, "image/jpeg");
            JSONObject params = args.optJSONObject(5);
            if (params == null) params = new JSONObject();
            boolean trustEveryone = args.optBoolean(6);
            boolean chunkedMode = args.optBoolean(7) || args.isNull(7); //Always use chunked mode unless set to false as per API
            JSONObject headers = args.optJSONObject(8);
            // Look for headers on the params map for backwards compatibility with older Cordova versions.
            if (headers == null && params != null) {
                headers = params.optJSONObject("headers");
            }

            Log.d(LOG_TAG, "fileKey: " + fileKey);
            Log.d(LOG_TAG, "fileName: " + fileName);
            Log.d(LOG_TAG, "mimeType: " + mimeType);
            Log.d(LOG_TAG, "params: " + params);
            Log.d(LOG_TAG, "trustEveryone: " + trustEveryone);
            Log.d(LOG_TAG, "chunkedMode: " + chunkedMode);
            Log.d(LOG_TAG, "headers: " + headers);

            // Create return object
            FileUploadResult result = new FileUploadResult();

            // Get a input stream of the file on the phone
            FileInputStream fileInputStream = (FileInputStream) getPathFromUri(source);

            DataOutputStream dos = null;

            int bytesRead, bytesAvailable, bufferSize;
            long totalBytes;
            byte[] buffer;
            int maxBufferSize = 8096;

            //------------------ CLIENT REQUEST
            // open a URL connection to the server
            URL url = new URL(target);
            boolean useHttps = url.getProtocol().toLowerCase().equals("https");
            // Open a HTTP connection to the URL based on protocol
            if (useHttps) {
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
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);

            // Set the cookies on the response
            String cookie = CookieManager.getInstance().getCookie(target);
            if (cookie != null) {
                conn.setRequestProperty("Cookie", cookie);
            }

            // Handle the other headers
            if (headers != null) {
                try {
                    for (Iterator iter = headers.keys(); iter.hasNext(); ) {
                        String headerKey = iter.next().toString();
                        JSONArray headerValues = headers.optJSONArray(headerKey);
                        if (headerValues == null) {
                            headerValues = new JSONArray();
                            headerValues.put(headers.getString(headerKey));
                        }
                        conn.setRequestProperty(headerKey, headerValues.getString(0));
                        for (int i = 1; i < headerValues.length(); ++i) {
                            conn.addRequestProperty(headerKey, headerValues.getString(i));
                        }
                    }
                } catch (JSONException e1) {
                  // No headers to be manipulated!
                }
            }

            /*
                * Store the non-file portions of the multipart data as a string, so that we can add it
                * to the contentSize, since it is part of the body of the HTTP request.
                */
            String extraParams = "";
            try {
                for (Iterator iter = params.keys(); iter.hasNext();) {
                    Object key = iter.next();
                    if(!String.valueOf(key).equals("headers"))
                    {
                      extraParams += LINE_START + BOUNDARY + LINE_END;
                      extraParams += "Content-Disposition: form-data; name=\"" +  key.toString() + "\";";
                      extraParams += LINE_END + LINE_END;
                      extraParams += params.getString(key.toString());
                      extraParams += LINE_END;
                    }
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }

            extraParams += LINE_START + BOUNDARY + LINE_END;
            extraParams += "Content-Disposition: form-data; name=\"" + fileKey + "\";" + " filename=\"";
            byte[] extraBytes = extraParams.getBytes("UTF-8");

            String midParams = "\"" + LINE_END + "Content-Type: " + mimeType + LINE_END + LINE_END;
            String tailParams = LINE_END + LINE_START + BOUNDARY + LINE_START + LINE_END;
            byte[] fileNameBytes = fileName.getBytes("UTF-8");

            int stringLength = extraBytes.length + midParams.length() + tailParams.length() + fileNameBytes.length;
            Log.d(LOG_TAG, "String Length: " + stringLength);
            int fixedLength = (int) fileInputStream.getChannel().size() + stringLength;
            Log.d(LOG_TAG, "Content Length: " + fixedLength);
            // setFixedLengthStreamingMode causes and OutOfMemoryException on pre-Froyo devices.
            // http://code.google.com/p/android/issues/detail?id=3164
            // It also causes OOM if HTTPS is used, even on newer devices.
            chunkedMode = chunkedMode && (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO || useHttps);
            		
            if (chunkedMode) {
                conn.setChunkedStreamingMode(maxBufferSize);
                // Although setChunkedStreamingMode sets this header, setting it explicitly here works
                // around an OutOfMemoryException when using https.
                conn.setRequestProperty("Transfer-Encoding", "chunked");
            } else {
                conn.setFixedLengthStreamingMode(fixedLength);
            }

            dos = new DataOutputStream( conn.getOutputStream() );
            //We don't want to change encoding, we just want this to write for all Unicode.
            dos.write(extraBytes);
            dos.write(fileNameBytes);
            dos.writeBytes(midParams);

            // create a buffer of maximum size
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            totalBytes = 0;

            long prevBytesRead = 0;
            while (bytesRead > 0) {
                totalBytes += bytesRead;
                result.setBytesSent(totalBytes);
                dos.write(buffer, 0, bufferSize);
                if (totalBytes > prevBytesRead + 102400) {
                	prevBytesRead = totalBytes;
                	Log.d(LOG_TAG, "Uploaded " + totalBytes + " of " + fixedLength + " bytes");
                }
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necessary after file data...
            dos.writeBytes(tailParams);

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
                Log.e(LOG_TAG, e.toString(), e);
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

            // Revert back to the proper verifier and socket factories
            if (trustEveryone && url.getProtocol().toLowerCase().equals("https")) {
                ((HttpsURLConnection) conn).setHostnameVerifier(defaultHostnameVerifier);
                HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSocketFactory);
            }

            Log.d(LOG_TAG, "****** About to return a result from upload");
            return new PluginResult(PluginResult.Status.OK, result.toJSONObject());

        } catch (FileNotFoundException e) {
            JSONObject error = createFileTransferError(FILE_NOT_FOUND_ERR, source, target, conn);
            Log.e(LOG_TAG, error.toString(), e);
            return new PluginResult(PluginResult.Status.IO_EXCEPTION, error);
        } catch (MalformedURLException e) {
            JSONObject error = createFileTransferError(INVALID_URL_ERR, source, target, conn);
            Log.e(LOG_TAG, error.toString(), e);
            return new PluginResult(PluginResult.Status.IO_EXCEPTION, error);
        } catch (IOException e) {
            JSONObject error = createFileTransferError(CONNECTION_ERR, source, target, conn);
            Log.e(LOG_TAG, error.toString(), e);
            return new PluginResult(PluginResult.Status.IO_EXCEPTION, error);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
        } catch (Throwable t) {
            // Shouldn't happen, but will
            JSONObject error = createFileTransferError(CONNECTION_ERR, source, target, conn);
            Log.e(LOG_TAG, error.toString(), t);
            return new PluginResult(PluginResult.Status.IO_EXCEPTION, error);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
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

    private JSONObject createFileTransferError(int errorCode, String source, String target, HttpURLConnection connection) {

        Integer httpStatus = null;

        if (connection != null) {
            try {
                httpStatus = connection.getResponseCode();
            } catch (IOException e) {
                Log.w(LOG_TAG, "Error getting HTTP status code from connection.", e);
            }
        }

        return createFileTransferError(errorCode, source, target, httpStatus);
    }

        /**
        * Create an error object based on the passed in errorCode
        * @param errorCode 	the error
        * @return JSONObject containing the error
        */
    private JSONObject createFileTransferError(int errorCode, String source, String target, Integer httpStatus) {
        JSONObject error = null;
        try {
            error = new JSONObject();
            error.put("code", errorCode);
            error.put("source", source);
            error.put("target", target);
            if (httpStatus != null) {
                error.put("http_status", httpStatus);
            }
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
        if (args.length() >= position) {
            arg = args.optString(position);
            if (arg == null || "null".equals(arg)) {
                arg = defaultString;
            }
        }
        return arg;
    }

    /**
     * Downloads a file form a given URL and saves it to the specified directory.
     *
     * @param source        URL of the server to receive the file
     * @param target      	Full path of the file on the file system
     * @return JSONObject 	the downloaded file
     */
    private PluginResult download(String source, String target) {
        Log.d(LOG_TAG, "download " + source + " to " +  target);

        HttpURLConnection connection = null;
        try {
            File file = getFileFromPath(target);

            // create needed directories
            file.getParentFile().mkdirs();

            // connect to server
            if (webView.isUrlWhiteListed(source))
            {
              URL url = new URL(source);
              connection = (HttpURLConnection) url.openConnection();
              connection.setRequestMethod("GET");

              //Add cookie support
              String cookie = CookieManager.getInstance().getCookie(source);
              if(cookie != null)
              {
                connection.setRequestProperty("cookie", cookie);
              }

              connection.connect();

              Log.d(LOG_TAG, "Download file: " + url);

                connection.connect();

                Log.d(LOG_TAG, "Download file:" + url);
                InputStream inputStream;
                try {
                    inputStream = connection.getInputStream();
                } catch(FileNotFoundException e) {
                    Log.e(LOG_TAG, e.toString(), e);
                    throw new IOException("Received error from server");
                }

                byte[] buffer = new byte[1024];
                int bytesRead = 0;

                FileOutputStream outputStream = new FileOutputStream(file);

                // write bytes to file
                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();

                Log.d(LOG_TAG, "Saved file: " + target);

              // create FileEntry object
              FileUtils fileUtil = new FileUtils();
              JSONObject fileEntry = fileUtil.getEntry(file);

              return new PluginResult(PluginResult.Status.OK, fileEntry);
            }
            else
            {
                Log.w(LOG_TAG, "Source URL is not in white list: '" + source + "'");
                JSONObject error = createFileTransferError(CONNECTION_ERR, source, target, 401);
                return new PluginResult(PluginResult.Status.IO_EXCEPTION, error);
            }

        } catch (FileNotFoundException e) {
            JSONObject error = createFileTransferError(FILE_NOT_FOUND_ERR, source, target, connection);
            Log.d(LOG_TAG, "I got a file not found exception");
            Log.e(LOG_TAG, error.toString(), e);
            return new PluginResult(PluginResult.Status.IO_EXCEPTION, error);
        } catch (MalformedURLException e) {
            JSONObject error = createFileTransferError(INVALID_URL_ERR, source, target, connection);
            Log.e(LOG_TAG, error.toString(), e);
            return new PluginResult(PluginResult.Status.IO_EXCEPTION, error);
        } catch (Exception e) {  // IOException, JSONException, NullPointer
            JSONObject error = createFileTransferError(CONNECTION_ERR, source, target, connection);
            Log.e(LOG_TAG, error.toString(), e);
            return new PluginResult(PluginResult.Status.IO_EXCEPTION, error);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Get an input stream based on file path or content:// uri
     *
     * @param path foo
     * @return an input stream
     * @throws FileNotFoundException
     */
    private InputStream getPathFromUri(String path) throws FileNotFoundException {
        if (path.startsWith("content:")) {
            Uri uri = Uri.parse(path);
            return cordova.getActivity().getContentResolver().openInputStream(uri);
        }
        else if (path.startsWith("file://")) {
            int question = path.indexOf("?");
            if (question == -1) {
                return new FileInputStream(path.substring(7));
            } else {
                return new FileInputStream(path.substring(7, question));
            }
        }
        else {
            return new FileInputStream(path);
        }
    }

    /**
     * Get a File object from the passed in path
     *
     * @param path file path
     * @return file object
     */
    private File getFileFromPath(String path) throws FileNotFoundException {
        File file;
        String prefix = "file://";

        if (path.startsWith(prefix)) {
            file = new File(path.substring(prefix.length()));
        } else {
            file = new File(path);
        }

        if (file.getParent() == null) {
            throw new FileNotFoundException();
        }

        return file;
    }
}
