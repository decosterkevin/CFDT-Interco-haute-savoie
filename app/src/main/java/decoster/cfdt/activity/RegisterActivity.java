package decoster.cfdt.activity;

/**
 * Created by Decoster on 02/02/2016.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import decoster.cfdt.R;
import decoster.cfdt.helper.SQLiteHandler;
import decoster.cfdt.helper.SessionManager;

public class RegisterActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnRegister;
    private EditText inputFullName;
    private EditText inputEmail;
    private EditText inputSurname;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputFullName = (EditText) findViewById(R.id.name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputSurname = (EditText) findViewById(R.id.surname);
        btnRegister = (Button) findViewById(R.id.btnRegister);


        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String name = inputFullName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String surname = inputSurname.getText().toString().trim();

                if (!name.isEmpty() && !email.isEmpty() && !surname.isEmpty()) {
                    registerUser(surname, name, email, null);
                } else {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.warning_register), Toast.LENGTH_LONG)
                            .show();
                }
            }
        });


    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     */
    private void registerUser(final String name, final String surname, final String email,
                              final String xls_file) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        db.addUser(surname, name, email);
        session.setLogin(true);
        finish();


    }


}
