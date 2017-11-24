package ca.wlu.johnny.akanksha.maptap;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButtonCallback;
import com.uber.sdk.rides.client.ServerTokenSession;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.rides.client.error.ApiError;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by johnny on 2017-11-22.
 */

public class PlaceDetailsFragment extends Fragment {

    private static final String ARG_PLACE  = "my_place";
    private static final long LOCATION_REFRESH_TIME = 5000;
    private static final float LOCATION_REFRESH_DISTANCE = 0;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 11;

    private SelectedPlace mPlace;
    private TextView mPlaceNameTextView;
    private TextView mPlaceTypeTextView;
    private TextView mPlacePriceTextView;
    private ImageView mPlaceImageView;
    private TextView mDirectionsTextView;
    private TextView mCallTextView;
    private TextView mPlaceWebsiteIcon;
    private RatingBar mRating;
    private RideRequestButton mUberRidesButton;
    private LocationManager mLocationManager;
    private double mLat;
    private double mLng;


    public static PlaceDetailsFragment newInstance(SelectedPlace place){
        Bundle args = new Bundle();
        args.putParcelable(ARG_PLACE, place);

        PlaceDetailsFragment placeDetailsFragment = new PlaceDetailsFragment();
        placeDetailsFragment.setArguments(args);
        return placeDetailsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActionBar().setTitle("Place Details");

        if ( ContextCompat.checkSelfPermission( getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            mLocationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

            ActivityCompat.requestPermissions( getActivity(), new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    MY_PERMISSION_ACCESS_FINE_LOCATION );
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE,mLocationListener);

            mLat =  mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
            mLng = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
        }


        mPlace = getArguments().getParcelable(ARG_PLACE);


    } // onCreate

    private final LocationListener mLocationListener = new LocationListener() {


        @Override
        public void onStatusChanged(String provider, int status, Bundle extras){
//            final String tvTxt = textView.getText().toString();
            switch (status) {
                case LocationProvider.AVAILABLE:
//                    textView.setText(tvTxt + "Network location available again\n");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
//                    textView.setText(tvTxt + "Network location out of service\n");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
//                    textView.setText(tvTxt
//                            + "Network location temporarily unavailable\n");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {

        }

    };
    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_details, container, false);

        setViews(view);
        onDirectionsClick();
        onCallClick();
        onUberClick(view);

        updateUI();
        return view;
    } // onCreateView

    private void onUberClick(View v) {
        //Uber button
        mUberRidesButton = new RideRequestButton(getContext());
        mUberRidesButton = v.findViewById(R.id.uber_icon);

        RideParameters rideParams = new RideParameters.Builder()
                // Optional product_id from /v1/products endpoint (e.g. UberX). If not provided, most cost-efficient product will be used
//                .setProductId("a1111c8c-c720-46c3-8534-2fcdd730040d")
                // Required for price estimates; lat (Double), lng (Double), nickname (String), formatted address (String) of dropoff location
//                .setDropoffLocation(43.4726916, -80.5264207, mPlace.getName(), mPlace.getAddress())
                // Required for pickup estimates; lat (Double), lng (Double), nickname (String), formatted address (String) of pickup location
                .setPickupLocation(mLat, mLng, "blah", "blah" )
                .setDropoffLocation(43.4726916, -80.5264207, mPlace.getName(), mPlace.getAddress())
                .build();
        // set parameters for the RideRequestButton instance
        mUberRidesButton.setRideParameters(rideParams);
        System.out.println("--------------------------HERE------------------");
        ServerTokenSession session = new ServerTokenSession(MainActivity.config);
        mUberRidesButton.setSession(session);
        mUberRidesButton.loadRideInformation();


        RideRequestButtonCallback callback = new RideRequestButtonCallback() {


            @Override
            public void onRideInformationLoaded() {
                // react to the displayed estimates
            }

            @Override
            public void onError(ApiError apiError) {
                // API error details: /docs/riders/references/api#section-errors
            }

            @Override
            public void onError(Throwable throwable) {
                // Unexpected error, very likely an IOException
            }
        };
        mUberRidesButton.setCallback(callback);
    }


    //opens google nav on click
    private void onDirectionsClick() {
        mDirectionsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+mPlace.getAddress()+"");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
    }

    //opens call
    private void onCallClick() {
        mCallTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+mPlace.getPhoneNumber()));
                startActivity(intent);
            }
        });
    }

    private void setViews(View v) {
        mPlaceNameTextView = v.findViewById(R.id.place_name);
        mPlaceTypeTextView = v.findViewById(R.id.place_type_text_view);
        mPlacePriceTextView = v.findViewById(R.id.place_price_text_view);
        mPlaceImageView = v.findViewById(R.id.place_image);
        mPlaceWebsiteIcon = v.findViewById(R.id.website_icon);
        mDirectionsTextView = v.findViewById(R.id.directions_icon);
        mCallTextView = v.findViewById(R.id.call_icon);
        mRating = v.findViewById(R.id.rating);


    } // setViews

    private void updateUI() {
        String name = mPlace.getName();
        mPlaceNameTextView.setText(name);

        String type = mPlace.getType();
        mPlaceTypeTextView.setText(type);

        int price = mPlace.getPrice();

        if (price == 0 || price == 1) {
            mPlacePriceTextView.setText("$");
        } else if (price == 2 || price == 3) {
            mPlacePriceTextView.setText("$$");
        } else if (price == 4) {
            mPlacePriceTextView.setText("$$$");
        } else {
            mPlacePriceTextView.setText("");
        }

        float ratings = mPlace.getRating();
        mRating.setRating(ratings);


        setUpWebsiteIcon();

    } // updateUI

    private void setUpWebsiteIcon() {
        mPlaceWebsiteIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    Intent webViewIntent = WebViewActivity.newIntent(getActivity(), mPlace.getUrl());
                    startActivity(webViewIntent);

                } else {
                    Toast.makeText(getActivity(), "Network not available", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available network Info will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private double parseLat(){
        double lat =  Double.parseDouble(mPlace.getLatLng().toString().split(",")[0].substring(10));
        System.out.println("------------------------ Parsed lat "+lat);
        return lat;

    }

    private double parseLng(){
        String lng = mPlace.getLatLng().toString().split(",")[1];
        double lang= Double.parseDouble(lng.substring(0, lng.lastIndexOf(")")));
        System.out.println("------------------------ Parsed lang "+lang);
        return lang;
    }



} // PlaceDetailsFragment
