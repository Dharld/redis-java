# Redis Java Implementation

A comprehensive Redis server implementation written in Java, featuring full Redis protocol support, master-slave replication, and persistent storage capabilities.

This project implements a fully functional Redis-compatible server that handles the complete Redis protocol (RESP) and supports advanced features including replication, persistence, and concurrent client handling.

## Features

### Redis Commands Supported
- **PING** - Test server connectivity
- **SET/GET** - Basic key-value operations with expiration support
- **ECHO** - Echo back the provided message
- **CONFIG** - Server configuration management (GET operations)
- **KEYS** - Pattern-based key listing
- **INFO** - Server information and statistics

### Advanced Features
- **Master-Slave Replication** - Full Redis replication protocol with PSYNC and REPLCONF
- **RDB File Support** - Read and parse Redis database files
- **Key Expiration** - TTL support with automatic cleanup
- **Multi-threading** - Concurrent client handling with thread pool
- **RESP Protocol** - Complete Redis Serialization Protocol implementation
- **Persistent Storage** - Database persistence with RDB format support

### Architecture
- **Master Server** - Primary Redis server with replication capabilities
- **Replica Server** - Slave server that syncs with master
- **Protocol Parser** - Handles complete RESP protocol parsing
- **Database Engine** - Thread-safe in-memory storage with expiration
- **Configuration System** - Flexible server configuration management

## Requirements

- Java 17 or higher
- Maven 3.6+

## Installation & Usage

### Building the Project

```bash
mvn clean package
```

### Running the Server

#### Master Server (Default)
```bash
# Run on default port 6379
./your_program.sh

# Run on custom port
./your_program.sh --port 6380

# Run with custom database directory and filename
./your_program.sh --port 6379 --dir /path/to/db --dbfilename dump.rdb
```

#### Replica Server
```bash
# Run as replica connecting to master
./your_program.sh --port 6380 --replicaof localhost 6379
```

### Connecting to the Server

You can connect using any Redis client or telnet:

```bash
# Using telnet
telnet localhost 6379

# Example commands
PING
SET mykey "Hello World"
GET mykey
CONFIG GET port
KEYS *
INFO replication
```

### Example Usage

```bash
# Terminal 1: Start master server
./your_program.sh --port 6379

# Terminal 2: Start replica server  
./your_program.sh --port 6380 --replicaof localhost 6379

# Terminal 3: Connect and test
telnet localhost 6379
> SET test "value"
> GET test
> PING
```

## Development

### Project Structure

```
src/main/java/
├── Main.java              # Application entry point
├── Master.java            # Master server implementation
├── Replica.java           # Replica server implementation
├── ProtocolParser.java    # RESP protocol parser
├── Database.java          # In-memory database engine
├── RDBReader.java         # RDB file parser
├── ClientHandler.java     # Client connection handler
├── core/
│   └── ServerConfig.java  # Configuration management
├── utils/
│   └── Command.java       # Supported commands enum
└── RESP/
    └── RESPEncoder.java   # RESP protocol encoder
```

### Key Components

- **Main.java** - Parses arguments and starts appropriate server type
- **Master.java** - Implements master server with replication support
- **Replica.java** - Implements replica server with master synchronization
- **ProtocolParser.java** - Handles all Redis command parsing and execution
- **Database.java** - Thread-safe singleton database with TTL support

## Contributing

This project implements the complete Redis protocol and advanced features. When contributing:

1. Ensure thread safety for all database operations
2. Follow RESP protocol specifications
3. Add comprehensive logging for debugging
4. Test both master and replica functionality
5. Validate RDB file compatibility

## License

This project was originally developed as part of the CodeCrafters Redis challenge but has evolved into a full-featured Redis implementation.
