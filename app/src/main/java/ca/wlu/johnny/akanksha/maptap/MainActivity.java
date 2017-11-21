package ca.wlu.johnny.akanksha.maptap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

public class MainActivity extends AppCompatActivity{

    private int PLACE_PICKER_REQUEST = 1;
    private int SIGN_IN_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, SignInActivity.class);
        startActivityForResult(intent, SIGN_IN_REQUEST);

    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data ) {

        if (requestCode == SIGN_IN_REQUEST) {

            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            try {
                Intent placePickerIntent = builder.build(getApplicationContext());
                startActivityForResult(placePickerIntent, PLACE_PICKER_REQUEST);

            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();

            } catch (GooglePlayServicesNotAvailableException e ) {
                e.printStackTrace();
            }
        }

        if (requestCode == PLACE_PICKER_REQUEST) {
            if(resultCode == RESULT_OK){
                Place place = PlacePicker.getPlace(data,this);

                String address = String.format("Place is: %s ",place.getAddress());
                System.out.println("---------------- " + address + " ---------------------");
                // TODO: handle onAddressSelected()
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}