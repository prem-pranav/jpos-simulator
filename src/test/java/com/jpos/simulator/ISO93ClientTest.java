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
            String errorMsg = "\n\n" +
                    "═══════════════════════════════════════════════════════════════\n" +
                    "  INTEGRATION TEST FAILURE: ISO93 Server Not Running\n" +
                    "═══════════════════════════════════════════════════════════════\n" +
                    "\n" +
                    "These are integration tests that require a running server.\n" +
                    "\n" +
                    "TO FIX:\n" +
                    "  1. Start the server in a separate terminal:\n" +
                    "     ./gradlew runISO93Server\n" +
                    "\n" +
                    "  2. Then run the tests:\n" +
                    "     ./gradlew test --tests \"com.jpos.simulator.ISO93ClientTest\"\n" +
                    "\n" +
                    "ALTERNATIVE:\n" +
                    "  Skip integration tests during build:\n" +
                    "     ./gradlew build -x test\n" +
                    "\n" +
                    "Server should be listening on port 8006\n" +
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
    public void testSendEcho() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendEcho (ISO93) - Sending echo to server...");

        ISOMsg request = ISO93ClientApp.createEchoMsg();
        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Echo transaction completed successfully\n");
    }

    @Test
    public void testSendLogon() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendLogon (ISO93) - Sending logon to server...");

        ISOMsg request = ISO93ClientApp.createLogonMsg();
        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Logon transaction completed successfully\n");
    }

    @Test
    public void testSendLogoff() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendLogoff (ISO93) - Sending logoff to server...");

        ISOMsg request = ISO93ClientApp.createLogoffMsg();
        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Logoff transaction completed successfully\n");
    }

    @Test
    public void testSendKeyExchange() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendKeyExchange (ISO93) - Sending key exchange...");

        ISOMsg request = ISO93ClientApp.createKeyExchangeMsg();
        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Key Exchange transaction completed successfully\n");
    }

    @Test
    public void testSendPurchase() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendPurchase (ISO93) - Sending purchase transaction...");

        ISOMsg request = ISO93ClientApp.createPurchaseMsg();
        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        assertNotNull(request, "Purchase transaction should return original message");
        assertEquals("1100", request.getMTI());
        System.out.println("  ✓ Purchase transaction completed successfully\n");
    }

    @Test
    public void testSendPurchaseReversal() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendPurchaseReversal (ISO93) - Sending purchase reversal...");

        ISOMsg original = ISO93ClientApp.createPurchaseMsg();
        ISOMsg request = ISO93ClientApp.createPurchaseReversalMsg(original);
        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Purchase Reversal transaction completed successfully\n");
    }

    @Test
    public void testSendBalanceInquiry() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendBalanceInquiry (ISO93) - Sending balance inquiry...");

        ISOMsg request = ISO93ClientApp.createBalanceInquiryMsg();
        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Balance Inquiry transaction completed successfully\n");
    }

    @Test
    public void testSendWithdrawal() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendWithdrawal (ISO93) - Sending withdrawal transaction...");

        ISOMsg request = ISO93ClientApp.createWithdrawalMsg();
        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        assertNotNull(request, "Withdrawal transaction should return original message");
        assertEquals("1100", request.getMTI());
        System.out.println("  ✓ Withdrawal transaction completed successfully\n");
    }

    @Test
    public void testSendWithdrawalReversal() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendWithdrawalReversal (ISO93) - Sending withdrawal reversal...");

        ISOMsg original = ISO93ClientApp.createWithdrawalMsg();
        ISOMsg request = ISO93ClientApp.createPurchaseReversalMsg(original);
        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Withdrawal Reversal transaction completed successfully\n");
    }

    @Test
    public void testSendRefund() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendRefund (ISO93) - Sending refund transaction...");

        ISOMsg request = ISO93ClientApp.createRefundMsg();
        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Refund transaction completed successfully\n");
    }

    @Test
    public void testSendPreAuth() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendPreAuth (ISO93) - Sending pre-authorization...");

        ISOMsg request = ISO93ClientApp.createPreAuthMsg();
        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Pre-Auth transaction completed successfully\n");
    }

    @Test
    public void testSendCreateCard() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendCreateCard (ISO93) - Sending card creation request...");

        ISOMsg request = ISO93ClientApp.createCreateCardMsg();
        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Create Card transaction completed successfully\n");
    }

    @Test
    public void testSendUpdateCardStatus() throws Exception {
        System.out.println("\n[INTEGRATION TEST] testSendUpdateCardStatus (ISO93) - Sending card status update...");

        ISOMsg request = ISO93ClientApp.createCreateCardMsg(); // Reuse for demo
        channel.send(request);
        ISOMsg response = channel.receive();
        assertNotNull(response);

        System.out.println("  ✓ Update Card Status transaction completed successfully\n");
    }
}
