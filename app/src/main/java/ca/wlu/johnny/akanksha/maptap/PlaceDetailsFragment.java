package ca.wlu.johnny.akanksha.maptap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by johnny on 2017-11-22.
 */

public class PlaceDetailsFragment extends Fragment {

    private static final String ARG_PLACE  = "my_place";
    private SelectedPlace mPlace;
    private TextView mNameTextView;

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

        mPlace = getArguments().getParcelable(ARG_PLACE);
    } // onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_details, container, false);

        setViews(view);

        updateUI();
        return view;
    } // onCreateView

    private void setViews(View v) {
        mNameTextView = v.findViewById(R.id.place_name);
    } // setViews

    private void updateUI(){
        String name = mPlace.getName();
        mNameTextView.setText(name);
    } // updateUI

} // PlaceDetailsFragment
