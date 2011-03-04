package se.msc.android.droidcouch.ubuntuone.samples;

import se.msc.android.droidcouch.ubuntuone.DroidCouchActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class SampleUbuntuOneNotesViewer extends DroidCouchActivity {
	private TextView view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new RetrieveNotesTask(this).execute();
        view = new TextView(this);
        view.setMovementMethod(ScrollingMovementMethod.getInstance());
        setContentView(view);
        appendString("Connecting to couchdb server...\n\n");
	}

	public void appendString(String string) {
		view.append(string);
	}
	
}
