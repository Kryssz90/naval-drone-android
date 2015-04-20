package com.kryssz.lego3;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kryssz on 2015.04.19..
 */
//Lesznek importok amiket nem irtam ide.

public class Navigation
{
    double compass; //Ez lesz az iránytü
    List<Location> locs = new ArrayList<Location>();
    //Ebbe pedig tároljuk az előzőleg mért n db kooridnátát

    Location dest; // Ide tároljuk a célkoordinátát, ahova menni kell


    public void setDestination(Location d)
    {
        dest = d;
    }

    public void setDestination(double lat, double lon)
    {
        Location loc = new Location("gps");
        loc.setLatitude(lat);
        loc.setLongitude(lon);
        dest = loc;
    }

    public Location getDestination()
    {
        return dest;
    }

    public void setCompass(double irany)
    {
        compass = irany;
    }

    public double getCompass()
    {
        return compass;
    }

    public void addLocation(Location l)
    {
        locs.add(l);
        while(locs.size() > 10)
        {
            locs.remove(0);
        }
	   /*
	   Itt lehet a size és a removeat nem igy van, ez javitsd ki.

	   Feladata: bejön egy location, betároljuk a lista végébe, a legelső elemeket pedig addig szedjük ki, amig a lista hossza nem lesz 10 alatt.
	   */
    }


    public void addLocation(double lat, double lon)
    {
        Location loc = new Location("gps");
        loc.setLatitude(lat);
        loc.setLongitude(lon);
        addLocation(loc);
    }

    public void clearLocations()
    {
        locs.clear();
        //Ez se biztos hogy igy van, a leényeg hogy törölje a lista elemeit.
        // utánaolvasgattam ennek kell lennie Following is the declaration for java.util.ArrayList.clear() method
    }


    public Location getLastLocation()
    {
        if(locs.size() > 0)
        {
            return locs.get( locs.size()-1 );
        }
        return null;
        // Ha van a listába már tárolt elem akkor a legutóbbit fogja visszaadni, különben pedig null-t.
    }

    private double BearingTo(Location a, Location b)
    {
        double irany = a.bearingTo(b);
        if(irany < 0) irany = 360+irany;
        return irany;
    }


    public double getHeading() //bearing
    {

        double a = BearingTo( locs.get(2), locs.get(3)  );
        double b = BearingTo( locs.get(1), locs.get(2)  );
        double c = BearingTo( locs.get(0), locs.get(1)  );

        double d = a*(1/2d) + b*(1/3d) + c*(1/6d);
        return  d;

    }

    public double getDistance()
    {
        return getLastLocation().distanceTo(getDestination());
    }


    public double getBearingToDest()
    {
        return BearingTo(getLastLocation(), getDestination());
    }

}
