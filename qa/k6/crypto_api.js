import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import ws from 'k6/ws';

// Load environment variables from config.json
const config = JSON.parse(open('./config.json'));

const HOST = config.HOST;
const USERS = config.USERS || 20;
const DURATION = config.DURATION || '30s';
const SYMBOLS = config.SYMBOLS;

// Custom metric for response time
let responseTime = new Trend('http_reply_duration');

export let options = {
    stages: [
        { duration: '10s', target: USERS / 2 },  // Ramp-up to half users
        { duration: DURATION, target: USERS },   // Stay at full users
        { duration: '10s', target: 0 },         // Ramp-down
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95% of requests should be below 500ms
    },
};

export default function () {
    const headers = {
        'Content-Type': 'application/json',
    };

    let res = http.get(`http://${HOST}/api/v1/crypto/live`, { headers });
    // Validate response
    check(res, {
        'Api Live Status is 200': (r) => r.status === 200,
        'Api Live Response time < 500ms': (r) => r.timings.duration < 500,
    });
    responseTime.add(res.timings.duration);

    const symbol = SYMBOLS[Math.floor(Math.random() * SYMBOLS.length)];
    res = http.get(`http://${HOST}/api/v1/crypto/${symbol}/quote`, { headers });
    // Validate response
    check(res, {
        'Api Crypto Quote Status is 200': (r) => r.status === 200,
        'Api Crypto Quote Response time < 500ms': (r) => r.timings.duration < 500,
    });

    // Record custom metric
    responseTime.add(res.timings.duration);

    handleCryptoWebsocket(symbol);

    sleep(1);
}

function handleCryptoWebsocket(symbol) {

    const url = `ws://${HOST}/ws/future`

    let cryptoAction = {
        name: symbol,
        symbol: symbol,
        quoteDate: new Date(),
        quotePrice: 0,
        quantity: 300
    }
    // Establish WebSocket connection
    const socket = ws.connect(url, null, function (socket) {

        // Send an initial message to the WebSocket server
        socket.send(JSON.stringify({action: 'sendMessage', data: JSON.stringify(cryptoAction)}));

        let messageCount = 0;  // Counter to track the number of received messages

        // Listen for incoming messages
        socket.on('message', function (message) {
            console.log('Received message: ' + message);

            // Increment the message count
            messageCount++;

            // Check if the server responds as expected (optional, adjust based on your protocol)
            check(message, {
                'WebSocket Response is correct': (msg) => msg === '{"status": "OK"}',
            });

            // If 10 messages have been received, close the connection
            if (messageCount >= 10) {
                console.log(`WebSocket Received 10 messages, closing connection.`);
                socket.close();
            }
        });

        // Handle errors
        socket.on('close', function () {
            console.log('Connection closed');
        });

        socket.on('error', function (err) {
            console.error('WebSocket error: ' + err);
        });
    });
}
