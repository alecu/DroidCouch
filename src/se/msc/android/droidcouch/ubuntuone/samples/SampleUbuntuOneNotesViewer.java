package se.msc.android.droidcouch.ubuntuone.samples;

import se.msc.android.droidcouch.ubuntuone.DroidCouchActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

/**
 * Sample activity that fetches the CouchDB of notes, and shows them on a list.
 *  
 * @author Alejandro J. Cura <alecu@canonical.com>
 */
public class SampleUbuntuOneNotesViewer extends DroidCouchActivity {
	private TextView view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        view = new TextView(this);
        view.setMovementMethod(ScrollingMovementMethod.getInstance());
        setContentView(view);
        appendString("Connecting to couchdb server...\n\n");

        // Start a task that will retrieve the notes in a new thread
        // and that will call appendString in the UI thread 
        // after the notes are retrieved.
        new RetrieveNotesTask(this).execute();
	}

	public void appendString(String string) {
		if (view != null && view.isShown()) {
			view.append(string);
		}
	}
	
}
