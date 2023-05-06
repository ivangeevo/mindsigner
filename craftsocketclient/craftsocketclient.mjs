import dhive = require('@hiveio/dhive');
import CraftSocketClient = require('./CraftSocketClient');

const client = new CraftSocketClient('localhost', 8080);
client.connect();

const clientWif = 'YOUR_ACCOUNT_POSTING_WIF';
const clientAccount = 'YOUR_ACCOUNT_NAME';
const clientKey = dhive.PrivateKey.fromString(clientWif);

const clientClient = new dhive.Client('https://api.hive.blog', {
  timeout: 8 * 1000,
  websocket: client,
});

client.on('open', () => {
  console.log('WebSocket connected');
});

client.on('message', (message) => {
  console.log(`Received message: ${message}`);
});

client.on('error', (error) => {
  console.error(error);
});

client.on('close', () => {
  console.log('WebSocket closed');
});

clientClient.database.call('get_dynamic_global_properties', []).then((properties) => {
  console.log(`Head block number: ${properties.head_block_number}`);
});

const onTransaction = (transaction) => {
  console.log(`Transaction: ${JSON.stringify(transaction)}`);
  const comment = transaction.operations[0][1];
  if (comment.title === '') {
    client.send(comment.body); // Send comment body to CraftSocketServer
  } else {
    client.send(`${comment.title}\n\n${comment.body}`); // Send comment title and body to CraftSocketServer
  }
};

const stream = clientClient.blockchain.getBlockStream();

stream.on('data', (block) => {
  block.transactions.forEach((transaction) => {
    if (transaction.operations[0][0] === 'comment') {
      onTransaction(transaction);
    }
  });
});

stream.on('error', (error) => {
  console.error(error);
});
