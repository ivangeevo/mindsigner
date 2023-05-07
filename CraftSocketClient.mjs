import ws from 'ws';

class CraftSocketClient {
  constructor(url) {
    this.socket = new ws(url);
  }

  connect() {
    this.socket.addEventListener('open', () => {
      console.log('WebSocket connected');
      this.sendOnConnect('Hello, server!');
    });

    this.socket.addEventListener('message', (event) => {
      console.log(`Received message: ${event.data}`);
    });

    this.socket.addEventListener('error', (error) => {
      console.error(error);
    });

    this.socket.addEventListener('close', () => {
      console.log('WebSocket closed');
    });
  }

  send(message) {
    this.socket.send(message);
  }

  on(event, callback) {
    this.socket.addEventListener(event, callback);
  }

  sendOnConnect(message) {
    this.socket.addEventListener('open', () => {
      this.send(message);
    });
  }
}

export { CraftSocketClient };
