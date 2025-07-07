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

import uk.ac.standrews.cs.valipop.implementations.Randomness;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.PopulationStatistics;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat.BirthSourceRecord;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class EGSkyeBirthSourceRecord extends BirthSourceRecord {

    protected int familyID = -1;
    protected LocalDate birthDate;
    protected LocalDate registrationDate;
    protected String mothersOccupation = "";
    protected String illegitimate = "";
    protected String marriageBaby = "";
    protected String deathID = "";

    public EGSkyeBirthSourceRecord(IPerson person) {

        super(person);

        familyID = parents_partnership_id;
        birthDate = person.getBirthDate();

        if (parents_partnership_id != -1) {
            mothersOccupation = person.getParents().getFemalePartner().getOccupation(birthDate);
            fathers_surname = person.getParents().getMalePartner().getSurname();
        }

        int registrationDay = Randomness.getRandomGenerator().nextInt(43);
        registrationDate = birthDate.plus(registrationDay, ChronoUnit.DAYS);

        illegitimate = person.isAdulterousBirth() ? "illegitimate" : "";

        if (person.getDeathDate() != null) {
            deathID = String.valueOf(uid);
        }
    }

    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();

        append(builder, "", "", uid, "", familyID, parents_partnership_id, "",
                "", "", "", "", "",
                "", "", "", "", "", "", "",
                "", forename, surname, birthDate.toString(), "", "",
                sex, fathers_forename, fathers_surname, "", mothers_forename,
                mothers_maiden_surname, "",
                parents_marriage_date == null ? "" : parents_marriage_date.getDayOfMonth(),
                parents_marriage_date == null ? "" : parents_marriage_date.getMonth(),
                parents_marriage_date == null ? "" : parents_marriage_date.getYear(), parents_place_of_marriage,
                "", "", "",
                "", "", "", registrationDate.getDayOfMonth(),
                registrationDate.getMonth(), registrationDate.getYear(), illegitimate, "SYNTHETIC DATA PRODUCED USING VALIPOP", "", "", "", "", deathID,
                "", "", marriageBaby);

        return builder.toString();
    }

    @Override
    public String getHeaders() {

        final StringBuilder builder = new StringBuilder();

        append(builder, "IOSBIRTH_Identifier", "corrected", "ID", "source", "family", "marriage", "line no",
                "RD Identifier", "IOS_RDIdentifier", "IOS_RSDIdentifier", "register identifier", "IOS_RegisterNumber",
                "IOS_Entry no", "IOS_RegisterYear", "sschild", "sxchild", "ssfather", "sxfather", "ssmother",
                "sxmother", "child's forname(s)", "child's surname", "birth date", "address 1", "address 2",
                "sex", "father's forename", "father's surname", "father's occupation", "mother's forename",
                "mother's maiden surname", "mother's occupation", "day of parents' marriage",
                "month of parents' marriage", "year of parents' marriage", "place of parent's marriage 1",
                "place of parent's marriage 2", "forename of informant", "surname of informant",
                "relationship of informant to child", "did inform sign?", "was inform present?", "day of reg",
                "month of reg", "year of reg", "illegit", "notes1", "notes2", "notes3", "repeats", "edits", "Death",
                "latepid", "latesch", "marriageBaby");

        return builder.toString();
    }
}
