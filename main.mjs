import WebSocket from 'ws';
import { CraftSocketClient } from './CraftSocketClient.mjs';

const client = new CraftSocketClient('wss://localhost:8080'); // Replace with your server's address
client.connect();

// Send a message to the server after connection is established
client.on('open', () => {
    console.log ('WebSocket connected');
    client.send('Hello, server!');

});

// Listen for messages from the server
client.on('message', (message) => {
  console.log(`Received message from server: ${message}`);
});
