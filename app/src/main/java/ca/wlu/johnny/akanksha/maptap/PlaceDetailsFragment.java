package ca.wlu.johnny.akanksha.maptap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
        View view = inflater.inflate(R.layout.fragment_place_details, container, false);

        setViews(view);

        updateUI();
        return view;
    } // onCreateView

    private void setViews(View v) {
        mPlaceNameTextView = v.findViewById(R.id.place_name);
        mPlaceTypeTextView = v.findViewById(R.id.place_type_text_view);
        mPlacePriceTextView = v.findViewById(R.id.place_price_text_view);
        mPlaceImageView = v.findViewById(R.id.place_image);

    } // setViews

    private void updateUI(){
        String name = mPlace.getName();
        mPlaceNameTextView.setText(name);

        String type = mPlace.getType();
        mPlaceTypeTextView.setText(type);

        String price = mPlace.getPrice();
        mPlacePriceTextView.setText(price);

    } // updateUI

} // PlaceDetailsFragment
