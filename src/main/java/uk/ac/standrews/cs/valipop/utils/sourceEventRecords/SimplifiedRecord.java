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

import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat.SourceRecord;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.processingVisualiserFormat.SimplifiedBirthSourceRecord;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.processingVisualiserFormat.SimplifiedDeathSourceRecord;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.processingVisualiserFormat.SimplifiedMarriageSourceRecord;

import java.util.Iterator;
import java.util.function.Function;

public class SimplifiedRecord extends Record {

    SimplifiedRecord(final Iterable<IPerson> people, final Iterable<IPartnership> partneships) {
      super(people, partneships);
    }

    @Override
    protected Iterable<SourceRecord> toBirthRecords(final Iterable<IPerson> people) {

        return getRecords(people, SimplifiedBirthSourceRecord::new);
    }

    @Override
    protected Iterable<SourceRecord> toDeathRecords(final Iterable<IPerson> people) {

        return getRecords(people, SimplifiedDeathSourceRecord::new);
    }

    @Override
    protected Iterable<SourceRecord> toMarriageRecords(final Iterable<IPartnership> partnerships) {

        return getRecords(partnerships, SimplifiedMarriageSourceRecord::new);
    }

    protected static <X> Iterable<SourceRecord> getRecords(final Iterable<X> source, final Function<X, SourceRecord> mapper) {

        return () -> {

            final Iterator<X> source_iterator = source.iterator();
            return new Iterator<>() {

                @Override
                public boolean hasNext() {
                    return source_iterator.hasNext();
                }

                @Override
                public SourceRecord next() {
                    return mapper.apply(source_iterator.next());
                }
            };
        };
    }
}

