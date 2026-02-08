package com.jpos.simulator.security;

import org.jpos.security.SMException;
import org.jpos.security.SecureDESKey;
import org.jpos.security.SecureKey;
import org.jpos.security.BaseSMAdapter;
import org.jpos.security.EncryptedPIN;
import org.jpos.iso.ISOUtil;

import java.security.SecureRandom;
import java.util.List;

import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A software-based HSM Simulator extending jPOS BaseSMAdapter.
 * Simulates basic cryptographic operations like PIN generation.
 */
public class HsmSimulator extends BaseSMAdapter<SecureKey> {

    private SecureRandom random = new SecureRandom();
    private Logger logger;
    private String realm;

    public HsmSimulator() {
        super();
    }

    @Override
    protected EncryptedPIN generatePINImpl(String accountNumber, int pinLen, List<String> excludes) throws SMException {
        StringBuilder pin = new StringBuilder();
        for (int i = 0; i < pinLen; i++) {
            pin.append(random.nextInt(10));
        }
        String pinStr = pin.toString();

        // Generate ISO-0 PIN Block
        byte[] pinBlock = generateISO0PinBlock(pinStr, accountNumber);

        log("generate-pin", "Generated Clear PIN [" + pinStr + "] for account [" + accountNumber + "]");
        log("generate-pin", "Generated ISO-0 PIN Block (HEX) [" + ISOUtil.hexString(pinBlock) + "]");

        return new EncryptedPIN(pinBlock, (byte) 1, accountNumber);
    }

    public byte[] generateISO0PinBlock(String pin, String pan) {
        // PIN format: 0 + L + PIN + F...F
        String pinBlockStr = "0" + pin.length() + pin;
        while (pinBlockStr.length() < 16) {
            pinBlockStr += "F";
        }

        // PAN format: 0000 + last 12 digits excluding check digit
        String panPart = getPan4Pin(pan);
        String panBlockStr = "0000" + panPart;

        return ISOUtil.xor(
                ISOUtil.hex2byte(pinBlockStr),
                ISOUtil.hex2byte(panBlockStr));
    }

    /**
     * Extracts the 12 digits used for PIN block generation (ISO-0/ANSI X9.8)
     * Usually the 12 digits before the check digit.
     */
    private String getPan4Pin(String pan) {
        if (pan == null || pan.length() < 13) {
            try {
                return ISOUtil.zeropad(pan != null ? pan : "", 12);
            } catch (org.jpos.iso.ISOException e) {
                return "000000000000";
            }
        }
        return pan.substring(pan.length() - 13, pan.length() - 1);
    }

    public boolean verifyPVV(EncryptedPIN pin, SecureKey kd, SecureKey pvk1, SecureKey pvk2, int pvki,
            String pvv) throws SMException {
        return verifyPVVImpl(pin, kd, pvk1, pvk2, pvki, pvv);
    }

    public boolean verifyCVV(String pan, SecureKey cvk1, SecureKey cvk2, String serviceCode, String expiry, String cvv)
            throws SMException {
        try {
            // expiry should be in yyMM format
            SimpleDateFormat sdf = new SimpleDateFormat("yyMM");
            Date expiryDate = sdf.parse(expiry);
            return verifyCVVImpl(pan, cvk1, cvk2, serviceCode, expiryDate, cvv);
        } catch (java.text.ParseException e) {
            throw new SMException("Invalid expiry date format: " + expiry, e);
        }
    }

    @Override
    protected String calculatePVVImpl(EncryptedPIN pin, SecureKey pvk1, SecureKey pvk2, int pvki,
            List<String> excludes) throws SMException {
        return calculatePVVImpl(pin, null, pvk1, pvk2, pvki, excludes);
    }

    @Override
    protected String calculatePVVImpl(EncryptedPIN pin, SecureKey kd, SecureKey pvk1, SecureKey pvk2, int pvki,
            List<String> excludes)
            throws SMException {
        try {
            return generatePVV(pin.getAccountNumber(), "1234");
        } catch (Exception e) {
            throw new SMException(e);
        }
    }

    @Override
    protected boolean verifyPVVImpl(EncryptedPIN pin, SecureKey kd, SecureKey pvk1, SecureKey pvk2, int pvki,
            String pvv)
            throws SMException {
        String calculatedPvv = calculatePVVImpl(pin, kd, pvk1, pvk2, pvki, null);
        boolean isValid = calculatedPvv.equals(pvv);
        log("verify-pvv",
                "PVV Verification for account [" + pin.getAccountNumber() + "]: " + (isValid ? "SUCCESS" : "FAILED"));
        return isValid;
    }

    @Override
    protected EncryptedPIN translatePINImpl(EncryptedPIN pin, SecureKey kd1, SecureKey kd2, byte destinationFormat)
            throws SMException {
        // Mock translation: In a real HSM, we decrypt with kd1 and encrypt with kd2
        log("translate-pin", "Translating PIN block for account [" + pin.getAccountNumber() + "] to format ["
                + destinationFormat + "]");
        return new EncryptedPIN(pin.getPINBlock(), destinationFormat, pin.getAccountNumber());
    }

    public String generatePVV(String accountNumber, String pin) {
        String data = accountNumber.substring(Math.max(0, accountNumber.length() - 5)) + pin;
        String pvv = String.format("%04d", Math.abs(data.hashCode()) % 10000);
        log("generate-pvv", "Generated PVV [" + pvv + "] for account [" + accountNumber + "]");
        return pvv;
    }

    @Override
    public SecureDESKey formKEYfromClearComponents(short keyLength, String keyType, String... components)
            throws SMException {
        SecureDESKey key = new SecureDESKey();
        key.setKeyType(keyType);
        key.setKeyLength(keyLength);
        byte[] keyBytes = new byte[keyLength / 8];
        random.nextBytes(keyBytes);
        key.setKeyBytes(keyBytes);
        log("form-key-from-components", "Formed Key type [" + keyType + "] length [" + keyLength + "]");
        return key;
    }

    @Override
    protected SecureDESKey generateKeyImpl(short keyLength, String keyType) throws SMException {
        SecureDESKey key = new SecureDESKey();
        key.setKeyType(keyType);
        key.setKeyLength(keyLength);
        byte[] keyBytes = new byte[keyLength / 8];
        random.nextBytes(keyBytes);
        key.setKeyBytes(keyBytes);
        log("generate-key", "Generated Key type [" + keyType + "] length [" + keyLength + "]");
        return key;
    }

    public String calculateKCV(SecureKey key) throws SMException {
        // Mock: First 6 hex digits of the key hash
        String kcv = ISOUtil.byte2hex(new byte[] { 0x11, 0x22, 0x33 }); // Mocked
        log("calculate-kcv", "Calculated KCV [" + kcv + "] for key type [" + key.getKeyType() + "]");
        return kcv;
    }

    @Override
    protected String calculateCVVImpl(String pan, SecureKey cvk1, SecureKey cvk2, Date expiry,
            String serviceCode) throws SMException {
        // Mock CVV: Last 3 digits of (pan + serviceCode) hash
        String expiryStr = new SimpleDateFormat("yyMM").format(expiry);
        String data = pan + expiryStr + serviceCode;
        String cvv = String.format("%03d", Math.abs(data.hashCode()) % 1000);
        log("generate-cvv", "Generated CVV [" + cvv + "] for PAN [" + pan + "]");
        return cvv;
    }

    @Override
    protected boolean verifyCVVImpl(String pan, SecureKey cvk1, SecureKey cvk2, String serviceCode,
            Date expiry, String cvv) throws SMException {
        String calculatedCvv = calculateCVVImpl(pan, cvk1, cvk2, expiry, serviceCode);
        boolean isValid = calculatedCvv.equals(cvv);
        log("verify-cvv", "CVV Verification for PAN [" + pan + "]: " + (isValid ? "SUCCESS" : "FAILED"));
        return isValid;
    }

    // MAC generation could be added here if the correct signature is verified.

    // LogSource implementation
    @Override
    public void setLogger(Logger logger, String realm) {
        this.logger = logger;
        this.realm = realm;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    private void log(String tag, String message) {
        if (logger != null) {
            LogEvent ev = new LogEvent(this, tag, message);
            Logger.log(ev);
        } else {
            System.out.println("HSM [" + tag + "]: " + message);
        }
    }
}
