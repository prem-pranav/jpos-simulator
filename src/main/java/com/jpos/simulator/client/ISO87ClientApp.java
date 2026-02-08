package com.jpos.simulator.client;

import com.jpos.simulator.core.ChannelFactory;
import com.jpos.simulator.core.ISOTransaction;
import com.jpos.simulator.client.base.ISO87Messages;
import com.jpos.simulator.card.CardInfo;
import com.jpos.simulator.card.CardDataLoader;
import com.jpos.simulator.card.CardGenerator;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;

import java.io.IOException;
import java.util.Scanner;

import java.util.LinkedHashMap;
import java.util.Map;

import com.jpos.simulator.security.HsmSimulator;

public class ISO87ClientApp extends ISO87Messages {
    private static final HsmSimulator hsm = new HsmSimulator();

    public static void main(String[] args) {
        try {
            Logger logger = new Logger();
            logger.addListener(new SimpleLogListener(System.out));

            java.io.File logDir = new java.io.File("log");
            if (!logDir.exists())
                logDir.mkdirs();
            logger.addListener(new SimpleLogListener(
                    new java.io.PrintStream(new java.io.FileOutputStream("log/ISO87CLIENT.log", true))));

            String configPath = "src/main/resources/xml/channel/iso87/client.xml";
            ISOChannel channel = ChannelFactory.createChannel(configPath);

            if (channel instanceof org.jpos.util.LogSource) {
                ((org.jpos.util.LogSource) channel).setLogger(logger, "iso87-client-channel");
            }

            Map<String, ISOTransaction> menu = new LinkedHashMap<>();
            // Network Management
            menu.put("Send Echo Request (0800/301)", ISO87ClientApp::sendEcho);
            menu.put("Send Logon Request (0800/001)", ISO87ClientApp::sendLogon);
            menu.put("Send Logoff Request (0800/002)", ISO87ClientApp::sendLogoff);
            menu.put("Send Key Exchange (0800/161)", ISO87ClientApp::sendKeyExchange);

            // File Action
            menu.put("Send Create Card (0300/1)", ISO87ClientApp::sendCreateCard);
            menu.put("Send Update Card Status (0300/2)", ISO87ClientApp::sendUpdateCardStatus);
            menu.put("Generate & Sync New Card (0300)", ISO87ClientApp::generateAndSyncCard);

            // Financial
            menu.put("Send Pre-Authorization (0100)", ISO87ClientApp::sendPreAuth);
            menu.put("Send Balance Inquiry (0200/31)", ISO87ClientApp::sendBalanceInquiry);
            menu.put("Send Purchase (0200)", ISO87ClientApp::sendPurchase);
            menu.put("Send Purchase Reversal (0420)", ISO87ClientApp::sendPurchaseReversal);
            menu.put("Send Withdrawal (0200/01)", ISO87ClientApp::sendWithdrawal);
            menu.put("Send Withdrawal Reversal (0420/01)", ISO87ClientApp::sendWithdrawalReversal);
            menu.put("Send Refund (0200/20)", ISO87ClientApp::sendRefund);

            try (Scanner scanner = new Scanner(System.in)) {
                boolean running = true;
                while (running) {
                    System.out.println("\n--- ISO87 Simulator Client ---");
                    int i = 1;
                    for (String desc : menu.keySet()) {
                        System.out.println(i++ + ". " + desc);
                    }
                    System.out.println(i + ". Exit");
                    System.out.print("Select an option: ");

                    String choiceStr = scanner.nextLine();
                    int choice;
                    try {
                        choice = Integer.parseInt(choiceStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                        continue;
                    }

                    if (choice == i) {
                        running = false;
                        if (channel instanceof org.jpos.iso.BaseChannel) {
                            ((org.jpos.iso.BaseChannel) channel).disconnect();
                        }
                        continue;
                    }

                    if (choice > 0 && choice < i) {
                        if (channel instanceof org.jpos.iso.BaseChannel
                                && !((org.jpos.iso.BaseChannel) channel).isConnected()) {
                            ((org.jpos.iso.BaseChannel) channel).connect();
                        }

                        // Get the function by index
                        String key = (String) menu.keySet().toArray()[choice - 1];
                        menu.get(key).execute(channel);
                    } else {
                        System.out.println("Invalid option.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper for transmission
    private static ISOMsg transmit(ISOChannel channel, ISOMsg m, String name) throws ISOException, IOException {
        System.out.println("Sending " + name + " [" + m.getMTI() + "]...");

        channel.send(m);
        ISOMsg r = channel.receive();

        System.out.println("Received " + name + " response [" + r.getMTI() + "] (F39=" + r.getString(39) + ")");
        return r;
    }

    // PIN / PVV helpers
    private static void addSecurityFields(ISOMsg m, CardInfo card) throws org.jpos.iso.ISOException {
        if (card != null) {
            byte[] pinBlock = hsm.generateISO0PinBlock(card.getPin(), card.getPan());
            m.set(52, pinBlock);
            m.set(44, card.getPvv());
            m.set(48, "CVV=" + card.getCvd());
        }
    }

    // Network Management Methods
    public static ISOMsg createEchoMsg() throws ISOException {
        return createNetMgmtBase("301");
    }

    public static ISOMsg sendEcho(ISOChannel channel) throws ISOException, IOException {
        return transmit(channel, createEchoMsg(), "Echo");
    }

    public static ISOMsg createLogonMsg() throws ISOException {
        return createNetMgmtBase("001");
    }

    public static ISOMsg sendLogon(ISOChannel channel) throws ISOException, IOException {
        return transmit(channel, createLogonMsg(), "Logon");
    }

    public static ISOMsg createLogoffMsg() throws ISOException {
        return createNetMgmtBase("002");
    }

    public static ISOMsg sendLogoff(ISOChannel channel) throws ISOException, IOException {
        return transmit(channel, createLogoffMsg(), "Logoff");
    }

    private static CardInfo ensureCardSelectedInfo() {
        java.util.List<CardInfo> cards = CardDataLoader.loadCards("src/main/resources/data/cards.csv");
        for (CardInfo card : cards) {
            if (card.isSelected()) {
                return card;
            }
        }
        return !cards.isEmpty() ? cards.get(0) : null;
    }

    public static ISOMsg createPurchaseMsg() throws ISOException {
        CardInfo card = ensureCardSelectedInfo();
        String pan = card != null ? card.getPan() : "1234567890123456";
        String expiry = card != null ? card.getExpiry() : "2912";
        ISOMsg m = createFinancialBase("0200", "000000", pan, expiry);
        m.set(4, "000000001000"); // 10.00
        addSecurityFields(m, card);
        return m;
    }

    public static ISOMsg sendPurchase(ISOChannel channel) throws ISOException, IOException {
        ISOMsg m = createPurchaseMsg();
        return transmit(channel, m, "Purchase");
    }

    public static ISOMsg createPurchaseReversalMsg(ISOMsg original) throws ISOException {
        String pan = original.getString(2);
        String rrn = original.getString(37);
        return createReversalBase("0420", "000000", pan, original.getString(4), rrn, original);
    }

    public static ISOMsg sendPurchaseReversal(ISOChannel channel) throws ISOException, IOException {
        ISOMsg original = sendPurchase(channel);
        return transmit(channel, createPurchaseReversalMsg(original), "Purchase Reversal");
    }

    public static ISOMsg createBalanceInquiryMsg() throws ISOException {
        CardInfo card = ensureCardSelectedInfo();
        String pan = card != null ? card.getPan() : "1234567890123456";
        String expiry = card != null ? card.getExpiry() : "2912";
        ISOMsg m = createFinancialBase("0200", "310000", pan, expiry);
        addSecurityFields(m, card);
        return m;
    }

    public static ISOMsg sendBalanceInquiry(ISOChannel channel) throws ISOException, IOException {
        return transmit(channel, createBalanceInquiryMsg(), "Balance Inquiry");
    }

    public static ISOMsg createWithdrawalMsg() throws ISOException {
        CardInfo card = ensureCardSelectedInfo();
        String pan = card != null ? card.getPan() : "1234567890123456";
        String expiry = card != null ? card.getExpiry() : "2912";
        ISOMsg m = createFinancialBase("0200", "010000", pan, expiry);
        m.set(4, "000000002000"); // 20.00
        addSecurityFields(m, card);
        return m;
    }

    public static ISOMsg sendWithdrawal(ISOChannel channel) throws ISOException, IOException {
        ISOMsg m = createWithdrawalMsg();
        return transmit(channel, m, "Withdrawal");
    }

    public static ISOMsg createWithdrawalReversalMsg(ISOMsg original) throws ISOException {
        String pan = original.getString(2);
        String rrn = original.getString(37);
        return createReversalBase("0420", "010000", pan, original.getString(4), rrn, original);
    }

    public static ISOMsg sendWithdrawalReversal(ISOChannel channel) throws ISOException, IOException {
        ISOMsg original = sendWithdrawal(channel);
        return transmit(channel, createWithdrawalReversalMsg(original), "Withdrawal Reversal");
    }

    public static ISOMsg createRefundMsg() throws ISOException {
        CardInfo card = ensureCardSelectedInfo();
        String pan = card != null ? card.getPan() : "1234567890123456";
        String expiry = card != null ? card.getExpiry() : "2912";
        ISOMsg m = createFinancialBase("0200", "200000", pan, expiry);
        m.set(4, "000000001500"); // 15.00
        addSecurityFields(m, card);
        return m;
    }

    public static ISOMsg sendRefund(ISOChannel channel) throws ISOException, IOException {
        return transmit(channel, createRefundMsg(), "Refund");
    }

    public static ISOMsg createPreAuthMsg() throws ISOException {
        CardInfo card = ensureCardSelectedInfo();
        String pan = card != null ? card.getPan() : "1234567890123456";
        String expiry = card != null ? card.getExpiry() : "2912";
        ISOMsg m = createFinancialBase("0100", "000000", pan, expiry);
        m.set(4, "000000005000"); // 50.00
        addSecurityFields(m, card);
        return m;
    }

    public static ISOMsg sendPreAuth(ISOChannel channel) throws ISOException, IOException {
        return transmit(channel, createPreAuthMsg(), "Pre-Auth");
    }

    public static ISOMsg createKeyExchangeMsg() throws ISOException {
        ISOMsg m = createNetMgmtBase("161");
        m.set(48, "1234567890ABCDEF1234567890ABCDEF"); // Dummy key data
        return m;
    }

    public static ISOMsg sendKeyExchange(ISOChannel channel) throws ISOException, IOException {
        return transmit(channel, createKeyExchangeMsg(), "Key Exchange");
    }

    public static ISOMsg createCreateCardMsg() throws ISOException {
        CardInfo card = ensureCardSelectedInfo();
        String pan = card != null ? card.getPan() : "1234567890123456";
        String exp = card != null ? card.getExpiry() : "1230";
        ISOMsg m = createFileActionBase("1"); // 1 = Add
        m.set(48, String.format("PAN=%s|EXP=%s|SVC=101", pan, exp));
        return m;
    }

    public static ISOMsg sendCreateCard(ISOChannel channel) throws ISOException, IOException {
        return transmit(channel, createCreateCardMsg(), "Create Card");
    }

    public static ISOMsg createUpdateCardStatusMsg() throws ISOException {
        CardInfo card = ensureCardSelectedInfo();
        String pan = card != null ? card.getPan() : "1234567890123456";
        ISOMsg m = createFileActionBase("2"); // 2 = Change
        m.set(48, String.format("PAN=%s|STATUS=BLOCKED", pan));
        return m;
    }

    public static ISOMsg sendUpdateCardStatus(ISOChannel channel) throws ISOException, IOException {
        ISOMsg m = createUpdateCardStatusMsg();
        ISOMsg r = transmit(channel, m, "Update Card Status");

        // Persist change to CSV
        CardInfo card = ensureCardSelectedInfo();
        if (card != null) {
            System.out.println("Updating card status to BLOCKED in CSV...");
            CardDataLoader.updateCardStatus("src/main/resources/data/cards.csv", card.getPan(), "BLOCKED");
        }
        return r;
    }

    public static ISOMsg createSyncCardMsg(CardInfo card) throws ISOException {
        ISOMsg m = createFileActionBase("1"); // Create
        m.set(2, card.getPan());
        m.set(14, card.getExpiry());
        m.set(41, card.getSourceId());

        m.set(48, String.format("SCHEME=%s|LIMIT_TXN=%s|LIMIT_DAILY=%s|BIN=%s",
                card.getScheme(), card.getPerTxLimit(), card.getDailyLimit(), card.getPrefix()));
        return m;
    }

    public static ISOMsg generateAndSyncCard(ISOChannel channel) throws ISOException, IOException {
        String csvPath = "src/main/resources/data/cards.csv";
        String prefix = "453211"; // Default VISA prefix
        int length = 16;

        CardInfo card = CardGenerator.generateCard(prefix, length);
        System.out.println("Generated New Card: " + card.toString());

        ISOMsg r = transmit(channel, createSyncCardMsg(card), "Sync Generated Card");

        System.out.println("Saving generated card to CSV...");
        CardDataLoader.appendCard(csvPath, card);
        return r;
    }

}
