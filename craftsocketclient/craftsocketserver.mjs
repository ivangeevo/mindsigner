import fs from 'fs';
import tls from 'tls';

const options = {
  key: fs.readFileSync('./mykey.key'),
  cert: fs.readFileSync('./mycert.pem')
};

const server = tls.createServer(options, (socket) => {
  socket.write('Hello, world!');
  socket.end();
});

server.listen(8080, () => {
  console.log('Server listening on port 8080');
});
