package uk.ac.standrews.cs.valipop.utils.sourceEventRecords;

import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat.BirthSourceRecord;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat.DeathSourceRecord;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat.MarriageSourceRecord;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat.SourceRecord;

public class DsRecord extends Record {

    DsRecord(final Iterable<IPerson> people, final Iterable<IPartnership> partnerships) {
      super(people, partnerships);
    }

    @Override
    protected Iterable<SourceRecord> toBirthRecords(final Iterable<IPerson> people) {
        return SimplifiedRecord.getRecords(people, BirthSourceRecord::new);
    }

    @Override
    protected Iterable<SourceRecord> toDeathRecords(final Iterable<IPerson> people) {
        return SimplifiedRecord.getRecords(people, DeathSourceRecord::new);
    }

    @Override
    protected Iterable<SourceRecord> toMarriageRecords(final Iterable<IPartnership> partnerships) {
        return SimplifiedRecord.getRecords(partnerships, MarriageSourceRecord::new);
    }
}
