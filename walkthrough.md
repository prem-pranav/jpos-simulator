# JPOS Simulator - Usage Walkthrough

This guide provides step-by-step instructions for testing various payment scenarios using the JPOS ISO8583 Simulator. It covers basic connectivity, financial transactions, and card management.

For detailed technical specifications, protocols, and field mappings, please refer to the **[README.md](README.md)**.

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Scenario 1: Basic Connectivity (Echo)](#scenario-1-basic-connectivity-echo)
- [Scenario 2: Financial Transaction (Purchase)](#scenario-2-financial-transaction-purchase)
- [Scenario 3: Card Management (Generate & Sync)](#scenario-3-card-management-generate--sync)
- [Scenario 4: ISO 93 Binary Testing](#scenario-4-iso-93-binary-testing)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

Ensure you have **Java 8+** installed.

**Build the Project:**
```bash
./gradlew clean build
```

---

## Scenario 1: Basic Connectivity (Echo)

**Goal**: Verify that the client can successfully connect to the server and exchange a network management message.

### Steps

1.  **Start the Server** (Terminal 1):
    ```bash
    ./gradlew runISO87Server
    ```

2.  **Start the Client** (Terminal 2):
    ```bash
    ./gradlew runISO87Client
    ```

3.  **Execute Echo**:
    - In the client menu, select **"Send Echo Request (0800/301)"**.

### ✅ Verification
- **Client Console**:
    > `Sending Echo [0800]...`
    > `Received Echo response [0810] (F39=00)`
- **Server Console**:
    > `ISO87 Received MTI: 0800`
    > `Processing Echo...`

---

## Scenario 2: Financial Transaction (Purchase)

**Goal**: Perform a standard financial transaction using a simulated card.

### Steps

1.  **Ensure Server is Running** (from Scenario 1).

2.  **Send Purchase**:
    - In the client menu, select **"Send Purchase (0200)"**.

### ✅ Verification
- **Client**: Sends a `0200` message with a valid PAN (automatically selected from `cards.csv`).
- **Server**: Responds with `0210` and Release Code `00` (Approved).

---

## Scenario 3: Card Management (Generate & Sync)

**Goal**: Create a new test card dynamically and sync it with the server.

### Steps

1.  **Select Option**:
    - In the client menu, choose **"Generate & Sync New Card (0300)"**.

2.  **Observe Process**:
    - The client generates a new card with a valid Luhn PAN.
    - It sends a `0300` (File Action) message to the server (Function Code: Add).
    - It appends the new card to the local storage.

### ✅ Verification
- **Persistence**: Open `src/main/resources/data/cards.csv`. The last line should contain the newly generated PAN.

---

## Scenario 4: ISO 93 Binary Testing

**Goal**: Switch protocols to test binary message handling.

### Steps

1.  **Start ISO 93 Server**:
    ```bash
    ./gradlew runISO93Server
    ```

2.  **Start ISO 93 Client**:
    ```bash
    ./gradlew runISO93Client
    ```

3.  **Execute Balance Inquiry**:
    - Select **"Send Balance Inquiry (1100/31)"**.

### ✅ Verification
- **Response**: The client should receive a response containing **Field 54** (Additional Amounts) with a mock balance (e.g., `123.45`).

---

## Troubleshooting

| Issue | Solution |
| :--- | :--- |
| **Connection Refused** | Ensure the **Server** is running *before* starting the **Client**. |
| **Card Not Found** | If transactions fail, run **"Generate & Sync New Card"** first to ensure valid data exists. |
| **Logs** | Check `log/ISO87HOST.log` or `log/ISO93HOST.log` for full ISO message dumps. |
