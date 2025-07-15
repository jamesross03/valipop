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

import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation;
import uk.ac.standrews.cs.valipop.simulationEntities.dataStructure.PeopleCollection;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class RecordGenerationFactory {

    public static final Logger log = Logger.getLogger(RecordGenerationFactory.class.getName());

    public static void outputRecords(final RecordFormat recordFormat, final Path recordsOutputDir, final PeopleCollection people, final LocalDate startDate) {

        final Iterable<IPartnership> partnerships = people.getPartnerships();
        final Iterable<IPerson> filteredPeople = filterPeople(people, startDate);
        final Iterable<IPartnership> filteredPartnerships = filterPartnerships(partnerships, startDate);

        Record record = null;

        switch (recordFormat) {
            case DS:
                record = new DsRecord(filteredPeople, filteredPartnerships);
                break;
            case EG_SKYE:
                record = new EgSkyeRecord(filteredPeople, filteredPartnerships);
                break;
            case TD:
                record = new TDRecord(filteredPeople, filteredPartnerships);
                break;
            case VIS_PROCESSING:
                record = new SimplifiedRecord(filteredPeople, filteredPartnerships);
                break;
            case NONE:
                break;
            default:
                break;
        }

        if (record == null) {
            return;
        }

        log.info("OBDModel --- Outputting records");

        try {
            record.exportRecords(recordsOutputDir);
        } catch (final Exception e) {
            log.info("Record generation failed");
            e.printStackTrace();
            log.info(e.getMessage());
        }
    }

    private static List<IPerson> filterPeople(final Iterable<IPerson> people, final LocalDate startDate) {

        final List<IPerson> result = new ArrayList<>();

        for (final IPerson person : people) {
            if (person.getDeathDate() != null && PopulationNavigation.inCountryOnDate(person, person.getDeathDate()) && person.getDeathDate() != null && startDate.isBefore( person.getDeathDate()))
                result.add(person);
        }

        return result;
    }

    private static List<IPartnership> filterPartnerships(final Iterable<IPartnership> partneships, final LocalDate startDate) {

        final List<IPartnership> result = new ArrayList<>();

        for (final IPartnership partnership : partneships) {
            if (partnership.getMarriageDate() != null && PopulationNavigation.inCountryOnDate(partnership.getMalePartner(), partnership.getMarriageDate()) && PopulationNavigation.inCountryOnDate(partnership.getFemalePartner(), partnership.getMarriageDate()) && startDate.isBefore( partnership.getMarriageDate()))
                result.add(partnership);
        }

        return result;
    }
}
