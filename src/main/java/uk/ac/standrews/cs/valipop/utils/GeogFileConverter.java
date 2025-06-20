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
package uk.ac.standrews.cs.valipop.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Area;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Cache;

/**
 * Used to convert a custom geography files (.ser) into JSON. May be executed standalone.
 */
public class GeogFileConverter {

    public static void convert(String source, String target) throws IOException, ClassNotFoundException {

        Cache geography = Cache.readFromFile(source);

        new ObjectMapper().writer().writeValue(new File(target),
                                               geography.getAllAreas().stream()
                                                       .filter(GeogFileConverter::isDataComplete)
                                                       .collect(Collectors.toList()));
    }

    private static boolean isDataComplete(Area area) {
        return !(area.getRoad() == null || area.getSuburb() == null && area.getTown() == null && area.getCounty() == null);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        convert("valipop/src/main/resources/valipop/inputs/scotland_test_population_no_mig/annotations/geography/scotland-residential-ways.ser",
                "valipop/src/main/resources/valipop/inputs/scotland_test_population/annotations/geography/scotland-residential-ways.json");
    }

}
