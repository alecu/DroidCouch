package se.msc.android.droidcouch.ubuntuone;

import org.apache.http.HttpResponse;

public class HttpError extends Exception {

	private HttpResponse response;

	public HttpError(HttpResponse response) {
		this.response = response;
	}

}
