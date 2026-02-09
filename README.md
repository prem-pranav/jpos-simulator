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
- **Smart Card Management**: Automated card generation with valid Luhn PANs and persistence via `cards.csv`. Supports BIN-based generation.
- **HSM Simulation**: Integrated ISO-0 PIN Block generation and PVV calculation for secure transaction testing.
- **Robust Logging**: Detailed transaction logs for audit and debugging (Console & File).

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

4.  **Run HSM Simulator (Optional)**:
    ```bash
    ./gradlew runHsmSim
    ```

5.  **Run Integration Tests**:
    Integration tests automatically manage the server lifecycle. If the server is not running, the test suite will start it programmatically and shut it down after completion.
    ```bash
    ./gradlew test --tests "com.jpos.simulator.ISO87ClientTest"
    ./gradlew test --tests "com.jpos.simulator.ISO93ClientTest"
    ```

---

## 📡 Supported Protocols

The simulator supports two distinct ISO 8583 versions, each running on its own port and using a specific channel type.

| Protocol | Version | Channel Type | Default Port | Encoding |
| :--- | :--- | :--- | :--- | :--- |
| **ISO 87** | 1987 | `ASCIIChannel` | `8005` | ASCII Encodings |
| **ISO 93** | 1993 | `PostChannel` | `9005` | Binary (2-byte length header) |

---

## 🔄 Transaction Flows

The simulator handles a variety of standard transaction types. Below are the specific MTI and Processing Code mappings used.

### Network Management
Used for "handshakes" and keeping the connection alive.

| Action | ISO 87 MTI | ISO 93 MTI | Net Mgmt Code |
| :--- | :--- | :--- | :--- |
| **Echo Test** | `0800` | `1804` | 301 / 801 |
| **Logon** | `0800` | `1804` | 001 |
| **Logoff** | `0800` | `1804` | 002 |
| **Key Exchange** | `0800` | `1804` | 161 |

### Financial Transactions
Financial messages carry monetary value and require authorization.

| Transaction | Request MTI (87/93) | Processing Code (DE 3) |
| :--- | :--- | :--- |
| **Purchase** | `0200` / `1100` | `000000` |
| **Withdrawal** | `0200` / `1100` | `010000` |
| **Balance Inquiry** | `0200` / `1100` | `310000` |
| **Refund** | `0200` / `1100` | `200000` |
| **Pre-Auth** | `0100` / `1100` | `000000` |

### File Actions
Used to sync card data between Client and Server.

| Action | MTI (87/93) | Function |
| :--- | :--- | :--- |
| **Add/Update Card** | `0300` / `1300` | Card Synchronization |

---

## 💳 Card Management

The simulator includes a robust Data Driven Testing capability backed by a CSV file.

- **Storage**: `src/main/resources/data/cards.csv`
- **Format**: `PREFIX,PAN_LENGTH,PAN,EXPIRY,PIN,PVV,CVD,STATUS,PRODUCT,SCHEME,LIMITS...`
- **Usage**:
    - **Read**: The Client reads this file to populate transactions (PAN, Expiry, CVV).
    - **Write**: The Client appends new cards here after the "Generate New Card" flow.
    - **Update**: Card status changes (e.g., Block Card) are persisted back to this file.

---

## ⚙️ Configuration

All configuration is managed via **jPOS XML** files located in `src/main/resources/xml/`.

### Channel Configuration
Define how the simulator connects (Client) or listens (Server).

- **ISO 87**: `xml/channel/iso87/server.xml` & `client.xml`
- **ISO 93**: `xml/channel/iso93/server.xml` & `client.xml`

**Example (ISO 87 Server)**:
```xml
<channel class="org.jpos.iso.channel.ASCIIChannel" packager="org.jpos.iso.packager.GenericPackager">
    <property name="packager-config" value="src/main/resources/xml/packager/iso87.xml" />
    <property name="port" value="8005" />
</channel>
```

### Packager Configuration
Defines the field structure (data types, lengths) for parsing messages.
- `xml/packager/iso87.xml`
- `xml/packager/iso93.xml`

---

## 📝 Logging

The simulator uses the built-in jPOS logging framework for both Client and Server, ensuring structured XML logs for all transactions.

- **Client Logs**: 
    - **Console**: Real-time XML dump.
    - **File**: `log/ISO87CLIENT.log` and `log/ISO93CLIENT.log`.
- **Server Logs**: 
    - **Console**: Real-time XML dump.
    - **File**: `log/ISO87HOST.log` and `log/ISO93HOST.log`.
- **Format**: Standard jPOS XML format, capturing MTI, direction, and all data fields.

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
│   │   │   ├── security/       # HSM Simulator (PIN/PVV generation)
│   │   │   └── server/         # Server Apps & Request Listeners
│   │   └── resources/
│   │       ├── data/           # cards.csv (Data Store)
│   │       └── xml/            # Configuration Files
│   └── test/                   # Automated Integration Tests
├── log/                        # Persistent Transaction Logs
├── build.gradle                # Build Configuration
├── README.md                   # This file
├── walkthrough.md              # Scenario-based Usage Guide & Flow Diagrams
```
