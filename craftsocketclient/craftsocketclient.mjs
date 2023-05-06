import WebSocket from 'ws';
import fs from 'fs';

const PORT = 8080;
const HOST = '127.0.0.1';

const options = {
  key: fs.readFileSync('./mykey.key'),
  cert: fs.readFileSync('./mycert.pem'),
  passphrase: 'password',
  rejectUnauthorized: true // This line disables server authentication
};

const socket = new WebSocket(`wss://${HOST}:${PORT}`, options);

socket.on('open', () => {
  console.log('Connected to server');
});

socket.on('message', (data) => {
  console.log(data.toString());
});

socket.on('error', (error) => {
  console.error(error);
});

socket.on('close', () => {
  console.log('Disconnected from server');
});
