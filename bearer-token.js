const jwt = require('jsonwebtoken');

// Generate a JWT Bearer token
const username = 'testuser';
const password = 'testpassword';
const payload = {
  sub: username,
  iat: Math.floor(Date.now() / 1000),
  exp: Math.floor(Date.now() / 1000) + (60 * 60), // Expires in 1 hour
};
const secret = Buffer.from(password, 'base64');
const token = jwt.sign(payload, secret);

console.log(token);
