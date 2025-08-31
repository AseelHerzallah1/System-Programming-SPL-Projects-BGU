# System-Programming-SPL-Projects-BGU
Projects for the System Programming course at BGU. Hands-on C++ and Java assignments covering runtime environments (Unix, JVM), concurrency, memory management, OOP, and sockets. From server-client communication to game logic and system design, each project emphasizes practical application and efficient programming practices.

#Assignments:

1. Set Card Game (Java)

General Description:
An implementation of the Set card game, designed to practice multi-threading and concurrency in Java. Players (human or computer-controlled) compete to find valid sets, while a dealer manages the game flow, synchronization, and scoring.

Key Features:

Full simulation of the Set card game with 81 cards and four attributes (color, number, shape, shading).

Multi-threaded players with human keyboard input and simulated AI players.

Dealer thread coordinates game state, checks sets, reshuffles cards, and handles penalties.

Real-time concurrency with synchronization mechanisms to ensure fairness and liveness.

GUI provided for visual gameplay with dynamic updates.

Technologies:
Java · Threads · Concurrency · Synchronization · Maven

2. TFTP Server and Client (Java)

General Description:
Implementation of an extended TFTP (Trivial File Transfer Protocol) system using a Thread-per-Client model. The project includes both client and server applications, supporting file transfer operations over TCP and bi-directional messaging between clients and the server.

Key Features:

Login with unique usernames for clients.

File operations: upload (WRQ), download (RRQ), delete (DELRQ), and directory listing (DIRQ).

Broadcast (BCAST) messages for file additions/deletions.

Bi-directional messaging enabling both server-client and peer communication.

Robust error handling (file not found, file already exists, user not logged in, etc.).

Two-threaded client: one thread for user input, one for server responses.

Technologies:
Java · TCP Networking · Multi-threading · Bi-directional Protocols · Maven

3. Food Warehouse Management System (C++)

General Description:
A simulation of a food warehouse that manages volunteers, customers, and orders. The system models daily warehouse operations such as placing orders, collecting, delivering, and tracking their status. Built with a strong emphasis on object-oriented design and efficient memory management in C++.

Key Features:

Customers and volunteers with unique roles (collectors, drivers, limited roles).

Order lifecycle: pending → collecting → delivering → completed.

Interactive commands for adding customers, placing orders, checking statuses, and running simulation steps.

Support for warehouse backup and restore.

Memory safety using the Rule of 5, avoiding leaks and ensuring efficiency.

Technologies:
C++ · OOP · Rule of 5 · Data Structures · Memory Management
