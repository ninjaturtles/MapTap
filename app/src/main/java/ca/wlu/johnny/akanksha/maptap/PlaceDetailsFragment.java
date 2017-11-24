package ca.wlu.johnny.akanksha.maptap;

import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.uber.sdk.android.core.UberButton;
import com.uber.sdk.android.rides.RideRequestButton;

/**
 * Created by johnny on 2017-11-22.
 */

public class PlaceDetailsFragment extends Fragment {

    private static final String ARG_PLACE  = "my_place";
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

        mPlace = getArguments().getParcelable(ARG_PLACE);
    } // onCreate

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        LinearLayout layout = (LinearLayout) findViewById(R.layout.fragment_place_details);

        View view = inflater.inflate(R.layout.fragment_place_details, container, false);

        setViews(view);
        onDirectionsClick();
        onCallClick();
//        uberButton(view);
        updateUI();
        return view;
    } // onCreateView

    private void uberButton(View v) {
        // get the context by invoking ``getApplicationContext()``, ``getContext()``, ``getBaseContext()`` or ``this`` when in the activity class
        RideRequestButton requestButton = new RideRequestButton(getContext());
        // get your layout, for instance:
//        RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_main);
//        layout.addView(requestButton);
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

        //Uber button
        mUberRidesButton = new RideRequestButton(getContext());
        mUberRidesButton = v.findViewById(R.id.uber_icon);


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

} // PlaceDetailsFragment
