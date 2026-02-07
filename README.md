# JPOS ISO8583 Simulator

A standalone, high-performance **ISO8583 Simulator** designed for testing payment switches and terminals. It supports both **ISO 87 (ASCII)** and **ISO 93 (Binary)** protocols with concurrent client/server capabilities, making it an ideal tool for QA and development of financial systems.

---

## Table of Contents

- [Features](#features)
- [Quick Start](#quick-start)
- [Supported Protocols](#supported-protocols)
- [Transaction Flows](#transaction-flows)
- [Card Management](#card-management)
- [Configuration](#configuration)
- [Logging](#logging)
- [Project Structure](#project-structure)

---

## ✨ Features

- **Dual Protocol Support**: Seamlessly handle ISO 87 (ASCII) and ISO 93 (Binary) messages.
- **Comprehensive Scenarios**: Built-in support for Financial (Purchase, Withdrawal, Refund, Pre-Auth, Balance) and Network Management (Logon, Echo, Key Exchange) flows.
- **Dynamic Response Logic**: Template-based responses with automated field population (STAN, RRN, Dates).
- **Smart Card Management**: Automated card generation with valid Luhn PANs and persistence via `cards.csv`.
- **Robust Logging**: Detailed transaction logs for audit and debugging.

---

## 🚀 Quick Start

### Prerequisites
- **Java 8** or higher installed.

### Build & Run

1.  **Compile the Project**:
    ```bash
    ./gradlew clean build
    ```

2.  **Run a Server** (e.g., ISO 87):
    ```bash
    ./gradlew runISO87Server
    ```

3.  **Run a Client** (in a new terminal):
    ```bash
    ./gradlew runISO87Client
    ```

4.  **Interact**: Follow the on-screen menu to send transactions.

---

## 🔌 Supported Protocols

The simulator supports multiple ISO8583 versions concurrently.

| Feature | ISO 87 (ASCII) | ISO 93 (Binary) |
| :--- | :--- | :--- |
| **Channel** | `ASCIIChannel` (Length-prefixed ASCII) | `PostChannel` (Length-prefixed Binary) |
| **Packager** | `iso87.xml` | `iso93.xml` |
| **Usage** | Terminal-to-Host | Host-to-Host / Switch |
| **Header** | None / Standard MTI | Variations (e.g., `1804` for Net Mgmt) |

---

## 💸 Transaction Flows

All financial transactions require a valid Primary Account Number (PAN) in DE002.

### Financial Transactions

| Transaction Type | ISO 87 (MTI / Proc) | ISO 93 (MTI / Proc / Func) | Notes |
| :--- | :--- | :--- | :--- |
| **Purchase** | `0200` / `000000` | `1100` / `000000` / `100` | Standard sale |
| **Pre-Auth** | `0100` / `000000` | `1100` / `000000` / `104` | Auth only |
| **Withdrawal** | `0200` / `010000` | `1100` / `010000` / `100` | Cash out |
| **Refund** | `0200` / `200000` | `1100` / `200000` / `200` | Purchase return |
| **Balance** | `0200` / `310000` | `1100` / `310000` / `100` | Mocked response in F54 |

### Network Management

| Transaction Type | ISO 87 (MTI / F70) | ISO 93 (MTI / F24) | Notes |
| :--- | :--- | :--- | :--- |
| **Echo Test** | `0800` / `301` | `1804` / `801` | Connectivity check |
| **Logon** | `0800` / `001` | `1804` / `001` | Session start |
| **Logoff** | `0800` / `002` | `1804` / `002` | Session end |
| **Key Exch** | `0800` / `161` | `1804` / `161` | Dummy Key in F48 |

---

## 💳 Card Management

Card data is persisted in `src/main/resources/data/cards.csv` to ensure consistency across sessions. Each card record includes:
`PREFIX, PAN, EXPIRY, PIN, STATUS, LIMITS, ...`

### Key Features
- **Auto-Selection**: The client automatically picks the card marked `SELECTED` in the CSV for transactions.
- **Generate & Sync**: 
    1.  Client generates a new valid card (Luhn check passed).
    2.  Sends a `0300` / `1304` message to the server (Func `1`).
    3.  Server and Client update the local CSV file.
- **Status Update**: Send a File Action message (Func `2`) to block or unblock cards.

---

## ⚙️ Configuration

Configurations are XML-based and located in `src/main/resources/xml/`.

| Component | ISO 87 Path | ISO 93 Path |
| :--- | :--- | :--- |
| **Server Channel** | `channel/iso87/server.xml` | `channel/iso93/server.xml` |
| **Client Channel** | `channel/iso87/client.xml` | `channel/iso93/client.xml` |
| **Packager** | `packager/iso87.xml` | `packager/iso93.xml` |

---

## 📝 Logging

- **Client**: Logs to **Console** only.
- **Server**: 
    - **Console**: Real-time event stream.
    - **File**: `log/ISO87HOST.log` and `log/ISO93HOST.log`.
- **Format**: Structured JPOS XML dump (Tag-Value).

---

## 📂 Project Structure

```text
jpos-simulator/
├── src/
│   ├── main/
│   │   ├── java/com/jpos/simulator/
│   │   │   ├── card/           # Card generation & file IO
│   │   │   ├── client/         # Client Apps & Message Builders
│   │   │   ├── core/           # ChannelFactory & Common Interfaces
│   │   │   └── server/         # Server Apps & Request Listeners
│   │   └── resources/
│   │       ├── data/           # cards.csv (Data Store)
│   │       └── xml/            # Configuration Files
│   └── test/                   # JUnit Protocol Tests
├── build.gradle                # Build Configuration
├── README.md                   # This file
└── walkthrough.md              # Scenario-based Usage Guide
```
