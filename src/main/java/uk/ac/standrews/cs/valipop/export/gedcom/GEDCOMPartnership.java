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

import org.apache.commons.math3.random.RandomGenerator;
import org.gedcom4j.model.Family;
import org.gedcom4j.model.FamilyEvent;
import org.gedcom4j.model.IndividualReference;
import org.gedcom4j.model.enumerations.FamilyEventType;
import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Partnership implementation for a population represented in a GEDCOM file.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk>
 */
public class GEDCOMPartnership implements IPartnership {

    protected int id;
    private final int maleId;
    private final int femaleId;
    private LocalDate marriageDate;
    private String marriagePlace;
    private List<Integer> childIds;
    private final GEDCOMPopulationAdapter adapter;

    public int getId() {
        return id;
    }

    @Override
    public boolean isFinalised() {
        return true;
    }

    @Override
    public void setFinalised(final boolean finalised) {
    }

    @Override
    public void setMarriagePlace(final String place) {
        marriagePlace = place;
    }

    public String getMarriagePlace() {
        return marriagePlace;
    }

    @SuppressWarnings("CompareToUsesNonFinalVariable")
    public int compareTo(final IPartnership other) {

        return Integer.compare(id, other.getId());
    }

    public boolean equals(final Object other) {
        return other instanceof IPartnership && compareTo((IPartnership) other) == 0;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    public int hashCode() {
        return id;
    }

    GEDCOMPartnership(final Family family, final GEDCOMPopulationAdapter adapter) {

        this.adapter = adapter;

        id = getId(family.getXref());
        maleId = getId(family.getHusband().getIndividual().getXref());
        femaleId = getId(family.getWife().getIndividual().getXref());

        setMarriage(family);
        setChildren(family);
    }

    private static int getId(final String reference) {

        return GEDCOMPopulationWriter.idToInt(reference);
    }

    private void setMarriage(final Family family) {

        final List<FamilyEvent> events = family.getEvents();

        if (events != null)
            for (final FamilyEvent event : events)
                if (event.getType() == FamilyEventType.MARRIAGE) {

                    try {
                        if (event.getDate() != null)
                            marriageDate = LocalDate.parse(event.getDate().toString(), GEDCOMPopulationWriter.FORMAT);
                    }
                    catch (final DateTimeParseException ignored) {
                        // If the GEDCOM file is valid then this is probably an approximate date such as 'ABT FEB 1901'.
                        // Approximate dates not handled in ValiPop.
                    }
                    if (event.getPlace() != null) marriagePlace = event.getPlace().getPlaceName();
                }
    }

    private void setChildren(final Family family) {

        childIds = new ArrayList<>();
        if (family.getChildren() != null)
            for (final IndividualReference child : family.getChildren())
                childIds.add(GEDCOMPopulationWriter.idToInt(child.getIndividual().getXref()));
    }

    @Override
    public IPerson getFemalePartner() {
        return adapter.findPerson(femaleId);
    }

    @Override
    public IPerson getMalePartner() {
        return adapter.findPerson(maleId);
    }

    @Override
    public IPerson getPartnerOf(final IPerson person) {
        return adapter.findPerson(person.getSex() == SexOption.MALE ? femaleId : maleId);
    }

    @Override
    public List<IPerson> getChildren() {

        final List<IPerson> children = new ArrayList<>();
        for (final int child_id : childIds)
            children.add(adapter.findPerson(child_id));

        return children;
    }

    @Override
    public LocalDate getPartnershipDate() {
        return marriageDate;
    }

    @Override
    public LocalDate getSeparationDate(final RandomGenerator randomGenerator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalDate getEarliestPossibleSeparationDate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEarliestPossibleSeparationDate(final LocalDate date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMarriageDate(final LocalDate marriageDate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalDate getMarriageDate() {
        return marriageDate;
    }

    @Override
    public void addChildren(final Collection<IPerson> children) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPartnershipDate(final LocalDate startDate) {
        throw new UnsupportedOperationException();
    }
}
