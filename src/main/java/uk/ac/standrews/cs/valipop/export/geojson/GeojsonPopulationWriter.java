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
package uk.ac.standrews.cs.valipop.export.geojson;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

import uk.ac.standrews.cs.valipop.export.AbstractFilePopulationWriter;
import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Address;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Area;
import uk.ac.standrews.cs.valipop.utils.addressLookup.BoundingBox;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Coords;

/**
 * Writes a representation of the population to file in geojson format.
 *
 * @author Daniel Brathagen (dbrathagen@gmail.com)
 */
public class GeojsonPopulationWriter extends AbstractFilePopulationWriter {

    private boolean first = true;

    public GeojsonPopulationWriter(Path path) throws IOException {
        super(path);
    }

    @Override
    protected void outputHeader(PrintWriter writer) {
        writer.println("{");
        writer.println("\"type\": \"FeatureCollection\",");
        writer.println("\"features\": [");
    }

    @Override
    public void recordPerson(IPerson person) {
        Address address = person.getAddress(person.getBirthDate());
        if (address == null) {
            return;
        }

        Area area = address.getArea();
        if (area == null) {
            return;
        }

        BoundingBox box = area.getBoundingBox();
        if (box == null) {
            return;
        }

        Coords topRight = box.getTopRight();
        Coords bottomLeft = box.getBottomLeft();
        Coords topLeft = new Coords(topRight.getLat(), bottomLeft.getLon());
        Coords bottomRight = new Coords(bottomLeft.getLat(), topRight.getLon());

        String[] coords = new String[] { topLeft.toString(), bottomLeft.toString(), bottomRight.toString(), topRight.toString(), topLeft.toString() };

        // JSON does not allow a trailing comma
        if (first) {
            writer.println("{");
            first = false;
        } else {
            writer.println(",{");
        }

        writer.println("\"type\": \"Feature\",");
        writer.println("\"geometry\": {");
        writer.println("\"type\": \"Polygon\",");
        writer.println("\"coordinates\": [[[" + String.join("],[", coords) + "]]]},");
        writer.println("\"properties\": {");
        recordPersonProprties(person);
        writer.println("}}");
    }

    @Override
    public void recordPartnership(IPartnership partnership) {}

    @Override
    protected void outputTrailer(PrintWriter writer) {
        writer.println("]}");
    }

    private void recordPersonProprties(IPerson person) {
        writer.println("\"name\": \"" + person.getFirstName() + " " + person.getSurname() + "\",");
        writer.println("\"birth_date\": \"" + person.getBirthDate() + "\",");

        if (person.getDeathDate() != null) {
            writer.println("\"death_date\": \"" + person.getDeathDate() + "\",");
        }

        writer.println("\"birth_place\": " + person.getBirthPlace());
    }
}