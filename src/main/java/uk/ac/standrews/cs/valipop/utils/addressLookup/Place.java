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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

/**
 * Represents an OSM place.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Place implements Serializable {

    private static final long serialVersionUID = 1234214123787932423L;

    private static ObjectMapper mapper = new ObjectMapper();

    @JsonProperty("category")
    private String category;

    @JsonProperty("type")
    private String type;

    @JsonProperty("osm_type")
    private String osm_type;

    private Coords centroid;

//    @JsonProperty("centroid")
//    private void unpackNestedCentroid(Map<String, Object> centroid) {
//        ArrayList<? extends Number> cen = (ArrayList<? extends Number>) centroid.get("coordinates");
//        if(cen != null) {
//            this.centroid = new Coords(cen.get(1).doubleValue(), cen.get(0).doubleValue());
//        }
//    }


    @JsonProperty("osm_id")
    private long osmWayID;

    public static Place makePlace(String jsonInput) throws IOException {
        Place place = mapper.readValue(jsonInput, Place.class);
        return place;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public Coords getCentroid() {
        return centroid;
    }

    public long getOsmWayID() {
        return osmWayID;
    }

    public boolean isWay() {
        return osm_type.toLowerCase().equals("w");
    }

    public void setCentroid(Coords centroid) {
        this.centroid = centroid;
    }
}
