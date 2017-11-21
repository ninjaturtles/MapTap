package ca.wlu.johnny.akanksha.maptap;

/**
 * Created by Akanksha on 2017-11-20.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";

    private DbUtils mDbUtils;

    @InjectView(R.id.input_name) EditText _nameText;
    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_signup) Button _signupButton;
    @InjectView(R.id.link_login) TextView _loginLink;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, SignUpActivity.class);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.inject(this);

        mDbUtils = DbUtils.get(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignUpFailed();
            return;
        }

        _signupButton.setEnabled(false);

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        // sign up logic.
        if (isUnique(email)) {
            User newUser = new User(name, email, password);
            mDbUtils.addUser(newUser);
            onSignUpSuccess();

        } else {
            // error, user exists
            onUserAlreadyExists();

        }
    }


    public void onSignUpSuccess() {
        Toast.makeText(getBaseContext(), "Sign up successfully", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignUpFailed() {
        Toast.makeText(getBaseContext(), "Sign up failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public void onUserAlreadyExists() {
        Toast.makeText(getBaseContext(), "Email already exists", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean isUnique(String email) {

        Boolean unique = false;

        User user = mDbUtils.getUser(email);

        if (user == null) {
            unique = true;
        }

        return unique;
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

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

