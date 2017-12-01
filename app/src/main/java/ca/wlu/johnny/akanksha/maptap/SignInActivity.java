package ca.wlu.johnny.akanksha.maptap;

/**
 * Created by Akanksha on 2017-11-20.
 */

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SignInActivity extends AppCompatActivity {

    private static final String ARG_SESSION_EXISTS = "ca.wlu.johnny.akanksha.maptap.sessionExists";
    private static final String GENERIC_PASSWORD = "ABC123YX";
    private static final String TAG = "SignInActivity";
    private static final int REQUEST_SIGNUP = 0;
    private CallbackManager mCallbackManager;
    private LoginButton mLoginButton;

    private DbUtils mDbUtils;
    private User mUser;

    @InjectView(R.id.input_email) EditText emailText;
    @InjectView(R.id.input_password) EditText passwordText;
    @InjectView(R.id.btn_login) Button loginButton;
    @InjectView(R.id.link_signup) TextView signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.inject(this);

        mDbUtils = DbUtils.get(this);

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "--------loginButton clicked-------");
                login();
            }
        });

        facebookLoginAuxiliary();

        signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Sign up activity
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    private void facebookLoginAuxiliary() {
        mCallbackManager = CallbackManager.Factory.create();

        mLoginButton = (LoginButton) findViewById(R.id.login_button);
        mLoginButton.setReadPermissions(Arrays.asList("email"));
        mLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {

                String accessToken = loginResult.getAccessToken().getToken();
                Log.i("accessToken", accessToken);

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {

                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.i("LoginActivity", response.toString());
                                // Get facebook data from login
                                Bundle bFacebookData = getFacebookData(object);
                                if(!isAlreadyRegistered(bFacebookData.getString("email"))){
                                    createNewUserAccount(bFacebookData);
                                }
                                // On complete call onSignInSuccess()
                                onSignInSuccess();
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // left empty intentionally
            }

            @Override
            public void onError(FacebookException e) {
                // left empty intentionally
            }
        });
    }

    private Bundle getFacebookData(JSONObject object) {

        try {
            Bundle bundle = new Bundle();
            String id = object.getString("id");

            try {
                URL profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?width=200&height=150");
                Log.i("profile_pic", profile_pic + "");
                bundle.putString("profile_pic", profile_pic.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            bundle.putString("idFacebook", id);
            if (object.has("first_name"))
                bundle.putString("first_name", object.getString("first_name"));
            if (object.has("last_name"))
                bundle.putString("last_name", object.getString("last_name"));
            if (object.has("email"))
                bundle.putString("email", object.getString("email"));
            return bundle;
        }
        catch(JSONException e) {
            Log.d(TAG,"Error parsing JSON");
        }
        return null;
    }

    private boolean isAlreadyRegistered(String email) {
        mUser = mDbUtils.getUser(email);
        if (mUser == null) {
            return false;
        }
        return true;
    }

    private void createNewUserAccount(Bundle bFacebookData) {
        String name = bFacebookData.getString("first_name");
        String email = bFacebookData.getString("email");
        User newUser = new User(name, email, GENERIC_PASSWORD);
        mDbUtils.addUser(newUser);
        mUser = newUser;
    }

    public void login() {
        Log.d(TAG, "--------login-------");

        if (!validate()) {
            onValidateFailed();
            return;
        }

        loginButton.setEnabled(false);

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

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
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                String email = data.getStringExtra("email");
                mUser = mDbUtils.getUser(email);

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

        Toast.makeText(getBaseContext(), "Signed in successfully", Toast.LENGTH_SHORT).show();

        loginButton.setEnabled(true);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("email", mUser.getEmail());

        setResult(RESULT_OK, resultIntent);

        // store session preference
        SharedPreferences.Editor editor = MainActivity.sharedpreferences.edit();
        editor.putString(ARG_SESSION_EXISTS, mUser.getEmail());
        editor.commit();

        finish();
    }

    public void onValidateFailed() {
        Log.d(TAG, "--------failed validation-------");
        Toast.makeText(getBaseContext(), "Sign in failed", Toast.LENGTH_SHORT).show();
        loginButton.setEnabled(true);
    }

    private void onAuthenticateFailed() {
        Log.d(TAG, "--------failed authentication-------");

        Toast.makeText(getBaseContext(), "Incorrect email or password", Toast.LENGTH_SHORT).show();

        loginButton.setEnabled(true);
    }

    public boolean authenticate(String email, String password) {
        Log.d(TAG, "--------authenticate-------");

        boolean authentic = true;

        mUser = mDbUtils.getUser(email);

        if (mUser == null) {
            authentic = false;
        } else if (!mUser.getEmail().equals(email)) {
            authentic = false;
        } else if (!mUser.getPassword().equals(password)) {
            authentic = false;
        }

        return authentic;
    }

    public boolean validate() {
        Log.d(TAG, "--------validate-------");

        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

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
}//End of class