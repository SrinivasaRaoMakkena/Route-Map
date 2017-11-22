package com.example.srinivas.routemap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;


    private double distance;
    private String startAddress;
    //GoogleMap map;
    ArrayList<Document> list;
    ArrayList<LatLng> listGeopoints;

    private double start_lattitude;
    private double start_longitude;
    private double dest_lattitude;
    private double dest_longitude;

    TextView Distance;

    private int getNodeIndex(NodeList nl, String nodename) {
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeName().equals(nodename))
                return i;
        }
        return -1;
    }
    private ArrayList<LatLng> decodePoly(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(position);
        }
        return poly;
    }



    public class DownloadTask extends AsyncTask<String,Void,String> {

        URL url;
        HttpURLConnection connection = null;
        String result="";
        //ArrayList<LatLng> directionPoint;

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            list= new ArrayList<>();


        }

        @Override
        protected String doInBackground(String... params) {

            try {
                url= new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);
//                int data = reader.read();
//
//                while (data != -1){
//                    char c = (char) data;
//                    result += c;
//                    data = reader.read();
//
//                }

                DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder();
                Document doc = builder.parse(in);
                RouteMaps md = new RouteMaps();
                //directionPoint = md.getDirection(doc);
               distance =  (double)md.getDistanceValue(doc)/1609.34;


                startAddress = md.getStartAddress(doc);



                NodeList nl1, nl2, nl3;
                listGeopoints = new ArrayList<LatLng>();

                if (doc.getElementsByTagName("step").getLength() > 0) {
                    nl1 = doc.getElementsByTagName("step");

                    if (nl1.getLength() > 0) {
                        for (int i = 0; i < nl1.getLength(); i++) {
                            Node node1 = nl1.item(i);
                            nl2 = node1.getChildNodes();

                            Node locationNode = nl2
                                    .item(getNodeIndex(nl2, "start_location"));
                            nl3 = locationNode.getChildNodes();
                            Node latNode = nl3.item(getNodeIndex(nl3, "lat"));
                            double lat = Double.parseDouble(latNode.getTextContent());
                            Node lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                            double lng = Double.parseDouble(lngNode.getTextContent());
                            listGeopoints.add(new LatLng(lat, lng));

                            locationNode = nl2.item(getNodeIndex(nl2, "polyline"));
                            nl3 = locationNode.getChildNodes();
                            latNode = nl3.item(getNodeIndex(nl3, "points"));
                            ArrayList<LatLng> arr = decodePoly(latNode.getTextContent());
                            for (int j = 0; j < arr.size(); j++) {
                                listGeopoints.add(new LatLng(arr.get(j).latitude, arr
                                        .get(j).longitude));
                            }

                            locationNode = nl2.item(getNodeIndex(nl2, "end_location"));
                            nl3 = locationNode.getChildNodes();
                            latNode = nl3.item(getNodeIndex(nl3, "lat"));
                            lat = Double.parseDouble(latNode.getTextContent());
                            lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                            lng = Double.parseDouble(lngNode.getTextContent());
                            listGeopoints.add(new LatLng(lat, lng));
                        }
                    }
                }
//                    list.add(doc);
                //return doc;

                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return "Failed";
            }

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


Intent intent = getIntent();
        start_lattitude = intent.getDoubleExtra("lattitude",30.3074624);
        start_longitude = intent.getDoubleExtra("longitude",-98.0335976);

        Toast.makeText(getApplicationContext(),intent.getDoubleExtra("lattitude",30.3074624) + " " + intent.getDoubleExtra("longitude",-98.0335976),Toast.LENGTH_SHORT).show();

        String theUrl = "http://maps.googleapis.com/maps/api/directions/xml?"
                + "origin=" + intent.getDoubleExtra("lattitude",30.3074624) + "," + intent.getDoubleExtra("longitude",-98.0335976)
                + "&destination=" + 34.1133541 + "," + -84.365512
                + "&sensor=false&units=metric&mode=driving";
        Log.d("url", theUrl);
        String output="";
        DownloadTask downloadTask = new DownloadTask();
        try {
            output = downloadTask.execute(theUrl).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Log.i("Output",output);


//Location Tracking from GPS




//        RouteMaps md = new RouteMaps();



//        Document doc = md.getDocument(new LatLng(34.0380054,-84.2619869), new LatLng(39.0921167,-94.8559162),
//                RouteMaps.MODE_DRIVING);

//    ArrayList<LatLng> directionPoint = md.getDirection(d);
//    PolylineOptions rectLine = new PolylineOptions().width(3).color(
//            Color.RED);
//
//    for (int i = 0; i < directionPoint.size(); i++) {
//        rectLine.add(directionPoint.get(i));
//    }
//    Polyline polylin = map.addPolyline(rectLine);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



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
        PolylineOptions rectLine = new PolylineOptions().width(40).color(
                Color.BLUE);

        for (int i = 0; i < listGeopoints.size(); i++) {
            rectLine.add(listGeopoints.get(i));
        }
        Polyline polylin = mMap.addPolyline(rectLine);


        // Add a marker in starting location  and move the camera
        LatLng startLocation = new LatLng(start_lattitude, start_longitude);
        mMap.addMarker(new MarkerOptions().position(startLocation).title(startAddress));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation,15));

        //Destination marker
//        LatLng destinationLocation = new LatLng(start_lattitude, start_longitude);
//        mMap.addMarker(new MarkerOptions().position(startLocation).title(startAddress));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation,10));

        Distance = (TextView) findViewById(R.id.distanceTV);
        Distance.setBackgroundColor(Color.WHITE);
        Distance.setText(""+Math.round(distance*100)/100.0+" miles away");
}
}
