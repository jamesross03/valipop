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
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.egSkyeFormat.EGSkyeBirthSourceRecord;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.egSkyeFormat.EGSkyeDeathSourceRecord;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.egSkyeFormat.EGSkyeMarriageSourceRecord;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat.SourceRecord;

public class EgSkyeRecord extends Record {

    EgSkyeRecord(final Iterable<IPerson> people, final Iterable<IPartnership> partnerships) {
      super(people, partnerships);
    }

    @Override
    protected Iterable<SourceRecord> toBirthRecords(final Iterable<IPerson> people) {

        return SimplifiedRecord.getRecords(people, EGSkyeBirthSourceRecord::new);
    }

    @Override
    protected Iterable<SourceRecord> toDeathRecords(final Iterable<IPerson> people) {

        return SimplifiedRecord.getRecords(people, EGSkyeDeathSourceRecord::new);
    }

    @Override
    protected Iterable<SourceRecord> toMarriageRecords(final Iterable<IPartnership> partnerships) {

        return SimplifiedRecord.getRecords(partnerships, EGSkyeMarriageSourceRecord::new);
    }
}
