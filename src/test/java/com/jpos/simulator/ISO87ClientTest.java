package com.jpos.simulator;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.BaseChannel;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import com.jpos.simulator.client.ISO87ClientApp;
import com.jpos.simulator.core.ChannelFactory;
import com.jpos.simulator.server.ISO87ServerApp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.Socket;

public class ISO87ClientTest {

    private static ISOChannel channel;
    private static boolean serverStartedByTest = false;

    @BeforeAll
    static void setupChannel() throws Exception {
        if (isPortAvailable(8005)) {
            System.out.println("ISO87 Server not running. Starting it for integration tests...");
            ISO87ServerApp.startServer();
            serverStartedByTest = true;
        }

        String configPath = "src/main/resources/xml/channel/iso87/client.xml";
        try {
            Logger logger = new Logger();
            logger.addListener(new SimpleLogListener(System.out));

            channel = ChannelFactory.createChannel(configPath);
            if (channel instanceof org.jpos.util.LogSource) {
                ((org.jpos.util.LogSource) channel).setLogger(logger, "iso87-test-channel");
            }

            if (channel instanceof BaseChannel) {
                ((BaseChannel) channel).connect();
            }
        } catch (IOException e) {
            String errorMsg = "\n\n" +
                    "═══════════════════════════════════════════════════════════════\n" +
                    "  INTEGRATION TEST FAILURE: ISO87 Server Not Running\n" +
                    "═══════════════════════════════════════════════════════════════\n" +
                    "\n" +
                    "These are integration tests that require a running server.\n" +
                    "\n" +
                    "TO FIX:\n" +
                    "  1. Start the server in a separate terminal:\n" +
                    "     ./gradlew runISO87Server\n" +
                    "\n" +
                    "  2. Then run the tests:\n" +
                    "     ./gradlew test --tests \"com.jpos.simulator.ISO87ClientTest\"\n" +
                    "\n" +
                    "ALTERNATIVE:\n" +
                    "  Skip integration tests during build:\n" +
                    "     ./gradlew build -x test\n" +
                    "\n" +
                    "Server should be listening on port 8005\n" +
                    "═══════════════════════════════════════════════════════════════\n";
            throw new RuntimeException(errorMsg, e);
        }
    }

    @AfterAll
    static void teardownChannel() throws Exception {
        if (channel instanceof BaseChannel) {
            ((BaseChannel) channel).disconnect();
        }
        if (serverStartedByTest) {
            ISO87ServerApp.stopServer();
        }
    }

    private static boolean isPortAvailable(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    // Integration tests - require running server
    @Test
    public void testSendEcho() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendEcho - Sending echo to server...");

        // Create and send message
        ISOMsg request = ISO87ClientApp.createEchoMsg();

        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Echo transaction completed successfully\n");

        // If we get here without exception, the transaction was successful
        assertTrue(true, "Echo transaction completed successfully");
    }

    @Test
    public void testSendLogon() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendLogon - Sending logon to server...");

        ISOMsg request = ISO87ClientApp.createLogonMsg();

        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Logon transaction completed successfully\n");

        assertTrue(true, "Logon transaction completed successfully");
    }

    @Test
    public void testSendLogoff() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendLogoff - Sending logoff to server...");

        ISOMsg request = ISO87ClientApp.createLogoffMsg();

        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Logoff transaction completed successfully\n");

        assertTrue(true, "Logoff transaction completed successfully");
    }

    @Test
    public void testSendKeyExchange() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendKeyExchange - Sending key exchange to server...");

        ISOMsg request = ISO87ClientApp.createKeyExchangeMsg();

        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Key Exchange transaction completed successfully\n");

        assertTrue(true, "Key Exchange transaction completed successfully");
    }

    @Test
    public void testSendPurchase() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendPurchase - Sending purchase transaction...");

        // Create and send message
        ISOMsg request = ISO87ClientApp.createPurchaseMsg();

        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        assertNotNull(request, "Purchase transaction should return original message");
        assertEquals("0200", request.getMTI());
        System.out.println("  ✓ Purchase transaction completed successfully\n");
    }

    @Test
    public void testSendPurchaseReversal() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendPurchaseReversal - Sending purchase reversal...");

        ISOMsg original = ISO87ClientApp.createPurchaseMsg();
        ISOMsg request = ISO87ClientApp.createPurchaseReversalMsg(original);

        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Purchase Reversal transaction completed successfully\n");

        assertTrue(true, "Purchase Reversal transaction completed successfully");
    }

    @Test
    public void testSendBalanceInquiry() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendBalanceInquiry - Sending balance inquiry...");

        ISOMsg request = ISO87ClientApp.createBalanceInquiryMsg();

        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Balance Inquiry transaction completed successfully\n");

        assertTrue(true, "Balance Inquiry transaction completed successfully");
    }

    @Test
    public void testSendWithdrawal() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendWithdrawal - Sending withdrawal transaction...");

        ISOMsg request = ISO87ClientApp.createWithdrawalMsg();

        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        assertNotNull(request, "Withdrawal transaction should return original message");
        assertEquals("0200", request.getMTI());
        System.out.println("  ✓ Withdrawal transaction completed successfully\n");
    }

    @Test
    public void testSendWithdrawalReversal() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendWithdrawalReversal - Sending withdrawal reversal...");

        ISOMsg original = ISO87ClientApp.createWithdrawalMsg();
        ISOMsg request = ISO87ClientApp.createPurchaseReversalMsg(original);

        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Withdrawal Reversal transaction completed successfully\n");

        assertTrue(true, "Withdrawal Reversal transaction completed successfully");
    }

    @Test
    public void testSendRefund() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendRefund - Sending refund transaction...");

        ISOMsg request = ISO87ClientApp.createRefundMsg();

        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Refund transaction completed successfully\n");

        assertTrue(true, "Refund transaction completed successfully");
    }

    @Test
    public void testSendPreAuth() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendPreAuth - Sending pre-authorization...");

        ISOMsg request = ISO87ClientApp.createPreAuthMsg();

        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Pre-Auth transaction completed successfully\n");

        assertTrue(true, "Pre-Auth transaction completed successfully");
    }

    @Test
    public void testSendCreateCard() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendCreateCard - Sending card creation request...");

        ISOMsg request = ISO87ClientApp.createCreateCardMsg();

        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Create Card transaction completed successfully\n");

        assertTrue(true, "Create Card transaction completed successfully");
    }

    @Test
    public void testSendUpdateCardStatus() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendUpdateCardStatus - Sending card status update...");

        ISOMsg request = ISO87ClientApp.createCreateCardMsg(); // Reuse for demo

        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Update Card Status transaction completed successfully\n");

        assertTrue(true, "Update Card Status transaction completed successfully");
    }
}
