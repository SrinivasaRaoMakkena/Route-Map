package com.example.srinivas.routemap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {


    LocationManager locationManager;
    LocationListener locationListener;
    private static double lattitude;
    private static double longitude;

    Button toMap;
    TextView coordiantes;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //request code --> 1
        //permissions array , we do haVE ONLY ONE PERMISSION
        //PERMISSION RESULT

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            } else {
                return;
            }

        }

    }


    public void clickTo(View view){
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        intent.putExtra("lattitude",lattitude);
        intent.putExtra("longitude",longitude);
        startActivity(intent);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // toMap = (Button) findViewById(R.id);


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //configure_button();
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lattitude = location.getLatitude();
                longitude = location.getLongitude();
                coordiantes.setText(lattitude + " " + longitude);
                //address = getCompleteAddressString(latitude, longitude);


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };
// first check for permissions
        //if API <23, no need of permissions
        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // if no permission -> ask for permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                //if we have permissions already..if not not ask for permission result
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            }
        }


       //toMap = (Button) findViewById(R.id.)
        coordiantes = (TextView) findViewById(R.id.latlongitude);

    }
}
