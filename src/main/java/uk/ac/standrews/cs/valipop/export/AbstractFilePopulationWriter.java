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
package uk.ac.standrews.cs.valipop.export;

import uk.ac.standrews.cs.valipop.Config;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;

/**
 * Writes a representation of the population to file in some external format.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 */
public abstract class AbstractFilePopulationWriter implements IPopulationWriter {

    private static final int NUMBER_OF_DIGITS_IN_ID = 8;

    private static final NumberFormat FORMATTER;

    private static final String PERSON_PREFIX = "P";
    private static final String FAMILY_PREFIX = "M";

    protected abstract void outputHeader(PrintWriter writer);

    protected abstract void outputTrailer(PrintWriter writer);

    protected PrintWriter writer;

    static {

        FORMATTER = NumberFormat.getInstance();
        FORMATTER.setMinimumIntegerDigits(NUMBER_OF_DIGITS_IN_ID);
        FORMATTER.setGroupingUsed(false);
    }

    /**
     * Initialises the exporter. This includes potentially expensive scanning of the population graph.
     *
     * @param path the path for the output file
     * @throws IOException if the file does not exist and cannot be created
     */
    public AbstractFilePopulationWriter(final Path path) throws IOException {

        Config.createParentDirectoryIfDoesNotExist(path);

        writer = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8));

        outputHeader(writer);
    }

    @Override
    public void close() {

        outputTrailer(writer);
        writer.close();
    }

    protected static String individualLabel(final int person_id) {

        return PERSON_PREFIX + padId(person_id);
    }

    protected static String familyLabel(final int partnership_id) {

        return FAMILY_PREFIX + padId(partnership_id);
    }

    private static String padId(final int index) {

        return FORMATTER.format(index);
    }
}
