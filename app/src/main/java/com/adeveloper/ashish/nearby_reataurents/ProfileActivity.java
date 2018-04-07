package com.adeveloper.ashish.nearby_reataurents;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity {
    TextView Rest_name,address;
    EditText phone_no,email_id,special;
    LatLng location_restra;
    String rest_name,Rest_address,der_phone,der_email;
   Button UpdateProfile;
   FirebaseAuth mAuth;
   FirebaseDatabase firebaseDatabase;
   DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Rest_name=(TextView)findViewById(R.id.restra_name);
        phone_no=(EditText)findViewById(R.id.phone);
        special=(EditText)findViewById(R.id.speciality);
        email_id=(EditText) findViewById(R.id.email);
        address=(TextView)findViewById(R.id.address);
        UpdateProfile=(Button)findViewById(R.id.update);
        mAuth=FirebaseAuth.getInstance();
        String UID=mAuth.getCurrentUser().getUid();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("Restaurents").child(UID);
        der_phone=phone_no.getText().toString();
        der_email=email_id.getText().toString();
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                location_restra= place.getLatLng();
               rest_name=place.getName().toString();
               Rest_address=place.getAddress().toString();

               Rest_name.setText(rest_name);
               address.setText(Rest_address);

                Log.i("ProfileActivity", "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("profileActivity", "An error occurred: " + status);
            }
        });

        UpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rest_name== null|Rest_address==null|phone_no.getText().toString()==null|special.getText().toString()==null|email_id.getText().toString()==null){
                    Rest_name.setText("all alues are not filled");
                }else {
                    pushData push = new pushData(rest_name, Rest_address, phone_no.getText().toString(), special.getText().toString(), email_id.getText().toString());
                    databaseReference.setValue(push);
                    Toast.makeText(getApplicationContext(), "profile successfully updated", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(i);
                }

            }
        });

    }


}

class pushData{
    String rest_name,Rest_address,Rest_phone,Rest_email,speciality;
    LatLng Rest_location;

    public pushData() {
    }

    public pushData(String rest_name, String rest_address, String rest_phone, String Speciality,String rest_email) {
        this.rest_name = rest_name;
        Rest_address = rest_address;
        Rest_phone = rest_phone;
        Rest_email = rest_email;
        speciality = Speciality;

    }
}
