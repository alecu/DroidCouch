package se.msc.android.droidcouch.ubuntuone;

import se.msc.android.droidcouch.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Activity that prompts the user for her email address and password.
 * It's used by the UbuntuOneCredentials management, so your code should not
 * need to instantiate this class. 
 *
 * @author Alejandro J. Cura <alecu@canonical.com>
 */
public class LoginActivity extends Activity {
	public static final String RESULT_USERNAME = "resultUsername";
	public static final String RESULT_PASSWORD = "resultPassword";

	/* Show the login dialog and listen for a click on the login button
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ubuntuone_login);
        Button returnMainButton = (Button) findViewById(R.id.login_button);

        returnMainButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	onLoginClicked();
            }
        });
	}

	/**
	 * Returns the login info as the activity result. 
	 */
	private void onLoginClicked() {
		EditText usernameEditor = (EditText) findViewById(R.id.username_editor);
		EditText passwordEditor = (EditText) findViewById(R.id.password_editor);
		Bundle bundle = new Bundle();
		bundle.putString(LoginActivity.RESULT_USERNAME, usernameEditor.getText().toString());
		bundle.putString(LoginActivity.RESULT_PASSWORD, passwordEditor.getText().toString());

		Intent i = new Intent();
		i.putExtras(bundle);
		setResult(RESULT_OK, i);
		finish();
	}
}
