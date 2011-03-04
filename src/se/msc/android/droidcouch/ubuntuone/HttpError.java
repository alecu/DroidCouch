package se.msc.android.droidcouch.ubuntuone;

import org.apache.http.HttpResponse;

/**
 * A generic HTTP error
 * 
 * @author Alejandro J. Cura <alecu@canonical.com>
 */
@SuppressWarnings("serial")
public class HttpError extends Exception {

	private HttpResponse response;

	/**
	 * Create an error for the given response
	 * 
	 * @param response the response that signaled this error
	 */
	public HttpError(HttpResponse response) {
		this.response = response;
	}

	/**
	 * Return the response that caused this error.
	 * 
	 * @return the response that caused this error
	 */
	public HttpResponse getResponse() {
		return response;
	}
}
