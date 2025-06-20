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
import org.gedcom4j.model.FamilyEventType;
import org.gedcom4j.model.Individual;
import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.statistics.analysis.validation.contingencyTables.TreeStructure.SexOption;

import java.time.LocalDate;
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
    private int male_id;
    private int female_id;
    private LocalDate marriage_date;
    private String marriage_place;
    private List<Integer> child_ids;
    private GEDCOMPopulationAdapter adapter;

    public int getId() {
        return id;
    }

    @Override
    public boolean isFinalised() {
        return true;
    }

    @Override
    public void setFinalised(boolean finalised) {

    }

    @Override
    public void setMarriagePlace(String place) {
        marriage_place = place;
    }

    public String getMarriagePlace() {
        return marriage_place;
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

    GEDCOMPartnership(final Family family, GEDCOMPopulationAdapter adapter) {

        this.adapter = adapter;

        id = getId(family.xref);
        male_id = getId(family.husband.xref);
        female_id = getId(family.wife.xref);

        setMarriage(family);
        setChildren(family);
    }

    private static int getId(final String reference) {

        return GEDCOMPopulationWriter.idToInt(reference);
    }

    private void setMarriage(final Family family) {

        for (final FamilyEvent event : family.events) {

            if (event.type == FamilyEventType.MARRIAGE) {
                marriage_date = LocalDate.parse(event.date.toString(), GEDCOMPopulationWriter.FORMAT);
                if (event.place != null) {
                    marriage_place = event.place.placeName;
                }
            }
        }
    }

    private void setChildren(final Family family) {

        child_ids = new ArrayList<>();
        for (final Individual child : family.children) {
            child_ids.add(GEDCOMPopulationWriter.idToInt(child.xref));
        }
    }

    @Override
    public IPerson getFemalePartner() {
        return adapter.findPerson(female_id);
    }

    @Override
    public IPerson getMalePartner() {
        return adapter.findPerson(male_id);
    }

    @Override
    public IPerson getPartnerOf(IPerson person) {
        return adapter.findPerson(person.getSex() == SexOption.MALE ? female_id : male_id);
    }

    @Override
    public List<IPerson> getChildren() {

        List<IPerson> children = new ArrayList<>();
        for (int child_id : child_ids) {
            children.add(adapter.findPerson(child_id));
        }
        return children;
    }

    @Override
    public LocalDate getPartnershipDate() {
        return marriage_date;
    }

    @Override
    public LocalDate getSeparationDate(RandomGenerator randomGenerator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalDate getEarliestPossibleSeparationDate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEarliestPossibleSeparationDate(LocalDate date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMarriageDate(LocalDate marriageDate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalDate getMarriageDate() {
        return marriage_date;
    }

    @Override
    public void addChildren(Collection<IPerson> children) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPartnershipDate(LocalDate startDate) {
        throw new UnsupportedOperationException();
    }
}
