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
import org.gedcom4j.model.enumerations.IndividualAttributeType;
import org.gedcom4j.model.enumerations.IndividualEventType;
import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Address;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Geography;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
    private String firstName;
    protected String surname;
    protected SexOption sex;
    private LocalDate birthDate;
    private String birthPlace = "";
    private LocalDate deathDate;
    private String deathPlace = "";
    protected String deathCause = "";
    protected String occupation = "";
    private List<Integer> partnershipIds;
    private int parent_id;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public SexOption getSex() {
        return sex;
    }

    @Override
    public String getBirthPlace() {
        return birthPlace;
    }

    @Override
    public String getDeathPlace() {
        return deathPlace;
    }

    @Override
    public String getDeathCause() {
        return deathCause;
    }

    @Override
    public void setDeathCause(final String deathCause) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getOccupation(final LocalDate date) {
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

    GEDCOMPerson(final Individual individual, final GEDCOMPopulationAdapter adapter) {

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

        id = GEDCOMPopulationWriter.idToInt(individual.getXref());
    }

    private void setSex(final Individual individual) {

        if (individual.getSex() != null) {
            String string = individual.getSex().toString();
            sex = string.equals(MALE_STRING) ? SexOption.MALE : SexOption.FEMALE;
        }
    }

    private void setNames(final Individual individual) {

        final List<PersonalName> names = individual.getNames();

        firstName = findFirstNames(names);
        surname = findSurname(names);
    }

    private void setParents(final Individual individual) {

        final List<FamilyChild> families = individual.getFamiliesWhereChild();
        parent_id = families != null && !families.isEmpty() ? GEDCOMPopulationWriter.idToInt(families.getFirst().getFamily().getXref()) : -1;
    }

    private void setPartnerships(final Individual individual) {

        partnershipIds = new ArrayList<>();

        final List<FamilySpouse> familiesWhereSpouse = individual.getFamiliesWhereSpouse();

        if (familiesWhereSpouse != null)
            for (final FamilySpouse family : familiesWhereSpouse)
                partnershipIds.add(GEDCOMPopulationWriter.idToInt(family.getFamily().getXref()));
    }

    private void setEvents(final Individual individual) {

        individual.getEvents(true);
        for (final IndividualEvent event : individual.getEvents()) {

            final LocalDate eventDate = event.getDate() != null ? parseDate(event.getDate().toString()) : null;

            if (event.getType() == IndividualEventType.BIRTH) {

                if (eventDate != null) birthDate = eventDate;
                if (event.getPlace() != null) birthPlace = event.getPlace().getPlaceName();
            }

            if (event.getType() == IndividualEventType.DEATH) {

                if (eventDate != null) deathDate = eventDate;
                if (event.getPlace() != null) deathPlace = event.getPlace().getPlaceName();
                if (event.getCause() != null) deathCause = event.getCause().toString();
            }
        }
    }

    private static LocalDate parseDate(final String date) {

        try {
            return LocalDate.parse(date, GEDCOMPopulationWriter.FORMAT);
        }
        catch (final DateTimeParseException ignore) {

            // Deal with the case where the date string contains only a year.
            try {
                return LocalDate.parse("1 JUL " + date, GEDCOMPopulationWriter.FORMAT);
            }
            catch (final DateTimeParseException ignore2) {
                return null;
            }
        }
    }

    private void setOccupation(final Individual individual) {

        // Necessary to force initialisation of attributes list.
        individual.getAttributes(true);

        final List<IndividualAttribute> occupation_attributes = individual.getAttributesOfType(IndividualAttributeType.OCCUPATION);
        if (!occupation_attributes.isEmpty()) {
            occupation = occupation_attributes.getFirst().getDescription().toString();
        }
    }

    private static String findSurname(final List<PersonalName> names) {

        for (final PersonalName gedcom_name : names) {

            final String name = gedcom_name.toString();

            if (name.contains("/")) { // Slashes denote surname
                final int start = name.indexOf('/');
                final int end = name.lastIndexOf('/');
                if (end > start)
                    return name.substring(start + 1, end);
            }
        }
        return null;
    }

    private static String findFirstNames(final List<PersonalName> names) {

        final StringBuilder builder = new StringBuilder();

        for (final PersonalName gedcom_name : names) {

            if (!builder.isEmpty())
                builder.append(' ');


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
        return birthDate;
    }

    @Override
    public LocalDate getDeathDate() {
        return deathDate;
    }

    @Override
    public void setDeathDate(final LocalDate deathDate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IPartnership> getPartnerships() {

        final List<IPartnership> partnerships = new ArrayList<>();
        for (final int id : partnershipIds) {
            final IPartnership partnership = adapter.findPartnership(id);
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
    public void setParents(final IPartnership parents) {
        parent_id = parents.getId();
    }

    @Override
    public boolean isAdulterousBirth() {
        return false;
    }

    @Override
    public void recordPartnership(final IPartnership partnership) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Address getAddress(final LocalDate onDate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAddress(final LocalDate onDate, final Address address) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalDate getEmigrationDate() {
        return null;
    }

    @Override
    public void setEmigrationDate(final LocalDate leavingDate) {

    }

    @Override
    public LocalDate getImmigrationDate() {
        return null;
    }

    @Override
    public void setImmigrationDate(final LocalDate arrivalDate) {

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
    public void rollbackLastMove(final Geography geography) {

    }

    @Override
    public LocalDate cancelLastMove(final Geography geography) {
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
    public void setOccupation(final LocalDate onDate, final String occupation) {
        this.occupation = occupation;
    }

    @Override
    public TreeMap<LocalDate, Address> getAddressHistory() {
        final TreeMap<LocalDate, Address> tm = new TreeMap<>();
        tm.put(birthDate, getAddress(birthDate));
        return tm;
    }

    @Override
    public void setAdulterousBirth(final boolean adulterousBirth) {

    }

    @Override
    public void setPhantom(final boolean isPhantom) {

    }

    @Override
    public boolean isPhantom() {
        return false;
    }

    @Override
    public int compareTo(final IPerson o) {
        return Integer.compare(id, o.getId());
    }
}
