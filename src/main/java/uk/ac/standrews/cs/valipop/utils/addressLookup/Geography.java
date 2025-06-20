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

import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class Geography {

    private final List<Area> residentialGeography;

    private TreeMap<Double, TreeMap<Double, Area>> areaLookup = new TreeMap<>();

    private final int HISTORY_PRECISION = 4;
    private final double PRECISION_ADJUSTMENT = Math.pow(10, HISTORY_PRECISION);

    private RandomGenerator rand;

    static final String[] SCOTLAND_COORDS = {"54.4","59.4","-7.9","-1.3"};
    static BoundingBox geographicalLimits;

    static {
        try {
            geographicalLimits = new BoundingBox(SCOTLAND_COORDS);
        } catch (InvalidCoordSet invalidCoordSet) {
            invalidCoordSet.printStackTrace();
        }
    }


    public Geography(List<Area> residentialGeography, RandomGenerator random, double overSizedGeographyFactor) {
        this.rand = random;

        ArrayList<Area> newAllAreasList = new ArrayList<>();

        for(Area area : residentialGeography) {

            if(geographicalLimits.containsPoint(area.getCentroid())) {
                if (!area.isFull()) {

                    // scale street numbers by factor
                    area.setNumberingOffset((int) Math.ceil(area.getNumberingOffset() * overSizedGeographyFactor));
                    area.setMaximumNumberOfAbodes((int) Math.ceil(area.getMaximumNumberOfAbodes() * overSizedGeographyFactor));

                    addToLookup(area);
                    newAllAreasList.add(area);
                }

            }
        }
        this.residentialGeography = newAllAreasList;
    }

    public void updated(Address address) {
        if(address.getArea().isFull()) {
            removeFromLookup(address.getArea());
        } else {
            addToLookup(address.getArea());
        }
    }

    public Address getRandomEmptyAddress() {

        List<Area> allAreas = residentialGeography;

        Address area = null;

        do {
            area = allAreas.get(rand.nextInt(allAreas.size())).getFreeAddress(this);
        } while (area == null);

        return area;

    }

    public Address getNearestEmptyAddressAtDistance(Coords origin, double distance) {

        int angle = rand.nextInt(360);
        int count = 0;

        int step = 6;

        Address address = null;
        double distanceDelta = Double.MAX_VALUE;

        do {
            Coords candidateLocation = GPSDistanceConverter.move(origin, distance, angle);

            Address selectedAddress = getNearestEmptyAddress(candidateLocation);

            if(selectedAddress != null) {
                double selectedDistanceDelta = Math.abs(distance - selectedAddress.getArea().getDistanceTo(origin));

                if(selectedDistanceDelta < distanceDelta) {
                    address = selectedAddress;
                    distanceDelta = selectedDistanceDelta;
                }
            }

            count++;

            if((angle += step) >= 360) {
                angle -= 360;
            }

        } while (count < 360 / step);

        if(address == null) {
            System.out.println("Cannot find the 'nearest' address to below location: ");
            System.out.println(origin.toString() + " @ distance " + distance);
            System.out.println("This likely means the population is too large for the provided residential geography");
            System.out.println("Your options are:\n" +
                    "1) If using a geographical limits bounding box increase its size (providing it does not already encompass the whole residentional geography)\n" +
                    "2) Provide a larger residential geography\n" +
                    "3) Increase the 'oversized_geography_factor' - this allows more houses to be created on each road");
        }

        return address;

    }

    public Address getNearestEmptyAddress(Coords origin) {
        return getNearestEmptyAddress(origin.lat, origin.lon);
    }

    public Address getNearestEmptyAddress(double lat, double lon) {

        List<Map.Entry<Double, Area>> list = new ArrayList<>();

        double flooredLon = lon;

        // middle row
        Map.Entry<Double, TreeMap<Double, Area>> e = areaLookup.floorEntry(lat);
        if(e != null) addToList(list, e.getValue().higherEntry(lon));

        Map.Entry<Double, Area> floorEntry;
        e = areaLookup.floorEntry(lat);
        if(e != null) {
            floorEntry = e.getValue().floorEntry(lon);
            addToList(list, floorEntry);

            if(floorEntry != null)
                flooredLon = floorEntry.getKey();
        }


        e = areaLookup.floorEntry(lat);
        if(e != null) addToList(list, e.getValue().lowerEntry(flooredLon));

        flooredLon = lon;

        double flooredLat = lat;

        if(!list.isEmpty()) {
            flooredLat = list.get(0).getKey();
        }

        // lower row
        e = areaLookup.lowerEntry(flooredLat);
        if(e != null) addToList(list, e.getValue().higherEntry(lon));


        e = areaLookup.lowerEntry(flooredLat);
        if(e != null) {
            floorEntry = e.getValue().floorEntry(lon);
            addToList(list, floorEntry);

            if (floorEntry != null)
                flooredLon = floorEntry.getKey();
        }

        e = areaLookup.lowerEntry(flooredLat);
        if(e != null) addToList(list, e.getValue().lowerEntry(flooredLon));

        flooredLon = lon;

        // upper row
        e = areaLookup.higherEntry(lat);
        if(e != null) addToList(list, e.getValue().floorEntry(lon));

        e = areaLookup.higherEntry(lat);
        if(e != null) {
            floorEntry = e.getValue().higherEntry(lon);
            addToList(list, floorEntry);

            if (floorEntry != null)
                flooredLon = floorEntry.getKey();
        }

        e = areaLookup.higherEntry(lat);
        if(e != null) addToList(list, e.getValue().lowerEntry(flooredLon));

        // now we have a list of the nearest existant areas by topology - we now need to calculate which is the nearest

        double smallestDistance = Double.MAX_VALUE;
        Address nearestAddress = null;

        for(Map.Entry<Double, Area> areaEntry : list) {

            Area area = areaEntry.getValue();

            Address address;
            if(area.containsPoint(lat, lon) && (address = area.getFreeAddress(this)) != null) {
                return address;
            }

            double distance = area.getDistanceTo(lat, lon);

            if(distance < smallestDistance && (address = area.getFreeAddress(this)) != null) {
                smallestDistance = distance;
                nearestAddress = address;
            }

        }

        if(nearestAddress == null) {
            System.out.println("Something seems broke - cannot find the 'nearest' address to below location: ");
            System.out.println(lat + ", " + lon);
        }

        return nearestAddress;

    }

    private void addToList(List<Map.Entry<Double, Area>> list, Map.Entry<Double, Area> toAdd) {

        if(toAdd != null) {
            list.add(toAdd);
        }

    }


    private Double round(double d) {
        return Math.round(d * PRECISION_ADJUSTMENT) / PRECISION_ADJUSTMENT;
    }

    private void addToLookup(Area area) {
        Coords centroid = area.getCentroid();
        addToLookup(area, centroid.lat, centroid.lon);
    }

    private void addToLookup(Area area, double lat, double lon) {
        areaLookup.computeIfAbsent(round(lat), k -> new TreeMap<>())
                .put(round(lon), area);
    }

    private void removeFromLookup(Area area) {

        Map<Double, Area> index = areaLookup.get(round(area.getCentroid().lat));
        if(index != null) {
            index.remove(round(area.getCentroid().lon), area);

            if (index.size() == 0) {
                areaLookup.remove(round(area.getCentroid().lat), index);
            }
        }

    }

}
