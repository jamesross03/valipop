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
package uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat;

import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.IndividualSourceRecord;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * A representation of a Death Record in the form used by the Digitising Scotland Project.
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 *         <p/>
 *         Fields are as follows:
 *         <p/>
 *         Ref Field
 *         1. Unique'Record'Identifier 1
 *         2. Surname 1
 *         3. Forename 1
 *         4. Sex 1
 *         5. Year of Registration 1
 *         6. Registration District Number 1
 *         7. Registration District Suffix 1
 *         8. Entry 1
 *         9. Death Year 1
 *         10. Age at Death 1
 *         11. Mother’s Maiden Surname 2
 *         12. Changed Surname 3(10)
 *         13. Changed Forename 3(10)
 *         14. Changed Death Age 3(23) (number followed by 'M', 'W', 'D' or 'H' for months/weeks/dates/hours when Age at Death < 1)
 *         15. Date of BirthFamilyGT (Only for 1966 – 1973, e.g. '17091881' or empty)
 *         16. Occupation
 *         17. Marital Status 3(20) ('B', 'D', 'M', 'S', 'W' or empty)
 *         18. Spouse’s Name(s) 3(22)
 *         19. Spouse’s Occupation(s) 3(22)
 *         20. Death Month
 *         21. Death Day
 *         22. Place of Death
 *         23. Father’s Forename
 *         24. Father’s Surname 3(11) ('0' if same as Surname)
 *         25. Father’s Occupation
 *         26. Father Deceased 3(21) ('Y' or empty)
 *         27. Mother’s Forename
 *         28. Changed Mothers Maiden Surname 3(10)
 *         29. Mother Deceased 3(21) ('Y' or empty)
 *         30. Cause of Death (a)
 *         31. Cause of Death (b) 2
 *         32. Cause of Death (c) 2
 *         33. Certifying Doctor
 *         34. Corrected Entry 3(12) ('1', '2', '3' or empty)
 *         35. Image Quality ('1', '2' or empty)
 *         <p/>
 *         <p/>
 *         Examples of death records:
 *         <p/>
 *         6000002|GARRIOCH|ANN|F|1855|010|1|16|1855|131||||||MIDWIFE|W|ROBERT_SCOTT|STOREMAN|5|18|SCALLOWAY_TINGWALL_ZETLAND|PETER|0|SAILOR|D|GRACE|TULLOCH|D|AGE_&_INFIMITIES||||||
 *         6000003|_ANDERSON|HELEN|F|1855|010|1|17|1855|19||||||DOMESTIC_SERVANT|S||||5|19|BERRY_NEAR_TALLOWAY_TINGWALL_ZETLAND|PETER_ANDERSON|CATTLE_DEALER||PHILIDELPHIA|POTTINGER||PNEUMONIA||||||
 *         6000004|MOUAT|JAMES|M|1855|010|1|18|1855|0||||82D||||||5|25|NORTH_HAMMERSLAND_TINGWALL_ZETLAND|JEREMIAH|0|FARMER||MARY|IRVINE||COSTIVENESS||||||
 *         8000001|WALTERSON|ROBINA|F|1966|010|00|0009|1966|84|||||17091881||W|ROBERT_JAM ES_WALTERSON|CROFTER|9|16|WEST_BURRAFIRTH_BRIDGE_OF_WALLS|JAMES|CH RISTIE|FISHERMAN|Y|AGNES|CHRISTIE|Y|HYPOSTATIC_PNEUMONIA|CORONARY_THR OMBOSIS|SENILITY|J._ROBERTSON_DURHAM|||
 */
public class DeathSourceRecord extends IndividualSourceRecord {

    private static final long FIRST_YEAR_DOB_PRESENT = 1966;
    private static final DateTimeFormatter DOB_DATE_FORMAT =  DateTimeFormatter.ofPattern("ddMMyyyy", Locale.UK);

    private LocalDate death_date;

    private String death_age;
    private String death_cause_a;
    private String death_cause_b;
    private String death_cause_c;

    private String birth_date;
    private String occupation = "";

    private String father_deceased = "";
    private String mother_deceased = "";

    private String marital_status = "";
    private String spouses_names = "";
    private String spouses_occupations = "";

    public DeathSourceRecord(final IPerson person) {

        // Attributes associated with individual
        setUid(String.valueOf(person.getId()));
        setSex(String.valueOf(person.getSex()));
        setForename(person.getFirstName());
        setSurname(person.getSurname());
        setOccupation(person.getOccupation(person.getDeathDate()));
        setDeathCauseA(person.getDeathCause());

        LocalDate birth_date = person.getBirthDate();
        death_date = person.getDeathDate();

        if (death_date != null) {

            int death_year = death_date.getYear();

            setDeathAge(String.valueOf(Period.between(birth_date, death_date).getYears()));

            if (death_year >= DeathSourceRecord.FIRST_YEAR_DOB_PRESENT) {
                setBirthDate(birth_date.format( DOB_DATE_FORMAT));
            }
        }

        IPartnership parents_partnership = person.getParents();
        if (parents_partnership != null) {

            setParentAttributes(person);
        }
    }

    protected String getDeathAge() {
        return death_age;
    }

    private void setDeathAge(final String death_age) {
        this.death_age = death_age;
    }

    protected String getDeathCauseA() {
        return death_cause_a;
    }

    protected String getDeathCauseB() {
        return death_cause_b;
    }

    protected String getDeathCauseC() {
        return death_cause_c;
    }

    protected void setDeathCauseA(final String death_cause_a) {
        this.death_cause_a = death_cause_a;
    }

    protected void setDeathCauseB(final String death_cause_b) {
        this.death_cause_b = death_cause_b;
    }

    protected void setDeathCauseC(final String death_cause_c) {
        this.death_cause_c = death_cause_c;
    }

    public String getBirthDate() {
        return birth_date;
    }

    private void setBirthDate(final String birth_date) {
        this.birth_date = birth_date;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(final String occupation) {
        this.occupation = occupation;
    }

    protected String getFatherDeceased() {
        return father_deceased;
    }

    protected void setFatherDeceased(final String father_deceased) {
        this.father_deceased = father_deceased;
    }

    protected String getMotherDeceased() {
        return mother_deceased;
    }

    protected void setMotherDeceased(final String mother_deceased) {
        this.mother_deceased = mother_deceased;
    }

    protected String getMaritalStatus() {
        return marital_status;
    }

    protected void setMaritalStatus(final String marital_status) {
        this.marital_status = marital_status;
    }

    protected String getSpousesNames() {
        return spouses_names;
    }

    protected void setSpousesNames(final String spouses_names) {
        this.spouses_names = spouses_names;
    }

    protected String getSpousesOccupations() {
        return spouses_occupations;
    }

    protected void setSpousesOccupations(final String spouses_occupations) {

        this.spouses_occupations = spouses_occupations;
    }

    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();

        append(builder, uid, surname, forename, sex, registration_year, registration_district_number,
                registration_district_suffix, entry, death_date.getYear(), death_age, mothers_maiden_surname,
                surname_changed, forename_changed, "", birth_date, occupation, marital_status,
                spouses_names, spouses_occupations, death_date.getMonth(), death_date.getDayOfMonth(),
                "", fathers_forename, fathers_surname, fathers_occupation, father_deceased, mothers_forename,
                mothers_surname, mothers_maiden_surname_changed, mother_deceased,
                death_cause_a, "", "", "", entry_corrected, image_quality);

        return builder.toString();
    }

    @Override
    public String getHeaders() {

        final StringBuilder builder = new StringBuilder();

        append(builder, "Unique Record Identifier", "Surname", "Forename", "Sex", "Year of Registration",
                "Registration District Number", "Registration District Suffix", "Entry", "Death Year", "Age at Death",
                "Mother’s Maiden Surname", "Changed Surname", "Changed Forename", "Changed Death Age",
                "Date of BirthFamilyGT", "Occupation", "Marital Status", "Spouse’s Nam3", "Spouse’s Occupation",
                "Death Month", "Death Day", "Place of Death", "Father’s Forename", "Father’s Surname",
                "Father’s Occupation", "Father Deceased", "Mother’s Forename", "Changed Mothers Maiden Surname",
                "Mother Deceased", "Cause of Death", "Cause of Death", "Cause of Death", "Certifying Doctor",
                "Corrected Entry", "Image Quality");

        return builder.toString();
    }
}
