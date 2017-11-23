package ca.wlu.johnny.akanksha.maptap;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity
    implements FragmentManager.OnBackStackChangedListener {

    // Constants
    private int PLACE_PICKER_REQUEST = 1;
    private int SIGN_IN_REQUEST = 0;
    private static final String BUNDLE_STATE_CODE = "ca.wlu.johnny.akanksha.maptap.MainActivity";
    private static final String FRAGMENT_PLACE_DETAILS = "ca.wlu.johnny.akanksha.maptap.PlaceDetailsFragment";

    private SelectedPlace mSelectedPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Listen for changes in the back stack
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        //Handle when activity is recreated like on orientation Change
        shouldDisplayHomeUp();

        // retrieve state if not null
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);

        } else {
            // leave commented out, for testing only
//            Intent intent = new Intent(this, SignInActivity.class);
//            startActivityForResult(intent, SIGN_IN_REQUEST);

            // for testing only
            startPlacePickerAPI();
        }
    } // onCreate

    private void startPlacePickerAPI() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            Intent placePickerIntent = builder.build(getApplicationContext());
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
//        savedStateInstance.putBundle(BUNDLE_STATE_CODE, savedStateInstance);
    } // onSaveInstanceState

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

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
        System.out.println(theBackStackCount);
        if (theBackStackCount > 0) {
            getSupportFragmentManager().popBackStack();
            startPlacePickerAPI();
        } else {
            super.onBackPressed();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//          leave commented out, for testing only
//        if (requestCode == SIGN_IN_REQUEST) {
//
//        startPlacePickerAPI();
//
//        }

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);

                // TODO: handle onAddressSelected()
//                String address = String.format("Place is: %s ", place.getAddress());
//                System.out.println("-------------------------------------");
                System.out.println(place.getName());
                System.out.println(place.getPhoneNumber());
                System.out.println(place.getWebsiteUri());
                System.out.println(place.getLatLng());
                System.out.println(getPlaceType(place.getPlaceTypes().get(0)));
                System.out.println(place.getPlaceTypes());
                System.out.println(place.getPriceLevel());
                System.out.println(place.getRating());


                String name = place.getName().toString();
                String address = place.getAddress().toString();
                String phoneNumber = place.getPhoneNumber().toString();
                String url;
                if (place.getWebsiteUri() != null){
                    url = place.getWebsiteUri().toString();
                } else {
                    url = null;
                }

                String latLng = place.getLatLng().toString();
                String type = getPlaceType(place.getPlaceTypes().get(0));
                int price = place.getPriceLevel();

                mSelectedPlace = new SelectedPlace(name, address, phoneNumber, url, latLng, type, price);

                FragmentManager fm = getSupportFragmentManager();
                Fragment fragment = fm.findFragmentById(R.id.fragment_container);

                if (fragment == null) {

                    fragment = PlaceDetailsFragment.newInstance(mSelectedPlace);
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
                        return field.getName();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return "error";

    } // getPlaceType

} // MainActivity