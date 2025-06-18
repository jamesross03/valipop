package uk.ac.standrews.cs.valipop.utils.sourceEventRecords;

import uk.ac.standrews.cs.utilities.FilteredIterator;
import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class RecordGenerationFactory {

    public static final Logger log = Logger.getLogger(RecordGenerationFactory.class.getName());

    public static void outputRecords(RecordFormat recordFormat, Path recordsOutputDir,  Iterable<IPerson> people, Iterable<IPartnership> partnerships, LocalDate startDate) {
        Iterable<IPerson> filteredPeople = filterPeople(people, startDate);
        Iterable<IPartnership> filteredPartnerships = filterPartnerships(partnerships, startDate);

        Record record = null;

        switch(recordFormat) {
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
        } catch (Exception e) {
            log.info("Record generation failed");
            e.printStackTrace();
            log.info(e.getMessage());
        }
    }

    private static Iterable<IPerson> filterPeople(Iterable<IPerson> people, LocalDate startDate) {
        return () -> {
            Predicate<IPerson> isPresent = person -> person.getDeathDate() != null && PopulationNavigation.presentOnDate(person, person.getDeathDate()) && person.getDeathDate() != null && startDate.isBefore( person.getDeathDate());
            return new FilteredIterator<>(people.iterator(), isPresent);
        };
    }

    private static Iterable<IPartnership> filterPartnerships(Iterable<IPartnership> partneships, LocalDate startDate) {
        return () -> {
            Predicate<IPartnership> isPresent = partnership -> partnership.getMarriageDate() != null && PopulationNavigation.presentOnDate(partnership.getMalePartner(), partnership.getMarriageDate()) && PopulationNavigation.presentOnDate(partnership.getFemalePartner(), partnership.getMarriageDate()) && startDate.isBefore( partnership.getMarriageDate());

            return new FilteredIterator<>(partneships.iterator(), isPresent);
        };
    }
}
