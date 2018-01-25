package decoster.cfdt.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import decoster.cfdt.R;
import decoster.cfdt.helper.MailSender;
import decoster.cfdt.helper.PasswordToken;

/**
 * Created by Decoster on 19/05/2016.
 */
public class PasswordActivity extends Activity{
    private EditText password;
    private Button btnPassword;
    private double initialTime = (long)1000;
    private Context context;
    private double currentTime = 0;
    private int numbWrongPw = 0;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        context = this;
        password = (EditText) findViewById(R.id.password);
        btnPassword = (Button) findViewById(R.id.btnPassword);
        btnPassword.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String pw =password.getText().toString();
                String token = PasswordToken.makeDigest(pw);
                //... GET token using the shared preferences

                if (PasswordToken.validate(token, PasswordToken.makeDigest(getString(R.string.app_secret)))) {
                    finish();
                }
                else {
                    password.setText("");
                    numbWrongPw += 1;
                    btnPassword.setBackgroundColor(ContextCompat.getColor(context ,R.color.cfdt_primary));
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.warning_password) +" " + (int)currentTime/1000 + " seconds", Toast.LENGTH_LONG)
                            .show();
                    btnPassword.setClickable(false);
                    currentTime = initialTime*Math.exp(numbWrongPw/2.0);
                    Log.d(MainActivity.class.getSimpleName(), Double.toString(currentTime));
                    new CountDownTimer((long)currentTime, (long)currentTime) {

                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
                            btnPassword.setBackgroundColor(ContextCompat.getColor(context ,R.color.cfdt_tertiaire));
                            btnPassword.setClickable(true);
                        }
                    }.start();

                }
            }
            });

    }

    @Override
    public void onBackPressed() {

    }
}
