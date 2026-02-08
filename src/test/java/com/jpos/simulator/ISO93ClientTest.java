package com.jpos.simulator;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.BaseChannel;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;
import com.jpos.simulator.client.ISO93ClientApp;
import com.jpos.simulator.core.ChannelFactory;
import com.jpos.simulator.server.ISO93ServerApp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.Socket;

public class ISO93ClientTest {

    private static ISOChannel channel;
    private static boolean serverStartedByTest = false;

    @BeforeAll
    static void setupChannel() throws Exception {
        if (isPortAvailable(8006)) {
            System.out.println("ISO93 Server not running. Starting it for integration tests...");
            ISO93ServerApp.startServer();
            serverStartedByTest = true;
        }

        String configPath = "src/main/resources/xml/channel/iso93/client.xml";
        try {
            Logger logger = new Logger();
            logger.addListener(new SimpleLogListener(System.out));

            channel = ChannelFactory.createChannel(configPath);
            if (channel instanceof org.jpos.util.LogSource) {
                ((org.jpos.util.LogSource) channel).setLogger(logger, "iso93-test-channel");
            }

            if (channel instanceof BaseChannel) {
                ((BaseChannel) channel).connect();
            }
        } catch (IOException e) {
            String errorMsg = "INTEGRATION TEST FAILURE: ISO93 Server not running on port 8006. " +
                    "Start it with './gradlew runISO93Server' for integration tests.";
            throw new RuntimeException(errorMsg, e);
        }
    }

    @AfterAll
    static void teardownChannel() throws Exception {
        if (channel instanceof BaseChannel) {
            ((BaseChannel) channel).disconnect();
        }
        if (serverStartedByTest) {
            ISO93ServerApp.stopServer();
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
    void testSendEcho() {
        try {
            System.out.println("\n[INTEGRATION TEST] testSendEcho (ISO93) - Sending echo to server...");
            ISOMsg response = ISO93ClientApp.sendEcho(channel);
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
            System.out.println("\n[INTEGRATION TEST] testSendLogon (ISO93) - Sending logon to server...");
            ISOMsg response = ISO93ClientApp.sendLogon(channel);
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
            System.out.println("\n[INTEGRATION TEST] testSendLogoff (ISO93) - Sending logoff to server...");
            ISOMsg response = ISO93ClientApp.sendLogoff(channel);
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
            System.out.println("\n[INTEGRATION TEST] testSendKeyExchange (ISO93) - Sending key exchange...");
            ISOMsg response = ISO93ClientApp.sendKeyExchange(channel);
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
            System.out.println("\n[INTEGRATION TEST] testSendPurchase (ISO93) - Sending purchase transaction...");
            ISOMsg response = ISO93ClientApp.sendPurchase(channel);
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
            System.out.println("\n[INTEGRATION TEST] testSendPurchaseReversal (ISO93) - Sending purchase reversal...");
            ISOMsg response = ISO93ClientApp.sendPurchaseReversal(channel);
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
            System.out.println("\n[INTEGRATION TEST] testSendBalanceInquiry (ISO93) - Sending balance inquiry...");
            ISOMsg response = ISO93ClientApp.sendBalanceInquiry(channel);
            assertNotNull(response);
            assertEquals("00", response.getString(39));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void testSendWithdrawal() {
        try {
            System.out.println("\n[INTEGRATION TEST] testSendWithdrawal (ISO93) - Sending withdrawal...");
            ISOMsg response = ISO93ClientApp.sendWithdrawal(channel);
            assertNotNull(response);
            assertEquals("00", response.getString(39));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void testSendWithdrawalReversal() {
        try {
            System.out.println(
                    "\n[INTEGRATION TEST] testSendWithdrawalReversal (ISO93) - Sending withdrawal reversal...");
            ISOMsg response = ISO93ClientApp.sendWithdrawalReversal(channel);
            assertNotNull(response);
            assertEquals("00", response.getString(39));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void testSendRefund() {
        try {
            System.out.println("\n[INTEGRATION TEST] testSendRefund (ISO93) - Sending refund...");
            ISOMsg response = ISO93ClientApp.sendRefund(channel);
            assertNotNull(response);
            assertEquals("00", response.getString(39));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void testSendPreAuth() {
        try {
            System.out.println("\n[INTEGRATION TEST] testSendPreAuth (ISO93) - Sending pre-auth...");
            ISOMsg response = ISO93ClientApp.sendPreAuth(channel);
            assertNotNull(response);
            assertEquals("00", response.getString(39));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    void testSendCreateCard() {
        try {
            System.out.println("\n[INTEGRATION TEST] testSendCreateCard (ISO93) - Sending card creation request...");
            ISOMsg response = ISO93ClientApp.sendCreateCard(channel);
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
            System.out.println("\n[INTEGRATION TEST] testSendUpdateCardStatus (ISO93) - Sending card status update...");
            ISOMsg response = ISO93ClientApp.sendUpdateCardStatus(channel);
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
                    "\n[INTEGRATION TEST] testGenerateAndSyncCard (ISO93) - Generating and syncing new card...");
            ISOMsg response = ISO93ClientApp.generateAndSyncCard(channel);
            assertNotNull(response);
            assertEquals("00", response.getString(39));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }
}
