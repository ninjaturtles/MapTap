package ca.wlu.johnny.akanksha.maptap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;

import static ca.wlu.johnny.akanksha.maptap.MainActivity.sharedpreferences;

/**
 * Created by johnny on 2017-11-29.
 */

public class UserProfileActivity extends AppCompatActivity {

    private static final String ARG_USER  = "ca.wlu.johnny.akanksha.maptap.User";
    private static final String EXTRA_USER = "ca.wlu.johnny.akanksha.maptap.userEmail";
    private static final String ARG_SESSION_EXISTS = "ca.wlu.johnny.akanksha.maptap.sessionExists";


    private User mUser;
    private DbUtils mDbUtils;
    private TextView mUserNameTextView;
    private TextView mUserEmailTextView;
    private ImageButton mSaveImageButton;

    public static Intent newIntent(Context packageContext, String userEmail) {
        Intent intent = new Intent(packageContext, UserProfileActivity.class);
        intent.putExtra(EXTRA_USER, userEmail);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // setup database connection
        mDbUtils = DbUtils.get(this);

        if (savedInstanceState != null) {
            // retrieve state if not null
            onRestoreInstanceState(savedInstanceState);
        }else{
            String userEmail = getIntent().getStringExtra(EXTRA_USER);
            mUser=mDbUtils.getUser(userEmail);
        }

        setViews();
        setUserName();
        changeEmail();
        saveButton();

    } // onCreate

    private void setViews(){
        mUserNameTextView = findViewById(R.id.user_profile_name);
        mSaveImageButton = findViewById(R.id.save_button);
        mUserEmailTextView = findViewById(R.id.user_email);
    }

    private void setUserName(){
        mUserNameTextView.setText(mUser.getName());
    }

    private void changeEmail(){
        mUserEmailTextView.setText(mUser.getEmail());

        mUserEmailTextView.setCursorVisible(true);
        mUserEmailTextView.setFocusableInTouchMode(true);
        mUserEmailTextView.setInputType(InputType.TYPE_CLASS_TEXT);
        mUserEmailTextView.requestFocus(); //to trigger the soft input
    }

    private void saveButton(){
        mSaveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newEmail = mUserEmailTextView.getText().toString();
                if (newEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    mUserEmailTextView.setError("enter a valid email address");
                } else {
                    mDbUtils.updateEmail(newEmail,mUser);
                    onSaveSuccess();
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("email", mUser.getEmail());

        setResult(RESULT_OK, resultIntent);
    } // onBackPressed

    private void onSaveSuccess(){
        Toast.makeText(this,"Saved",Toast.LENGTH_SHORT).show();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("email", mUser.getEmail());

        setResult(RESULT_OK, resultIntent);

        finish();
    } // onSaveSuccess

    @Override
    public void onSaveInstanceState(Bundle savedStateInstance) {
        super.onSaveInstanceState(savedStateInstance);
        savedStateInstance.putParcelable(ARG_USER, mUser);
    } // onSaveInstanceState

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUser = savedInstanceState.getParcelable(ARG_USER);
    } // onRestoreInstanceState

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    } // onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actions_log_out:
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    } // onOptionsItemSelected

    private void logOut() {
        Toast.makeText(this, "See you soon, " + mUser.getName() + "!", Toast.LENGTH_LONG).show();
        mUser = null;

        disconnectFromFacebook();

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove(ARG_SESSION_EXISTS);
        editor.commit();

        // start log in activity
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    } // logOut

    private void disconnectFromFacebook() {

        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }

        LoginManager.getInstance().logOut();
    } // disconnectFromFacebook

} // UserProfileActivity