package com.jpos.simulator;

import org.jpos.iso.ISOMsg;
import com.jpos.simulator.client.ISO93ClientApp;
import com.jpos.simulator.card.CardInfo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ISO93ClientTest {

    @Test
    public void testCreateEchoMsg() throws Exception {
        ISOMsg m = ISO93ClientApp.createEchoMsg();
        assertEquals("1804", m.getMTI());
        assertEquals("801", m.getString(24));
    }

    @Test
    public void testCreateLogonMsg() throws Exception {
        ISOMsg m = ISO93ClientApp.createLogonMsg();
        assertEquals("1804", m.getMTI());
        assertEquals("001", m.getString(24));
    }

    @Test
    public void testCreateKeyExchangeMsg() throws Exception {
        ISOMsg m = ISO93ClientApp.createKeyExchangeMsg();
        assertEquals("1804", m.getMTI());
        assertEquals("161", m.getString(24));
    }

    @Test
    public void testCreatePurchaseMsg() throws Exception {
        ISOMsg m = ISO93ClientApp.createPurchaseMsg();
        assertEquals("1100", m.getMTI());
        assertEquals("4532111084873030", m.getString(2));
        assertEquals("000000", m.getString(3));
        assertEquals("100", m.getString(24));
    }

    @Test
    public void testCreatePurchaseReversalMsg() throws Exception {
        ISOMsg original = ISO93ClientApp.createPurchaseMsg();
        ISOMsg m = ISO93ClientApp.createPurchaseReversalMsg(original);
        assertEquals("1420", m.getMTI());
        assertEquals(original.getString(2), m.getString(2));
        assertEquals("400", m.getString(24));
        assertNotNull(m.getString(90));
    }

    @Test
    public void testCreateBalanceInquiryMsg() throws Exception {
        ISOMsg m = ISO93ClientApp.createBalanceInquiryMsg();
        assertEquals("1100", m.getMTI());
        assertEquals("4532111084873030", m.getString(2));
        assertEquals("310000", m.getString(3));
    }

    @Test
    public void testCreateWithdrawalMsg() throws Exception {
        ISOMsg m = ISO93ClientApp.createWithdrawalMsg();
        assertEquals("1100", m.getMTI());
        assertEquals("4532111084873030", m.getString(2));
        assertEquals("010000", m.getString(3));
        assertEquals("100", m.getString(24));
    }

    @Test
    public void testCreateSyncCardMsg() throws Exception {
        CardInfo card = new CardInfo(
                new String[] { "111122", "16", "1111222233334444", "2912", "1111", "2222", "ACTIVE", "PLATINUM",
                        "MASTERCARD", "2000", "10000", "SRC" });
        ISOMsg m = ISO93ClientApp.createSyncCardMsg(card);
        assertEquals("1304", m.getMTI());
        assertEquals("1111222233334444", m.getString(2));
        assertTrue(m.getString(48).contains("SCHEME=MASTERCARD"));
        assertTrue(m.getString(48).contains("LIMIT_TXN=2000"));
        assertTrue(m.getString(48).contains("BIN=111122"));
        assertFalse(m.getString(48).contains("PIN="));
    }
}
