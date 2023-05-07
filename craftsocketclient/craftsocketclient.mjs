import WebSocket from 'ws'
const client = new WebSocket('ws://localhost:8080');

client.on('open', () => {
  console.log('WebSocket connected');
  // Send a message to the server
  client.send('Hello, server!');
});

client.on('message', (message) => {
  console.log(`Received message: ${message}`);
  // Do something with the received message from the server
});

client.on('error', (error) => {
  console.error(error);
});

client.on('close', () => {
  console.log('WebSocket closed');
});
