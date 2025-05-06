# JTP - Java Transfer Protocol

A lightweight, extensible protocol for structured communication between clients and servers in Java.  

## Overview


JTP is a flexible and efficient framework for building custom transfer systems. It enables seamless communication between clients and servers over network sockets, 
featuring asynchronous message handling and built-in SSL/TLS encryption for secure data transmission.

At the core of JTP is a **command-based communication model**, where each interaction between the client and server is encapsulated as a distinct _command_. 
This allows developers to define custom commands and implement specialized logic without modifying the core system. Custom commands are registered with the server via a simple handler interface, 
making it easy to extend and modify the protocol as needed. Requests can then be sent from the client and processed dynamically, promoting flexibility and modularity.

With JTP, you can build scalable and secure communication systems with minimal overhead.

---

## Features

- **Client and Server APIs**: Simplifies integration for both client-side and server-side applications.
- **SSL/TLS Support**: Ensures secure communication with robust encryption, protecting sensitive data in transit.
- **Asynchronous Communication**: Handles multiple requests concurrently, improving performance and responsiveness.
- **Command-Based Architecture**: Provides a modular and extensible framework, allowing developers to easily add custom commands and logic.

---

## Getting started

#### Requirements

- Java 21+

[//]: # (todo)
#### Installation

#### Configuration

JTP supports two methods of configuration:

- **Environment Variables** (highest priority)
- **Properties File** (fallback)

Each component (Client or Server) requires a set of mandatory properties, including hostnames, ports, and paths to keystore/truststore files.

⚠️ **Note:** All port values must be between 0–65535.

[//]: # (TODO: add links)
For full configuration details, refer to the Javadoc for the [Client](#) and [Server](#) classes.

---

## Basic Usage

JTP supports two primary usage patterns:

---

### 1. Client–Server Interaction with Built-in Features

Use this if you want a simple client-server setup for basic data or file transfer.

#### Steps:
- Start the `JTPServer` instance on the server machine.
- Use the `JTPClient` API on the client side to connect, authenticate (if needed), and send pre-defined commands using the `Request` system.

[//]: # (TODO)
#### Example:
```java
```

### 2. Extend Server with Custom Commands

This approach allows you to build your own domain-specific server logic while still using JTP's core messaging infrastructure.

#### Server-Side

1. Implement your own `CommandHandler`.
2. Register it with the server:

[//]: # (TODO)
```java
```

#### Client-Side

1. Send the command using a `Request`:

[//]: # (TODO)
```JAVA
```

---

## API Documentation

[//]: # (TODO: setup javadoc on github)
- Full Client Javadoc available here: [View Javadoc](https://RJDonnison.github.io/JTP/javadoc/)
- Full Server Javadoc available here: [View Javadoc](https://RJDonnison.github.io/JTP/javadoc/)

---

[//]: # (todo)
## Roadmap

- [x] Basic command communication
- [ ] Authentication
- [ ] Database implementation
- [ ] Basic data transfer
- [ ] Stream transfer
- [ ] File transfer
- [ ] Server UI
- [ ] Custom type transfer

---

## Licence

This project is licensed under the [MIT License](LICENSE).
