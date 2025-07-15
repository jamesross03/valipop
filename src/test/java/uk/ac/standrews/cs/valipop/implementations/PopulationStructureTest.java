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
package uk.ac.standrews.cs.valipop.implementations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.simulationEntities.IPersonCollection;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static uk.ac.standrews.cs.valipop.config.TestCases.getTestConfigurations;
import static uk.ac.standrews.cs.valipop.implementations.OBDModel.MAXIMUM_AGE_AT_DEATH;

/**
 * Tests of properties of abstract population interface that should hold for all populations.
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public abstract class PopulationStructureTest {

    public static final List<Integer> FAST_TEST_CASE_INITIAL_POPULATION_SIZES = List.of(200, 300);
    public static final List<Integer> SLOW_TEST_CASE_INITIAL_POPULATION_SIZES = List.of(1000, 10000);

    private static final int MAX_REASONABLE_FAMILY_SIZE = 20;
    private static final int MINIMUM_MOTHER_AGE_AT_CHILDBIRTH = 12;
    private static final int MAXIMUM_MOTHER_AGE_AT_CHILDBIRTH = 55;
    private static final int MINIMUM_FATHER_AGE_AT_CHILDBIRTH = 12;
    private static final int MAX_GESTATION_IN_DAYS = 300;
    private static final int MINIMUM_AGE_AT_MARRIAGE = 14;

    private final IPersonCollection population;

    PopulationStructureTest(final IPersonCollection population) {

        this.population = population;
    }

    static List<Arguments> getFastTestCases()  {

        return getTestConfigurations(FAST_TEST_CASE_INITIAL_POPULATION_SIZES);
    }

    static List<Arguments> getSlowTestCases()  {

        return getTestConfigurations(SLOW_TEST_CASE_INITIAL_POPULATION_SIZES);
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
    public void numberOfPeopleIsConsistent() {

        int count = 0;
        for (final IPerson ignored : population.getPeople())
            count++;

        assertEquals(population.getNumberOfPeople(), count);
    }

    @Test
    public void personIDsArentRepeated() {

        final Set<Integer> ids = new HashSet<>();

        for (final IPerson person : population.getPeople()) {
            assertFalse(ids.contains(person.getId()));
            ids.add(person.getId());
        }
    }

    @Test
    public void numberOfPartnershipsIsConsistent() {

        int count = 0;
        for (final IPartnership ignored : population.getPartnerships())
            count++;

        assertEquals(population.getNumberOfPartnerships(), count);
    }

    @Test
    public void partnershipIDsArentRepeated() {

        final Set<Integer> ids = new HashSet<>();

        for (final IPartnership partnership : population.getPartnerships()) {
            assertFalse(ids.contains(partnership.getId()));
            ids.add(partnership.getId());
        }
    }

    @Test
    public void tooManyPersonIterations() {

        assertThrows(NoSuchElementException.class, () ->
            doTooManyIterations(population.getPeople().iterator(), population.getNumberOfPeople()));
    }

    @Test
    public void tooManyPartnershipIterations() {

        assertThrows(NoSuchElementException.class, () ->
            doTooManyIterations(population.getPartnerships().iterator(), population.getNumberOfPartnerships()));
    }

    @Test
    public void peopleCanBeFoundById() {

        for (final IPerson person : population.getPeople())
            assertCanBeFoundById(person);
    }

    @Test
    public void partnershipsCanBeFoundById() {

        for (final IPartnership partnership : population.getPartnerships())
            assertCanBeFoundById(partnership);
    }

    @Test
    public void partnershipsConsistent() {

        final List<IPartnership> partnerships = new ArrayList<>();
        final List<IPerson> people = new ArrayList<>();

        for (final IPartnership partnership : population.getPartnerships())
            partnerships.add(partnership);

        for (final IPerson person : population.getPeople())
            people.add(person);

        for (final IPartnership partnership : partnerships) {
            assertTrue(people.contains(partnership.getMalePartner()));
            assertTrue(people.contains(partnership.getFemalePartner()));
            assertTrue(partnership.getMalePartner().getPartnerships().contains(partnership));
            assertTrue(partnership.getFemalePartner().getPartnerships().contains(partnership));
        }

        for (final IPerson person : people)
            for (final IPartnership partnership : person.getPartnerships()) {
                assertTrue(partnerships.contains(partnership));
                assertTrue(partnership.getMalePartner().equals(person) || partnership.getFemalePartner().equals(person));
            }
    }

    @Test
    public void familiesNotTooLarge() {

        for (final IPartnership partnership : population.getPartnerships())
            assertTrue(partnership.getChildren().size() <= MAX_REASONABLE_FAMILY_SIZE);
    }

    @Test
    public void birthsBeforeDeaths() {

        for (final IPerson person : population.getPeople())
            assertBirthBeforeDeath(person);
    }

    @Test
    public void sensibleAgeAtMarriages() {

        for (final IPerson person : population.getPeople())
            assertSensibleAgeAtMarriages(person);
    }

    @Test
    public void marriagesBeforeDeaths() {

        for (final IPerson person : population.getPeople())
            assertMarriagesBeforeDeath(person);
    }

    @Test
    public void sexesConsistent() {

        for (final IPartnership partnership : population.getPartnerships())
            assertSexesConsistent(partnership);
    }

    @Test
    public void surnamesInheritedOnMaleLine() {

        for (final IPerson person : population.getPeople())
            assertSurnameInheritedOnMaleLine(person);
    }

    @Test
    public void noSiblingPartners() {

        for (final IPerson person : population.getPeople())
            assertNoneOfChildrenAreSiblingPartners(person);
    }

    @Test
    public void noParentPartnerOfChild() {

        for (final IPartnership partnership : population.getPartnerships())
            assertParentNotPartnerOfChild(partnership);
    }

    @Test
    public void parentsHaveSensibleAgesAtBirths() {

        for (final IPartnership partnership : population.getPartnerships())
            assertParentsHaveSensibleAgesAtBirth(partnership);
    }

    @Test
    public void parentsAndChildrenConsistent() {

        for (final IPartnership partnership : population.getPartnerships())
            assertParentsAndChildrenConsistent(partnership);

        for (final IPerson person : population.getPeople())
            assertParentsAndChildrenConsistent(person);
    }

    private void assertParentsAndChildrenConsistent(final IPartnership partnership) {

        for (final IPerson child : partnership.getChildren()) {
            assertEquals(child.getParents(), partnership);
            assertPersonIsPresentInPopulation(child);
        }
    }

    private void assertParentsAndChildrenConsistent(final IPerson person) {

        final IPartnership parents = person.getParents();

        if (parents != null) {
            assertTrue(parents.getChildren().contains(person));

            assertPersonIsPresentInPopulation(parents.getMalePartner());
            assertPersonIsPresentInPopulation(parents.getFemalePartner());
        }
    }

    private void assertPersonIsPresentInPopulation(final IPerson person) {

        assertNotNull(population.findPerson(person.getId()));
    }

    private static void assertParentsHaveSensibleAgesAtBirth(final IPartnership partnership) {

        final IPerson mother = partnership.getFemalePartner();
        final IPerson father = partnership.getMalePartner();

        for (final IPerson child : partnership.getChildren())
            assertParentsHaveSensibleAgesAtBirth(father, mother, child);
    }

    private static void assertParentNotPartnerOfChild(final IPartnership partnership) {

        for (final IPerson child : partnership.getChildren()) {

            assertNotEquals(child, partnership.getFemalePartner());
            assertNotEquals(child, partnership.getMalePartner());
        }
    }

    private static void assertNoneOfChildrenAreSiblingPartners(final IPerson person) {

        // Include half-siblings.
        final Set<IPerson> siblings = new HashSet<>();

        for (final IPartnership partnership : person.getPartnerships())
            for (final IPerson child : partnership.getChildren()) {

                assertNotPartnerOfAny(child, siblings);
                siblings.add(child);
            }
    }

    private static void assertSexesConsistent(final IPartnership partnership) {

        if (partnership.getFemalePartner().getSex() != null)
            assertEquals(SexOption.FEMALE, partnership.getFemalePartner().getSex());
        if (partnership.getMalePartner().getSex() != null)
            assertEquals(SexOption.MALE, partnership.getMalePartner().getSex());
    }

    private static void assertNotPartnerOfAny(final IPerson person, final Set<IPerson> people) {

        for (final IPerson another_person : people)
            assertFalse(isPartnerOf(person, another_person));
    }

    private static boolean isPartnerOf(final IPerson p1, final IPerson p2) {

        for (final IPartnership partnership : p1.getPartnerships())
            if (partnership.getPartnerOf(p1).equals(p2))
                return true;

        return false;
    }

    private static void assertSurnameInheritedOnMaleLine(final IPerson person) {

        if (person.getSex() == SexOption.MALE) {

            for (final IPartnership partnership : person.getPartnerships())
                for (final IPerson child : partnership.getChildren()) {

                    if (person.getSurname() != null && child.getSurname() != null)
                        assertEquals(person.getSurname(), child.getSurname());

                    if (child.getSex() == SexOption.MALE)
                        assertSurnameInheritedOnMaleLine(child);
                }
        }
    }

    private static void assertBirthBeforeDeath(final IPerson person) {

        if (person.getBirthDate() != null && person.getDeathDate() != null) {

            final LocalDate deathDate = person.getDeathDate();
            final LocalDate birthDate = person.getBirthDate();

            assertFalse(birthDate.isAfter(deathDate));
        }
    }

    private static void assertSensibleAgeAtMarriages(final IPerson person) {

        if (person.getBirthDate() != null) {

            final LocalDate birthDate = person.getBirthDate();

            for (final IPartnership partnership : person.getPartnerships())
                if (partnership.getMarriageDate() != null) {

                    final LocalDate marriageDate = partnership.getMarriageDate();

                    final int ageAtMarriage = differenceInYears(birthDate, marriageDate);
                    assertTrue(ageAtMarriage >= MINIMUM_AGE_AT_MARRIAGE);
                }
        }
    }

    private static void assertMarriagesBeforeDeath(final IPerson person) {

        if (person.getDeathDate() != null) {

            final LocalDate deathDate = person.getDeathDate();

            for (final IPartnership partnership : person.getPartnerships())
                if (partnership.getMarriageDate() != null) {

                    final LocalDate marriageDate = partnership.getMarriageDate();
                    assertFalse(marriageDate.isAfter(deathDate));
                }
        }
    }

    private void assertCanBeFoundById(final IPerson person) {

        final IPerson retrievedPerson = population.findPerson(person.getId());
        assertEquals(person, retrievedPerson);
    }

    private void assertCanBeFoundById(final IPartnership partnership) {

        final IPartnership retrievedPartnership = population.findPartnership(partnership.getId());
        assertEquals(partnership, retrievedPartnership);
    }

    private static void doTooManyIterations(final Iterator<?> iterator, final int number_available) {

        for (int i = 0; i < number_available + 1; i++)
            iterator.next();
    }

    private static void assertParentsHaveSensibleAgesAtBirth(final IPerson father, final IPerson mother, final IPerson child) {

        final LocalDate motherBirthDate = mother.getBirthDate();
        final LocalDate motherDeathDate = mother.getDeathDate();

        final LocalDate fatherBirthDate = father.getBirthDate();
        final LocalDate fatherDeathDate = father.getDeathDate();

        final LocalDate childBirthDate = child.getBirthDate();

        assertTrue(motherDeathDate == null || childBirthDate == null || !childBirthDate.isAfter(motherDeathDate));
        assertTrue(fatherDeathDate == null || childBirthDate == null || !childBirthDate.isAfter(fatherDeathDate.plusDays(MAX_GESTATION_IN_DAYS)));

        assertTrue(motherBirthDate == null || childBirthDate == null || differenceInYears(motherBirthDate, childBirthDate) >= MINIMUM_MOTHER_AGE_AT_CHILDBIRTH);
        assertTrue(motherBirthDate == null || childBirthDate == null || differenceInYears(motherBirthDate, childBirthDate) <= MAXIMUM_MOTHER_AGE_AT_CHILDBIRTH);
        assertTrue(fatherBirthDate == null || childBirthDate == null || differenceInYears(fatherBirthDate, childBirthDate) >= MINIMUM_FATHER_AGE_AT_CHILDBIRTH);
    }

    private static int differenceInYears(final LocalDate parent_birth_date, final LocalDate child_birth_date) {

        return Period.between(parent_birth_date, child_birth_date).getYears();
    }
}
