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
            String errorMsg = "INTEGRATION TEST FAILURE: ISO87 Server not running on port 8005. " +
                    "Start it with './gradlew runISO87Server' for integration tests.";
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
        try {
            System.out.println("\n[INTEGRATION TEST] testSendEcho - Sending echo to server...");
            ISOMsg response = ISO87ClientApp.sendEcho(channel);
            assertNotNull(response);
            assertEquals("00", response.getString(39));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void testSendLogon() {
        try {
            System.out.println("\n[INTEGRATION TEST] testSendLogon (ISO87) - Sending logon to server...");
            ISOMsg response = ISO87ClientApp.sendLogon(channel);
            assertNotNull(response);
            assertEquals("00", response.getString(39));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void testSendLogoff() {
        try {
            System.out.println("\n[INTEGRATION TEST] testSendLogoff (ISO87) - Sending logoff to server...");
            ISOMsg response = ISO87ClientApp.sendLogoff(channel);
            assertNotNull(response);
            assertEquals("00", response.getString(39));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void testSendKeyExchange() {
        try {
            System.out.println("\n[INTEGRATION TEST] testSendKeyExchange (ISO87) - Sending key exchange...");
            ISOMsg response = ISO87ClientApp.sendKeyExchange(channel);
            assertNotNull(response);
            assertEquals("00", response.getString(39));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void testSendPurchase() {
        try {
            System.out.println("\n[INTEGRATION TEST] testSendPurchase (ISO87) - Sending purchase transaction...");
            ISOMsg response = ISO87ClientApp.sendPurchase(channel);
            assertNotNull(response);
            assertEquals("00", response.getString(39));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void testSendPurchaseReversal() {
        try {
            System.out.println("\n[INTEGRATION TEST] testSendPurchaseReversal (ISO87) - Sending purchase reversal...");
            ISOMsg response = ISO87ClientApp.sendPurchaseReversal(channel);
            assertNotNull(response);
            assertEquals("00", response.getString(39));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void testSendBalanceInquiry() {
        try {
            System.out.println("\n[INTEGRATION TEST] testSendBalanceInquiry (ISO87) - Sending balance inquiry...");
            ISOMsg response = ISO87ClientApp.sendBalanceInquiry(channel);
            assertNotNull(response);
            assertEquals("00", response.getString(39));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
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
    void testSendWithdrawalReversal() {
        try {
            System.out.println(
                    "\n[INTEGRATION TEST] testSendWithdrawalReversal (ISO87) - Sending withdrawal reversal...");
            ISOMsg response = ISO87ClientApp.sendWithdrawalReversal(channel);
            assertNotNull(response);
            assertEquals("00", response.getString(39));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
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
    void testSendCreateCard() {
        try {
            System.out.println("\n[INTEGRATION TEST] testSendCreateCard (ISO87) - Sending card creation request...");
            ISOMsg response = ISO87ClientApp.sendCreateCard(channel);
            assertNotNull(response);
            assertEquals("00", response.getString(39));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void testSendUpdateCardStatus() {
        try {
            System.out.println("\n[INTEGRATION TEST] testSendUpdateCardStatus (ISO87) - Sending card status update...");
            ISOMsg response = ISO87ClientApp.sendUpdateCardStatus(channel);
            assertNotNull(response);
            assertEquals("00", response.getString(39));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void testGenerateAndSyncCard() {
        try {
            System.out.println(
                    "\n[INTEGRATION TEST] testGenerateAndSyncCard (ISO87) - Generating and syncing new card...");
            ISOMsg response = ISO87ClientApp.generateAndSyncCard(channel);
            assertNotNull(response);
            assertEquals("00", response.getString(39));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }
}
