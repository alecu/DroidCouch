package se.msc.android.droidcouch.ubuntuone.samples;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TwoLineListItem;

public class NoteArrayAdapter extends ArrayAdapter<Note> {
	private int id;

	public NoteArrayAdapter(Context context, int id, List<Note> notes) {
		super(context, id, notes);
		this.id = id;
	}

	@Override
	public View getView(int n, View convertView, ViewGroup parent) {
		Note note = getItem(n);

		if (note == null) {
			return null;
		}

		TwoLineListItem itemView;
		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			itemView = (TwoLineListItem) layoutInflater.inflate(id, parent,
					false);
		} else {
			itemView = (TwoLineListItem) convertView;
		}

		if (itemView.getText1() != null) {
			itemView.getText1().setText(note.getTitle());
		}

		if (itemView.getText2() != null) {
			itemView.getText2().setText(note.getBody().length() + " chars");
		}

		return itemView;
	}
}
