package se.msc.android.droidcouch.ubuntuone.samples;

import java.util.ArrayList;
import java.util.List;

import se.msc.android.droidcouch.R;
import se.msc.android.droidcouch.ubuntuone.DroidCouchActivity;
import android.os.Bundle;
import android.widget.ListView;

/**
 * Sample activity that fetches the CouchDB of notes, and shows them on a list.
 *  
 * @author Alejandro J. Cura <alecu@canonical.com>
 */
public class SampleUbuntuOneNotesViewer extends DroidCouchActivity {
	private ListView view;
	private List<Note> items = new ArrayList<Note>();
	private NoteArrayAdapter noteArrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        view = new ListView(this);
        noteArrayAdapter = new NoteArrayAdapter(this, R.layout.sample_notes_item, items);
		view.setAdapter(noteArrayAdapter);
		setContentView(view);

        // Start a task that will retrieve the notes in a new thread
        // and that will add each note in the UI thread 
        // after the notes are retrieved.
        new RetrieveNotesTask(this).execute();
	}

	public void appendNote(String title, String body) {
		noteArrayAdapter.add(new Note(title, body));
	}
	
}
