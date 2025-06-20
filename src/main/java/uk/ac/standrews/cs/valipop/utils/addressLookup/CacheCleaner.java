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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * UNUSED
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class CacheCleaner {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        String file = "src/main/resources/valipop/geography-cache/scotland-residential-ways.ser";

        Cache cache = Cache.readFromFile(file);

        for(Area area : cache.getAllAreas()) {
            System.out.println(area.toString());
        }

//        removeNullAddressAreas(cache);

//        cache.writeToFile();


    }

    @SuppressWarnings("unused")
    private static void removeNonResidentialAreas(Cache cache) {

        System.out.println(cache.size());

        Collection<Area> toRemove = new ArrayList<>();

        for(Area area : cache.getAllAreas()) {
            if(!area.isResidential()) {
                toRemove.add(area);
                System.out.println(area.toString());

            }
        }

        System.out.println("-------");

        for(Area area : toRemove) {
            cache.removeArea(area);
            System.out.println(cache.size());
        }

    }

    @SuppressWarnings("unused")
    private static void removeNonScotlandAreas(Cache cache) {

        System.out.println(cache.size());

        Collection<Area> toRemove = new ArrayList<>();

        for(Area area : cache.getAllAreas()) {
            if(!area.getState().equals("Scotland")) {
                toRemove.add(area);
                System.out.println(area.toString());
            }
        }

        System.out.println("-------");

        for(Area area : toRemove) {
            cache.removeArea(area);
            System.out.println(cache.size());
        }

    }

    @SuppressWarnings("unused")
    private static void removeNullAddressAreas(Cache cache) {

        System.out.println(cache.size());

        Collection<Area> toRemove = new ArrayList<>();

        for(Area area : cache.getAllAreas()) {

            int count = 0;

            if(area.getRoad() == null)
                count++;

            if(area.getSuburb() == null)
                count++;

            if(count == 2)
                toRemove.add(area);

        }

        System.out.println(toRemove.size());



        for(Area area : toRemove) {
            cache.removeArea(area);
//            System.out.println(area.toString());
        }

        System.out.println(cache.size());

    }

}
