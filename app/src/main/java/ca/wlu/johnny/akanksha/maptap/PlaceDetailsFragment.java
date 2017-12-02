package ca.wlu.johnny.akanksha.maptap;

/**
 * Created by johnny on 2017-11-22.
 */

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButtonCallback;
import com.uber.sdk.rides.client.ServerTokenSession;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.rides.client.error.ApiError;

import com.google.android.gms.location.places.GeoDataClient;

public class PlaceDetailsFragment extends Fragment {

    private static final String ARG_PLACE  = "ca.wlu.johnny.akanksha.maptap.Place";
    private static final String ARG_USER  = "ca.wlu.johnny.akanksha.maptap.User";

    private SelectedPlace mPlace;
    private DbUtils mDbUtils;
    private User mUser;
    private Resources res;
    private TextView mPlaceNameTextView;
    private TextView mPlaceTypeTextView;
    private TextView mPlacePriceTextView;
    private ImageView mPlaceImageView;
    private ImageView mDirectionsImageView;
    private ImageView mCallImageView;
    private ImageView mPlaceWebsiteIcon;
    private RatingBar mRating;
    private RideRequestButton mUberRidesButton;
    private GeoDataClient mGeoDataClient;
    private ImageButton mAddToFavoriteButton;
    private Boolean isEnable = false;

    public static PlaceDetailsFragment newInstance(SelectedPlace place, User user){
        Bundle args = new Bundle();
        args.putParcelable(ARG_PLACE, place);
        args.putParcelable(ARG_USER, user);

        PlaceDetailsFragment placeDetailsFragment = new PlaceDetailsFragment();
        placeDetailsFragment.setArguments(args);

        return placeDetailsFragment;
    } // newInstance

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mDbUtils = DbUtils.get(getContext());

        mGeoDataClient = Places.getGeoDataClient(getActivity(), null);
        res = getResources();

        // retrieve state if not null
        if (savedInstanceState != null) {
            mPlace = savedInstanceState.getParcelable(ARG_PLACE);
            mUser = savedInstanceState.getParcelable(ARG_USER);
        } else {
            mPlace = getArguments().getParcelable(ARG_PLACE);
            mUser = getArguments().getParcelable(ARG_USER);
        }

        getActionBar().setTitle("Place Details");

    } // onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_details, container, false);

        setViews(view);
        onDirectionsClick();
        onCallClick();
        onUberClick(view);
        onWebsiteClick();
        onAddToFavoriteClick();
        getPhotos(mPlace.getId());

        updateUI();
        return view;
    } // onCreateView

    @Override
    public void onSaveInstanceState(Bundle savedStateInstance) {
        super.onSaveInstanceState(savedStateInstance);
        savedStateInstance.putParcelable(ARG_USER, mUser);
        savedStateInstance.putParcelable(ARG_PLACE, mPlace);
    } // onSaveInstanceState

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    } // ActionBar

    private void onAddToFavoriteClick() {
        SelectedPlace selectedPlace = mDbUtils.getPlace(mPlace.getId(), mUser.getEmail());
        if (selectedPlace != null) {
            mAddToFavoriteButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.btn_star_big_on));
            isEnable = true;
        } else {
            mAddToFavoriteButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.btn_star_big_off));
            isEnable = false;
        }

        mAddToFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEnable) {
                    mAddToFavoriteButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.btn_star_big_off));
                    mDbUtils.deletePlace(mPlace.getId(),mUser.getEmail());
                    onDeletedFromFavourite();
                    isEnable = !isEnable;

                }else{
                    mAddToFavoriteButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.btn_star_big_on));
                    SelectedPlace selectedPlace = mDbUtils.getPlace(mPlace.getId(), mUser.getEmail());
                    if (selectedPlace == null) {
                        mDbUtils.addPlace(mPlace);
                        onAddedToFavorite();
                        mAddToFavoriteButton.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.btn_star_big_on));
                        isEnable = !isEnable;

                    }
                }
            }
        });
    } // onAddToFavoriteClick

    private void onAddedToFavorite() {
        Toast.makeText(getActivity(),"Added to Favourite",Toast.LENGTH_SHORT).show();
    } // onAddedToFavorite

    private void onDeletedFromFavourite() {
        Toast.makeText(getActivity(),"Deleted from Favourite",Toast.LENGTH_SHORT).show();
    } // onDeletedFromFavourite

    private void onUberClick(View v) {
        //Uber button
        mUberRidesButton = new RideRequestButton(getContext());
        mUberRidesButton = v.findViewById(R.id.uber_icon);



        RideParameters rideParams = new RideParameters.Builder()
                .setPickupLocation(mUser.getLat(), mUser.getLng(), mUser.getName(), "---" )
                .setDropoffLocation(parsePlaceLat(), parsePlaceLng(), mPlace.getName(), mPlace.getAddress())
                .build();
        // set parameters for the RideRequestButton instance
        mUberRidesButton.setRideParameters(rideParams);
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
    } //onUberClick

    //opens google nav on click
    private void onDirectionsClick() {
        mDirectionsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+mPlace.getAddress()+"");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
    } // onDirectionsClick

    //opens call
    private void onCallClick() {
        // unclickable if place has no number
        mCallImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPlace.getPhoneNumber().equals("N/A")) {
                    Toast.makeText(getActivity(),"No phone number",Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+mPlace.getPhoneNumber()));
                startActivity(intent);
            }
        });
    } // onCallClick

    private void onWebsiteClick() {
        // unclickable if place has no url
        mPlaceWebsiteIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mPlace.getUrl().equals("N/A")) {
                    Toast.makeText(getActivity(),"No Website",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isNetworkAvailable()) {
                    Intent webViewIntent = WebViewActivity.newIntent(getActivity(), mPlace.getUrl());
                    startActivity(webViewIntent);

                } else {
                    Toast.makeText(getActivity(), "Network not available", Toast.LENGTH_LONG).show();
                }
            }
        });
    } // onWebsiteClick

    private void setViews(View v) {
        mPlaceNameTextView = v.findViewById(R.id.place_name);
        mPlaceTypeTextView = v.findViewById(R.id.place_type_text_view);
        mPlacePriceTextView = v.findViewById(R.id.place_price_text_view);
        mPlaceImageView = v.findViewById(R.id.place_image);
        mPlaceWebsiteIcon = v.findViewById(R.id.website_icon);
        mDirectionsImageView = v.findViewById(R.id.directions_icon);
        mCallImageView = v.findViewById(R.id.call_icon);
        mRating = v.findViewById(R.id.rating);
        mAddToFavoriteButton = v.findViewById(R.id.add_to_favorite_button);

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

    } // updateUI

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

    private double parsePlaceLat(){
        double lat =  Double.parseDouble(mPlace.getLatLng().toString().split(",")[0].substring(10));
        return lat;
    }

    private double parsePlaceLng(){
        String lng = mPlace.getLatLng().toString().split(",")[1];
        double lang= Double.parseDouble(lng.substring(0, lng.lastIndexOf(")")));
        return lang;
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
                    int resourceId = res.getIdentifier("nophoto", "drawable", getActivity().getPackageName());
                    mPlaceImageView.setImageResource(resourceId);
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
                        mPlaceImageView.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }

} // PlaceDetailsFragment
