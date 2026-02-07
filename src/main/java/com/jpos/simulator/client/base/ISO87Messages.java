package com.jpos.simulator.client.base;

import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import java.util.Date;
import java.util.Calendar;

public class ISO87Messages {

    protected static ISOMsg createBase(String mti) throws ISOException {
        ISOMsg m = new ISOMsg();
        m.setMTI(mti);
        m.set(7, ISODate.getDateTime(new Date()));
        m.set(11, String.format("%06d", (int) (Math.random() * 1000000)));
        return m;
    }

    protected static ISOMsg createNetMgmtBase(String netCode) throws ISOException {
        ISOMsg m = createBase("0800");
        m.set(70, netCode);
        return m;
    }

    protected static ISOMsg createFinancialBase(String mti, String procCode, String pan) throws ISOException {
        ISOMsg m = createBase(mti);
        Date now = new Date();
        String stan = m.getString(11);

        m.set(2, pan);
        m.set(3, procCode);
        m.set(4, "000000001000"); // 10.00
        m.set(12, ISODate.getTime(now));
        m.set(13, ISODate.getDate(now));
        m.set(14, "2612"); // YYMM
        m.set(18, "5999");
        m.set(22, "021");
        m.set(25, "00");
        m.set(32, "123456");
        // RRN (Field 37): Julian Day (3) + Hour (2) + STAN (6) + suffix (1) = 12 chars
        String julianDay = String.format("%03d", Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
        String rrn = julianDay + ISODate.getTime(now).substring(0, 2) + stan + "1";
        m.set(37, rrn);
        m.set(41, "TERM0001");
        m.set(42, "MERCHANT0000001");
        m.set(49, "840");
        return m;
    }

    protected static ISOMsg createFileActionBase(String updateCode) throws ISOException {
        ISOMsg m = createBase("0300");
        m.set(91, updateCode);
        m.set(101, "CARD_MASTER");
        return m;
    }

    protected static ISOMsg createReversalBase(String mti, String procCode, String pan, String amount, String rrn,
            ISOMsg original) throws ISOException {
        ISOMsg m = createBase(mti);
        m.set(2, pan);
        m.set(3, procCode);
        m.set(4, amount);
        m.set(37, rrn);
        m.set(41, "TERM0001");

        // F90: Original Data Elements (Original MTI + Original STAN + Original Trans DT
        // + Original Acq ID + Original Fwd ID)
        // Format: MTI(4) + STAN(6) + DT(10) + AcqID(11) + FwdID(11)
        StringBuilder f90 = new StringBuilder();
        f90.append(original.getMTI()); // Original MTI
        f90.append(String.format("%-6s", original.getString(11)).replace(' ', '0')); // Original STAN
        f90.append(original.getString(7)); // Original Transmission Date & Time (MMDDhhmmss)
        f90.append(String.format("%-11s", original.hasField(32) ? original.getString(32) : "00000000000").replace(' ',
                '0')); // Original Acq ID
        f90.append(String.format("%-11s", original.hasField(33) ? original.getString(33) : "00000000000").replace(' ',
                '0')); // Original Fwd ID

        m.set(90, f90.toString());
        return m;
    }
}
