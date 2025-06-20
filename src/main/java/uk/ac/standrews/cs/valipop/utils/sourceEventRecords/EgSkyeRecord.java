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
