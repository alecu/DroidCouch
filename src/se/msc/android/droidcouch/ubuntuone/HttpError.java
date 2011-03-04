package se.msc.android.droidcouch.ubuntuone;

import org.apache.http.HttpResponse;

@SuppressWarnings("serial")
public class HttpError extends Exception {

	private HttpResponse response;

	public HttpError(HttpResponse response) {
		this.response = response;
	}

}
