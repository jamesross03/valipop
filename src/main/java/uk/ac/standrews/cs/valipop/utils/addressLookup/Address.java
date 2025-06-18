package uk.ac.standrews.cs.valipop.utils.addressLookup;

import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a location with persons living in it.
 * 
 * May alternatively represent an entire country.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class Address {

    long number;
    Area area = null;
    Geography geography;

    public boolean isCountry() {
        return country;
    }

    boolean country = false;
    String name = "";

    ArrayList<IPerson> inhabitants = new ArrayList<>();

    public Address(long number, Area area, Geography geography) {
        this.number = number;
        this.area = area;
        this.geography = geography;
    }

    public Address(String country) {
        this.country = true;
        name = country;
    }

    public void addInhabitant(IPerson person) {

        boolean wasInhabited = isInhabited();

        inhabitants.add(person);

        if (!wasInhabited && !country)
            geography.updated(this);

    }

//    public void addInhabitants(Set<IPerson> people) {
//        for(IPerson p : people)
//            addInhabitant(p);
//    }

    public boolean removeInhabitant(IPerson person) {

        boolean ret = inhabitants.remove(person);

        if (!isInhabited() && !country)
            geography.updated(this);

        return ret;

    }

    public List<IPerson> getInhabitants() {
        return inhabitants;
    }

    public boolean isInhabited() {
        return inhabitants.size() != 0;
    }

    public Area getArea() {
        return area;
    }

    public String toString() {

        StringBuilder s = new StringBuilder();
        s.append("\"");

        if(!country) {
            s.append(number + " ");
            s.append(area.toString());
        } else {
            s.append(name);
        }

        s.append("\"");
        return s.toString();

    }

    public String toShortForm() {

        StringBuilder s = new StringBuilder();
        s.append("\"");

        if(!country) {

            int count = 2;

            if (area.getSuburb() != null && count-- > 0)
                s.append(area.getSuburb() + " ");

            if (area.getTown() != null && count-- > 0)
                s.append(area.getTown() + " ");

            if (area.getCounty() != null && count > 0)
                s.append(area.getCounty());

        } else {
            s.append(name);
        }

        s.append("\"");
        return s.toString();

    }

    public void displaceInhabitants() {

        Address moveTo;

        // find new empty address
        if(!area.isFull()) {
            moveTo = area.getFreeAddress(geography);
        } else {
            moveTo = geography.getNearestEmptyAddress(area.getCentroid());
        }

        while(inhabitants.size() > 0) {
            IPerson evictee = inhabitants.get(0);
            LocalDate moveDate = evictee.cancelLastMove(geography);
            evictee.setAddress(moveDate, moveTo);
        }

    }
}
