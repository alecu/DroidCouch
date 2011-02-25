package se.msc.android.droidcouch.ubuntuone;

import java.io.InputStream;
import java.net.URI;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import se.msc.android.droidcouch.DroidCouch;

public class UbuntuOneDroidCouch extends DroidCouch {
	private final String ACCOUNT_URL = "https://one.ubuntu.com/api/account/";
	private OAuthConsumer consumer;
	private String couchRoot;
	private int userId;

	public UbuntuOneDroidCouch() throws Exception {
		super();
		String consumer_key = "CONSUMER_KEY";
		String consumer_secret = "CONSUMER_SECRET";
		String access_token = "ACCESS_TOKEN";
		String token_secret = "TOKEN_SECRET";
		consumer = new CommonsHttpOAuthConsumer(consumer_key, consumer_secret);
		consumer.setTokenWithSecret(access_token, token_secret);
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
		consumer.sign(request);
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(request);
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
	public void signRequest(HttpUriRequest request) {
		super.signRequest(request);
		try {
			consumer.sign(request);
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		}
	}
}
