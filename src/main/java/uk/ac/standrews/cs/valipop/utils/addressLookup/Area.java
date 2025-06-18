package uk.ac.standrews.cs.valipop.utils.addressLookup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
 * Represents a serializable area defined by an address, bounding box, and place id.
 * Created by passing in input geography data.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Area implements Serializable {

    private static final long serialVersionUID = 8749827387328328989L;

    private static final ObjectMapper mapper = new ObjectMapper();

    @JsonProperty("error")
    private String error = "none";

    @JsonProperty("place_id")
    private long placeId;

    private String road;
    private String suburb;
    private String town;
    private String county;
    private String state;
    private String postcode;

    @JsonProperty("address")
    private void unpackNestedAddress(Map<String, Object> address) {
        road = (String) address.get("road");
        suburb = (String) address.get("suburb");
        town = (String) address.get("town");
        county = (String) address.get("county");
        state = (String) address.get("state");
        postcode = (String) address.get("postcode");
    }

    @JsonProperty("boundingbox")
    private String[] boundingBoxString;

    private BoundingBox boundingBox;

    private Place details;

    public long getNumberingOffset() {
        return numberingOffset;
    }

    public void setNumberingOffset(long numberingOffset) {
        this.numberingOffset = numberingOffset;
    }

    public void setMaximumNumberOfAbodes(long maximumNumberOfAbodes) {
        this.maximumNumberOfAbodes = maximumNumberOfAbodes;
    }

    // a street can be made up of many areas, the offset prevents each subpart having the same house numbers
    private long numberingOffset = 0;
    private long maximumNumberOfAbodes = 0;

    private transient ArrayList<Address> addresses = new ArrayList<>();

    public static Area makeArea(String jsonInput, Cache cache) throws IOException, InvalidCoordSet, InterruptedException, APIOverloadedException {
        Area area = mapper.readValue(jsonInput, Area.class);

        if(area.error.equals("none")) {

            // do we already have this area?
            Area a = cache.getAreaByID(area.placeId);
            if(a != null) {
                return a;
            }

            area.boundingBox = new BoundingBox(area.boundingBoxString);
            area.details = OpenStreetMapAPI.getPlaceFromAPI(area.placeId, cache);

            if (area.isResidential()) {
                area.maximumNumberOfAbodes = Math.round(ReverseGeocodeLookup.ABODES_PER_KM * GPSDistanceConverter.distance(area.boundingBox.getBottomLeft(), area.boundingBox.getTopRight(), 'K'));

                try {
                    String areaString = area.getAreaSetString();
                    AreaSet set = cache.getAreaSet(areaString);

                    if (set == null) {
                        cache.addAreaSet(areaString, new AreaSet(area));
                    } else {
                        area.numberingOffset = set.addArea(area);
                    }

                } catch (IncompleteAreaInformationException e) {
                    System.out.println("--- Area incomplete ---");
                    System.out.println(area);
                }

            }

            cache.addToAreaIndex(area.placeId, area);

        } else {
            area.placeId = cache.decrementErrorID();
        }

        return area;
    }

    public String getAreaSetString() throws IncompleteAreaInformationException {
        String s = "";

        if(road == null || suburb == null && town == null && county == null) {
            throw new IncompleteAreaInformationException();
        }

        s += road;

        if(suburb != null)
            s += suburb;

        if(town != null)
            s += town;

        return s;

    }

    public Address getFreeAddress(Geography geography) {

        if(addresses == null) addresses = new ArrayList<>();

        for(Address address : addresses) {
            if(!address.isInhabited()) {
                return address;
            }
        }

        if(addresses.size() < maximumNumberOfAbodes) {
            // +1 so that house numbers don't start at zero!
            Address newAddress = new Address(numberingOffset + addresses.size() + 1, this, geography);
            addresses.add(newAddress);
            return newAddress;
        }

        return null;
    }

    public boolean containsPoint(double lat, double lon) {
        return boundingBox.containsPoint(lat, lon);
    }

    public boolean isResidential() {
        return details.getType().equalsIgnoreCase("residential");
    }

    public long getMaximumNumberOfAbodes() {
        return maximumNumberOfAbodes;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();

        if(!isErroneous()) {

            int count = 3;

            if(getRoad() != null && count-- > 0)
                s.append(getRoad());

            if(getSuburb() != null && count-- > 0) {
                if (count != 2) s.append(", ");
                s.append(getSuburb());
            }

            if(getTown() != null && count-- > 0) {
                if (count != 2) s.append(", ");
                s.append(getTown());
            }

            if(getCounty() != null && count-- > 0) {
                if (count != 2) s.append(", ");
                s.append(getCounty());
            }

        } else {
            s.append("ERRONEOUS AREA - " + error);
        }

        return s.toString();
    }

    public boolean isErroneous() {
        return !error.equals("none");
    }

    public boolean isWay() {
        return (details != null && details.isWay());
    }

    public String getState() {
        return (state == null ? "" : state);
    }

    public Coords getCentroid() {
        return details.getCentroid();
    }

    public double getDistanceTo(double lat, double lon) {
        return Math.sqrt(Math.pow(lat - getCentroid().lat, 2) + Math.pow(lon - getCentroid().lon, 2));
    }

    public double getDistanceTo(Coords coords) {
        return getDistanceTo(coords.lat, coords.lon);
    }

    public String getRoad() {
        return road;
    }

    public String getSuburb() {
        return suburb;
    }

    public String getTown() {
        return town;
    }

    public String getCounty() {
        return county;
    }

    public String getPostcode() {
        return postcode;
    }

    public Long getPlaceID() {
        return placeId;
    }

    public boolean isFull() {

        if(addresses == null) addresses = new ArrayList<>();

        if(addresses.size() < maximumNumberOfAbodes)
            return false;

        for(Address address : addresses)
            if(!address.isInhabited())
                return false;

        return true;
    }

    public String getError() {
        return error;
    }

    public long getPlaceId() {
        return placeId;
    }

    public String[] getBoundingBoxString() {
        return boundingBoxString;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public Place getDetails() {
        return details;
    }

    public ArrayList<Address> getAddresses() {
        return addresses;
    }
}
