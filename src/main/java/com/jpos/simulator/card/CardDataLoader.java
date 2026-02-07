package com.jpos.simulator.card;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CardDataLoader {
    public static List<CardInfo> loadCards(String csvPath) {
        List<CardInfo> cards = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] fields = line.split(",");
                cards.add(new CardInfo(fields));
            }
        } catch (IOException e) {
            System.err.println("Error loading cards from " + csvPath + ": " + e.getMessage());
        }
        return cards;
    }

    public static void appendCard(String csvPath, CardInfo card) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(csvPath, true))) {
            StringBuilder sb = new StringBuilder();
            sb.append(card.getPrefix()).append(",");
            sb.append(card.getPanLength()).append(",");
            sb.append(card.getPan()).append(",");
            sb.append(card.getExpiry()).append(",");
            sb.append(card.getPin()).append(",");
            sb.append(card.getPvv()).append(",");
            sb.append(card.getStatus()).append(",");
            sb.append(card.getProduct()).append(",");
            sb.append(card.getScheme()).append(",");
            sb.append(card.getPerTxLimit()).append(",");
            sb.append(card.getDailyLimit()).append(",");
            sb.append(card.getSourceId()).append(",");
            sb.append(card.isSelected() ? "Y" : "N");
            pw.println(sb.toString());
        } catch (IOException e) {
            System.err.println("Error appending card to " + csvPath + ": " + e.getMessage());
        }
    }

    public static void persistSelection(String csvPath, String selectedPan) {
        List<CardInfo> cards = loadCards(csvPath);
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(csvPath))) {
            pw.println(
                    "PREFIX,PAN_LENGTH,PAN,EXPIRY,PIN,PVV,STATUS,PRODUCT,SCHEME,PER_TXN_LIMIT,DAILY_LIMIT,SOURCE_ID,SELECTED");
            for (CardInfo card : cards) {
                StringBuilder sb = new StringBuilder();
                sb.append(card.getPrefix()).append(",");
                sb.append(card.getPanLength()).append(",");
                sb.append(card.getPan()).append(",");
                sb.append(card.getExpiry()).append(",");
                sb.append(card.getPin()).append(",");
                sb.append(card.getPvv()).append(",");
                sb.append(card.getStatus()).append(",");
                sb.append(card.getProduct()).append(",");
                sb.append(card.getScheme()).append(",");
                sb.append(card.getPerTxLimit()).append(",");
                sb.append(card.getDailyLimit()).append(",");
                sb.append(card.getSourceId()).append(",");
                sb.append(card.getPan().equals(selectedPan) ? "Y" : "N");
                pw.println(sb.toString());
            }
        } catch (IOException e) {
            System.err.println("Error persisting selection to " + csvPath + ": " + e.getMessage());
        }
    }

    public static void updateCardStatus(String csvPath, String pan, String status) {
        List<CardInfo> cards = loadCards(csvPath);
        boolean found = false;
        for (CardInfo card : cards) {
            if (card.getPan().equals(pan)) {
                card.setStatus(status);
                found = true;
                break;
            }
        }

        if (found) {
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(csvPath))) {
                pw.println(
                        "PREFIX,PAN_LENGTH,PAN,EXPIRY,PIN,PVV,STATUS,PRODUCT,SCHEME,PER_TXN_LIMIT,DAILY_LIMIT,SOURCE_ID,SELECTED");
                for (CardInfo card : cards) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(card.getPrefix()).append(",");
                    sb.append(card.getPanLength()).append(",");
                    sb.append(card.getPan()).append(",");
                    sb.append(card.getExpiry()).append(",");
                    sb.append(card.getPin()).append(",");
                    sb.append(card.getPvv()).append(",");
                    sb.append(card.getStatus()).append(",");
                    sb.append(card.getProduct()).append(",");
                    sb.append(card.getScheme()).append(",");
                    sb.append(card.getPerTxLimit()).append(",");
                    sb.append(card.getDailyLimit()).append(",");
                    sb.append(card.getSourceId()).append(",");
                    sb.append(card.isSelected() ? "Y" : "N");
                    pw.println(sb.toString());
                }
            } catch (IOException e) {
                System.err.println("Error updating card status in " + csvPath + ": " + e.getMessage());
            }
        }
    }
}
