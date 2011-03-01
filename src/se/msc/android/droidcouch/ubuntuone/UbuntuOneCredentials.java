package se.msc.android.droidcouch.ubuntuone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

public class UbuntuOneCredentials {

	private static final String USERNAME = "";
	private static final String PASSWORD = "";

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

	public UbuntuOneCredentials(Context ctx) {
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		prefsEditor = prefs.edit();
	}

	public void invalidate() {
		prefsEditor.remove(CONSUMER_KEY);
		prefsEditor.remove(CONSUMER_SECRET);
		prefsEditor.remove(ACCESS_TOKEN);
		prefsEditor.remove(TOKEN_SECRET);
		prefsEditor.commit();
		consumer = null;
	}

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
			login(USERNAME, PASSWORD);
		}
	}

	private void login(String username, String password) {
		invalidate();
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials(
				new AuthScope(LOGIN_HOST, LOGIN_PORT),
				new UsernamePasswordCredentials(username, password));
		HttpUriRequest request = new HttpGet(buildLoginUrl());
		try {
			HttpResponse response = httpClient.execute(request);
			verifyResponse(response);
			JSONObject loginData = responseToJson(response);
			storeTokens(loginData.getString(CONSUMER_KEY), loginData
					.getString(CONSUMER_SECRET), loginData
					.getString(ACCESS_TOKEN), loginData.getString(TOKEN_SECRET));
			buildConsumer();
			ping_u1_url(username);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (HttpError e) {
			e.printStackTrace();
		}
	}

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

	private void verifyResponse(HttpResponse response) throws HttpError {
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode < 200 || statusCode > 299) {
			throw new HttpError(response);
		}
	}

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

	private void storeTokens(String consumer_key, String consumer_secret,
			String access_token, String token_secret) {
		prefsEditor.putString(CONSUMER_KEY, consumer_key);
		prefsEditor.putString(CONSUMER_SECRET, consumer_secret);
		prefsEditor.putString(ACCESS_TOKEN, access_token);
		prefsEditor.putString(TOKEN_SECRET, token_secret);
		prefsEditor.commit();
	}

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
