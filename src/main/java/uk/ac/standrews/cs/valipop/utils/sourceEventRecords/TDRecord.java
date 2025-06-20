package uk.ac.standrews.cs.valipop.utils.sourceEventRecords;

import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat.SourceRecord;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.tdFormat.TDBirthSourceRecord;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.tdFormat.TDDeathSourceRecord;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.tdFormat.TDMarriageSourceRecord;

public class TDRecord extends Record {

    TDRecord(final Iterable<IPerson> people, final Iterable<IPartnership> partnerships) {
      super(people, partnerships);
    }

    @Override
    protected Iterable<SourceRecord> toBirthRecords(final Iterable<IPerson> people) {
        return SimplifiedRecord.getRecords(people, TDBirthSourceRecord::new);
    }

    @Override
    protected Iterable<SourceRecord> toDeathRecords(final Iterable<IPerson> people) {
        return SimplifiedRecord.getRecords(people, TDDeathSourceRecord::new);
    }

    @Override
    protected Iterable<SourceRecord> toMarriageRecords(final Iterable<IPartnership> partnerships) {
        return SimplifiedRecord.getRecords(partnerships, TDMarriageSourceRecord::new);
    }
}
