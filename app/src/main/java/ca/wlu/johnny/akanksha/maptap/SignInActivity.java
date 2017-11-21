package ca.wlu.johnny.akanksha.maptap;

/**
 * Created by Akanksha on 2017-11-20.
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";
    private static final int REQUEST_SIGNUP = 0;

    private DbUtils mDbUtils;

    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_login) Button _loginButton;
    @InjectView(R.id.link_signup) TextView _signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.inject(this);

        mDbUtils = DbUtils.get(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "--------_loginButton clicked-------");
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Sign up activity
                Log.d(TAG, "--------_signupLink clicked-------");
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    public void login() {
        Log.d(TAG, "--------login-------");

        if (!validate()) {
            onValidateFailed();
            return;
        }

        _loginButton.setEnabled(false);

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        // authentication logic
        if (!authenticate(email, password)) {
            onAuthenticateFailed();
            return;
        }

        // On complete call onSignInSuccess()
        onSignInSuccess();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                //successful sign in logic
                onSignInSuccess();
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onSignInSuccess() {
        Log.d(TAG, "--------signed in successfully-------");

        Toast.makeText(getBaseContext(), "Sign in successfully", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
        finish();
    }

    public void onValidateFailed() {
        Log.d(TAG, "--------failed validation-------");

        Toast.makeText(getBaseContext(), "Sign in failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    private void onAuthenticateFailed() {
        Log.d(TAG, "--------failed authentication-------");

        Toast.makeText(getBaseContext(), "Incorrect email or password", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean authenticate(String email, String password) {
        Log.d(TAG, "--------authenticate-------");

        boolean authentic = true;

        User user = mDbUtils.getUser(email);

        if (user == null) {
            authentic = false;
        } else if (!user.getEmail().equals(email)) {
            authentic = false;
        } else if (!user.getPassword().equals(password)) {
            authentic = false;
        }

        return authentic;
    }

    public boolean validate() {
        Log.d(TAG, "--------validate-------");

        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}
