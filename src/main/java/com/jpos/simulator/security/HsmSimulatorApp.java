package com.jpos.simulator.security;

import org.jpos.security.EncryptedPIN;
import org.jpos.iso.ISOUtil;

public class HsmSimulatorApp {
    public static void main(String[] args) {
        try {
            System.out.println("=== jPOS HSM Simulator Demonstration ===");

            HsmSimulator hsm = new HsmSimulator();

            String accountNumber = "1234567890123456";
            int pinLen = 4;

            System.out.println("1. PIN & PVV Operations");
            EncryptedPIN encryptedPin = hsm.generatePIN(accountNumber, pinLen);
            String pvv = hsm.calculatePVV(encryptedPin, null, null, 0);
            System.out.println("Generated PVV: " + pvv);

            boolean pvvValid = hsm.verifyPVV(encryptedPin, null, null, null, 0, pvv);
            System.out.println("PVV Verification: " + (pvvValid ? "PASS" : "FAIL"));

            System.out.println("\n2. PIN Translation");
            EncryptedPIN translatedPin = hsm.translatePIN(encryptedPin, null, null, (byte) 1);
            System.out.println("Translated PIN Block: " + ISOUtil.byte2hex(translatedPin.getPINBlock()));

            System.out.println("\n3. Key Management");
            org.jpos.security.SecureKey tpk = hsm.generateKey((short) 128, "TPK");
            String kcv = hsm.calculateKCV(tpk);
            System.out.println("Generated TPK, KCV: " + kcv);

            System.out.println("\n4. Card Security (CVV)");
            java.util.Date expiry = new java.util.Date();
            String cvv = hsm.calculateCVV(accountNumber, null, null, expiry, "101");
            System.out.println("Generated CVV: " + cvv);
            boolean cvvValid = hsm.verifyCVV(accountNumber, null, null, "101", expiry, cvv);
            System.out.println("CVV Verification: " + (cvvValid ? "PASS" : "FAIL"));

            System.out.println("\nSimulation complete.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
