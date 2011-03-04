package se.msc.android.droidcouch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import android.util.Log;

/**
 * Class to query a CouchDB database. 
 */
public class DroidCouch {
    static final String TAG = "DroidCouchLibrary";
	private String hostUrl;

    /**
     * Create an instance of this class given the base url
     * 
     * @param hostUrl the base url to the CouchDB server 
     */
    public DroidCouch(String hostUrl) {
    	this.setHostUrl(hostUrl);
    }

	protected DroidCouch() {
	}

	/**
	 * Fetch all contents from a stream and make a string out of it
	 * 
	 * @param is the input stream
	 * @return all the contents of the stream
	 */
	public static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the
         * BufferedReader.readLine() method. We iterate until the BufferedReader
         * return null which means there's no more data to read. Each line will
         * appended to a StringBuilder and returned as String.
         * 
         * (c) public domain:
         * http://senior.ceng.metu.edu.tr/2009/praeda/2009/01/
         * 11/a-simple-restful-client-at-android/
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8192);
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * Create a new Couch database in the server
     * 
     * @param databaseName the name for the new database
     * @return true if the database was created
     */
    public boolean createDatabase(String databaseName) {
        try {
            HttpPut httpPutRequest = new HttpPut(getHostUrl() + databaseName);
            JSONObject jsonResult = sendCouchRequest(httpPutRequest);
            return jsonResult.getBoolean("ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Create a new json document in the given database
     * 
     * @param databaseName the name of an existing database
     * @param docId the document id for the new document
     * @param jsonDoc the json contents for the new document
     * @return the revision id of the created document
     */
    public String createDocument(String databaseName,
            String docId, JSONObject jsonDoc) {
        try {
            HttpPut httpPutRequest = new HttpPut(getHostUrl() + databaseName + "/"
                    + docId);
            StringEntity body = new StringEntity(jsonDoc.toString(), "utf8");
            httpPutRequest.setEntity(body);
            httpPutRequest.setHeader("Accept", "application/json");
            httpPutRequest.setHeader("Content-type", "application/json");
            JSONObject jsonResult = sendCouchRequest(httpPutRequest);
            if (!jsonResult.getBoolean("ok")) {
                return null; // Not ok!
            }
            return jsonResult.getString("rev");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Delete a given Couch database in the server
     * 
     * @param databaseName the name of the database to delete
     * @return true if the database was deleted
     */
    public boolean deleteDatabase(String databaseName) {
        try {
            HttpDelete httpDeleteRequest = new HttpDelete(getHostUrl()
                    + databaseName);
            JSONObject jsonResult = sendCouchRequest(httpDeleteRequest);
            return jsonResult.getBoolean("ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete a given document from a Couch database
     * 
     * @param databaseName the name of the database
     * @param docId the id of the document to delete
     * @return true if document successfully deleted
     */
    public boolean deleteDocument(String databaseName,
            String docId) {
        try {
            JSONObject jsonDoc = getDocument(databaseName, docId);
            return deleteDocument(databaseName, docId, jsonDoc
                    .getString("_rev"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete a given revision of a given document
     * 
     * @param databaseName the name of the database
     * @param docId the id of the document to delete
     * @param rev the revision to delete 
     * @return true if document successfully deleted
     */
    public boolean deleteDocument(String databaseName,
            String docId, String rev) {
        try {
            String url = getHostUrl() + databaseName + "/" + docId + "?rev=" + rev;
            HttpDelete httpDeleteRequest = new HttpDelete(url);
            JSONObject jsonResult = sendCouchRequest(httpDeleteRequest);
            if (jsonResult != null) {
                return jsonResult.getBoolean("ok");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Do a freeform couch query, by specifying a url path in the server  
     * 
     * @param url the url path to query, will be added to the base server url
     * @return the json found at that server url
     */
    public JSONObject get(String url) {
        // Prepare a request object
        HttpGet httpget = new HttpGet(getHostUrl() + url);
        // Execute the request
        HttpResponse response;
        JSONObject json = null;
        try {
        	response = executeRequest(httpget);
            // Examine the response status
            Log.i(TAG, response.getStatusLine().toString());

            // Get hold of the response entity
            HttpEntity entity = response.getEntity();
            // If the response does not enclose an entity, there is no need
            // to worry about connection release

            if (entity != null) {
                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                String result = convertStreamToString(instream);
                // Log.i(TAG, result);

                // A Simple JSONObject Creation
                json = new JSONObject(result);
                Log.i(TAG, json.toString(2));
                // Closing the input stream will trigger connection release
                instream.close();
            }
        } catch (Exception e) {
            // String timestamp =
            // TimestampFormatter.getInstance().getTimestamp();
            Log.e(TAG, getStacktrace(e));
            return null;
        }
        return json;
    }

	/**
	 * Get a given couch document
	 * 
	 * @param databaseName the name of the database
	 * @param docId the id for the document
     * @return the Json document
     */
    public JSONObject getDocument(String databaseName,
            String docId) {
        try {
            HttpGet httpGetRequest = new HttpGet(getHostUrl() + databaseName + "/"
                    + docId);
            JSONObject jsonResult = sendCouchRequest(httpGetRequest);
            if (jsonResult != null) {
                return jsonResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Formats an exception as a string
     *  
     * @param e the exception to format
     * @return a string description of the exception
     */
    public static String getStacktrace(Throwable e) {
        final Writer trace = new StringWriter();
        e.printStackTrace(new PrintWriter(trace));
        return trace.toString();
    }

    /**
     * Send a miscellaneous couch query to the server
     * 
     * @param an http request to send
     * @return the result as a Json object, null on error
     */
    private JSONObject sendCouchRequest(HttpUriRequest request) {
        try {
            HttpResponse httpResponse = executeRequest(request); 
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                // Read the content stream
                InputStream instream = entity.getContent();
                // Convert content stream to a String
                String resultString = convertStreamToString(instream);
                instream.close();
                // Transform the String into a JSONObject
                JSONObject jsonResult = new JSONObject(resultString);
                return jsonResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

	protected HttpResponse executeRequest(HttpUriRequest request) throws ClientProtocolException, IOException {
        return (HttpResponse) new DefaultHttpClient().execute(request);
	}

    /**
     * Update a given document in the couch database 
     * 
     * @param databaseName the name of the Couch database
     * @param jsonDoc the record to update 
     * @return the revision id of the updated document
     */
    public String updateDocument(String databaseName,
            JSONObject jsonDoc) {
        try {
            String docId = jsonDoc.getString("_id");
            HttpPut httpPutRequest = new HttpPut(getHostUrl() + databaseName + "/"
                    + docId);
            StringEntity body = new StringEntity(jsonDoc.toString(), "utf8");
            httpPutRequest.setEntity(body);
            httpPutRequest.setHeader("Accept", "application/json");
            httpPutRequest.setHeader("Content-type", "application/json");
            JSONObject jsonResult = sendCouchRequest(httpPutRequest);
            if (!jsonResult.getBoolean("ok")) {
                return null; // Not ok!
            }
            return jsonResult.getString("rev");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fetch all documents from the Couch database
     * 
     * @param databaseName the name of the Couch database
     * @return a JSON object with all documents in the database
     */
    public JSONObject getAllDocuments(String databaseName) {
        try {
            String url = getHostUrl() + databaseName
                    + "/_all_docs?include_docs=true";
            HttpGet httpGetRequest = new HttpGet(url);
            JSONObject jsonReceive = sendCouchRequest(httpGetRequest);
            if (jsonReceive != null) {
                return jsonReceive;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

	/**
	 * Set the base url for the Couch server
	 * 
	 * @param hostUrl the base url
	 */
	public void setHostUrl(String hostUrl) {
		this.hostUrl = hostUrl;
	}

	
	/**
	 * Get the base url for the Couch server
	 * 
	 * @return the base url
	 */
	public String getHostUrl() {
		return hostUrl;
	}

    // public static JSONObject SendHttpPut(String url, JSONObject jsonObjSend)
    // {
    // JSONObject jsonObjRecv = new JSONObject();
    // DefaultHttpClient httpclient = new DefaultHttpClient();
    // try {
    // String current_id = jsonObjSend.getString("_id");
    // HttpPut httpPutRequest = new HttpPut(url + "/" + current_id);
    // StringEntity se;
    // se = new StringEntity(jsonObjSend.toString());
    // httpPutRequest.setEntity(se);
    // httpPutRequest.setHeader("Accept", "application/json");
    // httpPutRequest.setHeader("Content-type", "application/json");
    // long t = System.currentTimeMillis();
    // HttpResponse response = (HttpResponse) httpclient
    // .execute(httpPutRequest);
    // Log.i(TAG, "HTTPResponse received in ["
    // + (System.currentTimeMillis() - t) + "ms]");
    // // Get hold of the response entity (-> the data):
    // HttpEntity entity = response.getEntity();
    // if (entity != null) {
    // // Read the content stream
    // InputStream instream = entity.getContent();
    // Header contentEncoding = response
    // .getFirstHeader("Content-Encoding");
    // if (contentEncoding != null
    // && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
    // instream = new GZIPInputStream(instream);
    // }
    // // convert content stream to a String
    // String resultString = convertStreamToString(instream);
    // instream.close();
    // // Transform the String into a JSONObject
    // jsonObjRecv = new JSONObject(resultString);
    // // Raw DEBUG output of our received JSON object:
    // Log.i(TAG, jsonObjRecv.toString(2));
    // }
    // } catch (Exception e) {
    // // More about HTTP exception handling in another tutorial.
    // // For now we just print the stack trace.
    // e.printStackTrace();
    // }
    // return jsonObjRecv;
    // }
    //
    // public static JSONObject SendHttpPutAttachment(String url,
    // JSONObject sender, String attachment, String data, String mimetype) {
    // JSONObject jsonObjRecv = new JSONObject();
    // DefaultHttpClient httpclient = new DefaultHttpClient();
    // try {
    // String id = sender.getString("_id");
    // String rev = sender.getString("_rev");
    // HttpPut httpPutRequest = new HttpPut(url + "/" + id + "/"
    // + attachment + "?rev=" + rev);
    // StringEntity se;
    // se = new StringEntity(data);
    // httpPutRequest.setEntity(se);
    // // httpPutRequest.setHeader("Content-length",
    // // Integer.toString(attachment.length()));
    // httpPutRequest.setHeader("Content-type", mimetype);
    // long t = System.currentTimeMillis();
    // HttpResponse response = (HttpResponse) httpclient
    // .execute(httpPutRequest);
    // Log.i(TAG, "HTTPResponse received in ["
    // + (System.currentTimeMillis() - t) + "ms]");
    // // Get hold of the response entity (-> the data):
    // HttpEntity entity = response.getEntity();
    // if (entity != null) {
    // // Read the content stream
    // InputStream instream = entity.getContent();
    // Header contentEncoding = response
    // .getFirstHeader("Content-Encoding");
    // if (contentEncoding != null
    // && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
    // instream = new GZIPInputStream(instream);
    // }
    // // convert content stream to a String
    // String resultString = convertStreamToString(instream);
    // instream.close();
    // // Transform the String into a JSONObject
    // jsonObjRecv = new JSONObject(resultString);
    // // Raw DEBUG output of our received JSON object:
    // Log.i(TAG, jsonObjRecv.toString(2));
    // }
    // } catch (Exception e) {
    // // More about HTTP exception handling in another tutorial.
    // // For now we just print the stack trace.
    // e.printStackTrace();
    // }
    // return jsonObjRecv;
    // }

    // public static JSONObject SendHttpPost(String url, JSONObject jsonObjSend)
    // {
    // JSONObject jsonObjRecv = new JSONObject();
    // DefaultHttpClient httpclient = new DefaultHttpClient();
    // try {
    //
    // HttpPost httpPostRequest = new HttpPost(url);
    // StringEntity se;
    // se = new StringEntity(jsonObjSend.toString());
    // // Set HTTP parameters
    // httpPostRequest.setEntity(se);
    // httpPostRequest.setHeader("Accept", "application/json");
    // httpPostRequest.setHeader("Content-type", "application/json");
    // long t = System.currentTimeMillis();
    // HttpResponse response = (HttpResponse) httpclient
    // .execute(httpPostRequest);
    // Log.i(TAG, "HTTPResponse received in ["
    // + (System.currentTimeMillis() - t) + "ms]");
    // // Get hold of the response entity (-> the data):
    // HttpEntity entity = response.getEntity();
    // if (entity != null) {
    // // Read the content stream
    // InputStream instream = entity.getContent();
    // Header contentEncoding = response
    // .getFirstHeader("Content-Encoding");
    // if (contentEncoding != null
    // && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
    // instream = new GZIPInputStream(instream);
    // }
    // // convert content stream to a String
    // String resultString = convertStreamToString(instream);
    // instream.close();
    // // Transform the String into a JSONObject
    // jsonObjRecv = new JSONObject(resultString);
    // // Raw DEBUG output of our received JSON object:
    // Log.i(TAG, jsonObjRecv.toString(2));
    // }
    // } catch (Exception e) {
    // // More about HTTP exception handling in another tutorial.
    // // For now we just print the stack trace.
    // e.printStackTrace();
    // }
    // return jsonObjRecv;
    // }
    //
    // public static JSONObject DeleteHttpDelete(String url) {
    // DefaultHttpClient httpclient = new DefaultHttpClient();
    // // Prepare a request object
    // HttpDelete httpDelete = new HttpDelete(url);
    // // Execute the request
    // HttpResponse response;
    // JSONObject json = null;
    // try {
    // response = httpclient.execute(httpDelete);
    // // Examine the response status
    // Log.i(TAG, response.getStatusLine().toString());
    //
    // // Get hold of the response entity
    // HttpEntity entity = response.getEntity();
    // // If the response does not enclose an entity, there is no need
    // // to worry about connection release
    //
    // if (entity != null) {
    // // A Simple JSON Response Read
    // InputStream instream = entity.getContent();
    // String result = convertStreamToString(instream);
    // Log.i(TAG, result);
    //
    // // A Simple JSONObject Creation
    // json = new JSONObject(result);
    // Log.i(TAG, json.toString(2));
    //
    // // Closing the input stream will trigger connection release
    // instream.close();
    // }
    // } catch (ClientProtocolException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (JSONException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // return json;
    // }

    // public static JSONObject getSampleData(String url, String status) {
    // // JSON object to hold the information, which is sent to the server
    // JSONObject jsonObjSend = new JSONObject();
    //
    // try {
    // // Add key/value pairs
    // Random generator = new Random();
    // int newKey = generator.nextInt(Integer.MAX_VALUE);
    // jsonObjSend.put("_id", Integer.toString(newKey));
    // jsonObjSend.put("status", status);
    // jsonObjSend.put("customer", "Imerica");
    //
    // // Add nested JSONObject (e.g. for header information)
    // JSONObject header = new JSONObject();
    // header.put("deviceType", "Android"); // Device type
    // header.put("deviceVersion", "1.5"); // Device OS version
    // header.put("language", "sv-se"); // Language of the Android client
    // jsonObjSend.put("header", header);
    //
    // // Add hardcoded inline attachment
    // JSONObject attachment = new JSONObject();
    // JSONObject doc = new JSONObject();
    // // doc.put("content_type", "text/plain");
    // doc.put("content_type", "image/jpeg");
    // // Base64 encoded image of red spot
    // String imageData =
    // "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAABGdBTUEAALGP"
    // + "C/xhBQAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9YGARc5KB0XV+IA"
    // + "AAAddEVYdENvbW1lbnQAQ3JlYXRlZCB3aXRoIFRoZSBHSU1Q72QlbgAAAF1J"
    // + "REFUGNO9zL0NglAAxPEfdLTs4BZM4DIO4C7OwQg2JoQ9LE1exdlYvBBeZ7jq"
    // + "ch9//q1uH4TLzw4d6+ErXMMcXuHWxId3KOETnnXXV6MJpcq2MLaI97CER3N0"
    // + "vr4MkhoXe0rZigAAAABJRU5ErkJggg==";
    // // Base64 encoded text
    // // String textData = "VGhpcyBpcyBhIGJhc2U2NCBlbmNvZGVkIHRleHQ=";
    // doc.put("data", imageData);
    // attachment.put("sig.jpeg", doc);
    // jsonObjSend.put("_attachments", attachment);
    //
    // // Output the JSON object we're sending to logcat:
    // Log.i(TAG, jsonObjSend.toString(2));
    //
    // } catch (JSONException e) {
    // e.printStackTrace();
    // }
    // return jsonObjSend;
    // }

}
