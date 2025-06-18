package uk.ac.standrews.cs.valipop.utils.sourceEventRecords.tdFormat;

import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TableStructure.PersonCharacteristicsIdentifier;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.PopulationStatistics;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat.BirthSourceRecord;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class TDBirthSourceRecord extends BirthSourceRecord {

    protected int familyID = -1;
    protected LocalDate birthDate;
    protected LocalDate registrationDate;
    protected String mothersOccupation = "";
    protected String illegitimate = "";
    protected String deathID = "";

    private String CHILD_IDENTITY = "";
    private String MOTHER_IDENTITY = "";
    private String FATHER_IDENTITY = "";
    private String DEATH_RECORD_IDENTITY = "";
    private String PARENT_MARRIAGE_RECORD_IDENTITY = "";
    private String FATHER_BIRTH_RECORD_IDENTITY = "";
    private String MOTHER_BIRTH_RECORD_IDENTITY = "";
    private String MARRIAGE_RECORD_IDENTITY1 = "";
    private String MARRIAGE_RECORD_IDENTITY2 = "";
    private String MARRIAGE_RECORD_IDENTITY3 = "";
    private String MARRIAGE_RECORD_IDENTITY4 = "";
    private String MARRIAGE_RECORD_IDENTITY5 = "";
    private String MARRIAGE_RECORD_IDENTITY6 = "";
    private String MARRIAGE_RECORD_IDENTITY7 = "";
    private String MARRIAGE_RECORD_IDENTITY8 = "";

    private String IMMIGRATION_GENERATION = "NA";


    public TDBirthSourceRecord(IPerson person) {

        super(person);

        familyID = parents_partnership_id;
        birthDate = person.getBirthDate();

        if (parents_partnership_id != -1) {
            mothersOccupation = person.getParents().getFemalePartner().getOccupation(birthDate);
            fathers_surname = person.getParents().getMalePartner().getSurname();

            IPartnership parents = person.getParents();
            MOTHER_IDENTITY = String.valueOf(parents.getFemalePartner().getId());
            FATHER_IDENTITY = String.valueOf(parents.getMalePartner().getId());
            FATHER_BIRTH_RECORD_IDENTITY = String.valueOf(parents.getMalePartner().getId());
            MOTHER_BIRTH_RECORD_IDENTITY = String.valueOf(parents.getFemalePartner().getId());

            if(parents.getMarriageDate() != null)
                PARENT_MARRIAGE_RECORD_IDENTITY = String.valueOf(parents.getId());

        }

        int registrationDay = PopulationStatistics.randomGenerator.nextInt(43);
        registrationDate = birthDate.plus(registrationDay, ChronoUnit.DAYS);

        illegitimate = person.isAdulterousBirth() || (person.getParents() != null && person.getParents().getMarriageDate() == null) ? "illegitimate" : "";

        if (person.getDeathDate() != null) {
            deathID = String.valueOf(uid);
            DEATH_RECORD_IDENTITY = String.valueOf(uid);
        }

        CHILD_IDENTITY = uid;

        ArrayList<IPartnership> marriages = new ArrayList<>();

        for(IPartnership p : person.getPartnerships()) {
            if(p.getMarriageDate() != null)
                marriages.add(p);
        }

        if(marriages.size() >= 1)
            MARRIAGE_RECORD_IDENTITY1 = String.valueOf(marriages.get(0).getId());

        if(marriages.size() >= 2)
            MARRIAGE_RECORD_IDENTITY2 = String.valueOf(marriages.get(1).getId());

        if(marriages.size() >= 3)
            MARRIAGE_RECORD_IDENTITY3 = String.valueOf(marriages.get(2).getId());

        if(marriages.size() >= 4)
            MARRIAGE_RECORD_IDENTITY4 = String.valueOf(marriages.get(3).getId());

        if(marriages.size() >= 5)
            MARRIAGE_RECORD_IDENTITY5 = String.valueOf(marriages.get(4).getId());

        if(marriages.size() >= 6)
            MARRIAGE_RECORD_IDENTITY6 = String.valueOf(marriages.get(5).getId());

        if(marriages.size() >= 7)
            MARRIAGE_RECORD_IDENTITY7 = String.valueOf(marriages.get(6).getId());

        if(marriages.size() >= 8)
            MARRIAGE_RECORD_IDENTITY8 = String.valueOf(marriages.get(7).getId());


        int immigantGen = PersonCharacteristicsIdentifier.getImmigrantGeneration(person);

        if(immigantGen == -1)
            IMMIGRATION_GENERATION = "NA";
        else
            IMMIGRATION_GENERATION = String.valueOf(immigantGen);




    }

    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();

        append(builder, uid, familyID, PARENT_MARRIAGE_RECORD_IDENTITY,
                forename, surname, birthDate.getDayOfMonth(), birthDate.getMonth(), birthDate.getYear(), birth_address,
                sex, fathers_forename, fathers_surname, fathers_occupation, mothers_forename,
                mothers_maiden_surname, mothersOccupation,
                parents_marriage_date == null ? "" : parents_marriage_date.getDayOfMonth(),
                parents_marriage_date == null ? "" : parents_marriage_date.getMonth(),
                parents_marriage_date == null ? "" : parents_marriage_date.getYear(), parents_place_of_marriage,
                illegitimate, "SYNTHETIC DATA PRODUCED USING VALIPOP", deathID,
                CHILD_IDENTITY, MOTHER_IDENTITY, FATHER_IDENTITY,
                DEATH_RECORD_IDENTITY, PARENT_MARRIAGE_RECORD_IDENTITY,
                FATHER_BIRTH_RECORD_IDENTITY, MOTHER_BIRTH_RECORD_IDENTITY,
                MARRIAGE_RECORD_IDENTITY1, MARRIAGE_RECORD_IDENTITY2, MARRIAGE_RECORD_IDENTITY3,
                MARRIAGE_RECORD_IDENTITY4, MARRIAGE_RECORD_IDENTITY5, MARRIAGE_RECORD_IDENTITY6,
                MARRIAGE_RECORD_IDENTITY7, MARRIAGE_RECORD_IDENTITY8, IMMIGRATION_GENERATION);

        return builder.toString();
    }

    @Override
    public String getHeaders() {

        final StringBuilder builder = new StringBuilder();

        append(builder, "ID", "family", "marriage",
                "child's forname(s)", "child's surname", "birth day", "birth month", "birth year", "address",
                "sex", "father's forename", "father's surname", "father's occupation", "mother's forename",
                "mother's maiden surname", "mother's occupation", "day of parents' marriage",
                "month of parents' marriage", "year of parents' marriage", "place of parent's marriage",
                "illegit", "notes", "Death", "CHILD_IDENTITY", "MOTHER_IDENTITY", "FATHER_IDENTITY",
                "DEATH_RECORD_IDENTITY", "PARENT_MARRIAGE_RECORD_IDENTITY",
                "FATHER_BIRTH_RECORD_IDENTITY", "MOTHER_BIRTH_RECORD_IDENTITY",
                "MARRIAGE_RECORD_IDENTITY1", "MARRIAGE_RECORD_IDENTITY2", "MARRIAGE_RECORD_IDENTITY3",
                "MARRIAGE_RECORD_IDENTITY4", "MARRIAGE_RECORD_IDENTITY5", "MARRIAGE_RECORD_IDENTITY6",
                "MARRIAGE_RECORD_IDENTITY7", "MARRIAGE_RECORD_IDENTITY8", "IMMIGRANT_GENERATION");

        return builder.toString();
    }
}
