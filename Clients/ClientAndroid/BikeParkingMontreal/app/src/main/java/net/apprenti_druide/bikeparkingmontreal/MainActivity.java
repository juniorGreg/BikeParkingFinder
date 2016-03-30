package net.apprenti_druide.bikeparkingmontreal;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Location;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;


import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import  org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.bonuspack.overlays.Polygon;



import java.util.ArrayList;

public class MainActivity extends Activity implements LocationListener, AddBikeParkingFragment.onAddBikeParkingListener,
        RemoveOrConfirmParkingFragment.RemoveOrConfirmParkingListener
{

    final String gpsLocationProvider = LocationManager.GPS_PROVIDER;
    final String networkLocationProvider = LocationManager.NETWORK_PROVIDER;
    private LocationManager locationManager;

    private ItemizedOverlay<OverlayItem> mMyLocationOverlay;
    private ItemizedOverlay<OverlayItem> bikeParkingsOverlay;
    private Polygon accCircle;
    private Polygon rangeCircle;
    private Overlay touchOverlay;
    private GeoPoint point;

    private ResourceProxy mResourceProxy;
    private float accuracy = 1000.0f;
    private MapView map;

    private AddBikeParkingFragment addBikeParkingFragment;
    private RemoveOrConfirmParkingFragment removeOrConfirmParkingFragment;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        //map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        mapController.setZoom(15);

        addBikeParkingFragment = AddBikeParkingFragment.newInstance();
        //builder = new AlertDialog.Builder(getApplicationContext());

        removeOrConfirmParkingFragment = RemoveOrConfirmParkingFragment.newInstance();



        GeoPoint startPoint = getLastPosition();
        updateMyPosition(startPoint);

        touchOverlay = new Overlay(this){
            ItemizedIconOverlay<OverlayItem> anotherItemizedIconOverlay = null;
            @Override
            protected void draw(Canvas arg0, MapView arg1, boolean arg2) {

            }
            @Override
            public boolean onSingleTapConfirmed(final MotionEvent e, final MapView mapView) {
                Projection proj = mapView.getProjection();
                GeoPoint loc = (GeoPoint) proj.fromPixels((int)e.getX(), (int)e.getY());
                updateMyPosition(loc);
                return true;
            }
        };
        map.getOverlays().add(touchOverlay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onResume() {
        super.onResume();

        getLastPosition();


    }

    @Override
    public void onPause(){
        super.onPause();

        locationManager.removeUpdates(this);
    }

    private GeoPoint getLastPosition()
    {
        GeoPoint point = new GeoPoint(45.5077, -73.544);

        locationManager =
                (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        Location lastKnownLocation_byGps =
                locationManager.getLastKnownLocation(gpsLocationProvider);
        Location lastKnownLocation_byNetwork =
                locationManager.getLastKnownLocation(networkLocationProvider);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);


        if(lastKnownLocation_byGps!=null) {
            point = new GeoPoint(lastKnownLocation_byGps.getLatitude(), lastKnownLocation_byGps.getLongitude());
            accuracy = lastKnownLocation_byGps.getAccuracy();


        }else
        {
            if (lastKnownLocation_byNetwork != null) {
                point = new GeoPoint(lastKnownLocation_byNetwork.getLatitude(), lastKnownLocation_byNetwork.getLongitude());
                accuracy = lastKnownLocation_byNetwork.getAccuracy();
            }
        }



        return point;
    }

    private void updateMyPosition(GeoPoint point)
    {
        if(this.mMyLocationOverlay != null)
            map.getOverlays().remove(this.mMyLocationOverlay);


        if(accCircle != null)
            map.getOverlays().remove(accCircle);

        if(rangeCircle != null)
            map.getOverlays().remove(rangeCircle);

        map.getController().setCenter(point);

        accCircle = drawCircle(point, accuracy, Color.GREEN, Color.argb(100, 0,255,0));
        map.getOverlays().add(accCircle);
        rangeCircle = drawCircle(point, 350.0f, Color.BLUE, Color.argb(80, 0, 0, 255));
        map.getOverlays().add(rangeCircle);

        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(new OverlayItem("Here", "SampleDescription", point));

        /* OnTapListener for the Markers, shows a simple Toast. */
        this.mMyLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index,
                                                     final OverlayItem item) {
                        Toast.makeText(MainActivity.this, "My position; accuracy:"+accuracy, Toast.LENGTH_LONG).show();
                        return true; // We 'handled' this event.
                    }
                    @Override
                    public boolean onItemLongPress(final int index,
                                                   final OverlayItem item) {

                        return false;
                    }
                }, mResourceProxy);
        map.getOverlays().add(this.mMyLocationOverlay);

        ServerRestfulCall serverRestfulCall = new ServerRestfulCall(this, ServerRestfulCall.REQUEST.GET_BIKE_PARKING, point.toDoubleString());
        serverRestfulCall.execute();

        this.point = point;

        map.invalidate();
    }

    private Polygon drawCircle(GeoPoint center, float radius, int color, int fillColor)
    {
        Polygon oPolygon = new Polygon(this);
        oPolygon.setStrokeWidth(3.0f);
        oPolygon.setStrokeColor(color);
        oPolygon.setFillColor(fillColor);
        ArrayList<GeoPoint> circlePoints = new ArrayList<GeoPoint>();
        for (float f = 0; f < 360; f += 1){
            circlePoints.add(center.destinationPoint(radius, f));
        }
       oPolygon.setPoints(circlePoints);
       return oPolygon;

    }

    public void updateBikeParkings(ArrayList<BikeParkingLocation> bikeParkings)
    {
        if(this.bikeParkingsOverlay != null)
            map.getOverlays().remove(this.bikeParkingsOverlay);

        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        for (BikeParkingLocation point:bikeParkings ) {
            items.add(new OverlayItem(point.id, "Here", "Description", point.coord));


        }

        /* OnTapListener for the Markers, shows a simple Toast. */
        this.bikeParkingsOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index,
                                                     final OverlayItem item) {
                        Toast.makeText(MainActivity.this, "Bike Parking", Toast.LENGTH_LONG).show();

                        return true; // We 'handled' this event.
                    }
                    @Override
                    public boolean onItemLongPress(final int index,
                                                   final OverlayItem item) {
                        removeOrConfirmParkingFragment.bikeParkingId = item.getUid();
                        removeOrConfirmParkingFragment.show(getFragmentManager(), "RemoveOrConfirm");
                        return true;
                    }
                }, mResourceProxy);



        map.getOverlays().add(this.bikeParkingsOverlay);

       map.invalidate();
    }



    @Override
    public void onLocationChanged(Location location) {
        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());

        accuracy = location.getAccuracy();
        updateMyPosition(point);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_all_location:
                getAllLocations();
                break;
            case R.id.action_add_location:
                showAddBikeParking();
                break;
            default:
                break;
        }

        return true;
    }

    //Options Items Actions
    public void getAllLocations()
    {
        ServerRestfulCall serverRestfulCall = new ServerRestfulCall(this, ServerRestfulCall.REQUEST.GET_ALL_BIKE_PARKING, null);
        serverRestfulCall.execute();
    }

    public void showAddBikeParking()
    {

        addBikeParkingFragment.show(getFragmentManager(), "AddBikeParking");

    }


    @Override
    public void addBikeParkingEvent(int capacity) {
        ServerRestfulCall serverRestfulCall = new ServerRestfulCall(this, ServerRestfulCall.REQUEST.ADD_BIKE_PARKING, point.toDoubleString(), Float.toString(accuracy));
        serverRestfulCall.execute();
        addBikeParkingFragment.dismiss();
    }

    @Override
    public void RemoveBikeParkingEvent(String id) {
        ServerRestfulCall serverRestfulCall = new ServerRestfulCall(this, ServerRestfulCall.REQUEST.CONFIRM_BIKE_PARKING, id);
        serverRestfulCall.execute();
        removeOrConfirmParkingFragment.dismiss();
    }

    @Override
    public void ConfirmBikeParkingEvent(String id) {
        ServerRestfulCall serverRestfulCall = new ServerRestfulCall(this, ServerRestfulCall.REQUEST.REMOVE_BIKE_PARKING, id);
        serverRestfulCall.execute();
        removeOrConfirmParkingFragment.dismiss();
    }
}
