package se.msc.android.droidcouch.ubuntuone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.SynchronousQueue;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.signature.HmacSha1MessageSigner;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

/**
 * Manage the credentials for the logged in user.
 * 
 * @author Alejandro J. Cura <alecu@canonical.com>
 */
public class UbuntuOneCredentials {
	private static final String BASE_TOKEN_NAME = "Ubuntu One @ ";
	private static final String CONSUMER_KEY = "consumer_key";
	private static final String CONSUMER_SECRET = "consumer_secret";
	private static final String ACCESS_TOKEN = "token";
	private static final String TOKEN_SECRET = "token_secret";

	private static final String LOGIN_HOST = "login.ubuntu.com";
	private static final int LOGIN_PORT = 443;
	private static final String LOGIN_URL = "https://" + LOGIN_HOST + ":"
			+ LOGIN_PORT + "/api/1.0/authentications"
			+ "?ws.op=authenticate&token_name=";
	private static final String PING_URL = "https://one.ubuntu.com/oauth/sso-finished-so-get-tokens/";
	private static final String UTF8 = "UTF-8";

	private SharedPreferences prefs;
	private SharedPreferences.Editor prefsEditor;

	private CommonsHttpOAuthConsumer consumer;
	private final DroidCouchActivity droidCouchActivity;

	/**
	 * Create this instance with an activity that will handle the login dialog response.
	 * 
	 * @param activity the activity that may handle the login dialog response.
	 */
	public UbuntuOneCredentials(DroidCouchActivity activity) {
		this.droidCouchActivity = activity;
		prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		prefsEditor = prefs.edit();
	}

	/**
	 * Remove all current credentials from the phone.
	 */
	public void invalidate() {
		prefsEditor.remove(CONSUMER_KEY);
		prefsEditor.remove(CONSUMER_SECRET);
		prefsEditor.remove(ACCESS_TOKEN);
		prefsEditor.remove(TOKEN_SECRET);
		prefsEditor.commit();
		consumer = null;
	}

	/**
	 * Sign an http request with OAuth credentials.
	 * May pop up the login dialog if needed.
	 * 
	 * @param request the request to sign with OAuth
	 */
	public void signRequest(HttpRequest request) {
		int retries = 3;

		if (consumer == null) {
			buildConsumer();
		}

		while (retries-- > 0) {
			try {
				if (consumer != null) {
					// We need to remove the previous Authorization header
					// because signpost fails to sign a second time otherwise. 
					request.removeHeaders("Authorization");
					consumer.sign(request);
					return;
				}
			} catch (OAuthException e) {
				e.printStackTrace();
			}
			login();
		}
	}

	/**
	 * Delete the current credentials and prompt for login to get new credentials.
	 */
	private void login() {
		invalidate();
		try {
			UserPassword userpass = promptUserPassword();
			DefaultHttpClient httpClient = new DefaultHttpClient();
			httpClient.getCredentialsProvider().setCredentials(
					new AuthScope(LOGIN_HOST, LOGIN_PORT),
					new UsernamePasswordCredentials(userpass.getUsername(), userpass.getPassword()));
			HttpUriRequest request = new HttpGet(buildLoginUrl());
			HttpResponse response = httpClient.execute(request);
			verifyResponse(response);
			JSONObject loginData = responseToJson(response);
			storeTokens(loginData.getString(CONSUMER_KEY), loginData
					.getString(CONSUMER_SECRET), loginData
					.getString(ACCESS_TOKEN), loginData.getString(TOKEN_SECRET));
			buildConsumer();
			ping_u1_url(userpass.getUsername());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (HttpError e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start the activity that will get the user and password on the UI thread.
	 * It will block until the new {@link UserPassword} is found.
	 * 
	 * @return the {@link UserPassword} with the info entered on the dialog. 
	 * @throws InterruptedException an error that prevented successful login.
	 */
	private UserPassword promptUserPassword() throws InterruptedException {
		final SynchronousQueue<UserPassword> queue = new SynchronousQueue<UserPassword>();
		droidCouchActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				droidCouchActivity.promptUserPassword(queue);
			}
		});
		return queue.take();
	}

	/**
	 * Fetch the Ubuntu One ping url.
	 * This will update login information from the Ubuntu SSO webservice into the
	 * Ubuntu One servers.
	 *   
	 * @param username the email in fact of the pinged user 
	 */
	private void ping_u1_url(String username) {
		try {
			String ping_url = PING_URL + username;
			HttpGet request = new HttpGet(ping_url);
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = null;
			int retries = 3;
			
			while (retries-- > 0) {
				signRequest(request);
				response = httpClient.execute(request);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 400 || statusCode == 401) {
					invalidate();
				} else {
					return;
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Verify that a response has a valid http status.
	 * 
	 * @param response the response to check
	 * @throws HttpError an error when the response is not 2xx
	 */
	private void verifyResponse(HttpResponse response) throws HttpError {
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode < 200 || statusCode > 299) {
			throw new HttpError(response);
		}
	}
	
	/**
	 * Parse a response into a JSON Object.
	 * 
	 * @param response the response to parse
	 * @return a JSONObject parsed from the response
	 * @throws IOException some error happened reading the response from the network 
	 * @throws JSONException the contents are not valid JSON
	 * @throws UnsupportedEncodingException the contents are not valid UTF-8
	 */
	private JSONObject responseToJson(HttpResponse response)
			throws UnsupportedEncodingException, IOException, JSONException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				response.getEntity().getContent(), "UTF-8"));
		StringBuilder builder = new StringBuilder();
		for (String line = null; (line = reader.readLine()) != null;) {
			builder.append(line).append("\n");
		}
		return new JSONObject(builder.toString());
	}

	/**
	 * Build the login url with the phone's model.
	 * 
	 * @return the url for logging in
	 */
	private String buildLoginUrl() {
		String token_name = BASE_TOKEN_NAME + Build.MODEL;
		String login_url = LOGIN_URL;
		try {
			login_url += URLEncoder.encode(token_name, UTF8);
		} catch (UnsupportedEncodingException e) {
			login_url += "Android";
		}
		return login_url;
	}

	/**
	 * Store tokens found in the phone preferences. 
	 * 
	 * @param consumer_key the CONSUMER_KEY
	 * @param consumer_secret the CONSUMER_SECRET
	 * @param access_token the ACCESS_TOKEN
	 * @param token_secret the TOKEN_SECRET
	 */
	private void storeTokens(String consumer_key, String consumer_secret,
			String access_token, String token_secret) {
		prefsEditor.putString(CONSUMER_KEY, consumer_key);
		prefsEditor.putString(CONSUMER_SECRET, consumer_secret);
		prefsEditor.putString(ACCESS_TOKEN, access_token);
		prefsEditor.putString(TOKEN_SECRET, token_secret);
		prefsEditor.commit();
	}

	/**
	 * Build an OAuth consumer from the token values stored in the phone preferences.
	 */
	private void buildConsumer() {
		String consumer_key = prefs.getString(CONSUMER_KEY, null);
		String consumer_secret = prefs.getString(CONSUMER_SECRET, null);
		String access_token = prefs.getString(ACCESS_TOKEN, null);
		String token_secret = prefs.getString(TOKEN_SECRET, null);

		if (consumer_key != null && consumer_secret != null
				&& access_token != null && token_secret != null) {
			consumer = new CommonsHttpOAuthConsumer(consumer_key,
					consumer_secret);
			consumer.setMessageSigner(new HmacSha1MessageSigner());
			consumer.setTokenWithSecret(access_token, token_secret);
		}
	}
}
