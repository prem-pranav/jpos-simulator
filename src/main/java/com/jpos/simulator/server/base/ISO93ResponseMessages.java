package com.jpos.simulator.server.base;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

public class ISO93ResponseMessages {

    protected static ISOMsg createResponse(ISOMsg m, String responseCode) throws ISOException {
        ISOMsg response = new ISOMsg();
        response.setPackager(m.getPackager());
        response.setMTI(m.getMTI());
        response.setResponseMTI();

        // Echo back essential fields
        if (m.hasField(7))
            response.set(7, m.getString(7)); // Transmission Date & Time
        if (m.hasField(11))
            response.set(11, m.getString(11)); // STAN
        if (m.hasField(24))
            response.set(24, m.getString(24)); // Function Code
        if (m.hasField(41))
            response.set(41, m.getString(41)); // Terminal ID
        if (m.hasField(42))
            response.set(42, m.getString(42)); // Merchant ID
        if (m.hasField(37))
            response.set(37, m.getString(37)); // Retrieval Reference Number
        if (m.hasField(49))
            response.set(49, m.getString(49)); // Currency Code
        response.set(39, responseCode);
        return response;
    }

    protected static ISOMsg createFinancialResponse(ISOMsg m, String responseCode) throws ISOException {
        ISOMsg response = createResponse(m, responseCode);
        if ("00".equals(responseCode)) {
            response.set(38, String.format("%06d", (int) (Math.random() * 1000000)));
        }
        return response;
    }

    protected static ISOMsg createNetMgmtResponse(ISOMsg m, String responseCode) throws ISOException {
        return createResponse(m, responseCode);
    }

    protected static ISOMsg createFileActionResponse(ISOMsg m, String responseCode) throws ISOException {
        ISOMsg response = createResponse(m, responseCode);
        if (m.hasField(91))
            response.set(91, m.getString(91));
        if (m.hasField(101))
            response.set(101, m.getString(101));
        return response;
    }
}
