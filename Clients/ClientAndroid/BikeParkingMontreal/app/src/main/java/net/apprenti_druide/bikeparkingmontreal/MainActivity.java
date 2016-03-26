package net.apprenti_druide.bikeparkingmontreal;

import android.app.Activity;

import android.content.Context;
import android.location.LocationManager;
import android.location.Location;
import android.os.Bundle;

import android.content.Context;
import android.widget.Toast;


import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import  org.osmdroid.views.overlay.ItemizedOverlay;


import java.util.ArrayList;

public class MainActivity extends Activity {

    final String gpsLocationProvider = LocationManager.GPS_PROVIDER;
    final String networkLocationProvider = LocationManager.NETWORK_PROVIDER;

    private ItemizedOverlay<OverlayItem> mMyLocationOverlay;
    private ResourceProxy mResourceProxy;
    private float accuracy = 1000.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
        MapView map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        //map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        mapController.setZoom(15);


        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        GeoPoint startPoint = getLastPosition();
        mapController.setCenter(startPoint);
        items.add(new OverlayItem("Here", "SampleDescription", startPoint));

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
                        Toast.makeText(
                                MainActivity.this,
                                "Item '" ,Toast.LENGTH_LONG).show();
                        return false;
                    }
                }, mResourceProxy);
        map.getOverlays().add(this.mMyLocationOverlay);
        map.invalidate();
    }

    private GeoPoint getLastPosition()
    {
        GeoPoint point = new GeoPoint(45.5077, -73.544);

        LocationManager locationManager =
                (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        Location lastKnownLocation_byGps =
                locationManager.getLastKnownLocation(gpsLocationProvider);
        Location lastKnownLocation_byNetwork =
                locationManager.getLastKnownLocation(networkLocationProvider);

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
}
