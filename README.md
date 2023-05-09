 # <center>Mindsigner



### Mindsigner is a minecraft mod that allows connection with the Hive Blockchain. 

Features:
### v0.0.33 
Endpoint's working, but still trying to troubleshoot why I cant connect with a client.

### v0.0.32
Websocket Server doesn't work, but atleast the game runs xD
Trying to add Endpoints to Handle Connections
Back to Websockets testing / SSL and TyrusServer configuration

### v0.0.31
Experimenting with ChatHandler to route websocket traffic to in-game chat.

### v0.0.3
Working Websocket Server connnection finally/ Removed CraftSocketServerHandler and other unused stuff.
***
>Versions below this don't really represent actual progress, but testing.

### v0.0.25
Stopped using Netty, started looking into Tyrus

### v0.0.23
Working socket connnection revised, better message handling / added Server Endpoint 

### v0.0.22
Changed the schema for the connection / added plan for connection path in README.md

### v.0.0.2 
first working socket connection

### v0.0.16
testing with websockets & Authentication

### v0.0.15

Optimizing code, changed to Netty only instead of BouncyCastle.

 ### v0.0.14
 
/added OpenSSL, Netty and testing BouncyCastle

### v0.0.1

## Pre-Alpha


# GAME PLAN:

1.Start the CraftSocketServer on the Minecraft server using Java code. This server listens for incoming WebSocket connections on port 8080.

2.Connect the CraftSocketClient to the CraftSocketServer using JavaScript code in the dhive console. The client sends and receives WebSocket messages to/from the server.

3.In the dhive console, use the dhive JavaScript library to connect to the Hive blockchain API endpoint (e.g. https://api.hive.blog).

4.Set up a WebSocket provider in the dhive JavaScript library using the CraftSocketClient instance created in step 2. This allows dhive to receive real-time updates via the WebSocket connection when new blocks are added to the blockchain.

5.Subscribe to new block events from the blockchain via the WebSocket provider in dhive. When a new block is received, parse the block data to check for new comments.

6.If a new comment is detected in the block data, extract the relevant information from the comment (e.g. author, body text).

7.Use the CraftSocketClient instance to send the comment text to the CraftSocketServer on the Minecraft server via the WebSocket connection.

8.On the Minecraft server, the CraftSocketServer receives the incoming comment text and displays it in the game in some way (e.g. as a chat message).

By following these steps, you can establish a real-time connection between Minecraft, the WebSocket server, the Hive blockchain, and the dhive JavaScript library.