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

    @InjectView(R.id.input_name) EditText nameText;
    @InjectView(R.id.input_email) EditText emailText;
    @InjectView(R.id.input_password) EditText passwordText;
    @InjectView(R.id.btn_signup) Button signupButton;
    @InjectView(R.id.link_login) TextView loginLink;

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

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
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

        signupButton.setEnabled(false);

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

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

        signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignUpFailed() {
        Toast.makeText(getBaseContext(), "Sign up failed", Toast.LENGTH_LONG).show();

        signupButton.setEnabled(true);
    }

    public void onUserAlreadyExists() {
        Toast.makeText(getBaseContext(), "Email already exists", Toast.LENGTH_LONG).show();

        signupButton.setEnabled(true);
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

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            nameText.setError("at least 3 characters");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }
}

