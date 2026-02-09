package com.jpos.simulator.client.base;

import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import java.util.Date;
import java.util.Calendar;

public class ISO93Messages {

    protected static ISOMsg createBase(String mti) throws ISOException {
        ISOMsg m = new ISOMsg();
        m.setMTI(mti);
        m.set(7, ISODate.getDateTime(new Date()));
        m.set(11, String.format("%06d", (int) (Math.random() * 1000000)));
        return m;
    }

    protected static ISOMsg createNetMgmtBase(String funcCode) throws ISOException {
        ISOMsg m = createBase("1804");
        m.set(24, funcCode);
        return m;
    }

    protected static ISOMsg createFinancialBase(String mti, String procCode, String funcCode, String pan, String expiry)
            throws ISOException {
        ISOMsg m = createBase(mti);
        Date now = new Date();
        String stan = m.getString(11);

        m.set(2, pan);
        m.set(3, procCode);
        m.set(4, "000000001000");
        m.set(12, ISODate.getTime(now));
        m.set(13, ISODate.getDate(now));
        m.set(14, expiry != null ? expiry : "2912");
        m.set(18, "5999");
        m.set(22, "021");
        m.set(24, funcCode);
        m.set(25, "00");
        m.set(32, "123456");
        String julianDay = String.format("%03d", Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
        String rrn = julianDay + ISODate.getTime(now).substring(0, 2) + stan + "1";
        m.set(37, rrn);
        m.set(41, "TERM0001");
        m.set(42, "MERCHANT0000001");
        m.set(49, "840");
        return m;
    }

    protected static ISOMsg createFileActionBase(String updateCode) throws ISOException {
        ISOMsg m = createBase("1304");
        m.set(91, updateCode);
        m.set(101, "CARD_MASTER");
        return m;
    }

    protected static ISOMsg createReversalBase(String mti, String procCode, String funcCode, String pan, String amount,
            String rrn, ISOMsg original) throws ISOException {
        ISOMsg m = createBase(mti);
        Date now = new Date();
        m.set(2, pan);
        m.set(3, procCode);
        m.set(4, amount);
        m.set(12, ISODate.getTime(now));
        m.set(13, ISODate.getDate(now));
        m.set(22, "021");
        m.set(24, funcCode);
        m.set(25, "00");
        m.set(37, rrn);
        if (original.hasField(38)) {
            m.set(38, original.getString(38));
        }
        m.set(41, "TERM0001");
        m.set(42, "MERCHANT0000001");
        m.set(49, "840");

        StringBuilder f90 = new StringBuilder();
        f90.append(original.getMTI());
        f90.append(String.format("%-6s", original.getString(11)).replace(' ', '0'));
        f90.append(original.getString(7));
        f90.append(String.format("%-11s", original.hasField(32) ? original.getString(32) : "00000000000").replace(' ',
                '0'));
        f90.append(String.format("%-11s", original.hasField(33) ? original.getString(33) : "00000000000").replace(' ',
                '0'));

        m.set(90, f90.toString());
        return m;
    }
}
