package se.msc.android.droidcouch.ubuntuone.samples;

import org.json.JSONArray;
import org.json.JSONException;
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
		try {
			JSONArray rows = result.getJSONArray("rows");
			for (int i = 0; i < rows.length(); i++) {
				JSONObject doc = rows.getJSONObject(i).getJSONObject("doc");
				String title = doc.getString("title");
				String body = doc.getString("content");
				if (!isDeleted(doc)) {
					activity.appendNote(title, body);
				} else {
					activity.appendNote(title + " (deleted)", body);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private boolean isDeleted(JSONObject doc) throws JSONException {
		JSONObject app_anotations = doc.getJSONObject("application_annotations");
		JSONObject ubuntuone_annotations = app_anotations.optJSONObject("Ubuntu One");
		if (ubuntuone_annotations != null) {
			JSONObject private_app_annotations = ubuntuone_annotations.getJSONObject("private_application_annotations");
			return private_app_annotations.getBoolean("deleted");
		}
		return false;
	}
}