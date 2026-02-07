package com.jpos.simulator.server.listener;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;

import com.jpos.simulator.server.base.ISO93ResponseMessages;

import java.io.IOException;

public class ISO93RequestListener extends ISO93ResponseMessages implements ISORequestListener {

    @Override
    public boolean process(ISOSource source, ISOMsg m) {
        try {
            String mti = m.getMTI();
            System.out.println("ISO93 Received MTI: " + mti);

            if (mti.equals("1804")) {
                handleNetMgmt(source, m);
            } else if (mti.equals("1100")) {
                handleFinancial(source, m);
            } else if (mti.equals("1420")) {
                handleReversal(source, m);
            } else if (mti.equals("1304")) {
                handleFileAction(source, m);
            } else {
                System.out.println("ISO93 Unhandled MTI: " + mti);
                sendUnknownResponse(source, m);
            }
            return true;
        } catch (ISOException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void handleNetMgmt(ISOSource source, ISOMsg m) throws ISOException, IOException {
        String funcCode = m.getString(24);
        String type = "Network Management";
        if ("801".equals(funcCode))
            type = "Echo";
        else if ("001".equals(funcCode))
            type = "Logon";
        else if ("002".equals(funcCode))
            type = "Logoff";
        else if ("161".equals(funcCode))
            type = "Key Exchange";

        System.out.println("ISO93: Processing " + type + " (" + funcCode + ") STAN=" + m.getString(11) + "...");
        ISOMsg response = createNetMgmtResponse(m, "00");
        if ("161".equals(funcCode)) {
            response.set(48, "FEDCBA0987654321FEDCBA0987654321"); // Dummy response key
        }
        source.send(response);
    }

    private void handleFinancial(ISOSource source, ISOMsg m) throws ISOException, IOException {
        String procCode = m.getString(3);
        String funcCode = m.getString(24);
        String type = "Financial";
        if ("000000".equals(procCode)) {
            if ("104".equals(funcCode))
                type = "Pre-Authorization";
            else
                type = "Purchase";
        } else if ("310000".equals(procCode)) {
            type = "Balance Inquiry";
        } else if ("010000".equals(procCode)) {
            type = "Withdrawal";
        } else if ("200000".equals(procCode)) {
            type = "Refund";
        }

        System.out.println("ISO93: Processing " + type + " (F3=" + procCode + ", F24=" + funcCode + ") STAN="
                + m.getString(11) + " TID=" + m.getString(41) + " MID=" + m.getString(42) + "...");

        ISOMsg response = createFinancialResponse(m, "00");
        if ("310000".equals(procCode)) {
            response.set(54, "12345");
        }
        source.send(response);
    }

    private void handleReversal(ISOSource source, ISOMsg m) throws ISOException, IOException {
        System.out.println("ISO93: Processing Reversal STAN=" + m.getString(11) + " TID=" + m.getString(41) + "...");
        ISOMsg response = createResponse(m, "00");
        source.send(response);
    }

    private void handleFileAction(ISOSource source, ISOMsg m) throws ISOException, IOException {
        String updateCode = m.getString(91);
        String fileName = m.getString(101);
        String type = "1".equals(updateCode) ? "Create Card" : "Update Card Status";

        System.out.println("ISO93: Processing " + type + " (F91=" + updateCode + ") File=" + fileName + " STAN="
                + m.getString(11) + "...");
        ISOMsg response = createFileActionResponse(m, "00");
        source.send(response);
    }

    private void sendUnknownResponse(ISOSource source, ISOMsg m) throws ISOException, IOException {
        ISOMsg response = createResponse(m, "40");
        source.send(response);
    }
}
