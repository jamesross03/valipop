/*
 * valipop - <https://github.com/stacs-srg/valipop>
 * Copyright © 2025 Systems Research Group, University of St Andrews (graham.kirby@st-andrews.ac.uk)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.valipop.utils.addressLookup;

/**
 * Utility functions for dealing with distance and movements with map coordinates. 
 * 
 * Also an executable class for some reason.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class GPSDistanceConverter {

    public static double distance(Coords a, Coords b, char unit) {

        unit = Character.toUpperCase(unit);

        double lat1 = a.lat;
        double lon1 = a.lon;

        double lat2 = b.lat;
        double lon2 = b.lon;

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    public static Coords move(Coords origin, double distanceKM, double onBearing) {

        double theta = Math.toRadians(onBearing);

        double lat = origin.lat + (360 * distanceKM * Math.cos(theta) / (2 * Math.PI * getEarthRadius(origin.lat)));
        double lon = origin.lon + (360 * distanceKM * Math.sin(theta) / (2 * Math.PI * getEarthRadius(origin.lat) * Math.sin(deg2rad(90 - origin.lat))));

        return new Coords(lat, lon);

    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private static final double EQUATOR_RADIUS = 6378.137;
    private static final double POLAR_RADIUS = 6356.752;

    private static double getEarthRadius(double lat) {

        lat = deg2rad(lat);

        return Math.sqrt(
            (Math.pow(Math.pow(EQUATOR_RADIUS, 2) * Math.cos(lat), 2) + Math.pow(Math.pow(POLAR_RADIUS, 2) * Math.sin(lat), 2))
                    /
            (Math.pow(EQUATOR_RADIUS * Math.cos(lat), 2) + Math.pow(POLAR_RADIUS * Math.sin(lat), 2))
        );
    }

    public static void main(String[] args) {

        Coords origin = new Coords(55.52654705, -2.43492198501177);

        for(int i = 0; i <= 360; i+=12) {
            System.out.println(move(origin, 25, i).toString());
        }

    }

}
