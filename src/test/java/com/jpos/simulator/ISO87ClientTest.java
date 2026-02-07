package com.jpos.simulator;

import org.jpos.iso.ISOMsg;
import com.jpos.simulator.client.ISO87ClientApp;
import com.jpos.simulator.card.CardInfo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ISO87ClientTest {

    @Test
    public void testCreateEchoMsg() throws Exception {
        ISOMsg m = ISO87ClientApp.createEchoMsg();
        assertEquals("0800", m.getMTI());
        assertEquals("301", m.getString(70));
    }

    @Test
    public void testCreateLogonMsg() throws Exception {
        ISOMsg m = ISO87ClientApp.createLogonMsg();
        assertEquals("0800", m.getMTI());
        assertEquals("001", m.getString(70));
    }

    @Test
    public void testCreateLogoffMsg() throws Exception {
        ISOMsg m = ISO87ClientApp.createLogoffMsg();
        assertEquals("0800", m.getMTI());
        assertEquals("002", m.getString(70));
    }

    @Test
    public void testCreateKeyExchangeMsg() throws Exception {
        ISOMsg m = ISO87ClientApp.createKeyExchangeMsg();
        assertEquals("0800", m.getMTI());
        assertEquals("161", m.getString(70));
        assertNotNull(m.getString(48));
    }

    @Test
    public void testCreatePurchaseMsg() throws Exception {
        ISOMsg m = ISO87ClientApp.createPurchaseMsg();
        assertEquals("0200", m.getMTI());
        assertEquals("4532111084873030", m.getString(2));
        assertEquals("000000", m.getString(3));
        assertEquals("000000001000", m.getString(4));
    }

    @Test
    public void testCreatePurchaseReversalMsg() throws Exception {
        ISOMsg original = ISO87ClientApp.createPurchaseMsg();
        ISOMsg m = ISO87ClientApp.createPurchaseReversalMsg(original);
        assertEquals("0420", m.getMTI());
        assertEquals(original.getString(2), m.getString(2));
        assertEquals("000000", m.getString(3));
        assertNotNull(m.getString(11));
        assertNotNull(m.getString(90));
    }

    @Test
    public void testCreateBalanceInquiryMsg() throws Exception {
        ISOMsg m = ISO87ClientApp.createBalanceInquiryMsg();
        assertEquals("0200", m.getMTI());
        assertEquals("4532111084873030", m.getString(2));
        assertEquals("310000", m.getString(3));
    }

    @Test
    public void testCreateWithdrawalMsg() throws Exception {
        ISOMsg m = ISO87ClientApp.createWithdrawalMsg();
        assertEquals("0200", m.getMTI());
        assertEquals("4532111084873030", m.getString(2));
        assertEquals("010000", m.getString(3));
    }

    @Test
    public void testCreateRefundMsg() throws Exception {
        ISOMsg m = ISO87ClientApp.createRefundMsg();
        assertEquals("0200", m.getMTI());
        assertEquals("4532111084873030", m.getString(2));
        assertEquals("200000", m.getString(3));
    }

    @Test
    public void testCreatePreAuthMsg() throws Exception {
        ISOMsg m = ISO87ClientApp.createPreAuthMsg();
        assertEquals("0100", m.getMTI());
        assertEquals("4532111084873030", m.getString(2));
        assertEquals("000000", m.getString(3));
    }

    @Test
    public void testCreateCreateCardMsg() throws Exception {
        ISOMsg m = ISO87ClientApp.createCreateCardMsg();
        assertEquals("0300", m.getMTI());
        assertEquals("1", m.getString(91));
        assertTrue(m.getString(48).contains("PAN="));
    }

    @Test
    public void testCreateSyncCardMsg() throws Exception {
        CardInfo card = new CardInfo(
                new String[] { "123456", "16", "1234567890123456", "2912", "1234", "5678", "ACTIVE", "GOLD",
                        "VISA", "1000", "5000", "SRC" });
        ISOMsg m = ISO87ClientApp.createSyncCardMsg(card);
        assertEquals("0300", m.getMTI());
        assertEquals("1234567890123456", m.getString(2));
        assertTrue(m.getString(48).contains("SCHEME=VISA"));
        assertTrue(m.getString(48).contains("LIMIT_TXN=1000"));
        assertTrue(m.getString(48).contains("BIN=123456"));
        assertFalse(m.getString(48).contains("PIN="));
    }
}
