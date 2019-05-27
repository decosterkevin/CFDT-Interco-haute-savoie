package decoster.cfdt.activity;

/**
 * Created by Decoster on 02/02/2016.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import decoster.cfdt.AppConfig;
import decoster.cfdt.R;
import decoster.cfdt.helper.SQLiteHandler;
import decoster.cfdt.helper.SessionManager;

public class RegisterActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnRegister;
    private EditText inputFullName;
    private EditText inputEmail;
    private EditText inputSurname;
    private EditText inputAccessCode;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputFullName = (EditText) findViewById(R.id.name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputSurname = (EditText) findViewById(R.id.surname);
        inputAccessCode = (EditText) findViewById(R.id.access_code);
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
                String accessCode = inputAccessCode.getText().toString().trim();
                if (!name.isEmpty() && !email.isEmpty() && !surname.isEmpty() && !accessCode.isEmpty()) {
                    registerUser(surname, name, email, accessCode);
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
    private void registerUser(final String name, final String surname, final String email, final String accessCode) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = AppConfig.SERVER_URL + "/api/" + accessCode;
        final Activity that = this;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            // Display the first 500 characters of the response string.
                            db.addUser(surname, name, email, accessCode, jsonObject.getString("gdriveUrl"));
                            session.setLogin(true);
                            that.finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.warning_register), Toast.LENGTH_LONG)
                        .show();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);


    }


}
