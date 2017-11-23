package ca.wlu.johnny.akanksha.maptap;

import android.content.Intent;
import android.net.Uri;
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

public class MainActivity extends AppCompatActivity {

    // Constants
    private int PLACE_PICKER_REQUEST = 1;
    private int SIGN_IN_REQUEST = 0;
    private static final String BUNDLE_STATE_CODE = "ca.wlu.johnny.akanksha.maptap.MainActivity";

    private SelectedPlace mSelectedPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // retrieve state if not null
        if (savedInstanceState != null) {
//            Bundle bundle = savedInstanceState.getBundle(BUNDLE_STATE_CODE);
            onRestoreInstanceState(savedInstanceState);

        } else {
            //        Intent intent = new Intent(this, SignInActivity.class);
            //        startActivityForResult(intent, SIGN_IN_REQUEST);


            // for testing only
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
    } // onCreate

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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    } // onOptionsItemSelected

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SIGN_IN_REQUEST) {

//            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
//
//            try {
//                Intent placePickerIntent = builder.build(getApplicationContext());
//                startActivityForResult(placePickerIntent, PLACE_PICKER_REQUEST);
//
//            } catch (GooglePlayServicesRepairableException e) {
//                e.printStackTrace();
//
//            } catch (GooglePlayServicesNotAvailableException e ) {
//                e.printStackTrace();
//            }
        }

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);

                // TODO: handle onAddressSelected()
//                String address = String.format("Place is: %s ", place.getAddress());
//                System.out.println("---------------- " + address + " ---------------------");
                System.out.println(place.getName());
                System.out.println(place.getPhoneNumber());
                System.out.println(place.getWebsiteUri());
                System.out.println(place.getLatLng());
                System.out.println(getPlaceType(place.getPlaceTypes().get(0)));
                System.out.println(place.getPlaceTypes());

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
                String price = place.getPlaceTypes().toString();

                mSelectedPlace = new SelectedPlace(name, address, phoneNumber, url, latLng, type, price);

                FragmentManager fm = getSupportFragmentManager();
                Fragment fragment = fm.findFragmentById(R.id.fragment_container);

                if (fragment == null) {
                    fragment = PlaceDetailsFragment.newInstance(mSelectedPlace);
                    fm.beginTransaction().add(R.id.fragment_container, fragment).commitAllowingStateLoss();
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