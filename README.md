# JTP (Java Transfer Protocol)

**JTP** is a lightweight, extensible protocol for structured communication between clients and servers in Java.  
It provides a simple API for building custom transfer systems over network sockets with strong typing, asynchronous handling, and SSL support.

## Features

- Client and Server APIs for easy integration
- SSL/TLS support for secure communication
- Extensible packet system to define custom data formats
- Built-in command handling
- Lightweight and fast, designed for real-time applications

## Project Structure

| Module | Description |
| :----- | :---------- |
| `jtp.shared` | Shared core utilities like data structures, and utility classes |
| `jtp.client` | API for connecting to a JTP server and sending/receiving packets |
| `jtp.server` | API for building a JTP server, managing clients, and handling commands |

## Requirements

- Java 21+
