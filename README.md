# SPL Course Projects – Ben-Gurion University  

This repository contains my programming assignments for the **System Programming (SPL)** course at BGU.  
The projects cover **concurrency, networking, object-oriented design, and memory management** in **Java, C++, and JavaScript**.  

---

## 📌 Assignments  

### 1️⃣ Set Card Game (Java)  
**General Description:**  
Implementation of the classic *Set* card game to practice **multi-threading and concurrency**. Players (human or AI) compete in real-time, while a dealer manages the game flow, synchronization, and scoring.  

**Key Features:**  
- Full simulation of the *Set* card game with 81 cards and 4 attributes (color, number, shape, shading).  
- **Multi-threaded players** with human keyboard input and simulated computer players.  
- Dealer thread controls game flow: validates sets, reshuffles cards, assigns penalties/rewards.  
- **Synchronization & fairness** to prevent race conditions and ensure liveness.  
- Visual **GUI** for gameplay interaction and updates.  

**Technologies:**  
`Java` · `Threads` · `Concurrency` · `Synchronization` · `Maven`  

---

### 2️⃣ TFTP Server and Client (Java)  
**General Description:**  
Implementation of an extended **TFTP (Trivial File Transfer Protocol)** system using a **Thread-per-Client model**. Supports file transfer operations, client logins, and **bi-directional messaging** between clients and the server.  

**Key Features:**  
- **Login system** with unique usernames.  
- File operations: **upload (WRQ)**, **download (RRQ)**, **delete (DELRQ)**, and **directory listing (DIRQ)**.  
- **Broadcast (BCAST)** messages when files are added or removed.  
- Robust **error handling** (file not found, already exists, invalid operations, etc.).  
- Two-threaded client:  
  - Keyboard thread (user input)  
  - Listening thread (server responses)  

**Technologies:**  
`Java` · `TCP Networking` · `Multi-threading` · `Bi-directional Protocols` · `Maven`  

---

### 3️⃣ Food Warehouse Management System (C++)  
**General Description:**  
Simulation of a food warehouse to practice **object-oriented design** and **memory management** in C++. The system models volunteers, customers, and order handling while ensuring efficiency and correctness.  

**Key Features:**  
- Multiple customer and volunteer roles (collectors, drivers, limited roles).  
- Order lifecycle: **Pending → Collecting → Delivering → Completed**.  
- Commands for adding customers, placing orders, checking statuses, and simulating steps.  
- **Backup & Restore** functionality for saving and restoring system state.  
- Strong emphasis on **Rule of 5** and avoiding memory leaks.  

**Technologies:**  
`C++` · `OOP` · `Rule of 5` · `Data Structures` · `Memory Management`  

---
