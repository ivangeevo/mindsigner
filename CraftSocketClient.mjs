import WebSocket from 'ws';
import dhive from '@hiveio/dhive';

const wssUrl = 'wss://0.0.0.0:8443/websockets/';
const subprotocol = 'craft-protocol';
const privateKey = 'mykey.pem';
const certificate = 'mycert.cer';

const ws = new WebSocket(wssUrl, subprotocol, {
  cert: certificate,
  key: privateKey,
  rejectUnauthorized: false
});

ws.on('open', () => {
  console.log('WebSocket connection established');
  const client = new Client('https://api.hive.blog');
  // Use the dhive client to interact with the Hive blockchain
});

ws.on('message', (data) => {
  console.log('Received message:', data);
});

ws.on('close', () => {
  console.log('WebSocket connection closed');
});

ws.on('error', (error) => {
  console.error('WebSocket error:', error);
});