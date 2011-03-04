package se.msc.android.droidcouch.ubuntuone;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import se.msc.android.droidcouch.DroidCouch;

/**
 * Similar to DroidCouch but using Ubuntu One CouchDB servers.
 * 
 * NOTE: Make sure to create and use instances of this class
 * from a worker thread (that is, never use it in the UI thread).
 *
 * @author Alejandro J. Cura <alecu@canonical.com>
 */
public class UbuntuOneDroidCouch extends DroidCouch {
	private final String ACCOUNT_URL = "https://one.ubuntu.com/api/account/";
	private String couchRoot;
	@SuppressWarnings("unused")
	private int userId;
	private UbuntuOneCredentials credentials;

	/**
	 * Create a new instance of this class. 
	 * 
	 * @param droidCouchActivity an activity that may handle the login dialog if needed.
	 * @throws Exception some error that occurred while creating this instance. 
	 */
	public UbuntuOneDroidCouch(DroidCouchActivity droidCouchActivity) throws Exception {
		super();
		credentials = new UbuntuOneCredentials(droidCouchActivity);
		findCouchRoot();
	}

	/**
	 * Find the root url for the users' Ubuntu One CouchDB servers
	 * 
	 * @throws Exception some problem that happened while getting at the root url
	 */
	private void findCouchRoot() throws Exception {
		String content = getUrl(ACCOUNT_URL);
		JSONObject accountInfo = new JSONObject(content);
		userId = accountInfo.getInt("id");
		URI baseUri = new URI(accountInfo.getString("couchdb_root"));
		String path = "/" + encodeSlashes(baseUri.getPath().substring(1) + "/");
		URI modifiedUri = new URI(baseUri.getScheme(), baseUri.getUserInfo(),
				baseUri.getHost(), baseUri.getPort(), path, baseUri.getQuery(),
				baseUri.getFragment());
		couchRoot = decodePercentages(modifiedUri.toString());
		setHostUrl(couchRoot);
	}

	/**
	 * Encode slashes as %2F
	 * 
	 * @param substring what to encode
	 * @return the encoded string
	 */
	private String encodeSlashes(String substring) {
		return substring.replace("/", "%2F");
	}

	/**
	 * Decode each %25 as a percentage symbol
	 *  
	 * @param substring what to decode
	 * @return the decoded string
	 */
	private String decodePercentages(String substring) {
		return substring.replace("%25", "%");
	}

	
	/**
	 * Get the contents of a given url.
	 * 
	 * @param url the url to get
	 * @return a string with the contents
	 * @throws Exception with some error that prevented execution
	 */
	private String getUrl(String url) throws Exception {
		HttpGet request = new HttpGet(url);
		HttpResponse response = executeRequest(request);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			InputStream instream = entity.getContent();
			String resultString = convertStreamToString(instream);
			instream.close();
			return resultString;
		}
		return null;
	}

	/* Execute an http request, signing, retrying and logging in if needed
	 * @see se.msc.android.droidcouch.DroidCouch#executeRequest(org.apache.http.client.methods.HttpUriRequest)
	 */
	@Override
	protected HttpResponse executeRequest(HttpUriRequest request)
			throws ClientProtocolException, IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = null;
		int retries = 3;

		while (retries-- > 0) {
			credentials.signRequest(request);
			response = httpClient.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 400 || statusCode == 401) {
				credentials.invalidate();
			} else {
				return response;
			}
		}
		return response;
	}

}
