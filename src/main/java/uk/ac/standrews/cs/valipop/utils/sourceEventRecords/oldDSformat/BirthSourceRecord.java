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
package uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat;

import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.IndividualSourceRecord;

import java.time.LocalDate;

/**
 * A representation of a BirthFamilyGT Record in the form used by the Digitising Scotland Project.
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 * <p/>
 * Fields are as follows:
 * <p/>
 * Ref Field
 * 1. Unique'Record'Identifier'
 * 2. Surname
 * 3. Forename
 * 4. Sex
 * 5. Year of Registration
 * 6. Registration District Number
 * 7. Registration District Suffix
 * 8. Entry
 * 9. BirthFamilyGT Year
 * 10. Mother’s Maiden Surname
 * 11. Changed Surname
 * 12. Changed Forename
 * 13. BirthFamilyGT Day
 * 14. BirthFamilyGT Month
 * 15. BirthFamilyGT Address
 * 16. Father’s Forename
 * 17. Father’s Surname ('0' if same as Surname)
 * 18. Father’s Occupation
 * 19. Mother’s Forename
 * 20. Mother’s Surname ('0' if same as Surname)
 * 21. Changed Mothers Maiden Surname
 * 22. Parents Day of Marriage
 * 23. Parents Month of Marriage
 * 24. Parents Year of Marriage
 * 25. Parents Place of Marriage
 * 26. Illegitimate indicator ('Y' or empty)
 * 27. Informant ('M', 'F' or empty)
 * 28. Informant did not Sign ('X' or empty)
 * 29. Corrected Entry ('1', '2', '3' or empty)
 * 30. Adoption ('A' or empty)
 * 31. Image Quality ('1', '2' or empty)
 * <p/>
 * <p/>
 * Examples of birth records:
 * <p/>
 * 1000001|HAY|HERCULES|M|1855|009|00|041||SKLATER|||21|7|SILWICK|WALTER|0|FISHERMAN|INGA|0|||1|1840|SELIVOE||F|||||
 * 1000002|JAMESON|JAMINA|F|1855|009|00|042|||||26|7|HOGANESS|ROBERT|0|FISH_CURER|ANN|0|SKLATER||11|1841|SELIVOE||F|||||
 * 1000003|IRVINE|CATHERINE|F|1855|009|00|043|||||20|7|TULKY|JOHN|0|FISHERMAN_&_CROFTER|MARGARET|0|JOHNSON||12|1841|SELIVOE| |F|||||
 * 1000004|HAWICK|CATHERINE|F|1855|009|00|044|||||25|7|AITH|SCOTT|0|SEAMAN|44|MARY|0|YELL||12|1841|SELIVOE||M|X||||
 * 1000005|GEORGESON|PETER|M|1855|009|00|045||ISBESTER|||17|5|SAND|GEORGE|0|SEAMAN|MARGARET|0|||11|1838|WATNESS||M|X||||
 */
public class BirthSourceRecord extends IndividualSourceRecord {

    protected LocalDate birth_date;
    protected String birth_address;

    protected LocalDate parents_marriage_date = LocalDate.of(1, 1, 1);
    protected String parents_place_of_marriage;

    protected String illegitimate_indicator;
    protected String informant;
    protected String informant_did_not_sign;
    protected String adoption;

    protected int parents_partnership_id = -1;

    public BirthSourceRecord(final IPerson person) {

        // Attributes associated with individual
        setUid(String.valueOf(person.getId()));
        setSex(String.valueOf(person.getSex()));
        setForename(person.getFirstName());
        setSurname(person.getSurname());

        birth_date = person.getBirthDate();

        final IPartnership parents_partnership = person.getParents();

        if (parents_partnership != null) {

            parents_partnership_id = parents_partnership.getId();

            // Attributes associated with individual's parents' marriage.
            parents_marriage_date = parents_partnership.getMarriageDate();

            // added into to allow for the record generator to work with the
            // organic population model which uses the partnership class with
            // no marriage date to represent a cohabitation and thus no
            // record should be generated.
            if (parents_marriage_date != null) {

                setParentsPlaceOfMarriage(parents_partnership.getMarriagePlace());

                setBirthAddress(person.getAddress(person.getBirthDate()).toString());
            }

            setParentAttributes(person);
        }
    }

    public void setBirthAddress(final String birth_address) {
        this.birth_address = birth_address;
    }

    public void setParentsPlaceOfMarriage(final String parents_place_of_marriage) {
        this.parents_place_of_marriage = parents_place_of_marriage;
    }

    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();

        append(builder, uid, surname, forename, sex, registration_year, registration_district_number,
                registration_district_suffix, entry, birth_date.getYear(), mothers_maiden_surname, surname_changed,
                forename_changed, birth_date.getDayOfMonth(), birth_date.getMonth(), birth_address, fathers_forename,
                fathers_surname, fathers_occupation, mothers_forename, mothers_surname, mothers_maiden_surname_changed,
                parents_marriage_date == null ? "" : parents_marriage_date.getDayOfMonth(),
                parents_marriage_date == null ? "" : parents_marriage_date.getMonth(),
                parents_marriage_date == null ? "" : parents_marriage_date.getYear(),
                parents_place_of_marriage, illegitimate_indicator, informant, informant_did_not_sign, entry_corrected,
                adoption, image_quality);

        return builder.toString();
    }

    @Override
    public String getHeaders() {

        final StringBuilder builder = new StringBuilder();

        append(builder, "Unique Record Identifier", "Surname", "Forename", "Sex", "Year of Registration",
                "Registration District Number", "Registration District Suffix", "Entry", "BirthFamilyGT Year",
                "Mother’s Maiden Surname", "Changed Surname", "Changed Forename", "BirthFamilyGT Day",
                "BirthFamilyGT Month", "BirthFamilyGT Address", "Father’s Forename", "Father’s Surname",
                "Father’s Occupation", "Mother’s Forename", "Mother’s Surname", "Changed Mothers Maiden Surname",
                "Parents Day of Marriage", "Parents Month of Marriage", "Parents Year of Marriage",
                "Parents Place of Marriage", "Illegitimate indicator", "Informant", "Informant did not Sign",
                "Corrected Entry", "Adoption", "Image Quality");

        return builder.toString();
    }
}
