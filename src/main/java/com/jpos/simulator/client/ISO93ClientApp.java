package com.jpos.simulator.client;

import com.jpos.simulator.core.ChannelFactory;
import com.jpos.simulator.core.ISOTransaction;
import com.jpos.simulator.client.base.ISO93Messages;
import com.jpos.simulator.card.CardInfo;
import com.jpos.simulator.card.CardDataLoader;
import com.jpos.simulator.card.CardGenerator;

import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;

import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

import java.util.LinkedHashMap;
import java.util.Map;

public class ISO93ClientApp extends ISO93Messages {

    public static void main(String[] args) {
        try {
            Logger logger = new Logger();
            logger.addListener(new SimpleLogListener(System.out));

            String configPath = "src/main/resources/xml/channel/iso93/client.xml";
            ISOChannel channel = ChannelFactory.createChannel(configPath);

            if (channel instanceof org.jpos.util.LogSource) {
                ((org.jpos.util.LogSource) channel).setLogger(logger, "iso93-client-channel");
            }

            Map<String, ISOTransaction> menu = new LinkedHashMap<>();
            // Network Management
            menu.put("Send Echo Request (1804/801)", ISO93ClientApp::sendEcho);
            menu.put("Send Logon Request (1804/001)", ISO93ClientApp::sendLogon);
            menu.put("Send Logoff Request (1804/002)", ISO93ClientApp::sendLogoff);
            menu.put("Send Key Exchange (1804/161)", ISO93ClientApp::sendKeyExchange);

            // File Action
            menu.put("Send Create Card (1304/1)", ISO93ClientApp::sendCreateCard);
            menu.put("Send Update Card Status (1304/2)", ISO93ClientApp::sendUpdateCardStatus);
            menu.put("Generate & Sync New Card (1304)", ISO93ClientApp::generateAndSyncCard);

            // Financial
            menu.put("Send Pre-Authorization (1100/100)", ISO93ClientApp::sendPreAuth);
            menu.put("Send Balance Inquiry (1100/31)", ISO93ClientApp::sendBalanceInquiry);
            menu.put("Send Purchase (1100/100)", ISO93ClientApp::sendPurchase);
            menu.put("Send Purchase Reversal (1420/400)", ISO93ClientApp::sendPurchaseReversal);
            menu.put("Send Withdrawal (1100/01)", ISO93ClientApp::sendWithdrawal);
            menu.put("Send Withdrawal Reversal (1420/400)", ISO93ClientApp::sendWithdrawalReversal);
            menu.put("Send Refund (1100/200)", ISO93ClientApp::sendRefund);

            try (Scanner scanner = new Scanner(System.in)) {
                boolean running = true;
                while (running) {
                    System.out.println("\n--- ISO93 Simulator Client (Binary) ---");
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

    private static CardInfo ensureCardSelectedInfo() {
        java.util.List<CardInfo> cards = CardDataLoader.loadCards("src/main/resources/data/cards.csv");
        for (CardInfo card : cards) {
            if (card.isSelected()) {
                return card;
            }
        }
        return !cards.isEmpty() ? cards.get(0) : null;
    }

    private static String ensureCardSelected() {
        CardInfo card = ensureCardSelectedInfo();
        return card != null ? card.getPan() : "1234567890123456";
    }

    // Helper for transmission
    private static void transmit(ISOChannel channel, ISOMsg m, String name) throws ISOException, IOException {
        System.out.println("Sending " + name + " [" + m.getMTI() + "]...");
        channel.send(m);
        ISOMsg r = channel.receive();
        System.out.println("Received " + name + " response [" + r.getMTI() + "] (F39=" + r.getString(39) + ")");
    }

    // Network Management Methods
    public static ISOMsg createEchoMsg() throws ISOException {
        return createNetMgmtBase("801");
    }

    public static void sendEcho(ISOChannel channel) throws ISOException, IOException {
        transmit(channel, createEchoMsg(), "Echo");
    }

    public static ISOMsg createLogonMsg() throws ISOException {
        return createNetMgmtBase("001");
    }

    public static void sendLogon(ISOChannel channel) throws ISOException, IOException {
        transmit(channel, createLogonMsg(), "Logon");
    }

    public static ISOMsg createLogoffMsg() throws ISOException {
        return createNetMgmtBase("002");
    }

    public static void sendLogoff(ISOChannel channel) throws ISOException, IOException {
        transmit(channel, createLogoffMsg(), "Logoff");
    }

    public static ISOMsg createKeyExchangeMsg() throws ISOException {
        ISOMsg m = createNetMgmtBase("161");
        m.set(48, "1234567890ABCDEF1234567890ABCDEF"); // Dummy key data
        return m;
    }

    public static void sendKeyExchange(ISOChannel channel) throws ISOException, IOException {
        transmit(channel, createKeyExchangeMsg(), "Key Exchange");
    }

    // Financial Methods
    public static ISOMsg createPurchaseMsg() throws ISOException {
        String pan = ensureCardSelected();
        ISOMsg m = createFinancialBase("1100", "000000", "100", pan);
        m.set(4, "000000001000"); // 10.00
        return m;
    }

    public static ISOMsg sendPurchase(ISOChannel channel) throws ISOException, IOException {
        ISOMsg m = createPurchaseMsg();
        transmit(channel, m, "Purchase");
        return m;
    }

    public static ISOMsg createPurchaseReversalMsg(ISOMsg original) throws ISOException {
        String pan = original.getString(2);
        String rrn = original.getString(37);
        return createReversalBase("1420", "000000", "400", pan, original.getString(4), rrn, original);
    }

    public static void sendPurchaseReversal(ISOChannel channel) throws ISOException, IOException {
        ISOMsg original = sendPurchase(channel);
        transmit(channel, createPurchaseReversalMsg(original), "Purchase Reversal");
    }

    public static ISOMsg createBalanceInquiryMsg() throws ISOException {
        String pan = ensureCardSelected();
        return createFinancialBase("1100", "310000", "100", pan);
    }

    public static void sendBalanceInquiry(ISOChannel channel) throws ISOException, IOException {
        transmit(channel, createBalanceInquiryMsg(), "Balance Inquiry");
    }

    public static ISOMsg createWithdrawalMsg() throws ISOException {
        String pan = ensureCardSelected();
        ISOMsg m = createFinancialBase("1100", "010000", "100", pan);
        m.set(4, "000000002000"); // 20.00
        return m;
    }

    public static ISOMsg sendWithdrawal(ISOChannel channel) throws ISOException, IOException {
        ISOMsg m = createWithdrawalMsg();
        transmit(channel, m, "Withdrawal");
        return m;
    }

    public static ISOMsg createWithdrawalReversalMsg(ISOMsg original) throws ISOException {
        String pan = original.getString(2);
        String rrn = original.getString(37);
        return createReversalBase("1420", "010000", "400", pan, original.getString(4), rrn, original);
    }

    public static void sendWithdrawalReversal(ISOChannel channel) throws ISOException, IOException {
        ISOMsg original = sendWithdrawal(channel);
        transmit(channel, createWithdrawalReversalMsg(original), "Withdrawal Reversal");
    }

    public static ISOMsg createPreAuthMsg() throws ISOException {
        String pan = ensureCardSelected();
        ISOMsg m = createFinancialBase("1100", "000000", "104", pan);
        m.set(4, "000000005000"); // 50.00
        return m;
    }

    public static void sendPreAuth(ISOChannel channel) throws ISOException, IOException {
        transmit(channel, createPreAuthMsg(), "Pre-Auth");
    }

    public static ISOMsg createRefundMsg() throws ISOException {
        String pan = ensureCardSelected();
        ISOMsg m = createFinancialBase("1100", "200000", "200", pan);
        m.set(4, "000000001500"); // 15.00
        return m;
    }

    public static void sendRefund(ISOChannel channel) throws ISOException, IOException {
        transmit(channel, createRefundMsg(), "Refund");
    }

    // File Action Methods
    public static ISOMsg createCreateCardMsg() throws ISOException {
        CardInfo card = ensureCardSelectedInfo();
        String pan = card != null ? card.getPan() : "1234567890123456";
        String exp = card != null ? card.getExpiry() : "1230";
        ISOMsg m = createFileActionBase("1"); // 1 = Add
        m.set(48, String.format("PAN=%s|EXP=%s|SVC=101", pan, exp));
        return m;
    }

    public static void sendCreateCard(ISOChannel channel) throws ISOException, IOException {
        transmit(channel, createCreateCardMsg(), "Create Card");
    }

    public static ISOMsg createUpdateCardStatusMsg() throws ISOException {
        CardInfo card = ensureCardSelectedInfo();
        String pan = card != null ? card.getPan() : "1234567890123456";
        ISOMsg m = createFileActionBase("2"); // 2 = Change
        m.set(48, String.format("PAN=%s|STATUS=BLOCKED", pan));
        return m;
    }

    public static void sendUpdateCardStatus(ISOChannel channel) throws ISOException, IOException {
        ISOMsg m = createUpdateCardStatusMsg();
        transmit(channel, m, "Update Card Status");

        // Persist change to CSV
        CardInfo card = ensureCardSelectedInfo();
        if (card != null) {
            System.out.println("Updating card status to BLOCKED in CSV...");
            CardDataLoader.updateCardStatus("src/main/resources/data/cards.csv", card.getPan(), "BLOCKED");
        }
    }

    public static ISOMsg createSyncCardMsg(CardInfo card) throws ISOException {
        ISOMsg m = createFileActionBase("1"); // Create
        m.set(2, card.getPan());
        m.set(14, card.getExpiry());
        m.set(41, card.getSourceId());

        String cardData = String.format("SCHEME=%s|LIMIT_TXN=%s|LIMIT_DAILY=%s|BIN=%s",
                card.getScheme(), card.getPerTxLimit(), card.getDailyLimit(), card.getPrefix());
        m.set(48, cardData);
        return m;
    }

    public static void generateAndSyncCard(ISOChannel channel) throws ISOException, IOException {
        String csvPath = "src/main/resources/data/cards.csv";
        String prefix = "541234"; // Default MasterCard prefix
        int length = 16;

        CardInfo card = CardGenerator.generateCard(prefix, length);
        System.out.println("Generated New Card: " + card.toString());

        transmit(channel, createSyncCardMsg(card), "Sync Generated Card");

        System.out.println("Saving generated card to CSV...");
        CardDataLoader.appendCard(csvPath, card);
    }

    // Keep legacy names for existing tests
    public static ISOMsg createNetworkManagementMsg() throws ISOException {
        return createEchoMsg();
    }

    public static ISOMsg createFinancialTransactionMsg() throws ISOException {
        return createPurchaseMsg();
    }
}
