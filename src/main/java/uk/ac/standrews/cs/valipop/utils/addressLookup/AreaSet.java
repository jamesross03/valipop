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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a set of areas.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class AreaSet implements Serializable {

    private static final long serialVersionUID = 76428763786238422L;

    private ArrayList<Area> areas = new ArrayList<>();

    private long uptoNumber = 0;

    public AreaSet(Area firstArea) {
        areas.add(firstArea);
        uptoNumber += firstArea.getMaximumNumberOfAbodes();
    }

    public Collection<Area> getAreas() {
        return areas;
    }

    public long addArea(Area area) {
        areas.add(area);
        long offset = uptoNumber;
        uptoNumber += area.getMaximumNumberOfAbodes();
        return offset;
    }

}
