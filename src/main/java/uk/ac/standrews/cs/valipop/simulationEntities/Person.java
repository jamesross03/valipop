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
package uk.ac.standrews.cs.valipop.simulationEntities;

import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;
import uk.ac.standrews.cs.valipop.statistics.populationStatistics.PopulationStatistics;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Address;
import uk.ac.standrews.cs.valipop.utils.addressLookup.Geography;

import java.time.LocalDate;
import java.time.Year;
import java.util.*;

/**
 * Implementation of person in tabular format
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class Person implements IPerson {

    private static int nextId = 0;

    private final int id;
    private final SexOption sex;
    private final LocalDate birthDate;
    private LocalDate deathDate = null;
    private final List<IPartnership> partnerships = new ArrayList<>();
    private IPartnership parents;

    private final String firstName;
    private String surname;
    private final String representation;
    private boolean adulterousBirth;

    private String deathCause = "";

    private final TreeMap<LocalDate, Address> addressHistory = new TreeMap<>();
    private final TreeMap<LocalDate, String> occupationHistory = new TreeMap<>();

    private LocalDate emigrationDate;
    private LocalDate immigrationDate;

    public Person(final SexOption sex, final LocalDate birthDate, final IPartnership parents, final PopulationStatistics statistics, final boolean adulterousBirth) {
        this(sex, birthDate, parents, statistics, adulterousBirth, false);
    }

    public Person(final SexOption sex, final LocalDate birthDate, final IPartnership parents, final PopulationStatistics statistics, final boolean adulterousBirth, final boolean immigrant) {

        id = getNewId();

        this.sex = sex;
        this.birthDate = birthDate;
        this.parents = parents;
        this.adulterousBirth = adulterousBirth;

        firstName = getForename(statistics, immigrant);
        surname = getSurname(statistics, immigrant);

        representation = firstName + " " + surname + " (" + id + ") " + birthDate;

        setOccupation(birthDate, statistics.getOccupation(Year.of(birthDate.getYear()), sex).getDistributionForAge(0).getSample());
    }

    public String toString() {
        return representation;
    }

    public void setSurname(final String surname) {
        this.surname = surname;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public SexOption getSex() {
        return sex;
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
        this.deathDate = deathDate;
    }

    @Override
    public List<IPartnership> getPartnerships() {
        return partnerships;
    }

    @Override
    public IPartnership getParents() {
        return parents;
    }

    @Override
    public void setParents(final IPartnership parents) {
        if (this.parents == null)
            this.parents = parents;
    }

    @Override
    public boolean isAdulterousBirth() {
        return adulterousBirth;
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
    public String getBirthPlace() {
        final Address a =  getAddress(birthDate);
        return a == null ? "" : a.toString();
    }

    @Override
    public String getDeathPlace() {
        final Address a =  getAddress(deathDate);
        return a == null ? "" : a.toString();
    }

    @Override
    public String getOccupation(final LocalDate onDate) {
        return occupationHistory.floorEntry(onDate).getValue();
    }

    @Override
    public void setOccupation(final LocalDate onDate, String occupation) {
        if (occupation.isBlank()) occupation = "";
        occupationHistory.put(onDate, occupation);
    }

    @Override
    public TreeMap<LocalDate, Address> getAddressHistory() {
        return addressHistory;
    }

    @Override
    public void setAdulterousBirth(final boolean adulterousBirth) {
        this.adulterousBirth = adulterousBirth;
    }

    private boolean phantom = false;

    @Override
    public void setPhantom(final boolean isPhantom) {
        this.phantom = isPhantom;
    }

    @Override
    public boolean isPhantom() {
        return phantom;
    }

    @Override
    public String getDeathCause() {
        return deathCause;
    }

    @Override
    public void setDeathCause(final String deathCause) {

        this.deathCause = deathCause;
    }

    @Override
    public int compareTo(final IPerson other) {

        return Integer.compare(id, other.getId());
    }

    @Override
    public boolean equals(final Object other) {

        return other instanceof IPerson && id == ((IPerson) other).getId();
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public void recordPartnership(final IPartnership partnership) {
        partnerships.add(partnership);
    }

    @Override
    public Address getAddress(final LocalDate onDate) {
        if (onDate == null)
            return null;

        final Map.Entry<LocalDate, Address> entry = addressHistory.floorEntry(onDate);
        if (entry != null)
            return entry.getValue();

        return null;
    }

    @Override
    public void setAddress(final LocalDate onDate, final Address address) {

        if (address != null) {
            if (!addressHistory.isEmpty()) { // Pass this bit if no previous address

                boolean removed = false;

                if (getAddress(onDate) != null)
                    removed = getAddress(onDate).removeInhabitant(this);

                // if children get shuttled around before birth then remove old addresses
                if (addressHistory.get(onDate) != null) { // this is different to the above if as it looks for values at the exact key rather than taking the value at the floor of the key!
                    addressHistory.remove(onDate);
                }

                if (!removed) {
                    while (addressHistory.ceilingEntry(onDate) != null) { // if theres a future move - from a forced adulterousBirth move - we scratch that move
                        addressHistory.ceilingEntry(onDate).getValue().removeInhabitant(this);
                        addressHistory.remove(addressHistory.ceilingKey(onDate));
                    }
                }
            }

            address.addInhabitant(this);
            addressHistory.put(onDate, address);
        }
    }

    @Override
    public LocalDate getEmigrationDate() {
        return emigrationDate;
    }

    @Override
    public void setEmigrationDate(final LocalDate leavingDate) {
        emigrationDate = leavingDate;
    }

    @Override
    public LocalDate getImmigrationDate() {
        return immigrationDate;
    }

    @Override
    public void setImmigrationDate(final LocalDate arrivalDate) {
        immigrationDate = arrivalDate;
    }

    @Override
    public LocalDate getLastMoveDate() {
        try {
            return addressHistory.lastKey();
        } catch(final NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public Collection<Address> getAllAddresses() {
        return addressHistory.values();
    }

    @Override
    public void rollbackLastMove(final Geography geography) {

        final Address cancelledAddress = addressHistory.lastEntry().getValue();
        final Set<IPerson> family = getChildrenOfAtAddress(this, cancelledAddress);
        family.add(this);

        // remove from curent abode and remove from address history
        for (final IPerson person : family)
            person.cancelLastMove(geography);

        if (!addressHistory.isEmpty()) {
            // check previous abode
            final Address previousAddress = addressHistory.lastEntry().getValue();

            if (!previousAddress.isCountry() && previousAddress.isInhabited()) {
                // if by family
                if (containsFamily(previousAddress, this)) {
                    // move back in
                    returnFamilyToHouse(family, previousAddress);
                } else {
                    // displace current residents at distance zero
                    previousAddress.displaceInhabitants();
                    returnFamilyToHouse(family, previousAddress);
                }
            } else if (previousAddress.isCountry()) {
                // if cancelling last move results in the 'new last address' being forign country then we need to give the
                // person (who is a  migrant) an address to live in from there emmigration date
                setAddress(immigrationDate, geography.getRandomEmptyAddress());
            } else {
                // move back in
                returnFamilyToHouse(family, previousAddress);
            }
        }
    }

    private void returnFamilyToHouse(final Collection<IPerson> family, final Address previousAddress) {

        LocalDate parentsMoveInDate = null;

        for (final Map.Entry<LocalDate, Address> entry : addressHistory.entrySet()) {
            if (entry.getValue().equals(previousAddress)) {
                parentsMoveInDate = entry.getKey();
                break;
            }
        }

        if (parentsMoveInDate == null)
            throw new Error("Address unexpectedly not found");

        // for each person
        for (final IPerson p : family) {
            // check if place is in history of person (checking in case of child address overwrites followed by cancelations)
            if (!p.getAllAddresses().contains(previousAddress)) {
                // work out move in date
                final LocalDate moveDate = parentsMoveInDate.isBefore(p.getBirthDate()) ? p.getBirthDate() : parentsMoveInDate;

                if (p.getAddressHistory().ceilingEntry(moveDate) != null)
                    throw new Error("Unexpected addresss ordering");

                p.getAddressHistory().put(moveDate, previousAddress);
            }

            // for all add person into house
            previousAddress.addInhabitant(p);
        }
    }

    private static Set<IPerson> getChildrenOfAtAddress(final IPerson parent, final Address address) {

        final HashSet<IPerson> childrenAtAddress = new HashSet<>();

        for (final IPerson person : address.getInhabitants())
            if (PopulationNavigation.childOf(parent, person))
                childrenAtAddress.add(person);

        return childrenAtAddress;
    }

    @Override
    public LocalDate cancelLastMove(final Geography geography) {

        final Map.Entry<LocalDate, Address> lastMove = addressHistory.lastEntry();
        final LocalDate moveDate = lastMove.getKey();

        lastMove.getValue().removeInhabitant(this);
        addressHistory.remove(addressHistory.lastKey());

        return moveDate;
    }

    @Override
    public boolean hasEmigrated() {
        return emigrationDate != null;
    }

    @Override
    public IPartnership getLastPartnership() {
        
        return partnerships.isEmpty() ? null : partnerships.getLast();
    }

    @Override
    public String getLastOccupation() {
        return getOccupation(LocalDate.MAX);
    }

    private static boolean containsFamily(final Address address, final Person person) {

        final Collection<IPerson> family = PopulationNavigation.immediateFamilyOf(person);

        for (final IPerson inhabitant : address.getInhabitants()) {
            if (family.contains(inhabitant))
                return true;
            
        }

        return false;
    }

    private synchronized static int getNewId() {
        return nextId++;
    }

    public synchronized static void resetIds() {
        nextId = 0;
    }

    private String getForename(final PopulationStatistics statistics, final boolean immigrant) {

        if (immigrant) {
            return statistics.getMigrantForenameDistribution(Year.of(birthDate.getYear()), getSex()).getSample();
        } else {
            return statistics.getForenameDistribution(Year.of(birthDate.getYear()), getSex()).getSample();
        }
    }

    private String getSurname(final PopulationStatistics statistics, final boolean immigrant) {

        if (parents != null) {
            return parents.getMalePartner().getSurname();
        }
        else {
            if (immigrant) {
                return statistics.getMigrantSurnameDistribution(Year.of(birthDate.getYear())).getSample();
            } else {
                return statistics.getSurnameDistribution(Year.of(birthDate.getYear())).getSample();
            }
        }
    }
}
