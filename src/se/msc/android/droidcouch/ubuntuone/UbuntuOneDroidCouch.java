package se.msc.android.droidcouch.ubuntuone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import se.msc.android.droidcouch.DroidCouch;
import android.app.Activity;

public class UbuntuOneDroidCouch extends DroidCouch {
	private final String ACCOUNT_URL = "https://one.ubuntu.com/api/account/";
	private String couchRoot;
	private int userId;
	private UbuntuOneCredentials credentials;

	public UbuntuOneDroidCouch(DroidCouchActivity droidCouchActivity) throws Exception {
		super();
		credentials = new UbuntuOneCredentials(droidCouchActivity);
		findCouchRoot();
	}

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

	private String encodeSlashes(String substring) {
		return substring.replace("/", "%2F");
	}

	private String decodePercentages(String substring) {
		return substring.replace("%25", "%");
	}

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

	private String responseToString(HttpResponse response)
			throws UnsupportedEncodingException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				response.getEntity().getContent(), "UTF-8"));
		StringBuilder builder = new StringBuilder();
		for (String line = null; (line = reader.readLine()) != null;) {
			builder.append(line).append("\n");
		}
		return builder.toString();
	}

}
