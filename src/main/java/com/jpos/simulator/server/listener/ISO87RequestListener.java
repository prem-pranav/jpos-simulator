package com.jpos.simulator.server.listener;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;

import com.jpos.simulator.server.base.ISO87ResponseMessages;

import java.io.IOException;

public class ISO87RequestListener extends ISO87ResponseMessages implements ISORequestListener {

    @Override
    public boolean process(ISOSource source, ISOMsg m) {
        try {
            String mti = m.getMTI();
            System.out.println("ISO87 Received MTI: " + mti);

            if (mti.equals("0800")) {
                handleNetMgmt(source, m);
            } else if (mti.equals("0200") || mti.equals("0100")) {
                handleFinancial(source, m);
            } else if (mti.equals("0420")) {
                handleReversal(source, m);
            } else if (mti.equals("0300")) {
                handleFileAction(source, m);
            } else {
                System.out.println("ISO87 Unhandled MTI: " + mti);
                sendUnknownResponse(source, m);
            }
            return true;
        } catch (ISOException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void handleNetMgmt(ISOSource source, ISOMsg m) throws ISOException, IOException {
        String netCode = m.getString(70);
        String type = "Network Management";
        if ("301".equals(netCode))
            type = "Echo";
        else if ("001".equals(netCode))
            type = "Logon";
        else if ("002".equals(netCode))
            type = "Logoff";
        else if ("161".equals(netCode))
            type = "Key Exchange";

        System.out.println("ISO87: Processing " + type + " (" + netCode + ") STAN=" + m.getString(11) + "...");
        ISOMsg response = createNetMgmtResponse(m, "00");
        if ("161".equals(netCode)) {
            response.set(48, "FEDCBA0987654321FEDCBA0987654321"); // Dummy response key
        }
        source.send(response);
    }

    private void handleFinancial(ISOSource source, ISOMsg m) throws ISOException, IOException {
        String procCode = m.getString(3);
        String type = "Financial";
        if ("000000".equals(procCode))
            type = m.getMTI().equals("0100") ? "Pre-Auth" : "Purchase";
        else if ("310000".equals(procCode))
            type = "Balance Inquiry";
        else if ("010000".equals(procCode))
            type = "Withdrawal";
        else if ("200000".equals(procCode))
            type = "Refund";

        System.out.println("ISO87: Processing " + type + " (" + procCode + ") STAN=" + m.getString(11) + " TID="
                + m.getString(41) + " MID=" + m.getString(42) + "...");
        ISOMsg response = createFinancialResponse(m, "00");
        if ("310000".equals(procCode)) {
            response.set(54, "00010840C000000012345"); // Sample balance: 123.45
        }
        source.send(response);
    }

    private void handleReversal(ISOSource source, ISOMsg m) throws ISOException, IOException {
        String procCode = m.getString(3);
        String type = "Reversal";
        if ("000000".equals(procCode))
            type = "Purchase Reversal";
        else if ("010000".equals(procCode))
            type = "Withdrawal Reversal";

        System.out.println("ISO87: Processing " + type + " (" + procCode + ") STAN=" + m.getString(11) + " TID="
                + m.getString(41) + "...");
        ISOMsg response = createReversalResponse(m, "00");
        source.send(response);
    }

    private void handleFileAction(ISOSource source, ISOMsg m) throws ISOException, IOException {
        String updateCode = m.getString(91);
        String fileName = m.getString(101);
        String type = "1".equals(updateCode) ? "Create Card" : "Update Card Status";

        System.out.println("ISO87: Processing " + type + " (F91=" + updateCode + ") File=" + fileName + " STAN="
                + m.getString(11) + "...");
        ISOMsg response = createFileActionResponse(m, "00");
        source.send(response);
    }

    private void sendUnknownResponse(ISOSource source, ISOMsg m) throws ISOException, IOException {
        ISOMsg response = createResponse(m, "40");
        source.send(response);
    }
}
