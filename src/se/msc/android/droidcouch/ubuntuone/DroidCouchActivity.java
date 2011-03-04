package se.msc.android.droidcouch.ubuntuone;

import java.util.concurrent.SynchronousQueue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class DroidCouchActivity extends Activity {
	protected static final int ACTIVITY_LOGIN = 0xBEBEFE0;
	private SynchronousQueue<UserPassword> replyQueue;

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		Bundle extras = intent.getExtras();

		switch (requestCode) {
		case ACTIVITY_LOGIN:
			String newUsername = extras.getString(LoginActivity.RESULT_USERNAME);
			String newPassword = extras.getString(LoginActivity.RESULT_PASSWORD);
			UserPassword userpassword = new UserPassword(newUsername, newPassword);
			try {
				replyQueue.put(userpassword);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	public void promptUserPassword(SynchronousQueue<UserPassword> replyQueue) {
		this.replyQueue = replyQueue;
		Intent i = new Intent(this, LoginActivity.class);
		startActivityForResult(i, DroidCouchActivity.ACTIVITY_LOGIN);
	}
}