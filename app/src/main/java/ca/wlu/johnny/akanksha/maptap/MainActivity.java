package ca.wlu.johnny.akanksha.maptap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.SessionConfiguration;

public class MainActivity extends AppCompatActivity
        implements FragmentManager.OnBackStackChangedListener {

    // Constants
    private static final String FRAGMENT_PLACE_DETAILS = "ca.wlu.johnny.akanksha.maptap.PlaceDetailsFragment";
    private static final String ARG_USER  = "ca.wlu.johnny.akanksha.maptap.User";
    private static final String ARG_WIDGETS_VISIBILITY  = "ca.wlu.johnny.akanksha.maptap.widgetsVisibility";
    private static final String ARG_SHARED_PREFERENCE = "ca.wlu.johnny.akanksha.maptap.sharedPerefernce";
    private static final String ARG_SESSION_EXISTS = "ca.wlu.johnny.akanksha.maptap.sessionExists";

    private static final int SIGN_IN_REQUEST = 0;
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int REQUEST_LOCATION = 2;
    private static final int SHARED_PREFERENCE_REQUEST = 3;
    private static final int PROFILE_REQUEST = 4;

    // global variables
    protected static SessionConfiguration config;
    protected static SharedPreferences sharedpreferences;
    protected LocationManager mLocationManager;
    private SelectedPlace mSelectedPlace;
    private User mUser;
    private DbUtils mDbUtils;
    private Button mLaunchPlacePickerButton;
    private TextView mFavPlacesLabel;
    private TextView mEmptyView;
    private RecyclerView mFavPlacesRecyclerView;
    private FavouritePlacesAdapter mFavPlacesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Map Tap");

        //Listen for changes in the back stack
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        shouldDisplayHomeUp();

        // setup main activity widgets
        setupMainActivityWidgets();

        // setup database connection
        mDbUtils = DbUtils.get(this);

        // verify if the user has "session" started or not
        sharedpreferences = getApplicationContext().getSharedPreferences(ARG_SHARED_PREFERENCE, SHARED_PREFERENCE_REQUEST);
        String userEmail = sharedpreferences.getString(ARG_SESSION_EXISTS, null);

        // if session exists, no need to login again
        if (userEmail != null && savedInstanceState == null) {
            mUser = mDbUtils.getUser(userEmail);
            startPlacePickerAPI();

        } else if (savedInstanceState != null) {
            // retrieve state if not null
            onRestoreInstanceState(savedInstanceState);

        } else {
            // start log in activity
            Intent intent = new Intent(this, SignInActivity.class);
            startActivityForResult(intent, SIGN_IN_REQUEST);
        }

        // setup location manager
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // fetch user location and favourite places if user is not null
        if (mUser != null) {
            getLocation();
            displayActivityWidgets();
            updateFavPlacesDataSet();
        }

        // setup uber request a ride api
        configureUberSDK();

    } // onCreate

    private void updateFavPlacesDataSet() {
        List<SelectedPlace> places = mDbUtils.getPlaces(mUser.getEmail());

        // update favorite places data set
        if (mFavPlacesAdapter == null) {
            mFavPlacesAdapter = new FavouritePlacesAdapter(places);
            mFavPlacesRecyclerView.setAdapter(mFavPlacesAdapter);

        } else {

            mFavPlacesAdapter.setPlaces(places);
            mFavPlacesAdapter.notifyDataSetChanged();
        }
    }

    private void updateRecyclerViewVisibility() {
        List<SelectedPlace> places = mDbUtils.getPlaces(mUser.getEmail());

        // set recycler view visibility
        if (places.isEmpty()) {
            mFavPlacesRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }

        else {
            mFavPlacesRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        }
    }

    private void displayActivityWidgets () {
        mLaunchPlacePickerButton.setVisibility(View.VISIBLE);
        mFavPlacesLabel.setVisibility(View.VISIBLE);
        updateRecyclerViewVisibility();

    }

    private void undisplayActivityWidgets() {
        mLaunchPlacePickerButton.setVisibility(View.GONE);
        mFavPlacesLabel.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mFavPlacesRecyclerView.setVisibility(View.GONE);

    }

    private void setupMainActivityWidgets() {
        mEmptyView = findViewById(R.id.empty_view);
        mFavPlacesRecyclerView = findViewById(R.id.fav_place_recycler_view);
        mFavPlacesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mFavPlacesLabel = findViewById(R.id.fav_place_label);

        mLaunchPlacePickerButton = findViewById(R.id.launch_place_picker_button);
        mLaunchPlacePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPlacePickerAPI();
            }
        });
    }

    private boolean isMainActivityWidgetsVisible() {

        if (mLaunchPlacePickerButton.getVisibility() == View.VISIBLE
                && mFavPlacesLabel.getVisibility() == View.VISIBLE)  {
            return true;
        }
        return false;
    }

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
    }

    @Override
    public void onSaveInstanceState(Bundle savedStateInstance) {
        super.onSaveInstanceState(savedStateInstance);
        savedStateInstance.putParcelable(ARG_USER, mUser);
        Boolean isVisible = isMainActivityWidgetsVisible();
        savedStateInstance.putBoolean(ARG_WIDGETS_VISIBILITY, isVisible);
    } // onSaveInstanceState

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUser = savedInstanceState.getParcelable(ARG_USER);
        Boolean isVisible = savedInstanceState.getBoolean(ARG_WIDGETS_VISIBILITY);
        if (isVisible) {
            displayActivityWidgets();
        } else {
            undisplayActivityWidgets();
        }
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
            case R.id.action_profile:
                Intent userProfileIntent = UserProfileActivity.newIntent(this, mUser.getEmail());
                startActivityForResult(userProfileIntent,PROFILE_REQUEST);
                return true;
            case R.id.actions_log_out:
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    } // onOptionsItemSelected

    @Override
    public void onBackPressed() {
        getSupportActionBar().setTitle("Map Tap");

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
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST) {

            String email = data.getStringExtra("email");
            mUser = mDbUtils.getUser(email);
            getLocation();
            startPlacePickerAPI();
        }

        else if (requestCode == PLACE_PICKER_REQUEST) {

            String email = sharedpreferences.getString(ARG_SESSION_EXISTS, null);
            mUser = mDbUtils.getUser(email);

            displayActivityWidgets();

            if (resultCode == RESULT_OK) {

                Place place = PlacePicker.getPlace(this, data);

                if(place.getName().equals("") && place.getAddress().equals("")) {
                    Toast.makeText(this, "No place chosen", Toast.LENGTH_SHORT).show();
                    startPlacePickerAPI();
                    return;
                }

                processPlaceAttributes(place);

                FragmentManager fm = getSupportFragmentManager();
                Fragment fragment = fm.findFragmentById(R.id.fragment_container);

                if (fragment == null) {

                    fragment = PlaceDetailsFragment.newInstance(mSelectedPlace, mUser);
                    fm.beginTransaction().add(R.id.fragment_container, fragment)
                            .addToBackStack(FRAGMENT_PLACE_DETAILS)
                            .commitAllowingStateLoss();

                    undisplayActivityWidgets();

                }
            }
        }

        else if (requestCode == PROFILE_REQUEST) {
            if (resultCode == RESULT_OK) {
                String email = data.getStringExtra("email");
                mUser = mDbUtils.getUser(email);
                getLocation();
                startPlacePickerAPI();
            }else{
                String email = sharedpreferences.getString(ARG_SESSION_EXISTS, null);
                mUser = mDbUtils.getUser(email);
            }
        }

        updateFavPlacesDataSet();
    } // onActivityResult

    private void processPlaceAttributes(Place place) {
        String id = place.getId();
        String name = place.getName().toString();
        String address = place.getAddress().toString();
        String phoneNumber = (place.getPhoneNumber() == null) ? "N/A"
                : (place.getPhoneNumber().equals("")) ? "N/A"
                : place.getPhoneNumber().toString();
        String url = (place.getWebsiteUri() == null) ? "N/A" : place.getWebsiteUri().toString();
        String latLng = place.getLatLng().toString();
        String type = getPlaceType(place.getPlaceTypes().get(0));
        float rating = place.getRating();
        int price=place.getPriceLevel();

        mSelectedPlace = new SelectedPlace(id, name, address, phoneNumber, url,
                latLng, type, price, rating, mUser.getEmail());
    }

    private String getPlaceType(int myPlaceType){

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

    private void getLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null && mUser != null){
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

    private void logOut() {
        Toast.makeText(this, "See you soon, " + mUser.getName() + "!", Toast.LENGTH_LONG).show();
        mUser = null;

        disconnectFromFacebook();

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove(ARG_SESSION_EXISTS);
        editor.commit();

        // start log in activity
        Intent intent = new Intent(this, SignInActivity.class);
        startActivityForResult(intent, SIGN_IN_REQUEST);

        getSupportFragmentManager().popBackStack();
    } // logOut


    private void disconnectFromFacebook() {

        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }

        LoginManager.getInstance().logOut();
    } // disconnectFromFacebook

    private class FavouritePlacesAdapter extends RecyclerView.Adapter<FavouritePlacesHolder>{
        private List<SelectedPlace> mPlaces;

        public FavouritePlacesAdapter(List<SelectedPlace> places){
            mPlaces = places;
        }

        @Override
        public FavouritePlacesHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            return new FavouritePlacesHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(FavouritePlacesHolder holder, int position){
            SelectedPlace selectedPlace = mPlaces.get(position);
            holder.bind(selectedPlace);
        }

        @Override
        public int getItemCount(){
            return mPlaces.size();
        }

        public void setPlaces(List<SelectedPlace> places) {
            mPlaces = places;
        }

    }

    private class FavouritePlacesHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        private ImageView mFavPlaceImageView;
        private TextView mFavPlaceNameTextView;
        private TextView mFavPlaceTypeTextView;
        private TextView mFavPlaceAddressTextView;
        private SelectedPlace mPlace;
        private Resources res;
        private GeoDataClient mGeoDataClient;


        public FavouritePlacesHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_fav_place, parent, false));
            itemView.setOnClickListener(this);

            mGeoDataClient = Places.getGeoDataClient(MainActivity.this, null);
            res = getResources();

            mFavPlaceImageView = itemView.findViewById(R.id.fav_place_photo);
            mFavPlaceNameTextView = itemView.findViewById(R.id.fav_place_name);
            mFavPlaceTypeTextView = itemView.findViewById(R.id.fav_place_type);
            mFavPlaceAddressTextView = itemView.findViewById(R.id.fav_place_address);
        }

        public void bind(SelectedPlace selectedPlace){
            mPlace = selectedPlace;

            updateImage();
            updateName();
            updateType();
            updateAddress();
        }

        private void updateImage() {
            getPhotos(mPlace.getId());
        }

        private void updateName() {
            String name = mPlace.getName();
            mFavPlaceNameTextView.setText(name);
        }

        private void updateAddress() {
            String address = mPlace.getAddress();
            mFavPlaceAddressTextView.setText(address);
        }

        private void updateType() {
            String type = mPlace.getType();
            mFavPlaceTypeTextView.setText(type);
        }

        @Override
        public void onClick(View view){
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.fragment_container);

            if (fragment == null) {

                fragment = PlaceDetailsFragment.newInstance(mPlace, mUser);
                fm.beginTransaction().add(R.id.fragment_container, fragment)
                        .addToBackStack(FRAGMENT_PLACE_DETAILS)
                        .commitAllowingStateLoss();

                undisplayActivityWidgets();
            }
        }

        // Request photos and metadata for the specified place.
        private void getPhotos(String placeId) {

            final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId);
            photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {

                @Override
                public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                    // Get the list of photos.
                    PlacePhotoMetadataResponse photos = task.getResult();
                    // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                    PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                    // Get the first photo in the list.
                    if (photoMetadataBuffer.getCount() == 0) {
                        int resourceId = res.getIdentifier("nophoto", "drawable", MainActivity.this.getPackageName());
                        mFavPlaceImageView.setImageResource(resourceId);
                        return;
                    }

                    PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                    // Get the attribution text.
                    CharSequence attribution = photoMetadata.getAttributions();
                    // Get a full-size bitmap for the photo.
                    Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                    photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {

                        @Override
                        public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                            PlacePhotoResponse photo = task.getResult();
                            Bitmap bitmap = photo.getBitmap();
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 75, 75, false);
                            mFavPlaceImageView.setImageBitmap(scaledBitmap);
                        }
                    });
                }
            });
        }
    }
} // MainActivity