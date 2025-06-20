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

import java.time.LocalDate;
import java.time.Period;

/**
 * Created by graham on 13/05/2014.
 */
public abstract class SourceRecord {

    public static final String SEPARATOR = ",";

    protected String uid;
    protected String entry;
    protected String entry_corrected;

    protected String registration_year;
    protected String registration_district_number;
    protected String registration_district_suffix;

    protected String image_quality;

    public void setUid(final String uid) {
        this.uid = uid;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(final String entry) {
        this.entry = entry;
    }

    protected String getMaidenSurname(IPerson female) {

        IPartnership parents_partnership = female.getParents();

        if (parents_partnership != null) {

            IPerson femalesFather = parents_partnership.getMalePartner();
            return femalesFather.getSurname();

        } else return null;
    }

    protected String getRecordedParentsSurname(final String parents_surname, final String childs_surname) {

        return parents_surname.equals(childs_surname) ? "0" : parents_surname;
    }

    protected void append(final StringBuilder builder, final Object... fields) {

        for (Object field : fields) {
            append(builder, field != null ? field.toString() : null);
        }
        builder.deleteCharAt(builder.length() - 1);
    }

    protected void append(final StringBuilder builder, final String field) {

        if (field != null) {
            builder.append(field);
        }
        builder.append(SEPARATOR);
    }

    protected int fullYearsBetween(LocalDate d1, LocalDate d2) {

        return Period.between(d1, d2).getYears();

//        return d1.before(d2) ? DateManipulation.differenceInYears(d1, d2) : DateManipulation.differenceInYears(d2, d1);
    }

    public abstract String getHeaders();

//    public static class DateRecord {
//
//        private String day;
//        private String month;
//        private String year;
//
//        public String getDay() {
//            return day;
//        }
//
//        public void setDay(final String day) {
//            this.day = day;
//        }
//
//        public String getMonth() {
//            return month;
//        }
//
//        public void setMonth(final String month) {
//            this.month = month;
//        }
//
//        public String getYear() {
//            return year;
//        }
//
//        public void setYear(final String year) {
//            this.year = year;
//        }
//    }
}
