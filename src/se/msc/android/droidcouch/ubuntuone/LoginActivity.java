package se.msc.android.droidcouch.ubuntuone;

import se.msc.android.droidcouch.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {
	public static final String RESULT_USERNAME = "resultUsername";
	public static final String RESULT_PASSWORD = "resultPassword";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ubuntuone_login);
        Button returnMainButton = (Button) findViewById(R.id.login_button);

        returnMainButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
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
        });
	}
}
