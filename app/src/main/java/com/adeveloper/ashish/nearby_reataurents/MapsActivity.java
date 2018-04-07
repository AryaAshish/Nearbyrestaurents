package com.adeveloper.ashish.nearby_reataurents;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    public GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastlocation;
    private Marker currentLocationmMarker;
    public static final int REQUEST_LOCATION_CODE = 99;
    int PROXIMITY_RADIUS = 10000;
    public double latitude, longitude;
    double end_latitude, end_longitude;
    private static final int RC_SIGN_IN = 123;
    private static final int RC_SIGN_IN_USER = 124;
    private BottomSheetBehavior mBottomSheetBehavior;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    LatLng pos;
    pushData data;
    TextView nameb, phoneb, specialityb, mailb;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();

        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        nameb = (TextView) findViewById(R.id.restra_nameb);
        phoneb = (TextView) findViewById(R.id.phoneb);
        specialityb = (TextView) findViewById(R.id.specialityb);
        mailb = (TextView) findViewById(R.id.email);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setPeekHeight(100);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);



    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.signIn:
            {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(true)
                                .setAvailableProviders(Arrays.asList(
                                        new AuthUI.IdpConfig.EmailBuilder().build(),
                                        new AuthUI.IdpConfig.PhoneBuilder().build(),
                                        new AuthUI.IdpConfig.GoogleBuilder().build()
                                ))
                                .build(),
                        RC_SIGN_IN);

                return true;

            }
            case R.id.signInUser:
                {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(true)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.PhoneBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()
                                    ))
                                    .build(),
                            RC_SIGN_IN_USER);

                    return true;
                }
            case R.id.SignOut:
                {
                    AuthUI.getInstance()
                            .signOut(this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(getApplicationContext(),"sucessfully signed out",Toast.LENGTH_SHORT).show();
                                    // user is now signed out
                                    startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                                    finish();
                                }
                            });
                }
        }


        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        Object dataTransfer[] = new Object[2];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();

        switch (id) {

            case R.id.hospitals:
                mMap.clear();
                String hospital = "hospital";
                String url = getUrl(latitude, longitude, hospital);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;

                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing Nearby Hospitals", Toast.LENGTH_SHORT).show();
                break;


            case R.id.school:
                mMap.clear();
                String school = "school";
                url = getUrl(latitude, longitude, school);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;

                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing Nearby Schools", Toast.LENGTH_SHORT).show();
                break;
            case R.id.restaurent:
                mMap.clear();
                String resturant = "restuarant";
                url = getUrl(latitude, longitude, resturant);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;

                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing Nearby Restaurants", Toast.LENGTH_SHORT).show();
                break;
            case R.id.signIn:
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(true)
                                .setAvailableProviders(Arrays.asList(
                                        new AuthUI.IdpConfig.EmailBuilder().build(),
                                        new AuthUI.IdpConfig.PhoneBuilder().build(),
                                        new AuthUI.IdpConfig.GoogleBuilder().build()
                                ))
                                .build(),
                        RC_SIGN_IN);
                break;

        }
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;


    }

    private void populateBottomsheet(pushData data) {
        nameb.setText(data.rest_name);
        phoneb.setText(data.Rest_phone);
        specialityb.setText(data.speciality);
        mailb.setText(data.Rest_email);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (client == null) {
                            bulidGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);

                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bulidGoogleApiClient();
            mMap.setMyLocationEnabled(true);


        }
        mMap.setOnMarkerClickListener(this);


    }


    protected synchronized void bulidGoogleApiClient() {
        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        client.connect();

    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        lastlocation = location;
        if (currentLocationmMarker != null) {
            currentLocationmMarker.remove();

        }
        Log.d("lat = ", "" + latitude);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        currentLocationmMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));


        if (client != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }

    private String getDirectionsUrl()
    {
        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+latitude+","+longitude);
        googleDirectionsUrl.append("&destination="+end_latitude+","+end_longitude);
        googleDirectionsUrl.append("&key="+"AIzaSyBl2aH_KA1LnNqIcw7fkXRJ9z6zCejCHKo");

        return googleDirectionsUrl.toString();
    }


    public void onClick(View v) {
        Object dataTransfer[] = new Object[2];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();

        switch (v.getId()) {


            case R.id.B_to:
                if(end_latitude==0 && end_longitude== 0){
                    end_longitude=longitude;
                    end_latitude=latitude;
                }
                dataTransfer = new Object[3];
                String url = getDirectionsUrl();
                GetDirectionsData getDirectionsData = new GetDirectionsData();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = new LatLng(end_latitude, end_longitude);
                getDirectionsData.execute(dataTransfer);

                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode==RC_SIGN_IN) {
            IdpResponse idpResponse = IdpResponse.fromResultIntent(data);
            startActivity(new Intent(this, ProfileActivity.class)
                    .putExtra("my_token", idpResponse.getIdpToken()));
        }
        else if (resultCode == RESULT_OK && requestCode==RC_SIGN_IN_USER){
            IdpResponse idpResponse = IdpResponse.fromResultIntent(data);
            startActivity(new Intent(getApplicationContext(), MapsActivity.class)
                    .putExtra("my_token", idpResponse.getIdpToken()));
        }
    }



    public String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=" + latitude + "," + longitude);
        googlePlaceUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type=" + nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key=" + "AIzaSyBLEPBRfw7sMb73Mr88L91Jqh3tuE4mKsE");

        Log.d("MapsActivity", "url = " + googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        @SuppressLint("RestrictedApi") LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;

        } else
            return true;
    }


    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


    @Override
    public boolean onMarkerClick(Marker marker) {



        end_latitude = marker.getPosition().latitude;
        end_longitude = marker.getPosition().longitude;
        final String nam=marker.getTitle();

        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("Restaurents");

       try {
           databaseReference.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot dataSnapshot) {
                   if(dataSnapshot.exists())
                   {
                       for(DataSnapshot d:dataSnapshot.getChildren()) {
                           if(d.child("rest_name").getValue().toString().equals(nam)) {
                               try {
                                   data = d.getValue(pushData.class);
                                   populateBottomsheet(data);
                                   count++;
                               }
                               catch (Exception e) {


                                   Toast.makeText(getApplication(), "Data : " + e.getMessage(), Toast.LENGTH_LONG).show();
                               }
                           }
                       /* if (d.child("Rest_location").getValue(LatLng.class).equals(pos))
                        //if (d.child("rest_name").getValue()==nam)m
                        {
                            try {
                                data = d.getValue(pushData.class);
                                populateBottomsheet(data);
                                count++;
                            }catch (Exception e)
                            {
                                Log.d("MapsActivity",""+e.getMessage());
                            }

                        }*/

                       }
                   }

               }

               @Override
               public void onCancelled(DatabaseError databaseError) {
                   specialityb.setText("cancelled");


               }
           });
       }catch (Exception e){Toast.makeText(getApplicationContext(),"not able to fetch",Toast.LENGTH_SHORT).show();}
        if (count==0){
            populateBottomsheet1(marker);
        }

        Toast.makeText(getApplicationContext(),"markerclicked",Toast.LENGTH_SHORT).show();
        return true;
    }

    private void populateBottomsheet1(Marker marker) {
        nameb.setText(marker.getTitle());
        phoneb.setText(marker.getId());
        specialityb.setText(marker.getSnippet());
        mailb.setText(marker.getPosition().toString());
    }

}
