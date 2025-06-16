package uk.ac.standrews.cs.valipop.utils.sourceEventRecords;

import uk.ac.standrews.cs.utilities.MappedIterator;
import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.simulationEntities.IPerson;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat.BirthSourceRecord;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat.DeathSourceRecord;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat.MarriageSourceRecord;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat.SourceRecord;

public class DsRecord extends Record {

    DsRecord(Iterable<IPerson> people, Iterable<IPartnership> partnerships) {
      super(people, partnerships);
    }

    @Override
    protected Iterable<SourceRecord> toBirthRecords(Iterable<IPerson> people) {
        return () -> new MappedIterator<>(people.iterator(), BirthSourceRecord::new);
    }

    @Override
    protected Iterable<SourceRecord> toDeathRecords(Iterable<IPerson> people) {
        return () -> new MappedIterator<>(people.iterator(), DeathSourceRecord::new);
    }

    @Override
    protected Iterable<SourceRecord> toMarriageRecords(Iterable<IPartnership> partnerships) {
        return () -> new MappedIterator<>(partnerships.iterator(), MarriageSourceRecord::new);
    }
}

