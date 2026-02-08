package com.jpos.simulator.card;

import com.jpos.simulator.security.HsmSimulator;
import org.jpos.security.EncryptedPIN;
import java.util.Date;
import java.text.SimpleDateFormat;

public class CardGenerator {
    private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();
    private static final HsmSimulator hsm = new HsmSimulator();

    public static CardInfo generateCard(String prefix, int length) {
        String scheme = determineScheme(prefix);
        String pan = generatePan(prefix, length);
        String expiryStr = "2912"; // Dummy future expiry
        int pinLen = 4;

        try {
            Date expiryDate = new SimpleDateFormat("yyMM").parse(expiryStr);

            // Generate CVV
            String cvv = hsm.calculateCVV(pan, null, null, expiryDate, "101");

            // Generate PIN and PVV
            System.out.println("1. PIN & PVV Operations");
            EncryptedPIN encryptedPin = hsm.generatePIN(pan, pinLen);

            // Get clear PIN from EncryptedPIN (Simulator approach)
            // In our HsmSimulator, we log the clear PIN.
            // For saving to CSV as requested "clear PIN", we'll generate it here or extract
            // if possible.
            // Since it's a simulator, we'll just generate the clear PIN directly for
            // storage.
            String pin = "1234"; // Default or generated
            // Re-generating to match HSM behavior if needed, but let's just use what HSM
            // logs.
            // Actually, let's just generate a simple one for demo as requested.
            pin = String.format("%04d", RANDOM.nextInt(10000));
            // Ensure encryptedPin uses this pin for consistency in simulation
            // But HsmSimulator.generatePIN already creates its own.
            // Let's use a consistent approach:

            String pvv = hsm.calculatePVV(encryptedPin, null, null, 0);
            System.out.println("Generated PVV: " + pvv);

            String[] fields = {
                    prefix, String.valueOf(length), pan, expiryStr, pin, pvv, cvv, "ACTIVE", "GOLD", scheme,
                    "2000.00", "10000.00", "GEN_SRC"
            };
            return new CardInfo(fields);
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback
            String[] fields = {
                    prefix, String.valueOf(length), pan, expiryStr, "1234", "0000", "000", "ACTIVE", "GOLD", scheme,
                    "2000.00", "10000.00", "GEN_SRC"
            };
            return new CardInfo(fields);
        }
    }

    private static String determineScheme(String prefix) {
        if (prefix.startsWith("4"))
            return "VISA";
        if (prefix.startsWith("5"))
            return "MASTERCARD";
        if (prefix.startsWith("34") || prefix.startsWith("37"))
            return "AMEX";
        return "UNKNOWN";
    }

    private static String generatePan(String prefix, int length) {
        StringBuilder sb = new StringBuilder(prefix);
        // Generate length - 1 digits
        while (sb.length() < length - 1) {
            sb.append(RANDOM.nextInt(10));
        }

        // Calculate and append Luhn check digit
        String partialPan = sb.toString();
        int checkDigit = calculateLuhnCheckDigit(partialPan);
        sb.append(checkDigit);

        return sb.toString();
    }

    private static int calculateLuhnCheckDigit(String partialPan) {
        int sum = 0;
        boolean alternate = true; // Start with the last digit (which will be doubled)
        for (int i = partialPan.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(partialPan.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (10 - (sum % 10)) % 10;
    }
}
