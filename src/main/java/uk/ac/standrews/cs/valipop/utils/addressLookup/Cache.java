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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the raw data read from a geography file.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class Cache implements Serializable {

    private static final long serialVersionUID = 748931747946237593L;

    private ArrayList<Area> areaDB = new ArrayList<>();

    private Map<Double, Map<Double, Area>> lookupHistory = new HashMap<>();

    private Map<String, AreaSet> areaSets = new HashMap<>();

    private long nextErrorID = -1;

    private Map<Long, Area> areaIndex = new HashMap<>();

    private final int HISTORY_PRECISION = 4;
    private final double PRECISION_ADJUSTMENT = Math.pow(10, HISTORY_PRECISION);

    private boolean updated = false;

    private transient String filePath;

    public Area checkCache(double lat, double lon) {

        // Checks that given coords don't correspond to a bounding box already retrieved
        Map<Double, Area> index = lookupHistory.get(round(lat));
        if(index != null) {
            return index.get(round(lon));
        }

        for(Area area : areaDB) {
            if(area.containsPoint(lat, lon)) {
                // Here we are just taking the first one we find - may want to change this later
                // (Bounding boxes can overlap due to their definition)

                return area;
            }
        }

        return null;
    }

    public void addHistory(double lat, double lon, Area area) {
        Map<Double, Area> index = lookupHistory.get(round(lat));

        if(index == null) {
            index = new HashMap<>();
            lookupHistory.put(round(lat), index);
        }

        index.put(round(lon), area);
    }

    private Double round(double d) {
        return Math.round(d * PRECISION_ADJUSTMENT) / PRECISION_ADJUSTMENT;
    }

    public void removeArea(Area area)  {

        areaIndex.remove(area.getPlaceID(), area);

        Map<Double, Area> index = lookupHistory.get(round(area.getCentroid().lat));
        if(index != null) {
            index.remove(round(area.getCentroid().lon), area);

            if (index.size() == 0) {
                lookupHistory.remove(round(area.getCentroid().lat), index);
            }
        }

        areaDB.remove(area);

        for(AreaSet aS : areaSets.values()) {
            aS.getAreas().remove(area);

            if(aS.getAreas().size() == 0) {
                try {
                    areaSets.remove(area.getAreaSetString(), area);
                } catch (IncompleteAreaInformationException e) {
                    // if an areaset string cannot be generated then the area will never have been added to areaSets.
                    // Thus we needn't worry about not being able to remove it
                }
            }

        }

    }

    public void writeToFile() throws IOException {
        writeToFile(filePath);
    }

    public void writeToFile(String fileName) throws IOException {

        if(updated) {
            FileOutputStream fileOut = new FileOutputStream(fileName + ".new");
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(this);
            objectOut.flush();
            objectOut.close();

            // Ensures writing a new copy either complete or the old version survives
            Files.move(Paths.get(fileName + ".new"), Paths.get(fileName), StandardCopyOption.ATOMIC_MOVE);

            updated = false;
        }

    }

    public static Cache readFromFile(String fileName) throws IOException, ClassNotFoundException {

        FileInputStream fileIn = new FileInputStream(fileName);
        ObjectInputStream objectIn = new ObjectInputStream(fileIn);

        Cache cache = (Cache) objectIn.readObject();
        objectIn.close();

        cache.setFilePath(fileName);

        return cache;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void addArea(Area area) {
        areaDB.add(area);
        updated = true;
    }

    public List<Area> getAllAreas() {
        return areaDB;
    }

    public AreaSet getAreaSet(String areaString) {
        return areaSets.get(areaString);
    }

    public void addAreaSet(String areaString, AreaSet areaSet) {
        areaSets.put(areaString, areaSet);
        updated = true;
    }

    public long decrementErrorID() {
        updated = true;
        return nextErrorID--;
    }

    public Area getAreaByID(long placeID) {
        return areaIndex.get(placeID);
    }

    public void addToAreaIndex(long placeId, Area area) {
        areaIndex.put(placeId, area);
        updated = true;
    }

    public int size() {
        return areaDB.size();
    }

    public void setAllAreas(ArrayList<Area> newAllAreasList) {
        this.areaDB = newAllAreasList;
    }
}

