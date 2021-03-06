package se.msc.android.droidcouch;

import org.json.JSONObject;

import se.msc.android.droidcouch.ubuntuone.DroidCouchActivity;
import se.msc.android.droidcouch.ubuntuone.UbuntuOneDroidCouch;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Tests for all DroidCouch methods.  
 */
public class TestClient extends DroidCouchActivity {
    String DBNAME = "hackathon_83";
    String TEST_DOC_ID = "test_document_1";

	private DroidCouch droidCouch;
	private TextView view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        view = new TextView(this);
        view.append("Running tests...\n\n");
        setContentView(view);
        
        new Thread(new Runnable() {
			@Override
			public void run() {
				runAllTests();
			}
		}).run();
    }
    
	private void runAllTests() {
		try {
        	droidCouch = new UbuntuOneDroidCouch(this);
            testDatabaseDoesNotExist();
            testCreateDatabase();
            testGetDatabaseInfo();
            testCreateDocument();
            testGetDocument();
            testGetAllDocuments();
            testUpdateDocument();
            testDeleteDocument();
            testDeleteDatabase();
            appendView("All tests passed!");
        } catch (Exception e) {
            appendView(DroidCouch.getStacktrace(e));
        }
	}

    private void appendView(final String message) {
    	runOnUiThread(new Runnable() {
			@Override
			public void run() {
				view.append(message);
			}
		});
	}

	public void testCreateDatabase() throws Exception {
        boolean result = droidCouch.createDatabase(DBNAME);
        shouldBeTrue(result, "Database could not be created!");
    }

    public void testDatabaseDoesNotExist() throws Exception {
        String couchUrl = DBNAME;
        JSONObject responseObject = droidCouch.get(couchUrl);
        String error = responseObject.getString("error");
        String reason = responseObject.getString("reason");
        shouldBeTrue(error.equals("not_found"), "Incorrect message received");
        shouldBeTrue(reason.equals("no_db_file"), "Incorrect message received");
    }

    public void testDeleteDatabase() throws Exception {
        boolean result = droidCouch.deleteDatabase(DBNAME);
        shouldBeTrue(result, "Database could not be deleted!");
    }

    public void testGetDatabaseInfo() throws Exception {
        String couchUrl = DBNAME;
        JSONObject responseObject = droidCouch.get(couchUrl);
        String actualDbName = responseObject.getString("db_name");
        shouldBeTrue(actualDbName.endsWith(DBNAME), "Incorrect message received");
    }

    public void testCreateDocument() throws Exception {
        JSONObject json = new JSONObject().put("test_key_1", "test_value_1")
                .put("test_key_2", "test_value_2");
        String revision = droidCouch.createDocument(DBNAME, TEST_DOC_ID,
                json);
        shouldBeTrue(null != revision, "Document has no revision number");
    }

    public void testGetDocument() throws Exception {
        JSONObject doc = droidCouch.getDocument(DBNAME, TEST_DOC_ID);
        shouldBeTrue(null != doc, "Document is null");
        shouldBeTrue(doc.getString("test_key_2").equals("test_value_2"),
                "Wrong value");
    }

    public void testDeleteDocument() throws Exception {
        boolean result = droidCouch.deleteDocument(DBNAME, TEST_DOC_ID);
        shouldBeTrue(result, "Document could not be deleted!");
    }

    public void testUpdateDocument() throws Exception {
        JSONObject doc = droidCouch.getDocument(DBNAME, TEST_DOC_ID);
        String oldRevision = doc.getString("_rev");
        doc.put("test_key_1", "updated_test_value_1");
        String newRevision = droidCouch.updateDocument(DBNAME, doc);
        shouldBeTrue(null != newRevision, "Revision is null");
        shouldBeTrue(!oldRevision.equals(newRevision),
                "Revision has not changed!");
        JSONObject newDoc = droidCouch.getDocument(DBNAME, TEST_DOC_ID);
        shouldBeTrue(newDoc.getString("test_key_1").equals(
                "updated_test_value_1"), "Wrong value");
    }

    public void testGetAllDocuments() throws Exception {
        JSONObject responseObject = droidCouch.getAllDocuments(DBNAME);
        shouldBeTrue(responseObject.has("total_rows"),
                "Incorrect message recieved");
    }
 
    public void testPutAttachment() throws Exception {
    }

    public void testGetAttachment() throws Exception {
    }

    private void shouldBeTrue(boolean mustBeTrue, String message)
            throws Exception {
        if (!mustBeTrue)
            throw new Exception("Assert failed: " + message);
    }
}