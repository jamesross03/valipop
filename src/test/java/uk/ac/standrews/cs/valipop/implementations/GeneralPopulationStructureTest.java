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
package uk.ac.standrews.cs.valipop.implementations;

import org.junit.Test;
import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.simulationEntities.IPersonCollection;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests of properties of abstract population interface that should hold for all populations.
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public abstract class GeneralPopulationStructureTest {

    private static final int PEOPLE_ITERATION_SAMPLE_THRESHOLD = 40;
    private static final int PEOPLE_ITERATION_SAMPLE_START = 30;
    private static final int PARTNERSHIP_ITERATION_SAMPLE_THRESHOLD = 20;
    private static final int PARTNERSHIP_ITERATION_SAMPLE_START = 10;
    private static final int MAX_REASONABLE_FAMILY_SIZE = 20;
    private static final int POPULATION_SIZE_LIMIT_FOR_EXPENSIVE_TESTS = 10000;

    private final IPersonCollection population;
    private final int initialSize;

    GeneralPopulationStructureTest(final IPersonCollection population, int initialSize) {

        this.population = population;
        this.initialSize = initialSize;
    }

    @Test
    public void nonExistentPersonIsntFound() {

        assertNull(population.findPerson(-1));
    }

    @Test
    public void nonExistentPartnershipIsntFound() {

        assertNull(population.findPartnership(-1));
    }

    @Test
    public void numberOfPeopleIsConsistentAndIDsArentRepeated() {

        final Set<Integer> ids = new HashSet<>();

        for (final IPerson person : population.getPeople()) {
            assertFalse(ids.contains(person.getId()));
            ids.add(person.getId());
        }

        assertEquals(population.getNumberOfPeople(), ids.size());
    }

    @Test
    public void numberOfPartnershipsIsConsistent() {

        final Set<Integer> partnerships = new HashSet<>();
        for (final IPartnership partnership : population.getPartnerships()) {
            assertFalse(partnerships.contains(partnership.getId()));
            partnerships.add(partnership.getId());
        }
        assertEquals(population.getNumberOfPartnerships(), partnerships.size());
    }

    @Test(expected = NoSuchElementException.class)
    public void tooManyPersonIterations() {

        doTooManyIterations(population.getPeople().iterator(), population.getNumberOfPeople());
    }

    @Test(expected = NoSuchElementException.class)
    public void tooManyPartnershipIterations() {

        doTooManyIterations(population.getPartnerships().iterator(), population.getNumberOfPartnerships());
    }

    @Test
    public void peopleRetrievedConsistently() {

        // Check consistency after iteration, if the population is large enough to take a sample from the middle.

        if (population.getNumberOfPeople() > PEOPLE_ITERATION_SAMPLE_THRESHOLD) {
            final Iterator<IPerson> person_iterator = population.getPeople().iterator();

            for (int i = 0; i < PEOPLE_ITERATION_SAMPLE_START; i++) {
                person_iterator.next();
            }

            final IPerson[] sample = new IPerson[]{person_iterator.next(), person_iterator.next(), person_iterator.next(), person_iterator.next(), person_iterator.next()};
            assertRetrievedConsistently(sample);
        }

        if (testingSmallPopulation()) {
            // Check consistency during iteration.

            for (final IPerson person : population.getPeople()) {
                assertRetrievedConsistently(person);
            }
        }
    }

    @Test
    public void partnershipsRetrievedConsistently() {

        // Check consistency after iteration, if the population is large enough to take a sample from the middle.

        if (population.getNumberOfPartnerships() > PARTNERSHIP_ITERATION_SAMPLE_THRESHOLD) {

            final Iterator<IPartnership> partnership_iterator = population.getPartnerships().iterator();

            // Check consistency after iteration.
            for (int i = 0; i < PARTNERSHIP_ITERATION_SAMPLE_START; i++) {
                partnership_iterator.next();
            }

            final IPartnership[] sample = new IPartnership[]{partnership_iterator.next(), partnership_iterator.next(), partnership_iterator.next(), partnership_iterator.next(), partnership_iterator.next()};
            assertRetrievedConsistently(sample);
        }

        // Check consistency during iteration.

        for (final IPartnership partnership : population.getPartnerships()) {
            assertRetrievedConsistently(partnership);
        }
    }

    @Test
    public void familiesNotTooLarge() {

        for (final IPartnership partnership : population.getPartnerships()) {
            assertTrue(partnership.getChildren().size() <= MAX_REASONABLE_FAMILY_SIZE);
        }
    }

    @Test
    public void birthsBeforeDeaths() {

        for (final IPerson person : population.getPeople()) {
            assertBirthBeforeDeath(person);
        }
    }

    @Test
    public void birthInfoConsistent() {

        for (final IPerson person : population.getPeople()) {
            assertBirthInfoConsistent(person);
        }
    }

    @Test
    public void deathInfoConsistent() {

        for (final IPerson person : population.getPeople()) {
            assertDeathInfoConsistent(person);
        }
    }

    @Test
    public void agesAtDeathNotTooHigh() {

        for (final IPerson person : population.getPeople()) {
            assertAgeAtDeathNotTooHigh(person);
        }
    }

    @Test
    public void birthsBeforeMarriages() {

        for (final IPerson person : population.getPeople()) {
            assertBirthBeforeMarriages(person);
        }
    }

    @Test
    public void marriagesBeforeDeaths() {

        for (final IPerson person : population.getPeople()) {
            assertMarriagesBeforeDeath(person);
        }
    }

    @Test
    public void sexesConsistent() {

        for (final IPartnership partnership : population.getPartnerships()) {
            assertSexesConsistent(partnership);
        }
    }

    @Test
    public void surnamesInheritedOnMaleLine() {

        for (final IPerson person : population.getPeople()) {
            assertSurnameInheritedOnMaleLine(person);
        }
    }

    @Test
    public void noSiblingPartners() {

        for (final IPerson person : population.getPeople()) {
            assertNoneOfChildrenAreSiblingPartners(person);
        }
    }

    @Test
    public void noParentPartnerOfChild() {

        for (final IPartnership partnership : population.getPartnerships()) {
            assertParentNotPartnerOfChild(partnership);
        }
    }

    @Test
    public void parentsHaveSensibleAgesAtBirths() {

        for (final IPartnership partnership : population.getPartnerships()) {
            assertParentsHaveSensibleAgesAtBirth(partnership);
        }
    }

    @Test
    public void parentsAndChildrenConsistent() {

        for (final IPartnership partnership : population.getPartnerships()) {
            assertParentsAndChildrenConsistent(partnership);
        }

        for (final IPerson person : population.getPeople()) {
            assertParentsAndChildrenConsistent(person);
        }
    }

    private boolean testingSmallPopulation() {
        return initialSize <= POPULATION_SIZE_LIMIT_FOR_EXPENSIVE_TESTS;
    }

    private static void assertBirthInfoConsistent(final IPerson person) {

        assertFalse(person.getBirthDate() == null && person.getBirthPlace() != null);
    }

    private void assertParentsAndChildrenConsistent(final IPartnership partnership) {

        for (final IPerson child : partnership.getChildren()) {
            assertEquals(child.getParents(), partnership);
            assertPersonIsPresentInPopulation(child);
        }
    }

    private void assertParentsAndChildrenConsistent(final IPerson person) {

        IPartnership parents = person.getParents();

        if (parents != null) {
            assertTrue(parents.getChildren().contains(person));
            IPerson father = parents.getMalePartner();

            assertPersonIsPresentInPopulation(father);
            assertPersonIsPresentInPopulation(parents.getFemalePartner());
        }
    }

    private void assertPersonIsPresentInPopulation(final IPerson person) {

        assertNotNull(population.findPerson(person.getId()));
    }

    private void assertParentsHaveSensibleAgesAtBirth(final IPartnership partnership) {

        final IPerson mother = partnership.getFemalePartner();
        final IPerson father = partnership.getMalePartner();

        for (final IPerson child : partnership.getChildren()) {

            assertTrue(PopulationLogic.parentsHaveSensibleAgesAtChildBirth(father, mother, child));
        }
    }

    private static void assertParentNotPartnerOfChild(final IPartnership partnership) {

        for (final IPerson child : partnership.getChildren()) {

            assertNotEquals(child, partnership.getFemalePartner());
            assertNotEquals(child, partnership.getMalePartner());
        }
    }

    private void assertNoneOfChildrenAreSiblingPartners(final IPerson person) {

        // Include half-siblings.
        final Set<IPerson> siblings = new HashSet<>();

        for (final IPartnership partnership : person.getPartnerships()) {

            for (final IPerson child : partnership.getChildren()) {

                assertNotPartnerOfAny(child, siblings);
                siblings.add(child);
            }
        }
    }

    private static void assertDeathInfoConsistent(final IPerson person) {
        assertFalse(!deathDateIsDefined(person) && (deathPlaceIsDefined(person) || deathCauseIsDefined(person)));

    }

    private void assertAgeAtDeathNotTooHigh(final IPerson person) {

        assertTrue(deathDateIsDefined(person) || ageAtEndOfSimulationNotTooHigh(person));
    }

    private boolean ageAtEndOfSimulationNotTooHigh(final IPerson person) {

        return Period.between(person.getBirthDate(), population.getEndDate()).minus(OBDModel.MAX_AGE).isNegative();
    }

    private static boolean deathDateIsDefined(IPerson person) {

        return person.getDeathDate() != null;
    }

    private static boolean deathPlaceIsDefined(IPerson person) {

        return person.getDeathPlace() != null && person.getDeathPlace().length() > 0;
    }

    private static boolean deathCauseIsDefined(IPerson person) {

        return person.getDeathCause() != null && person.getDeathCause().length() > 0;
    }

    private void assertSexesConsistent(final IPartnership partnership) {

        assertEquals(SexOption.FEMALE, partnership.getFemalePartner().getSex());
        assertEquals(SexOption.MALE, partnership.getMalePartner().getSex());
    }

    private void assertNotPartnerOfAny(final IPerson person, final Set<IPerson> people) {

        for (final IPerson another_person : people) {
            boolean partnerOf = isPartnerOf(person, another_person);
            assertFalse(partnerOf);
        }
    }

    private boolean isPartnerOf(final IPerson p1, final IPerson p2) {

        for (final IPartnership partnership : p1.getPartnerships()) {

            if (partnership.getPartnerOf(p1).equals(p2)) {
                return true;
            }
        }

        return false;
    }

    private static void assertSurnameInheritedOnMaleLine(final IPerson person) {

        if (person.getSex() == SexOption.MALE) {

            for (final IPartnership partnership : person.getPartnerships()) {

                for (final IPerson child : partnership.getChildren()) {

                    boolean same = person.getSurname().equals(child.getSurname());
                    assertTrue(same);


//                    assertEquals(person.getSurname(), child.getSurname());

                    if (child.getSex() == SexOption.MALE) {
                        assertSurnameInheritedOnMaleLine(child);
                    }
                }
            }
        }
    }

    private static void assertBirthBeforeDeath(final IPerson person) {

        if (person.getDeathDate() != null) {

            final LocalDate death_date = person.getDeathDate();
            final LocalDate birth_date = person.getBirthDate();

            assertFalse(birth_date.isAfter(death_date));
        }
    }

    private void assertBirthBeforeMarriages(final IPerson person) {

        if (person.getBirthDate() != null) {

            final LocalDate birth_date = person.getBirthDate();

            for (final IPartnership partnership : person.getPartnerships()) {
                if (partnership.getMarriageDate() != null) {

                    final LocalDate marriage_date = partnership.getMarriageDate();
                    assertFalse(birth_date.isAfter(marriage_date));
                }
            }
        }
    }

    private void assertMarriagesBeforeDeath(final IPerson person) {

        if (person.getDeathDate() != null) {

            final LocalDate death_date = person.getDeathDate();

            for (final IPartnership partnership : person.getPartnerships()) {
                if (partnership.getMarriageDate() != null) {

                    final LocalDate marriage_date = partnership.getMarriageDate();
                    assertFalse(marriage_date.isAfter(death_date));
                }
            }
        }
    }

    private void assertRetrievedConsistently(final IPerson[] sample) {

        for (final IPerson person : sample) {
            assertRetrievedConsistently(person);
        }
    }

    private void assertRetrievedConsistently(final IPerson person) {

        final IPerson retrieved_person = population.findPerson(person.getId());
        assertEquals(person, retrieved_person);
    }

    private void assertRetrievedConsistently(final IPartnership[] sample) {

        for (final IPartnership partnership : sample) {
            assertRetrievedConsistently(partnership);
        }
    }

    private void assertRetrievedConsistently(final IPartnership partnership) {

        final int id = partnership.getId();
        final IPartnership retrieved_person = population.findPartnership(id);

        assertEquals(id, retrieved_person.getId());
    }

    private static void doTooManyIterations(final Iterator<?> iterator, final int number_available) {

        for (int i = 0; i < number_available + 1; i++) {
            iterator.next();
        }
    }
}
