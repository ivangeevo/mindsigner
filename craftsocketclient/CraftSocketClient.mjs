import fs from 'fs';
import debug from 'debug';
import dotenv from 'dotenv';
import ioClient from 'socket.io-client';

// Load environment variables from a .env file
dotenv.config();

// Enable debug logging for socket.io-client
debug.enable(process.env.DEBUG || 'socket.io-client');

// Enable debugging for the 'socket.io-client' module
ioClient(`https://localhost:8443/websockets/`, {
  transports: ['websocket'],
  rejectUnauthorized: false,
  cert: fs.readFileSync('mycert.pem', 'utf8', { password: 'password' }),
  key: fs.readFileSync('mycert.p12', 'utf8', { password: 'password' }),
}).on('connect', () => {
  console.log('WebSocket connection established');
}).on('response', (message) => {
  console.log('Received response:', message);
}).on('message', (data) => {
  console.log('Received message:', data);
}).on('disconnect', () => {
  console.log('WebSocket connection closed');
}).on('error', (error) => {
  console.error('WebSocket error:', error);
});

