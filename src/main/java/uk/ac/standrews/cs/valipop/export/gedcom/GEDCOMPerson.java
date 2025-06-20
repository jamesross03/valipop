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
package uk.ac.standrews.cs.valipop.export.gedcom;

import org.gedcom4j.model.*;
import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.simulationEntities.Surname;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Address;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Geography;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

/**
 * Person implementation for a population represented in a GEDCOM file.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk>
 */
public class GEDCOMPerson implements IPerson {

    private final GEDCOMPopulationAdapter adapter;
    protected int id;
    private String first_name;
    protected Surname surname;
    protected SexOption sex;
    private LocalDate birth_date;
    private String birth_place = "";
    private LocalDate death_date;
    private String death_place = "";
    protected String death_cause = "";
    protected String occupation = "";
    private List<Integer> partnership_ids;
    private int parent_id;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getFirstName() {
        return first_name;
    }

    @Override
    public String getSurname() {
        return surname.getName();
    }

    @Override
    public SexOption getSex() {
        return sex;
    }

    @Override
    public String getBirthPlace() {
        return birth_place;
    }

    @Override
    public String getDeathPlace() {
        return death_place;
    }

    @Override
    public String getDeathCause() {
        return death_cause;
    }

    @Override
    public void setDeathCause(final String deathCause) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getOccupation(LocalDate date) {
        return occupation;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Override
    public boolean equals(final Object other) {
        return other instanceof IPerson && ((IPerson) other).getId() == id;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "GEDCOM person";
    }

    private static final String MALE_STRING = SexOption.MALE.toString();

    GEDCOMPerson(final Individual individual, GEDCOMPopulationAdapter adapter) {

        this.adapter = adapter;

        setId(individual);
        setSex(individual);
        setNames(individual);
        setParents(individual);
        setEvents(individual);
        setOccupation(individual);
        setPartnerships(individual);
    }

    private void setId(final Individual individual) {

        id = GEDCOMPopulationWriter.idToInt(individual.xref);
    }

    private void setSex(final Individual individual) {

        sex = individual.sex.toString().equals(MALE_STRING) ? SexOption.MALE : SexOption.FEMALE;
    }

    private void setNames(final Individual individual) {

        final List<PersonalName> names = individual.names;

        first_name = findFirstNames(names);
        surname = findSurname(names);
    }

    private void setParents(final Individual individual) {

        final List<FamilyChild> families = individual.familiesWhereChild;
        parent_id = !families.isEmpty() ? GEDCOMPopulationWriter.idToInt(families.get(0).family.xref) : -1;
    }

    private void setPartnerships(final Individual individual) {

        partnership_ids = new ArrayList<>();

        List<FamilySpouse> familiesWhereSpouse = individual.familiesWhereSpouse;

        for (FamilySpouse family : familiesWhereSpouse) {
            partnership_ids.add(GEDCOMPopulationWriter.idToInt(family.family.xref));
        }
    }

    private void setEvents(final Individual individual) {

        for (final IndividualEvent event : individual.events) {

            switch (event.type) {

                case BIRTH:
                    birth_date = LocalDate.parse(event.date.toString(), GEDCOMPopulationWriter.FORMAT);
                    if (event.place != null) {
                        birth_place = event.place.placeName;
                    }
                    break;

                case DEATH:
                    death_date = LocalDate.parse(event.date.toString(), GEDCOMPopulationWriter.FORMAT);
                    if (event.place != null) {
                        death_place = event.place.placeName;
                    }
                    if (event.cause != null) {
                        death_cause = event.cause.toString();
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private void setOccupation(final Individual individual) {

        final List<IndividualAttribute> occupation_attributes = individual.getAttributesOfType(IndividualAttributeType.OCCUPATION);
        if (!occupation_attributes.isEmpty()) {
            occupation = occupation_attributes.get(0).description.toString();
        }
    }

    private static Surname findSurname(final List<PersonalName> names) {

        for (final PersonalName gedcom_name : names) {

            final String name = gedcom_name.toString();
            if (name.contains("/")) { // Slashes denote surname
                final int start = name.indexOf('/');
                final int end = name.lastIndexOf('/');
                if (end > start) {
                    return new Surname(name.substring(start + 1, end));
                }
            }
        }
        return null;
    }

    private static String findFirstNames(final List<PersonalName> names) {

        final StringBuilder builder = new StringBuilder();

        for (final PersonalName gedcom_name : names) {

            if (builder.length() > 0) {
                builder.append(' ');
            }

            String name = gedcom_name.toString();
            if (name.contains("/")) { // Slashes denote surname
                final int start = name.indexOf('/');
                final int end = name.lastIndexOf('/');
                if (end > start) {
                    name = name.substring(0, start).trim() + name.substring(end + 1).trim();
                }
            }
            builder.append(name);
        }
        return builder.toString();
    }

    @Override
    public LocalDate getBirthDate() {
        return birth_date;
    }

    @Override
    public LocalDate getDeathDate() {
        return death_date;
    }

    @Override
    public void setDeathDate(final LocalDate deathDate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IPartnership> getPartnerships() {

        List<IPartnership> partnerships = new ArrayList<>();
        for (int id : partnership_ids) {
            IPartnership partnership = adapter.findPartnership(id);
            if (partnership != null)
                partnerships.add(partnership);
        }
        return partnerships;
    }

    @Override
    public IPartnership getParents() {
        return parent_id == -1 ? null : adapter.findPartnership(parent_id);
    }

    @Override
    public void setParents(IPartnership parents) {
        parent_id = parents.getId();
    }

    @Override
    public boolean isAdulterousBirth() {
        return false;
    }

    @Override
    public void recordPartnership(IPartnership partnership) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Address getAddress(LocalDate onDate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAddress(LocalDate onDate, Address address) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalDate getEmigrationDate() {
        return null;
    }

    @Override
    public void setEmigrationDate(LocalDate leavingDate) {

    }

    @Override
    public LocalDate getImmigrationDate() {
        return null;
    }

    @Override
    public void setImmigrationDate(LocalDate arrivalDate) {

    }

    @Override
    public LocalDate getLastMoveDate() {
        return null;
    }

    @Override
    public Collection<Address> getAllAddresses() {
        return null;
    }

    @Override
    public void rollbackLastMove(Geography geography) {

    }

    @Override
    public LocalDate cancelLastMove(Geography geography) {
        return null;
    }

    @Override
    public boolean hasEmigrated() {
        return false;
    }

    @Override
    public IPartnership getLastPartnership() {
        return null;
    }

    @Override
    public String getLastOccupation() {
        return occupation;
    }

    @Override
    public void setOccupation(LocalDate onDate, String occupation) {
        this.occupation = occupation;
    }

    @Override
    public TreeMap<LocalDate, Address> getAddressHistory() {
        TreeMap<LocalDate, Address> tm = new TreeMap<>();
        tm.put(birth_date, getAddress(birth_date));
        return tm;
    }

    @Override
    public void setAdulterousBirth(boolean adulterousBirth) {

    }

    @Override
    public void setPhantom(boolean isPhantom) {

    }

    @Override
    public boolean isPhantom() {
        return false;
    }

    @Override
    public int compareTo(IPerson o) {
        return Integer.compare(id, o.getId());
    }
}
