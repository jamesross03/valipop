/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module population_model.
 *
 * population_model is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population_model is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population_model. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.valipop.utils.sourceEventRecords.processingVisualiserFormat;

import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.IndividualSourceRecord;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * A representation of a Death Record in the form used by the Digitising Scotland Project.
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class SimplifiedDeathSourceRecord extends IndividualSourceRecord {

    private static final int FIRST_YEAR_DOB_PRESENT = 1966;
    private static final DateTimeFormatter DOB_DATE_FORMAT =  DateTimeFormatter.ofPattern("ddMMyyyy", Locale.UK);

    private LocalDate death_date;

    private String death_place;
    private String death_cause_a;

    private String birth_date;
    private String occupation;

    private String spouses_names;
    private String spouses_id = "";

    public SimplifiedDeathSourceRecord(final IPerson person) {

        // Attributes associated with individual
        setUid(String.valueOf(person.getId()));
        setSex(String.valueOf(person.getSex()));
        setForename(person.getFirstName());
        setSurname(person.getSurname());
        setDeathDate(person.getDeathDate());
        setDeathCauseA(person.getDeathCause());

        if (death_date != null) {
            setOccupation(person.getOccupation(death_date));
        }

        List<IPartnership> partnerships = person.getPartnerships();

        if (partnerships.size() != 0) {
            IPerson spouse = partnerships.get(partnerships.size() - 1).getPartnerOf(person);
            setSpousesNames(spouse.getFirstName() + " " + spouse.getSurname());
            setSpousesId(String.valueOf(spouse.getId()));
        }

        LocalDate birth_date = person.getBirthDate();

        if (death_date != null && !death_date.isBefore(LocalDate.of( FIRST_YEAR_DOB_PRESENT,1,1))) {
            setBirthDate(birth_date.format( DOB_DATE_FORMAT));
        }

        final IPartnership parents_partnership = person.getParents();
        if (parents_partnership != null) {

            setParentAttributes(person);
        }
    }

    private void setDeathCauseA(final String death_cause_a) {
        this.death_cause_a = death_cause_a;
    }

    public String getBirthDate() {
        return birth_date;
    }

    private void setBirthDate(final String birth_date) {
        this.birth_date = birth_date;
    }

    public LocalDate getDeathDate() {
        return death_date;
    }

    private void setDeathDate(final LocalDate death_date) {
        this.death_date = death_date;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(final String occupation) {
        this.occupation = occupation;
    }

    private void setSpousesId(String id) {
        this.spouses_id = id;
    }

    private void setSpousesNames(final String spouses_names) {
        this.spouses_names = spouses_names;
    }

    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();

        int rnd;

        if (fathers_id != null) {
            rnd = new Random().nextInt(101);
            RelationshipsTable.relationshipsFather.add(new String[]{"Father", String.valueOf(uid), String.valueOf(fathers_id), String.valueOf(rnd), death_date.getDayOfMonth() + "." + death_date.getMonth() + "." + death_date.getYear()});
        }

        if (mothers_id != null) {
            rnd = new Random().nextInt(101);
            RelationshipsTable.relationshipsMother.add(new String[]{"Mother", String.valueOf(uid), String.valueOf(mothers_id), String.valueOf(rnd), death_date.getDayOfMonth() + "." + death_date.getMonth() + "." + death_date.getYear()});
        }

        if (fathers_id != null && mothers_id != null) {
            rnd = new Random().nextInt(101);
            RelationshipsTable.relationshipsMarriage.add(new String[]{"Marriage", String.valueOf(fathers_id), String.valueOf(mothers_id), String.valueOf(rnd), "-"});
        }

        if (!Objects.equals(spouses_id, "")) {
            rnd = new Random().nextInt(101);
            RelationshipsTable.relationshipsMarriage.add(new String[]{"Marriage", String.valueOf(uid), String.valueOf(spouses_id), String.valueOf(rnd), "-"});
        }

        append(builder, uid, forename + " " + surname, sex, fathers_id, fathers_forename + " " + fathers_surname,
                mothers_id, mothers_forename + " " + mothers_surname,
                spouses_id, spouses_names,
                death_date == null ? "" : death_date.getDayOfMonth() + "." + death_date.getMonth() + "." + death_date.getYear(),
                death_place, registration_district_suffix, death_cause_a);

        return builder.toString();
    }

    @Override
    public String getHeaders() {

        final StringBuilder builder = new StringBuilder();

        append(builder, "uid", "full_name", "sex", "fathers_id", "fathers_name", "mothers_id", "mothers_name",
                "spouses_id", "spouses_names", "death_date", "death_place", "registration_district_suffix", "death_cause_a");

        return builder.toString();
    }
}
