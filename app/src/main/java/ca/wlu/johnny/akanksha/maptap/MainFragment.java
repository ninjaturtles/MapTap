package ca.wlu.johnny.akanksha.maptap;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Akanksha on 2017-11-20.
 */

public class MainFragment extends Fragment {

    private Button mSignUpButton;
    private Button mSignInButton;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        setView(view);
        setSignUpButton();
        setSignInButton();
        return view;
    }

    private void setSignUpButton() {
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = SignUpActivity.newIntent(getActivity());
                startActivity(intent);
            }
        });
    }

    private void setSignInButton() {
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = SignInActivity.newIntent(getActivity());
                startActivity(intent);
            }
        });
    }

    /**
     * inflates the widgets
     * param view v
     */
    private void setView(View v) {
        mSignUpButton = (Button) v.findViewById(R.id.sign_up_button);
        mSignInButton = (Button) v.findViewById(R.id.sign_in_button);

    }

}
