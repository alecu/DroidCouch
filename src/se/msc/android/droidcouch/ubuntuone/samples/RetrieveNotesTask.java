package se.msc.android.droidcouch.ubuntuone.samples;

import org.json.JSONObject;

import se.msc.android.droidcouch.DroidCouch;
import se.msc.android.droidcouch.ubuntuone.UbuntuOneDroidCouch;
import android.os.AsyncTask;

/**
 * Sample task that retrieves the notes DB from the Ubuntu One CouchDB servers.
 * It also sends the result data to the activity that invoked this.
 * 
 * @author Alejandro J. Cura <alecu@canonical.com>
 */
public class RetrieveNotesTask extends AsyncTask<Void, Void, JSONObject> {

	private final SampleUbuntuOneNotesViewer activity;

	/**
	 * Construct this task
	 * @param activity the activity that is running this task
	 */
	public RetrieveNotesTask(SampleUbuntuOneNotesViewer activity) {
		this.activity = activity;
	}

	/* This gets executed in a background thread, so it does not block the UI thread. 
	 */
	@Override
	protected JSONObject doInBackground(Void... params) {
		DroidCouch droidCouch;
		try {
			droidCouch = new UbuntuOneDroidCouch(activity);
			JSONObject allDocuments = droidCouch.getAllDocuments("notes");
			return allDocuments;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/* This part gets executed in the UI thread, so it can update UI elements. 
	 */
	@Override
	protected void onPostExecute(JSONObject result) {
		super.onPostExecute(result);
		if (result != null) {
			activity.appendString(result.toString());
		}
	}
}