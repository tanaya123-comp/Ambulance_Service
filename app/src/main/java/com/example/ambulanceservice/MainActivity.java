package com.example.ambulanceservice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;



import com.google.android.material.navigation.NavigationView;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;



// Classes needed to add the location engine
// Classes needed to add the location component


public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener,AsyncResponseRideDetails, ExampleBottomSheetDialog.BottomSheetListener {


    NavigationView nav;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;

    public static MapboxMap mapboxMap;
    public MapView mapView;

    private String TAG = "MainActivity";

    public static  Context cxt;



    public static List<Feature> symbolLayerIconFeatureList = new ArrayList<>();


    // Variables needed to handle location permissions
    private PermissionsManager permissionsManager;
    // Variables needed to add the location engine
    private LocationEngine locationEngine;
    private LocationComponent locationComponent;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    // Variables needed to listen to location updates
    //Globally declare an instance of the class you created above:
    private MainActivityLocationCallback callback = new MainActivityLocationCallback(this);

    // variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    public NavigationMapRoute navigationMapRoute;

    // variables needed to initialize navigation
    private Button button;

    // source and destination edit texts
    public static EditText txtSource;
    public static EditText txtDestination;
    public Location source;
    public static Point source_pt,destination_pt, driver_pt;
    Marker driver_mk=null;

    private String rideId = "";
    private String cabId = "";

    private double driverLat;
    private double driverLng;

    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String ICON_ID = "ICON_ID";
    private static final String LAYER_ID = "LAYER_ID";



    /**
     * That's where the PermissionsManager class comes into play.
     * With the PermissionsManager class, you can check whether the user has granted
     * location permission and request permissions if the user hasn't granted them yet.
     * You can use PermissionsManager permissionsManager = new PermissionsManager(this);
     * if you're implementing PermissionsListener.
     * */
    /**
     * Once you have set up your permissions manager, you will still need to override
     * onRequestPermissionsResult() and call the permissionsManager's same method.
     * Note: The PermissionsManager can be used for requesting other permissions in addition to location.
     * */
    // private Location originLocation;

    /*
    * The PermissionsListener is an interface that returns information about the state of permissions.
    * Set up the interface and pass it into the PermissionsManager's constructor.
The permission result is invoked once the user decides whether to allow or deny the permission.
* A boolean value is given, which you can then use to write an if statement. Both cases should be handled correctly.
* Continue with your permission-sensitive logic if the user approves. Otherwise, if the user denies, display a message
* that tells the user that the permission is required for your application to work. An explanation isn't required but
* strongly encouraged to allow the user to understand why you are requesting this permission.
    * */


    @SuppressLint("MissingPermission")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cxt=getApplicationContext();


        // we pass access token to get our map
        Mapbox.getInstance(this, getString(R.string.access_token));



        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this); //after this whenever the map is loaded we go to on mapready method  in this class because we have implemented OnMapReadyCallback


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nav = (NavigationView) findViewById(R.id.nav_menu);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);



        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_menu);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.usernameText);
        navUsername.setText(sh.getString("name",null));


        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                if(item.getTitle().equals("Profile"))
                {
                    // Log.e("Open Profile","opening");
                    openProfile();
                }
                else if(item.getTitle().equals("Call"))
                {
                    openCall();
                }
                else if(item.getTitle().equals("Logout"))
                {
                    logoutFrom();
                }
                else if(item.getTitle().equals("Rides"))
                {
                    getRides();
                }



                return false;
            }
        });


        Button openBottomSheet = findViewById(R.id.button_open_bottom_sheet);
        openBottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExampleBottomSheetDialog bottomSheet = new ExampleBottomSheetDialog();
                bottomSheet.show(getSupportFragmentManager(), "exampleBottomSheet");
            }
        });

        txtSource = findViewById(R.id.editTextSource);
        txtDestination = findViewById(R.id.editTextDestination);


        ExampleBottomSheetDialog bottomSheet = new ExampleBottomSheetDialog();
        bottomSheet.show(getSupportFragmentManager(), "exampleBottomSheet");



    }

    private void getRides()
    {
        Intent intent=new Intent(this,Rides.class);
        startActivity(intent);
    }

    public void recenterUser(View view)
    {
        Log.e("In recentre","source");

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(source_pt.latitude(), source_pt.longitude())) // Sets the new camera position
                .zoom(17) // Sets the zoom
                .bearing(180) // Rotate the camera
                .tilt(30) // Set the camera tilt
                .build(); // Creates a CameraPosition from the builder

        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 7000);




    }


    private void logoutFrom()
    {
        ExampleBottomSheetDialog.resetDetails();
        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sh.edit();
        editor.remove("name");
        editor.remove("phone");
        editor.remove("Password");
        editor.remove("token");
        editor.remove("ride_id");
        editor.remove("cab_id");
        editor.commit();
        Intent intent=new Intent(this,LoginActivity.class);
        startActivity(intent);
        finishAffinity();

    }

    private void openProfile()
    {
        Intent intent=new Intent(this,ProfileActivity.class);
        startActivity(intent);
    }

    private void openCall()
    {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:0123456789"));
        startActivity(intent);
    }

    public static void clearRideDetails()
    {

        MainActivity.txtSource.setText("");
        MainActivity.txtDestination.setText("");

    }



    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }





    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap; //when map get's ready it passes mapboxMap instance

        //setting style
        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41"),
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        enableLocationComponent(style); ///if permission given set location
                        addDestinationIconSymbolLayer(style); // give style to marker

                        mapboxMap.addOnMapClickListener(MainActivity.this);
                        button = findViewById(R.id.startButton);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getDestination();


                            }
                        });

                    }
                });
    }


    private void getRoute(Point origin, Point destination) {
        Log.e("origin", String.valueOf(origin));
        Log.e("destination", String.valueOf(destination));
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);


                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }



    /**
     * Initialize the Maps SDK's LocationComponent
     */
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            locationComponent = mapboxMap.getLocationComponent();





            // Set the LocationComponent activation options
            //Retrieve and activate the LocationComponent once the user has granted location permission and the map has fully loaded.
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();

            // Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            // Enable to make component visible
            /*
             * There is a single method to either enable or disable the LocationComponent's visibility after activation.
             *  The setLocationComponentEnabled() method requires a true/false boolean parameter. When set to false,
             * this method will hide the device location icon and stop map camera animations from occurring.
             * */
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            /*
             * The method LocationComponent#setCameraMode(@CameraMode.Mode int cameraMode) allows developers to
             *  set specific camera tracking instructions as the device location changes.
             * There are 7 CameraMode options available:
             * */
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode type of compass it is rendered
            //The RenderMode class contains preset options for the device location image.
            //NORMAL
            //COMPASS
            //GPS
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }


    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {

        /*
         * If your application needs location information,
         * the LocationEngine class can help you get this information while also simplifying
         * the process and being flexible enough to use different services. The LocationEngine found in the core
         * module now supports the following location providers:
         * */

        /**
         * This will obtain the best location engine that is available and eliminate the need to
         * create a new LocationEngine from scratch.
         * */
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);


        /*
         * Request location updates once you know location permissions have been granted:
         * */
        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            if (mapboxMap.getStyle() != null) {
                enableLocationComponent(mapboxMap.getStyle());
            }
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        Log.e("in mainactivity","on resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /*
     * To prevent your application from having a memory leak, it is a good idea to stop
     * requesting location updates inside of your activity's onStop() method.
     * */
    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /*
     * To prevent your application from having a memory leak, it is a good idea to stop
     * requesting location updates inside of your activity's onStop() method.
     * */
    protected void onDestroy() {
        super.onDestroy();
        //hide lines
// Prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        //  hide lines
        mapView.onDestroy();
    }

    void getSource()  {

        try{
            String source=txtSource.getText().toString();

            List<Address> foundGeocode = null;
            /* find the addresses  by using getFromLocationName() method with the given address*/
            foundGeocode = new Geocoder(this).getFromLocationName(source, 1);
            foundGeocode.get(0).getLatitude(); //getting latitude
            foundGeocode.get(0).getLongitude();//getting longitude
        }catch(IOException e)
        {
            Log.d("MainActivity","got in error fetching source");
        }
    }

    void getDestination(){

        try{
            String destination=txtDestination.getText().toString();

            List<Address> foundGeocode = null;
            /* find the addresses  by using getFromLocationName() method with the given address*/
            foundGeocode = new Geocoder(this).getFromLocationName(destination, 1);
            foundGeocode.get(0).getLatitude(); //getting latitude
            foundGeocode.get(0).getLongitude();//getting longitude
            destination_pt=Point.fromLngLat(foundGeocode.get(0).getLongitude(),foundGeocode.get(0).getLatitude());
            Log.e("Destination location is",String.valueOf(foundGeocode.get(0).getLatitude())+" "+String.valueOf(foundGeocode.get(0).getLongitude()));


            if(symbolLayerIconFeatureList.size()>=2)
            {
                symbolLayerIconFeatureList.set(1,Feature.fromGeometry(
                        Point.fromLngLat(foundGeocode.get(0).getLongitude(), foundGeocode.get(0).getLatitude())));
            }else
            {
                symbolLayerIconFeatureList.add(1,Feature.fromGeometry(
                        Point.fromLngLat(foundGeocode.get(0).getLongitude(), foundGeocode.get(0).getLatitude())));
            }
            SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            String id = sh.getString("id", null);

            String source_text,destination_text;
            TextView source_view=findViewById(R.id.editTextSource);
            TextView destination_view=findViewById(R.id.editTextDestination);
            source_text=String.valueOf(source_view.getText());
            destination_text=String.valueOf(destination_view.getText());



            // book cab
            BackgroundBookCab backgroundBookCab=new BackgroundBookCab(getApplicationContext());
            backgroundBookCab.delegate=MainActivity.this;
            backgroundBookCab.execute(String.valueOf(source_pt.latitude()),String.valueOf(source_pt.longitude()),String.valueOf(destination_pt.latitude()),String.valueOf(destination_pt.longitude()),id,String.valueOf(1),source_text,destination_text);



        }catch (IOException e)
        {
            Log.d("MainActivity","got in error fetching destination");
        }


    }






    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        if(sh.getString("ride_id",null)==null)
        {
            //setting the address string from points
            getAddressFromLocation(point,MainActivity.this,new GeocoderHandler());



            //
            Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
            Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                    locationComponent.getLastKnownLocation().getLatitude());
            source_pt=destinationPoint;

            if(symbolLayerIconFeatureList.size()>=1)
            {
                symbolLayerIconFeatureList.set(0,Feature.fromGeometry(
                        Point.fromLngLat(point.getLongitude(), point.getLatitude())));
            }else
            {
                symbolLayerIconFeatureList.add(0,Feature.fromGeometry(
                        Point.fromLngLat(point.getLongitude(), point.getLatitude())));
            }




            //oth index-customer source
            //1 -customer destination
            //2-driver location

//        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
//        if (source != null) {
//            source.setGeoJson(Feature.fromGeometry(destinationPoint));
//        }

            button.setEnabled(true);
            button.setBackgroundResource(R.color.mapbox_blue);
            return true;

        }

        return true;
    }



    public static void getAddressFromLocation(LatLng point, final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                try {
                    List<Address> list = geocoder.getFromLocation(
                            point.getLatitude(), point.getLongitude(), 1);
                    if (list != null && list.size() > 0) {
                        Address address = list.get(0);
                        // sending back first address line and locality
                        result = address.getAddressLine(0) + ", " + address.getLocality();
                    }
                } catch (IOException e) {
                    Log.e("Helllo", "Impossible to connect to Geocoder", e);
                } finally {
                    Message msg = Message.obtain();
                    msg.setTarget(handler);
                    if (result != null) {
                        msg.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putString("address", result);
                        msg.setData(bundle);
                    } else
                        msg.what = 0;
                    msg.sendToTarget();
                }
            }
        };
        thread.start();
    }


    @Override
    public void processStringFinish(Ride_Details s) {

        Log.e("Cab Booked","Ride booked");

        // do here
        ExampleBottomSheetDialog.driverName += s.driver_name;
        ExampleBottomSheetDialog.phone += s.driver_phone;
        ExampleBottomSheetDialog.cabNo += String.valueOf(s.cab_no);
        ExampleBottomSheetDialog.cabFare += String.valueOf(s.cab_fare);
        ExampleBottomSheetDialog.modelName += s.model_name;

        rideId = String.valueOf(s.ride_id);
        cabId = String.valueOf(s.cab_id);

        driverLat = s.getCab_lat();
        driverLng = s.getCab_lng();

        symbolLayerIconFeatureList.add(2,Feature.fromGeometry(
                Point.fromLngLat(driverLng, driverLat)));




        driver_pt=Point.fromLngLat(driverLng, driverLat);
        Log.e("Driver location is",String.valueOf(driverLat+" "+driverLng));

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")

// Add the SymbolLayer icon image to the map style
                .withImage(ICON_ID, BitmapFactory.decodeResource(
                        MainActivity.this.getResources(), R.drawable.mapbox_marker_icon_default))

// Adding a GeoJson source for the SymbolLayer icons.
                .withSource(new GeoJsonSource(SOURCE_ID,
                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))

// Adding the actual SymbolLayer to the map style. An offset is added that the bottom of the red
// marker icon gets fixed to the coordinate, rather than the middle of the icon being fixed to
// the coordinate point. This is offset is not always needed and is dependent on the image
// that you use for the SymbolLayer icon.
                .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                        .withProperties(
                                iconImage(ICON_ID),
                                iconAllowOverlap(true),
                                iconIgnorePlacement(true)
                        )
                ), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

// Map is set up and the style has loaded. Now you can add additional data or make other map adjustments.


            }
        });

//        if(driver_mk==null)
//        {
//            driver_mk=mapboxMap.addMarker(new MarkerOptions()
//                    .position(new LatLng(driverLat, driverLng)));
//        }else
//        {
//            driver_mk.setPosition(new LatLng(driverLat, driverLng));
//        }
        Log.e("Number of markers",String.valueOf(symbolLayerIconFeatureList.size()));
        Log.e("markers",String.valueOf(symbolLayerIconFeatureList));
        getRoute(source_pt,driver_pt);

        ExampleBottomSheetDialog.updateDetails();

        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String cust_name=sh.getString("name",null);
        String cust_phone=sh.getString("phone",null);
        SharedPreferences.Editor myEdit = sh.edit();
        myEdit.putString("cab_id",cabId);
        myEdit.putString("ride_id",rideId);
        myEdit.putString("driver_phone",s.driver_phone);
        myEdit.putString("driver_lat",String.valueOf(driverLat));
        myEdit.putString("driver_lng",String.valueOf(driverLng));
        myEdit.putString("ride_started","no");
        myEdit.apply();

        BackgroundSendNotification backgroundSendNotification=new BackgroundSendNotification(getApplicationContext());
        backgroundSendNotification.execute("Ride Awaiting",cust_name+" has booked a ride "+cust_phone,s.driver_phone);
    }



    @SuppressLint("LongLogTag")
    public void arrageMarkers()
    {
        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")

// Add the SymbolLayer icon image to the map style
                .withImage(ICON_ID, BitmapFactory.decodeResource(
                        MainActivity.this.getResources(), R.drawable.mapbox_marker_icon_default))

// Adding a GeoJson source for the SymbolLayer icons.
                .withSource(new GeoJsonSource(SOURCE_ID,
                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))

// Adding the actual SymbolLayer to the map style. An offset is added that the bottom of the red
// marker icon gets fixed to the coordinate, rather than the middle of the icon being fixed to
// the coordinate point. This is offset is not always needed and is dependent on the image
// that you use for the SymbolLayer icon.
                .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                        .withProperties(
                                iconImage(ICON_ID),
                                iconAllowOverlap(true),
                                iconIgnorePlacement(true)
                        )
                ), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
        // Map is set up and the style has loaded. Now you can add additional data or make other map adjustments
            }
        });
        if(symbolLayerIconFeatureList.size()>0)
        {
            if(symbolLayerIconFeatureList.size()>2)
            {
                getRoute(source_pt,driver_pt);
            }
            else
            {
                Log.e("From source to dest","get route");
                Toast.makeText(getApplicationContext(),"Your ambulance has arrived",Toast.LENGTH_LONG).show();
                SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sh.edit();
                myEdit.putString("ride_started","yes");
                myEdit.apply();
                getRoute(source_pt,destination_pt);
            }

        }


    }

    @Override
    public void onButtonClicked(String text) {
        if(text.equals("Button 2 clicked")) {
            // cancel ride button clicked

            Log.e("Hello", "Cancel Cab executes");
            // cancel cab
            BackgroundCancelCab backgroundCancelCab=new BackgroundCancelCab(getApplicationContext());
            backgroundCancelCab.execute(rideId, cabId);
        }
    }
}



class GeocoderHandler extends Handler {
    @Override
    public void handleMessage(Message message) {
        String result;
        switch (message.what) {
            case 1:
                Bundle bundle = message.getData();
                result = bundle.getString("address");
                break;
            default:
                result = null;
        }
        // replace by what you need to do
        //Log.e("geocoder source",result);
        MainActivity.txtSource.setText(result);
    }
}