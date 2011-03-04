package se.msc.android.droidcouch.ubuntuone.samples;

import org.json.JSONObject;

import se.msc.android.droidcouch.DroidCouch;
import se.msc.android.droidcouch.ubuntuone.UbuntuOneDroidCouch;
import android.os.AsyncTask;

public class RetrieveNotesTask extends AsyncTask<Void, Void, JSONObject> {

	private final SampleUbuntuOneNotesViewer activity;

	public RetrieveNotesTask(SampleUbuntuOneNotesViewer activity) {
		this.activity = activity;
	}

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

	@Override
	protected void onPostExecute(JSONObject result) {
		super.onPostExecute(result);
		if (result != null) {
			activity.appendString(result.toString());
		}
	}
}