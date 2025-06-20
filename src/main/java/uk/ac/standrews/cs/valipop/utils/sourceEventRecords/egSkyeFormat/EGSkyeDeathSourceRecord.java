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
package uk.ac.standrews.cs.valipop.utils.sourceEventRecords.egSkyeFormat;

import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.PopulationStatistics;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat.DeathSourceRecord;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation.getLastPartnership;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class EGSkyeDeathSourceRecord extends DeathSourceRecord {

    protected LocalDate deathDate;
    protected LocalDate registrationDate;
    protected String mothersOccupation;
    protected String marriageIDs;
    protected String deathAddress;

    public EGSkyeDeathSourceRecord(IPerson person) {

        super(person);

        deathDate = person.getDeathDate();
        deathAddress = person.getAddress(deathDate).toString();

        if (person.getParents() != null) {

            IPerson mother = person.getParents().getFemalePartner();
            mothersOccupation = mother.getOccupation(deathDate);

            IPerson father = person.getParents().getMalePartner();
            if (!PopulationNavigation.aliveOnDate(father, person.getDeathDate())) {
                // father is dead
                setFatherDeceased("D"); // deceased
            }

            fathers_surname = father.getSurname();

            if (!PopulationNavigation.aliveOnDate(mother, person.getDeathDate())) {
                // mother is dead
                setMotherDeceased("D"); // deceased
            }
        }

        int registrationDay = PopulationStatistics.randomGenerator.nextInt(9);
        registrationDate = deathDate.plus(registrationDay, ChronoUnit.DAYS);

        setMaritalStatus(identifyMaritalStatus(person));
        String[] spousesInfo = identifyNameAndOccupationOfSpouses(person);
        setSpousesNames(spousesInfo[0]);
        setSpousesOccupations(spousesInfo[1]);
        marriageIDs = spousesInfo[2];
    }

    public String identifyMaritalStatus(IPerson deceased) {

        List<IPartnership> partnerships = deceased.getPartnerships();

        if (partnerships.size() == 0) {
            if (deceased.getSex() == SexOption.MALE) {
                return "B"; // bachelor
            } else {
                return "S"; // single/spinster
            }
        } else {
            if (getLastPartnership(deceased).getSeparationDate(PopulationStatistics.randomGenerator) == null) {
                // not separated from last partner

                IPerson lastPartner = getLastPartnership(deceased).getPartnerOf(deceased);
                if (PopulationNavigation.aliveOnDate(lastPartner, deceased.getDeathDate())) {

                    // last spouse alive on death date of deceased
                    return partnerships.size() > 1 ? "R" : "M"; // married

                } else {
                    // last spouse dead on death date of deceased
                    return "W"; // widow/er
                }
            } else {
                // separated from last partner
                return "D"; // divorced
            }
        }
    }

    private String[] identifyNameAndOccupationOfSpouses(IPerson deceased) {

        String[] ret = new String[3];

        StringBuilder names = new StringBuilder();
        StringBuilder occupations = new StringBuilder();
        StringBuilder marIDs = new StringBuilder();

        for (IPartnership partnership : deceased.getPartnerships()) {

            IPerson spouse = partnership.getPartnerOf(deceased);

            String spousesName = spouse.getFirstName() + " " + spouse.getSurname();
            String spousesOccupation = spouse.getOccupation(deceased.getDeathDate());

            if (names.length() == 0) {
                names.append(spousesName);
                occupations.append(spousesOccupation);
                marIDs.append(partnership.getId());
            } else {
                names.append("+").append(spousesName);
                occupations.append("+").append(spousesOccupation);
                marIDs.append("+").append(partnership.getId());
            }
        }

        ret[0] = names.toString();
        ret[1] = occupations.toString();
        ret[2] = marIDs.toString();

        return ret;
    }

    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();

        append(builder, "", "", uid, "", "", "",
                "", "", "", "", "",
                "", "", "", "", "", "", "", "",
                "", "", "", forename, surname, getOccupation(),
                getMaritalStatus(), sex, getSpousesNames(), getSpousesOccupations(), deathDate.toString(),
                deathDate.getDayOfMonth(), deathDate.getMonth(), deathDate.getYear(),
                deathAddress, "", getDeathAge(), getDeathAge(), fathers_forename,
                fathers_surname, "", getFatherDeceased(), mothers_forename,
                mothers_maiden_surname, "", getMotherDeceased(), getDeathCauseA(),
                "", "", "", "",
                "", "", "", "",
                registrationDate.getDayOfMonth(), registrationDate.getMonth(), registrationDate.getYear(), "SYNTHETIC DATA PRODUCED USING VALIPOP", "", "", "", "", uid,
                "", "", marriageIDs);

        return builder.toString();
    }

    @Override
    public String getHeaders() {

        final StringBuilder builder = new StringBuilder();

        append(builder, "IOSidentifier", "corrected", "ID", "source", "input", "identifier",
                "IOS_Rdindentifier", "IOS_RSDindentifier", "register identifier", "IOS_Regisdentifier", "entry number",
                "IOS_yearofregistration", "ssdec", "sxdec", "ssfather", "sxfather", "ssmother", "sxmother", "spousesn",
                "spousexn", "infxn", "infsn", "forename(s) of deceased", "surname of deceased", "occupation",
                "marital status", "sex", "name of spouse(s)", "spouse's occ", "death date",
                "day", "month", "year", "address 1", "address 2", "age at death", "agey", "father's forename",
                "father's surname", "father's occupation", "if father deceased", "mother's forename",
                "mother's maiden surname", "mother's occupation", "if mother deceased", "cause of death",
                "length of last illness", "medically certified", "doctor's name", "forename of informant",
                "surname of informant", "relationship of informant to deceased", "did inform sign?", "was inform pres?",
                "day of reg", "month of reg", "year of reg", "notes1", "notes2", "notes3", "repeats", "edits", "Birth",
                "earlypid", "earlysch", "mar");

        return builder.toString();
    }
}
