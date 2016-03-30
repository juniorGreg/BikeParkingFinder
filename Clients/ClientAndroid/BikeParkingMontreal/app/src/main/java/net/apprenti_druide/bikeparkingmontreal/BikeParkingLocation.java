package net.apprenti_druide.bikeparkingmontreal;

import org.osmdroid.util.GeoPoint;

/**
 * Created by novae on 16-03-28.
 */

public class BikeParkingLocation {
    public int status;
    public GeoPoint coord;
    public String id;

    public BikeParkingLocation(String id, GeoPoint coord, int status)
    {
        this.id = id;
        this.coord = coord;
        this.status = status;
    }

}
