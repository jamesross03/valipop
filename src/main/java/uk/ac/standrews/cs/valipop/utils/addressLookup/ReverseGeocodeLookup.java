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
import java.net.URISyntaxException;

/**
 * Return an area given coordinates.
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class ReverseGeocodeLookup {

    public static final int ABODES_PER_KM = 500;

    Cache cache;

    public ReverseGeocodeLookup(Cache cache) {
        this.cache = cache;
    }

    public Area getArea(Coords coords) throws IOException, InvalidCoordSet, InterruptedException, APIOverloadedException, URISyntaxException {
        return getArea(coords.lat, coords.lon);
    }

    public Area getArea(double lat, double lon) throws IOException, InvalidCoordSet, InterruptedException, APIOverloadedException, URISyntaxException {

        // look up in cache
        Area area = cache.checkCache(lat, lon);

        // if not found then hit API
        if(area == null) {
            area = OpenStreetMapAPI.getAreaFromAPI(lat, lon, cache);

            if(!area.isErroneous() && area.isWay()) {
                cache.addArea(area);
            }

            // Check if requested point falls in requested boundng box, if not then keep note of which BB the point relates to
            if(area.isErroneous() || !area.isWay() || !area.containsPoint(lat, lon)) {
                cache.addHistory(lat, lon, area);
            }
        }

        return area;
    }
}
