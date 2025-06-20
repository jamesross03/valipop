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

/**
 * Represents the rectangle formed by a pair of coordinates.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class BoundingBox implements Serializable {

    private static final long serialVersionUID = 568989809832084920L;

    private Coords bottomLeft;
    private Coords topRight;

    public BoundingBox() {}

    public BoundingBox(String[] coords) throws InvalidCoordSet {

        if(coords.length != 4) {
            throw new InvalidCoordSet();
        }

        bottomLeft = new Coords(coords[0], coords[2]);
        topRight = new Coords(coords[1], coords[3]);

        assertCorrectOrientation();

    }

    private void assertCorrectOrientation() {

        if(bottomLeft.lon > topRight.lon) {
            Coords temp = bottomLeft;
            bottomLeft = topRight;
            topRight = temp;
        }

        if(bottomLeft.lat > topRight.lat) {

            // recording wrong corners - so swap to other corners
            double tempLat = bottomLeft.lat;
            bottomLeft = new Coords(topRight.lat, bottomLeft.lon);
            topRight = new Coords(tempLat, topRight.lon);

        }

    }

    public Coords getBottomLeft() {
        return bottomLeft;
    }

    public Coords getTopRight() {
        return topRight;
    }

    public boolean containsPoint(Coords coords) {
        return containsPoint(coords.lat, coords.lon);
    }

    public boolean containsPoint(double lat, double lon) {
        return (bottomLeft.lat <= lat && lat <= topRight.lat) && (bottomLeft.lon <= lon && lon <= topRight.lon);
    }

    public void setBottomLeft(Coords bottomLeft) {
        this.bottomLeft = bottomLeft;
    }

    public void setTopRight(Coords topRight) {
        this.topRight = topRight;
    }
}
