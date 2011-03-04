package se.msc.android.droidcouch.ubuntuone.samples;

/**
 * A note gotten from Ubuntu One  
 * 
 * @author Alejandro J. Cura <alecu@canonical.com>
 */
public class Note {

	private CharSequence title;
	private CharSequence body;

	public Note(CharSequence title, CharSequence body) {
		this.title = title;
		this.body = body;
	}
	
	public CharSequence getTitle() {
		return this.title;
	}

	public CharSequence getBody() {
		return this.body;
	}

}
