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
package uk.ac.standrews.cs.valipop.simulationEntities;

import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Address;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Geography;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

/**
 * Interface for person objects.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public interface IPerson extends Comparable<IPerson> {

    /**
     * Gets the person's unique identifier. It can be assumed that identifiers are allocated in temporal
     * order, so an older person's identifier is always less than that of a younger person.
     * @return the person's unique identifier
     */
    int getId();

    /**
     * Gets the person's first name.
     * @return the person's first name
     */
    String getFirstName();

    /**
     * Gets the person's surname (family name).
     * @return the person's surname
     */
    String getSurname();

    /**
     * Gets the person's sex.
     * @return the person's sex
     */
    SexOption getSex();

    /**
     * Gets the person's date of birth.
     *
     * @return the person's date of birth
     */
    LocalDate getBirthDate();

    /**
     * Gets the person's place of birth, or null if not recorded.
     * @return the person's place of birth
     */
    String getBirthPlace();

    /**
     * Gets the person's date of death, or null if they are living.
     *
     * @return the person's date of death
     */
    LocalDate getDeathDate();

    void setDeathDate(LocalDate deathDate);

    /**
     * Gets the person's place of death, or null if not recorded.
     * @return the person's place of death
     */
    String getDeathPlace();

    /**
     * Gets the person's occupation, or null if not recorded.
     * @return the person's occupation
     */
    String getOccupation(LocalDate onDate);

    /**
     * Gets the cause of the person's death, or null if not recorded.
     * @return the cause of the person's death
     */
    String getDeathCause();

    void setDeathCause(String deathCause);

    /**
     * Gets the person's partnerships.
     *
     * @return the identifiers of the person's partnerships
     */
    List<IPartnership> getPartnerships();

    /**
     * Gets the person's parents' partnership, or null if none are recorded.
     *
     * @return the person's parents' partnership
     */
    IPartnership getParents();

    void setParents(IPartnership parents);

    boolean isAdulterousBirth();

    void recordPartnership(IPartnership partnership);

    Address getAddress(LocalDate onDate);

    void setAddress(LocalDate onDate, Address address);

    LocalDate getEmigrationDate();

    void setEmigrationDate(LocalDate leavingDate);

    LocalDate getImmigrationDate();

    void setImmigrationDate(LocalDate arrivalDate);

    LocalDate getLastMoveDate();

    Collection<Address> getAllAddresses();

    void rollbackLastMove(Geography geography);

    LocalDate cancelLastMove(Geography geography);

    boolean hasEmigrated();

    IPartnership getLastPartnership();

    String getLastOccupation();

    void setOccupation(LocalDate onDate, String occupation);

    TreeMap<LocalDate, Address> getAddressHistory();

    void setAdulterousBirth(boolean adulterousBirth);

    void setPhantom(boolean isPhantom);

    boolean isPhantom();
}
