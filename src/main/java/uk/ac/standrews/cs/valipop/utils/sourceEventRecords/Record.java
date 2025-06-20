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
package uk.ac.standrews.cs.valipop.utils.sourceEventRecords;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import uk.ac.standrews.cs.valipop.Config;
import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat.SourceRecord;

abstract public class Record {

    private static final String BIRTH_RECORDS_PATH = "birth_records.csv";
    private static final String DEATH_RECORDS_PATH = "death_records.csv";
    private static final String MARRIAGE_RECORDS_PATH = "marriage_records.csv";

    // TODO allow output file paths to be configured, add -i option to output to console

    private Iterable<IPerson> people;
    private Iterable<IPartnership> partnerships;

    Record(Iterable<IPerson> people, Iterable<IPartnership> partnerships) {
        this.people = people;
        this.partnerships = partnerships;
    }

    abstract protected Iterable<? extends SourceRecord> toBirthRecords(Iterable<IPerson> people);

    abstract protected Iterable<? extends SourceRecord> toDeathRecords(Iterable<IPerson> people);

    abstract protected Iterable<? extends SourceRecord> toMarriageRecords(Iterable<IPartnership> partnerships);

    public void exportRecords(Path recordDir) throws IOException {

        exportRecord(toBirthRecords(people), recordDir.resolve(BIRTH_RECORDS_PATH));
        exportRecord(toDeathRecords(people), recordDir.resolve(DEATH_RECORDS_PATH));
        exportRecord(toMarriageRecords(partnerships), recordDir.resolve(MARRIAGE_RECORDS_PATH));
    }

    private void exportRecord(final Iterable<? extends SourceRecord> records, Path recordPath) throws IOException {
        // Generate birth records
        Config.createParentDirectoryIfDoesNotExist(recordPath);

        try (final PrintWriter writer = new PrintWriter(Files.newBufferedWriter(recordPath, StandardCharsets.UTF_8))) {

            boolean first = true;

            for (final SourceRecord record : records) {

                if (first) {
                    writer.println(record.getHeaders());
                    first = false;
                }

                writer.println(record);
            }
        }
    }
}
