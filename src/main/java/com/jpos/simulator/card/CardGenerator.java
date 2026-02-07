package com.jpos.simulator.card;

import java.util.Random;

public class CardGenerator {
    private static final Random RANDOM = new Random();

    public static CardInfo generateCard(String prefix, int length) {
        String scheme = determineScheme(prefix);
        String pan = generatePan(prefix, length);
        String expiry = "2912"; // Dummy future expiry
        String pin = String.format("%04d", RANDOM.nextInt(10000));
        String pvv = String.format("%04d", RANDOM.nextInt(10000));
        String bin = pan.substring(0, Math.min(pan.length(), 6));

        String[] fields = {
                prefix, String.valueOf(length), pan, expiry, pin, pvv, "ACTIVE", "GOLD", scheme,
                "2000.00", "10000.00", "GEN_SRC"
        };
        return new CardInfo(fields);
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
        while (sb.length() < length) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
