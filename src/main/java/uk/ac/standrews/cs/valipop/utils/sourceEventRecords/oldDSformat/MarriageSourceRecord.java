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
import uk.ac.standrews.cs.valipop.simulationEntities.PopulationNavigation;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.PopulationStatistics;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Address;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A representation of a Marriage Record in the form used by the Digitising Scotland Project.
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 * <p/>
 * Fields are as follows:
 * Ref Field
 * 1. Unique 'Record' Identifier
 * 2. Groom Surname
 * 3. Groom Forename
 * 4. Bride Surname
 * 5. Bride Forename
 * 6. Year of Registration
 * 7. Registration District Number
 * 8. Registration District Suffix
 * 9. Entry
 * 10. Marriage Year
 * 11. Changed Groom Surname
 * 12. Changed Groom Forename
 * 13. Changed Bride Surname
 * 14. Changed Bride Forename
 * 15. Groom did not Sign ('X' or empty)
 * 16. Bride did not Sign ('X' or empty)
 * 17. Marriage Day
 * 18. Marriage Month
 * 19. Denomination
 * 20. Groom’s Address
 * 21. Groom Age or Date of BirthFamilyGT
 * 22. Groom’s Occupation
 * 23. Groom Marital Status
 * 24. Bride’s Address
 * 25. Bride Age or Date of BirthFamilyGT
 * 26. Bride’s Occupation
 * 27. Bride marital status
 * 28. Groom Father’s Forename
 * 29. Groom Father’s Surname ('0' if same as Groom Surname)
 * 30. Groom Father Deceased ('Y' or empty)
 * 31. Groom Mother’s Forename
 * 32. Groom Mother’s Maiden Surname
 * 33. Groom mother Deceased ('Y' or empty)
 * 34. Groom Father Occupation
 * 35. Bride Father’s Forename
 * 36. Bride Father’s Surname ('0' if same as Bride Surname)
 * 37. Bride Father Deceased ('Y' or empty)
 * 38. Bride Mother’s Forename
 * 39. Bride Mother’s Maiden Surname
 * 40. Bride Mother Deceased ('Y' or empty)
 * 41. Bride Father Occupation
 * 42. Corrected Entry ('1', '2', '3' or empty)
 * 43. Image Quality ('1', '2' or empty)
 * <p/>
 * <p/>
 * <p/>
 * Examples of marriage records:
 * <p/>
 * 9000001|MCMILLAN|JOHN|MCDONALD|JANET|1855|107|01|15|1855||||||X|20|11|1|MILLHAVEN_OF_URQUHART_CO_INVERNESS|30|TAILOR_(M ASTER)|B|MILLHAVEN_OF_URQUHART_CO_INVERNESS|23|DOMESTIC_SERVANT|S|WILLIAM|0|Y|HELEN|GRANT||TAILOR|JOHN|0|Y|CATH ERINE|CAMERON||FOX_HUNTER|||
 * 9000002|FRASER|DONALD|FRASER|CHRISTINA|1855|107|01|0006|1855|||||||29|11|1|EASTLOCH_OF_INVERNESS|26|MASON_(JOURNEYMAN)| B|DRUMNADROCHIT_OF_URQUHART|25|DOMESTIC_SERVANT|S|ANDREW|0||ELINA|CUMMING|Y|FARMER|ALEXANDER|0|Y|ELIZABETH|C UMMING||CARPENTER|||
 */
public class MarriageSourceRecord extends SourceRecord {

    private LocalDate marriage_date;

    private String denomination;

    private String groom_forename;
    private String groom_forename_changed;
    private String groom_surname;
    private String groom_surname_changed;
    private String groom_did_not_sign;

    private String groom_address;
    private String groom_age_or_date_of_birth;
    private String groom_occupation;
    protected String groom_marital_status;

    private String groom_fathers_forename;
    private String groom_fathers_surname;
    private String groom_father_deceased;

    private String groom_mothers_forename;
    private String groom_mothers_maiden_surname;
    private String groom_mother_deceased;
    private String groom_fathers_occupation;

    private String bride_forename;
    private String bride_forename_changed;
    private String bride_surname;
    private String bride_surname_changed;
    private String bride_did_not_sign;

    private String bride_address;
    private String bride_age_or_date_of_birth;
    private String bride_occupation;
    protected String bride_marital_status;

    private String bride_fathers_forename;
    private String bride_fathers_surname;
    private String bride_father_deceased;

    private String bride_mothers_forename;
    private String bride_mothers_maiden_surname;
    private String bride_mother_deceased;
    private String bride_father_occupation;

    public MarriageSourceRecord(final IPartnership partnership) {

        marriage_date = partnership.getMarriageDate();

        setUid(String.valueOf(partnership.getId()));

        final IPerson bride = partnership.getFemalePartner();
        final IPerson groom = partnership.getMalePartner();

        setGroomForename(groom.getFirstName());
        setGroomSurname(groom.getSurname());
        setGroomOccupation(groom.getOccupation(marriage_date));
        setGroomAgeOrDateOfBirth(String.valueOf(fullYearsBetween(groom.getBirthDate(), marriage_date)));

        final Address groomAddress = groom.getAddress(marriage_date.minusDays(1));
        if (groomAddress != null) setGroomAddress(groomAddress.toString());

        setBrideForename(bride.getFirstName());
        setBrideSurname(bride.getSurname());
        setBrideOccupation(bride.getOccupation(marriage_date));
        setBrideAgeOrDateOfBirth(String.valueOf(fullYearsBetween(bride.getBirthDate(), marriage_date)));

        final Address brideAddress = bride.getAddress(marriage_date.minusDays(1));
        if (brideAddress != null) setBrideAddress(brideAddress.toString());

        final IPartnership groom_parents_partnership = groom.getParents();
        if (groom_parents_partnership != null) {

            final IPerson groom_mother = groom_parents_partnership.getFemalePartner();
            final IPerson groom_father = groom_parents_partnership.getMalePartner();

            setGroomFathersForename(groom_father.getFirstName());
            setGroomFathersSurname(getRecordedParentsSurname(groom_father.getSurname(), groom.getSurname()));
            setGroomFathersOccupation(groom_father.getOccupation(marriage_date));

            setGroomMothersForename(groom_mother.getFirstName());
            setGroomMothersMaidenSurname(getMaidenSurname(groom_mother));
        }

        final IPartnership bride_parents_partnership = bride.getParents();
        if (bride_parents_partnership != null) {

            final IPerson bride_mother = bride_parents_partnership.getFemalePartner();
            final IPerson bride_father = bride_parents_partnership.getMalePartner();

            setBrideFathersForename(bride_father.getFirstName());
            setBrideFathersSurname(getRecordedParentsSurname(bride_father.getSurname(), bride.getSurname()));
            setBrideFatherOccupation(bride_father.getOccupation(marriage_date));

            setBrideMothersForename(bride_mother.getFirstName());
            setBrideMothersMaidenSurname(getMaidenSurname(bride_mother));
        }
    }

    private static List<IPartnership> getPartnershipsBeforeDate(final IPerson person, final LocalDate date) {

        final List<IPartnership> partnershipsBeforeDate = new ArrayList<>();

        for (final IPartnership partnership : person.getPartnerships()) {
            if (partnership.getPartnershipDate().isBefore(date)) {
                partnershipsBeforeDate.add(partnership);
            }
        }

        return partnershipsBeforeDate;
    }

    public static String identifyMaritalStatus(final IPerson spouse, final LocalDate marriageDate) {

        final List<IPartnership> partnerships = getPartnershipsBeforeDate(spouse, marriageDate);

        if (partnerships.isEmpty()) {
            if (spouse.getSex() == SexOption.MALE) {
                return "B"; // bachelor
            } else {
                return "S"; // single/spinster
            }
        } else {

            final IPartnership lastPartnership = partnerships.get(partnerships.size() - 1);
//            final IPartnership lastPartnership = partnerships.getLast();

            if (lastPartnership.getSeparationDate(PopulationStatistics.randomGenerator) == null) {
                // not separated from last partner
                if (PopulationNavigation.aliveOnDate(lastPartnership.getPartnerOf(spouse), marriageDate)) {
                    // last spouse alive on death date of deceased
                    return "M-ERROR?";
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

    public String getGroomForename() {
        return groom_forename;
    }

    public void setGroomForename(final String groom_forename) {
        this.groom_forename = groom_forename;
    }

    public String getGroomSurname() {
        return groom_surname;
    }

    public void setGroomSurname(final String groom_surname) {
        this.groom_surname = groom_surname;
    }

    public String getGroomAddress() {
        return groom_address;
    }

    public void setGroomAddress(final String groomAddress) {
        this.groom_address = groomAddress;
    }

    public String getGroomAgeOrDateOfBirth() {
        return groom_age_or_date_of_birth;
    }

    public void setGroomAgeOrDateOfBirth(final String groom_age_or_date_of_birth) {
        this.groom_age_or_date_of_birth = groom_age_or_date_of_birth;
    }

    public String getGroomOccupation() {
        return groom_occupation;
    }

    public void setGroomOccupation(final String groom_occupation) {
        this.groom_occupation = groom_occupation;
    }

    public String getGroomMaritalStatus() {
        return groom_marital_status;
    }

    public String getGroomFathersForename() {
        return groom_fathers_forename;
    }

    public void setGroomFathersForename(final String groom_fathers_forename) {
        this.groom_fathers_forename = groom_fathers_forename;
    }

    public String getGroomFathersSurname() {
        return groom_fathers_surname;
    }

    public void setGroomFathersSurname(final String groom_fathers_surname) {
        this.groom_fathers_surname = groom_fathers_surname;
    }

    public String getGroomFatherDeceased() {
        return groom_father_deceased;
    }

    public String getGroomMothersForename() {
        return groom_mothers_forename;
    }

    public void setGroomMothersForename(final String groom_mothers_forename) {
        this.groom_mothers_forename = groom_mothers_forename;
    }

    public String getGroomMothersMaidenSurname() {
        return groom_mothers_maiden_surname;
    }

    public void setGroomMothersMaidenSurname(final String groom_mothers_maiden_surname) {
        this.groom_mothers_maiden_surname = groom_mothers_maiden_surname;
    }

    public String getGroomMotherDeceased() {
        return groom_mother_deceased;
    }

    public String getGroomFathersOccupation() {
        return groom_fathers_occupation;
    }

    public void setGroomFathersOccupation(final String groom_fathers_occupation) {
        this.groom_fathers_occupation = groom_fathers_occupation;
    }

    public String getBrideForename() {
        return bride_forename;
    }

    public void setBrideForename(final String bride_forename) {
        this.bride_forename = bride_forename;
    }

    public String getBrideSurname() {
        return bride_surname;
    }

    public void setBrideSurname(final String bride_surname) {
        this.bride_surname = bride_surname;
    }

    public String getBrideAddress() {
        return bride_address;
    }

    public void setBrideAddress(final String brideAddress) {
        this.bride_address = brideAddress;
    }

    public String getBrideAgeOrDateOfBirth() {
        return bride_age_or_date_of_birth;
    }

    public void setBrideAgeOrDateOfBirth(final String bride_age_or_date_of_birth) {
        this.bride_age_or_date_of_birth = bride_age_or_date_of_birth;
    }

    public String getBrideOccupation() {
        return bride_occupation;
    }

    public void setBrideOccupation(final String bride_occupation) {
        this.bride_occupation = bride_occupation;
    }

    public String getBrideMaritalStatus() {
        return bride_marital_status;
    }

    public String getBrideFathersForename() {
        return bride_fathers_forename;
    }

    public void setBrideFathersForename(final String bride_fathers_Forename) {
        this.bride_fathers_forename = bride_fathers_Forename;
    }

    public String getBrideFathersSurname() {
        return bride_fathers_surname;
    }

    public void setBrideFathersSurname(final String bride_fathers_surname) {
        this.bride_fathers_surname = bride_fathers_surname;
    }

    public String getBrideFatherDeceased() {
        return bride_father_deceased;
    }

    public String getBrideMothersForename() {
        return bride_mothers_forename;
    }

    public void setBrideMothersForename(final String bride_mothers_forename) {
        this.bride_mothers_forename = bride_mothers_forename;
    }

    public String getBrideMothersMaidenSurname() {
        return bride_mothers_maiden_surname;
    }

    public void setBrideMothersMaidenSurname(final String bride_mothers_maiden_surname) {
        this.bride_mothers_maiden_surname = bride_mothers_maiden_surname;
    }

    public String getBrideMotherDeceased() {
        return bride_mother_deceased;
    }


    public String getBrideFatherOccupation() {
        return bride_father_occupation;
    }

    public void setBrideFatherOccupation(final String bride_father_occupation) {
        this.bride_father_occupation = bride_father_occupation;
    }

    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();

        append(builder, uid, groom_surname, groom_forename, bride_surname, bride_forename, registration_year,
                registration_district_number, registration_district_suffix, entry, marriage_date.getYear(),
                groom_surname_changed, groom_forename_changed, bride_surname_changed, bride_forename_changed,
                groom_did_not_sign, bride_did_not_sign, marriage_date.getDayOfMonth(), marriage_date.getMonth(), denomination,
                groom_address, groom_age_or_date_of_birth, groom_occupation, groom_marital_status, bride_address,
                bride_age_or_date_of_birth, bride_occupation, bride_marital_status, groom_fathers_forename,
                groom_fathers_surname, groom_father_deceased, groom_mothers_forename, groom_mothers_maiden_surname,
                groom_mother_deceased, groom_fathers_occupation, bride_fathers_forename, bride_fathers_surname,
                bride_father_deceased, bride_mothers_forename, bride_mothers_maiden_surname, bride_mother_deceased,
                bride_father_occupation, entry_corrected, image_quality);

        return builder.toString();
    }

    @Override
    public String getHeaders() {

        final StringBuilder builder = new StringBuilder();

        append(builder, "Unique 'Record' Identifier", "Groom Surname", "Groom Forename", "Bride Surname",
                "Bride Forename", "Year of Registration", "Registration District Number",
                "Registration District Suffix", "Entry", "Marriage Year", "Changed Groom Surname",
                "Changed Groom Forename", "Changed Bride Surname", "Changed Bride Forename", "Groom did not Sign",
                "Bride did not Sign", "Marriage Day", "Marriage Month", "Denomination", "Groom’s Address",
                "Groom Age or Date of BirthFamilyGT", "Groom’s Occupation", "Groom Marital Status", "Bride’s Address",
                "Bride Age or Date of BirthFamilyGT", "Bride’s Occupation", "Bride marital status",
                "Groom Father’s Forename", "Groom Father’s Surname", "Groom Father Deceased", "Groom Mother’s Forename",
                "Groom Mother’s Maiden Surname", "Groom mother Deceased", "Groom Father Occupation",
                "Bride Father’s Forename", "Bride Father’s Surname", "Bride Father Deceased", "Bride Mother’s Forename",
                "Bride Mother’s Maiden Surname", "Bride Mother Deceased", "Bride Father Occupation", "Corrected Entry",
                "Image Quality");

        return builder.toString();
    }
}
