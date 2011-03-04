package se.msc.android.droidcouch.ubuntuone;

/**
 * A container for the user login information
 *
 * @author Alejandro J. Cura <alecu@canonical.com>
 */
public class UserPassword {

	private final String username;
	private final String password;

	/**
	 * Initialize this instance with the default values.
	 * 
	 * @param username the user name or email
	 * @param password the password for the above
	 */
	public UserPassword(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Get the user name
	 * 
	 * @return the user name or email
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Get the password
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

}