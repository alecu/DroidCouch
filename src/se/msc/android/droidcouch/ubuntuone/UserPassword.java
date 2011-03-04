package se.msc.android.droidcouch.ubuntuone;

public class UserPassword {

	private final String username;
	private final String password;

	public UserPassword(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

}