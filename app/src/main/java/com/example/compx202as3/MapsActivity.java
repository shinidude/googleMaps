package com.example.compx202as3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.compx202as3.databinding.ActivityMapsBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * MapsActivity displays the maps,  search bar,
 * the weather markers, cameras and the route.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "Hewwo";//A tage for the response messages
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private boolean locationPermGranted = false; //A value that checks if the permission has been granted or not
    FusedLocationProviderClient fusedLocationClient; //for the  retrieval of the  device location information
    private static final int DEFAULT_ZOOM = 12; //Value for the zooming of the camera
    LatLng lastLocation; //A variable to store the latitude and longitude of the last location
    LatLng selectLocation; //A variable to store the latitude and longitude of the selected location
//    Polyline mPolyline; //A polyline to display the route of the current location to the searched location
    RequestQueue queue; //All the requests are queued up that has to be executed
    //Necessary URLS for to access API's to display the webcam and the weather
    MarkerOptions m;//For the markers of the webcams
    Map<LatLng, JSONObject> MarkerMap = new HashMap<>(); //A hashmap to store the JSON object of the displayed webcams

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        queue = Volley.newRequestQueue(this);

        CheckPermission();//Checks if the application's location permissions has been permitted

        Places.initialize(getApplicationContext(),getString(R.string.api_key));

        //Setting up the autocomplete fragment to be able to display options in the search bars
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.getView().setBackgroundColor(Color.WHITE);//Displaying a white background for the autocomplete fragment
        autocompleteFragment.setTypeFilter(TypeFilter.ADDRESS); //Setting the filter to the address of the location
        //Sets the desired fields of the Place object returned from selecting an autocomplete result.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.ID, Place.Field.LAT_LNG));

        // Specify the types of place data to return.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            /**
             * onPlaceSelected
             * Displays what occurs to the selected option ing the search bar
             */
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng pSearchLoc = place.getLatLng(); //get the latitude and longitude of the selected option and store it in pSarchLoc
                selectLocation = pSearchLoc; //Set the pSearchLoc to the selectLocation

                mMap.clear();//Clearing the maps for any markers
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pSearchLoc, DEFAULT_ZOOM));//Zooming the camera into the selected location
                //Making the weather url flexible for any selected locations
                String weatherURl = createWeatherURL(pSearchLoc.latitude, pSearchLoc.longitude);

                /**
                 *request for retrieving a JSONObject response body at a given URL,
                 * allowing for an optional JSONObject to be passed in as part of the request body.
                 */
                JsonObjectRequest jObjReqWeather = new JsonObjectRequest(Request.Method.GET, weatherURl, null, new Response.Listener<JSONObject>(){
                    /**
                     * onResponse(JSONObject response)
                     * Displays the weather marker in the map depending on the locations weather from the api
                     */
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject weather = response.getJSONArray("weather").getJSONObject(0); //Accessing the weather in the json api
                            Double temperature = response.getJSONObject("main").getDouble("temp")-273.15;  // Get the temperature and convert it to Celsius
                            String[] weatherInfo = getWeatherInfo(weather);
                            int weatherId = getResources().getIdentifier(weatherInfo[0], "drawable", getPackageName());//Gets the weather id
                            //Displays the marker icon depending on the weather
                             m = new MarkerOptions().position(pSearchLoc).title(place.getName()).icon(BitmapDescriptorFactory.fromResource(weatherId)).snippet(weatherInfo[1]+", "+String.format("%.1f C",temperature));
                             //Adds a marker on that location
                            mMap.addMarker(m);
                        } catch (JSONException je) {
                            //Displays any error in this area
                            je.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    /**
                     * onErrorResponse(VolleyError error)
                     * Displays error
                     */
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "Weather call didn't work");
                    }
                });
                queue.add(jObjReqWeather);

                // Get the URL for the camera volley call
                String camURL = createCamURL(pSearchLoc.latitude, pSearchLoc.longitude);
                /**
                 *request for retrieving a JSONObject response body at a given URL,
                 * allowing for an optional JSONObject to be passed in as part of the request body.
                 */
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, camURL, null, new Response.Listener<JSONObject>() {
                    /**
                     * onResponse(JSONObject response)
                     * Displays five webcam markers in the area
                     */
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Gets access to the webcams JSON array
                            JSONArray array =  response.getJSONObject("result").getJSONArray("webcams");
                            //Looks through the array of webcams
                            for (int i=0; i<array.length(); i++){ //For each arrays in the webcams
                                    JSONObject jsonObject = array.getJSONObject(i); //Gets the object in the array
                                    JSONObject locationDetails = jsonObject.getJSONObject("location"); //gets access to the location object
                                    String city =  locationDetails.getString("city");//Gets the city in the location details
                                    LatLng markerPos= new LatLng(locationDetails.getDouble("latitude"),locationDetails.getDouble("longitude"));
                                    //Puts a webcam marker on the location with webcams
                                    m = new MarkerOptions().position(markerPos).title(city).icon(BitmapDescriptorFactory.fromResource(R.drawable.camera));
                                    MarkerMap.put(m.getPosition(),jsonObject);//Add the location in the MarkerMap hashmap
                                    mMap.addMarker(m);
                            }
                        } catch (JSONException je){
                            //Displays any error
                            je.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    /**Displays an error message */
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "That didn't work!");
                    }
                });
                queue.add(jsonObjectRequest);

            }
            /**Displays an error message */
            @Override
            public void onError(@NonNull Status status) {
            }
        });
    }

    // Creates/Formats the URL used in the volley call to get the camera info
    public static String createCamURL(Double lat, Double lon) {
        String start = "https://api.windy.com/api/webcams/v2/list/limit=5/nearby=";
        String end = ",250/orderby=distance/?show=webcams:location,image;?&key=";

        return start+lat+","+lon+end+"G7nva6bysKOxdZfgYI8caTQ5xoAPhRbC";
    }

    // Creates/Formats the URL used in the volley call to get the camera info
    public static String createWeatherURL(Double lat, Double lon) {
        String start = "https://api.openweathermap.org/data/2.5/weather?lat=";
        String end = "&appid=fcc6e74e69cc189958cf9bc68906255a";

        return start+lat+"&lon="+lon+end;
    }

    public static String[] getWeatherInfo(JSONObject weather) {
        String[] output = new String[2];
        try {
            String general = weather.getString("main").toLowerCase();//Gets the general states of the weather and store it in the general variable
            String description = weather.getString("description");//Gets the description of the weather and store it in the description variable
            description = description.substring(0, 1).toUpperCase() + description.substring(1);

            output[0] = general;
            output[1] = description;
        } catch (JSONException e) {
        }
        return output;
    }

    /**
     * CheckPermissions
     * returns nothing
     * Checks the permission of the if it has been set.
     */
    private void CheckPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permissions have not yet been granted
            GetPermission();
            // Launch a location permission request
        } else {
            // Permissions have already been granted
            getCurrentLocation();
        }
    }

    /**
     * GetPermissions()
     * returns nothing
     * Gets the permission from the user to allow location access
     */
    private void GetPermission() {
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                locationPermGranted = true;
                                getCurrentLocation();
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                Toast.makeText(this, "Not all features can be used", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "No permissions granted", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
        // Before you perform the actual permission request, check whether your app
        // already has the permissions, and whether your app needs to show a permission
        // rationale dialog. For more details, see Request permissions.
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void getCurrentLocation() {
        try {
            if (locationPermGranted) {
                Task<Location> loc = fusedLocationClient.getLastLocation();
                loc.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location location = task.getResult();
                            if (location != null) {
                                lastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, DEFAULT_ZOOM));
                                mMap.clear();
                                mMap.addMarker(new MarkerOptions().position(lastLocation).title("Current Location"));
                            } else {
                                Toast.makeText(MapsActivity.this, "No known last location", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String tag = "MapsActivity";
                            Toast.makeText(MapsActivity.this, "Couldn't get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(MapsActivity.this, "Couldn't get permission", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException se) {
        }
    }

    // Manipulates the map once available.
    // This callback is triggered when the map is ready to be used.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent detailedIntent = new Intent(MapsActivity.this, DetailsActivity.class);
                 JSONObject clickedCamera = MarkerMap.get(marker.getPosition());
                 if (clickedCamera == null)
                     return false;
                try {
                    String title = clickedCamera.getString("title");
                    String region = clickedCamera.getJSONObject("location").getString("region");
                    String city  = clickedCamera.getJSONObject("location").getString("city");
                    String cityAndRegion = city+", "+region;
                    String img = clickedCamera.getJSONObject("image").getJSONObject("current").getString("preview");
                    Log.i(TAG,  title+" ,"+city+" ,"+ region +", "+img +" The on marker works" );
                    detailedIntent.putExtra("title", title);
                    detailedIntent.putExtra("img", img);
                    detailedIntent.putExtra("cityAndRegion", cityAndRegion);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(detailedIntent);

                return false;
            }
        });
    }

    /*
    *   The following can be uncommented to draw a route path from the last
    *   known location and the selected destination, just call drawRoute
    */
//    private void drawRoute(){
//        // Getting URL to the Google Directions API
//        if (lastLocation==null) {
//            Toast.makeText(this, "Can't give directions,", Toast.LENGTH_LONG).show();
//            Toast.makeText(this, "No known last location", Toast.LENGTH_LONG).show();
//            return;
//        }
//        String url = getDirectionsUrl(lastLocation, selectLocation);
//
//        DownloadTask downloadTask = new DownloadTask();
//
//        // Start downloading json data from Google Directions API
//        downloadTask.execute(url);
//    }
//
//
//    private String getDirectionsUrl(LatLng origin,LatLng dest){
//        // Origin of route
//        String str_origin = "origin="+origin.latitude+","+origin.longitude;
//        // Destination of route
//        String str_dest = "destination="+dest.latitude+","+dest.longitude;
//        // Key
//        String key = "key=" + getString(R.string.api_key);
//        // Building the parameters to the web service
//        String parameters = str_origin+"&"+str_dest+"&"+key;
//        // Output format
//        String output = "json";
//        // Building the url to the web service
//        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
//
//        return url;
//    }
//
//    /** A method to download json data from url */
//    private String downloadUrl(String strUrl) throws IOException {
//        String data = "";
//        InputStream iStream = null;
//        HttpURLConnection urlConnection = null;
//        try{
//            URL url = new URL(strUrl);
//
//            // Creating an http connection to communicate with url
//            urlConnection = (HttpURLConnection) url.openConnection();
//
//            // Connecting to url
//            urlConnection.connect();
//
//            // Reading data from url
//            iStream = urlConnection.getInputStream();
//            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
//            StringBuffer sb  = new StringBuffer();
//
//            String line = "";
//            while( ( line = br.readLine())  != null){
//                sb.append(line);
//            }
//
//            data = sb.toString();
//            br.close();
//        }catch(Exception e){
//            Log.d("Exception on download", e.toString());
//        }finally{
//            iStream.close();
//            urlConnection.disconnect();
//        }
//        return data;
//    }
//
//    /** A class to download data from Google Directions URL */
//    private class DownloadTask extends AsyncTask<String, Void, String> {
//
//        // Downloading data in non-ui thread
//        @Override
//        protected String doInBackground(String... url) {
//
//            // For storing data from web service
//            String data = "";
//
//            try{
//                // Fetching the data from web service
//                data = downloadUrl(url[0]);
//                Log.d("DownloadTask","DownloadTask : " + data);
//            }catch(Exception e){
//                Log.d("Background Task",e.toString());
//            }
//            return data;
//        }
//
//        // Executes in UI thread, after the execution of
//        // doInBackground()
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//
//            ParserTask parserTask = new ParserTask();
//
//            // Invokes the thread for parsing the JSON data
//            parserTask.execute(result);
//        }
//    }
//
//    /** A class to parse the Google Directions in JSON format */
//    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {
//
//        // Parsing the data in non-ui thread
//        @Override
//        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
//
//            JSONObject jObject;
//            List<List<HashMap<String, String>>> routes = null;
//
//            try{
//                jObject = new JSONObject(jsonData[0]);
//                DirectionsJSONParser parser = new DirectionsJSONParser();
//
//                // Starts parsing data
//                routes = parser.parse(jObject);
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//            return routes;
//        }
//
//        // Executes in UI thread, after the parsing process
//        @Override
//        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
//            ArrayList<LatLng> points = null;
//            PolylineOptions lineOptions = null;
//
//            // Traversing through all the routes
//            for(int i=0;i<result.size();i++){
//                points = new ArrayList<LatLng>();
//                lineOptions = new PolylineOptions();
//
//                // Fetching i-th route
//                List<HashMap<String, String>> path = result.get(i);
//
//                // Fetching all the points in i-th route
//                for(int j=0;j<path.size();j++){
//                    HashMap<String,String> point = path.get(j);
//
//                    double lat = Double.parseDouble(point.get("lat"));
//                    double lng = Double.parseDouble(point.get("lng"));
//                    LatLng position = new LatLng(lat, lng);
//
//                    points.add(position);
//                }
//
//                // Adding all the points in the route to LineOptions
//                lineOptions.addAll(points);
//                lineOptions.width(8);
//                lineOptions.color(Color.RED);
//            }
//
//            // Drawing polyline in the Google Map for the i-th route
//            if(lineOptions != null) {
//                if(mPolyline != null){
//                    mPolyline.remove();
//                }
//                mPolyline = mMap.addPolyline(lineOptions);
//            }else
//                Toast.makeText(getApplicationContext(),"No route is found", Toast.LENGTH_LONG).show();
//        }
//    }
}