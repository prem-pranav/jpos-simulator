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

4.  **Run Integration Tests**:
    Integration tests automatically manage the server lifecycle. If the server is not running, the test suite will start it programmatically and shut it down after completion.
    ```bash
    ./gradlew test --tests "com.jpos.simulator.ISO87ClientTest"
    ./gradlew test --tests "com.jpos.simulator.ISO93ClientTest"
    ```

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
│   │   │   └── server/         # Server Apps & Request Listeners
│   │   └── resources/
│   │       ├── data/           # cards.csv (Data Store)
│   │       └── xml/            # Configuration Files
│   └── test/                   # Automated Integration Tests
├── log/                        # Persistent Transaction Logs
├── build.gradle                # Build Configuration
├── README.md                   # This file
└── walkthrough.md              # Scenario-based Usage Guide
```
