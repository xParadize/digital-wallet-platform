import http from 'k6/http';
import {sleep} from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 38 },
        { duration: '1m', target: 75 },
        { duration: '2m', target: 150 },
        { duration: '1m', target: 38 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
        http_req_failed: ['rate<0.1'],
    },
};

const BASE_URL = 'http://localhost:8991/api/v1';
const JWT_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJpZCI6IjE1NWI2ZWFkLWZkNDEtNDM3ZC1iNWUwLTY1MjQ0MGU3ZGZjZCIsImVtYWlsIjoiaXZhbW4uc21pdGhAZXhhbXBsZS5jb20iLCJzdWIiOiIxNTViNmVhZC1mZDQxLTQzN2QtYjVlMC02NTI0NDBlN2RmY2QiLCJpYXQiOjE3NjI4NTc3MzIsImV4cCI6MTc2MzQ2MjUzMn0.rPMNNSqIf4_iE2zUwHMsrwifkowoE9ZKuOp-N09DN04';
const USER_ID = '155b6ead-fd41-437d-b5e0-652440e7dfcd';

const CARD_IDS = Array.from({ length: 50 }, (_, i) => i + 1);

const params = {
    headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${JWT_TOKEN}`,
    },
    timeout: '10s',
};

export default function () {
    const cardId = CARD_IDS[Math.floor(Math.random() * CARD_IDS.length)];
    http.get(`${BASE_URL}/cards/${cardId}?userId=${USER_ID}`, params);
    sleep(0.1);
}
