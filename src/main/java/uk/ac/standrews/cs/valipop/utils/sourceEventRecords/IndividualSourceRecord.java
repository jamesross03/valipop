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

import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat.SourceRecord;

/**
 * Created by graham on 13/05/2014.
 */
public abstract class IndividualSourceRecord extends SourceRecord {

    protected String surname;
    protected String surname_changed;
    protected String forename;
    protected String forename_changed;

    protected String sex;

    protected String mothers_id;
    protected String mothers_forename;
    protected String mothers_surname;
    protected String mothers_maiden_surname;
    protected String mothers_maiden_surname_changed;

    protected String fathers_id;
    protected String fathers_forename;
    protected String fathers_surname;
    protected String fathers_occupation;

    protected void setParentAttributes(IPerson person) {

        // Attributes associated with individual's parents.
        IPerson mother = person.getParents().getFemalePartner();
        IPerson father = person.getParents().getMalePartner();

        setMothersId(String.valueOf(mother.getId()));
        setMothersForename(mother.getFirstName());
        setMothersSurname(getRecordedParentsSurname(mother.getSurname(), person.getSurname()));
        setMothersMaidenSurname(getMaidenSurname(mother));

        setFathersId(String.valueOf(father.getId()));
        setFathersForename(father.getFirstName());
        setFathersSurname(getRecordedParentsSurname(father.getSurname(), person.getSurname()));
        setFathersOccupation(father.getOccupation(person.getBirthDate())); // TODO fix this - is also used by death record, date for that should be death date
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(final String surname) {
        this.surname = surname;
    }

    public String getSurnameChanged() {
        return surname_changed;
    }

    public void setSurnameChanged(final String surname_changed) {
        this.surname_changed = surname_changed;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(final String forename) {
        this.forename = forename;
    }

    public String getForenameChanged() {
        return forename_changed;
    }

    public void setForenameChanged(final String forename_changed) {
        this.forename_changed = forename_changed;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(final String sex) {
        this.sex = sex;
    }

    public String getMothersForename() {
        return mothers_forename;
    }

    public void setMothersForename(final String mothers_forename) {
        this.mothers_forename = mothers_forename;
    }

    public String getMothersSurname() {
        return mothers_surname;
    }

    public void setMothersSurname(final String mothers_surname) {
        this.mothers_surname = mothers_surname;
    }

    public String getMothersMaidenSurname() {
        return mothers_maiden_surname;
    }

    public void setMothersMaidenSurname(final String mothers_maiden_surname) {
        this.mothers_maiden_surname = mothers_maiden_surname;
    }

    public String getMothersMaidenSurnameChanged() {
        return mothers_maiden_surname_changed;
    }

    public void setMothersMaidenSurnameChanged(final String mothers_maiden_surname_changed) {
        this.mothers_maiden_surname_changed = mothers_maiden_surname_changed;
    }

    public String getFathersForename() {
        return fathers_forename;
    }

    public void setFathersForename(final String fathers_forename) {
        this.fathers_forename = fathers_forename;
    }

    public String getFathersSurname() {
        return fathers_surname;
    }

    public void setFathersSurname(final String fathers_surname) {
        this.fathers_surname = fathers_surname;
    }

    public String getFathersOccupation() {
        return fathers_occupation;
    }

    public void setFathersOccupation(final String fathers_occupation) {
        this.fathers_occupation = fathers_occupation;
    }

    public void setMothersId(final String id) {
        this.mothers_id = id;
    }

    public String getMothersId() {
        return mothers_id;
    }

    public void setFathersId(final String id) {
        this.fathers_id = id;
    }

    public String getFathersId() {
        return fathers_id;
    }
}
