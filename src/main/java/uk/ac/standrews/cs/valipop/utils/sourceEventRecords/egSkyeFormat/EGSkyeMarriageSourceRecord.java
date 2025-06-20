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
package uk.ac.standrews.cs.valipop.utils.sourceEventRecords.egSkyeFormat;

import uk.ac.standrews.cs.valipop.simulationEntities.IPartnership;
import uk.ac.standrews.cs.valipop.utils.sourceEventRecords.oldDSformat.MarriageSourceRecord;

import java.time.LocalDate;

/**
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class EGSkyeMarriageSourceRecord extends MarriageSourceRecord {

    protected LocalDate marriageDate;
    protected int groomID;
    protected int brideID;
    protected String marriageLocation;

    public EGSkyeMarriageSourceRecord(IPartnership partnership) {
        super(partnership);

        marriageDate = partnership.getPartnershipDate();
        groomID = partnership.getMalePartner().getId();
        brideID = partnership.getFemalePartner().getId();

        marriageLocation = partnership.getMarriagePlace();

        if(partnership.getFemalePartner().getParents() != null)
            setBrideFathersSurname(partnership.getFemalePartner().getParents().getMalePartner().getSurname());

        if(partnership.getMalePartner().getParents() != null)
            setGroomFathersSurname(partnership.getMalePartner().getParents().getMalePartner().getSurname());
    }

    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();

        append(builder, "", "", uid, "", "", "",
                "", "", "", "",
                "", "", "", marriageDate.toString(), getGroomAgeOrDateOfBirth(), getBrideAgeOrDateOfBirth(), "",
                "", "", "", "", "", "", "", marriageDate.toString(), marriageDate.getDayOfMonth(), marriageDate.getMonth(), marriageDate.getYear(),
                marriageLocation, "", "", getGroomForename(), getGroomSurname(),
                getGroomOccupation(), getGroomMaritalStatus(), getGroomAgeOrDateOfBirth(), getGroomAddress(),
                "", getBrideForename(), getBrideSurname(), getBrideOccupation(),
                getBrideMaritalStatus(), getBrideAgeOrDateOfBirth(), getBrideAddress(), "",
                getGroomFathersForename(), getGroomFathersSurname(), getGroomFathersOccupation(),
                getGroomFatherDeceased(), getGroomMothersForename(), getGroomMothersMaidenSurname(),
                getGroomMotherDeceased(), getBrideFathersForename(), getBrideFathersSurname(),
                getBrideFatherOccupation(), getBrideFatherDeceased(), getBrideMothersForename(),
                getBrideMothersMaidenSurname(), getBrideMotherDeceased(), "", "",
                "SYNTHETIC DATA PRODUCED USING VALIPOP", "", "", "", "", "", "", "", "",
                "", "", "", groomID, brideID);

        return builder.toString();
    }

    @Override
    public String getHeaders() {

        final StringBuilder builder = new StringBuilder();

        append(builder, "IOS_identifier", "corrected", "ID", "source", "line no", "rd identifier",
                "IOS_Rdindentifier", "IOS_RSDidentifier", "register identifier", "IOS_registeridentifier",
                "entry number", "IOS_entrynumber", "IOS_YrofReg", "date of marriage", "groomagey", "brideagey", "gsn",
                "gxn", "bsn", "bxn", "gfxn", "gmxn", "bfxn", "bmxn", "clean date of marriage", "day", "month", "year",
                "place of marriage 1", "place of marriage 2", "denomination", "forename of groom", "surname of groom",
                "occupation of groom", "marital status of groom", "age of groom", "address of groom 1",
                "address of groom 2", "forename of bride", "surname of bride", "occupation of bride",
                "marital status of bride", "age of bride", "address of bride 1", "address of bride 2",
                "groom's father's forename", "groom's father's surname", "groom's father's occupation",
                "if groom's father deceased", "groom's mother's forename", "groom's mother's maiden surname",
                "if groom's mother deceased", "bride's father's forename", "bride's father's surname",
                "bride's father's occupation", "if bride's father deceased", "bride's mother's forename",
                "bride's mother's maiden surname", "if bride's mother deceased", "did groom sign?", "did bride sign?",
                "notes1", "notes2", "notes3", "repeats", "gearlypid", "gearlysch", "bearlypid", "bearlysch", "glatepid",
                "glatesch", "blatepid", "blatesch", "gdeath", "bdeath");

        return builder.toString();
    }
}
