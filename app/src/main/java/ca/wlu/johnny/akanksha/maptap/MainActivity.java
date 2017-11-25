package ca.wlu.johnny.akanksha.maptap;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.SessionConfiguration;

public class MainActivity extends AppCompatActivity
        implements FragmentManager.OnBackStackChangedListener {

    // Constants
    private static final String FRAGMENT_PLACE_DETAILS = "ca.wlu.johnny.akanksha.maptap.PlaceDetailsFragment";
    private static final String ARG_USER  = "ca.wlu.johnny.akanksha.maptap.User";
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int SIGN_IN_REQUEST = 0;
    static final int REQUEST_LOCATION = 2;

    // global variables
    private SelectedPlace mSelectedPlace;
    public static SessionConfiguration config;
    public LocationManager mLocationManager;
    private User mUser;
    private DbUtils mDbUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Listen for changes in the back stack
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        //Handle when activity is recreated like on orientation Change
        shouldDisplayHomeUp();

        // setup database connection
        mDbUtils = DbUtils.get(this);

        // retrieve state if not null
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);

        } else {
            //TODO: leave commented out, for testing only
//          Intent intent = new Intent(this, SignInActivity.class);
//          startActivityForResult(intent, SIGN_IN_REQUEST);
            mUser = mDbUtils.getUser("akanksha@wlu.ca"); //for testing
            startPlacePickerAPI();
        }

        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        getLocation();
        configureUberSDK();

    } // onCreate

    private void startPlacePickerAPI() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            Intent placePickerIntent = builder.build(this);
            startActivityForResult(placePickerIntent, PLACE_PICKER_REQUEST);

        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();

        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    public void shouldDisplayHomeUp(){
        //Enable Up button only  if there are entries in the back stack
        boolean canback = getSupportFragmentManager().getBackStackEntryCount()>0;
        System.out.println(canback);
        getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
    }

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
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_settings:
                //TODO: settings menu here
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    } // onOptionsItemSelected

    @Override
    public void onBackPressed() {
        int theBackStackCount =
                getSupportFragmentManager().getBackStackEntryCount();

        if (theBackStackCount > 0) {
            getSupportFragmentManager().popBackStack();
            startPlacePickerAPI();
        } else {
            super.onBackPressed();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SIGN_IN_REQUEST) {

            String email = data.getStringExtra("email");
            mUser = mDbUtils.getUser(email);
            startPlacePickerAPI();
        }

        else if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);

                if(place.getName().equals("") && place.getAddress().equals("")) {
                    Toast.makeText(this, "No place chosen", Toast.LENGTH_SHORT).show();
                    startPlacePickerAPI();
                    return;
                }

                String name = place.getName().toString();
                String address = place.getAddress().toString();
                String phoneNumber = (place.getPhoneNumber() == null) ? "N/A" : place.getPhoneNumber().toString();
                String url = (place.getWebsiteUri() == null) ? "N/A" : place.getWebsiteUri().toString();
                String latLng = place.getLatLng().toString();
                String type = getPlaceType(place.getPlaceTypes().get(0));
                float rating = place.getRating();
                int price=place.getPriceLevel();

                mSelectedPlace = new SelectedPlace(name, address, phoneNumber, url, latLng, type, price, rating);


                FragmentManager fm = getSupportFragmentManager();
                Fragment fragment = fm.findFragmentById(R.id.fragment_container);

                if (fragment == null) {

                    fragment = PlaceDetailsFragment.newInstance(mSelectedPlace, mUser);
                    fm.beginTransaction().add(R.id.fragment_container, fragment)
                            .addToBackStack(FRAGMENT_PLACE_DETAILS)
                            .commitAllowingStateLoss();
                }
            }
        }
    } // onActivityResult

    public String getPlaceType(int myPlaceType){

        Field[] fields = Place.class.getDeclaredFields();

        for(Field field :fields)

        {
            Class<?> type = field.getType();

            if (type == int.class) {
                try {
                    if (myPlaceType == field.getInt(null)) {
                        Log.i("Testing", "onCreate: " + field.getName());
                        String[] name = field.getName().split("_");
                        String types = "";
                        for (int i = 1; i < name.length; i++) {
                            types = types + name[i] + " ";
                        }
                        String s1 = types.substring(0, 1).toUpperCase();
                        String nameCapitalized = s1 + types.substring(1).toLowerCase();
                        return nameCapitalized;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return "error";

    } // getPlaceType

    private void configureUberSDK(){
        config = new SessionConfiguration.Builder()
                // mandatory
                .setClientId("uhIYTbs7oF1njNDMbXX-NPQXMdiz3Ymz")
                // required for enhanced button features
                .setServerToken("7236uz0ebZ4u6vfuVp85vsfhtOqNsHhc0v1-Xr_y")
                // required for implicit grant authentication
                .setRedirectUri("https://localhost:8000")
                // required scope for Ride Request Widget features
                .setScopes(Arrays.asList(Scope.RIDE_WIDGETS))
                // optional: set sandbox as operating environment
                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .build();

        UberSdk.initialize(config);
    }

    void getLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null){
                double lat= location.getLatitude();
                double lng = location.getLongitude();
                mUser.setLat(lat);
                mUser.setLng(lng);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_LOCATION:
                getLocation();
                break;
        }
    }

} // MainActivity