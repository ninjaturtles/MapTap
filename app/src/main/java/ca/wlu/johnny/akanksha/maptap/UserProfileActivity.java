package ca.wlu.johnny.akanksha.maptap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;
import java.util.List;

import static ca.wlu.johnny.akanksha.maptap.MainActivity.sharedpreferences;

/**
 * Created by johnny on 2017-11-29.
 */

public class UserProfileActivity extends AppCompatActivity {

    private static final String ARG_USER  = "ca.wlu.johnny.akanksha.maptap.User";
    private static final String ARG_PROFILE_PHOTO_FILE_PATH  = "ca.wlu.johnny.akanksha.maptap.PROFILE_PHOTO_FILE_PATH";
    private static final String EXTRA_USER = "ca.wlu.johnny.akanksha.maptap.userEmail";
    private static final String ARG_SESSION_EXISTS = "ca.wlu.johnny.akanksha.maptap.sessionExists";

    private static final int REQUEST_PHOTO = 0;

    private User mUser;
    private DbUtils mDbUtils;
    private TextView mUserNameTextView;
    private TextView mUserEmailTextView;
    private ImageButton mSaveImageButton;
    private TextView mUserPasswordTextView;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private File mPhotoFile;

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
        } else {
            String userEmail = getIntent().getStringExtra(EXTRA_USER);
            mUser = mDbUtils.getUser(userEmail);
        }

        mPhotoFile = mDbUtils.getPhotoFile(mUser);

        setViews();
        setUserName();
        changeEmail();
        changePw();
        saveButton();
        handleCameraIntent();

    } // onCreate

    private void handleCameraIntent() {
        PackageManager packageManager = this.getPackageManager();

        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(UserProfileActivity.this,
                        "ca.wlu.johnny.akanksha.maptap.fileprovider",
                        mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                List<ResolveInfo> cameraActivities = UserProfileActivity.this
                        .getPackageManager().queryIntentActivities(captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities) {
                    UserProfileActivity.this.grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        updatePhotoView();
    }

    private void setViews(){
        mUserNameTextView = findViewById(R.id.user_profile_name);
        mSaveImageButton = findViewById(R.id.save_button);
        mUserEmailTextView = findViewById(R.id.user_email);
        mUserPasswordTextView = findViewById(R.id.user_pw);
        mPhotoButton = findViewById(R.id.user_camera);
        mPhotoView = findViewById(R.id.user_profile_photo);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

       if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(this,
                    "ca.wlu.johnny.akanksha.maptap.fileprovider",
                    mPhotoFile);

            this.revokeUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            updatePhotoView();
        }
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);

        } else {

            ExifInterface exif = null;
            try {
                exif = new ExifInterface(mPhotoFile.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), this);

            Bitmap rotateBitmap = PictureUtils.rotateBitmap(
                    bitmap, orientation);

            Bitmap croppedBitmap = PictureUtils.getCircularBitmap(rotateBitmap);

            Glide.with(this).load(croppedBitmap).into(mPhotoView);
        }
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
                String newPw = mUserPasswordTextView.getText().toString();

                if ((newEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches())){
                        mUserEmailTextView.setError("enter a valid email address");
                } else if (((newPw.isEmpty() || newPw.length() < 4 || newPw.length() > 10))) {
                        mUserPasswordTextView.setError("between 4 and 10 alphanumeric characters");

                } else {
                    mDbUtils.updatePw(newPw,mUser);
                    mDbUtils.updateEmail(newEmail,mUser);
                    onSaveSuccess();
                }
            }
        });
    }

    private void changePw(){
        mUserPasswordTextView.setCursorVisible(true);
        mUserPasswordTextView.setFocusableInTouchMode(true);
        mUserPasswordTextView.requestFocus(); //to trigger the soft input
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
        Toast.makeText(this, "See you soon, " + mUser.getName() + "!", Toast.LENGTH_SHORT).show();
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