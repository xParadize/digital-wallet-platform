import http from 'k6/http';
import {sleep} from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 100 },
        { duration: '2m', target: 200 },
        { duration: '1m', target: 100 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
        http_req_failed: ['rate<0.05'],
    },
};

const BASE_URL = 'http://localhost:8991/api/v1';
const JWT_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJpZCI6IjE1NWI2ZWFkLWZkNDEtNDM3ZC1iNWUwLTY1MjQ0MGU3ZGZjZCIsImVtYWlsIjoiaXZhbW4uc21pdGhAZXhhbXBsZS5jb20iLCJzdWIiOiIxNTViNmVhZC1mZDQxLTQzN2QtYjVlMC02NTI0NDBlN2RmY2QiLCJpYXQiOjE3NjM4OTMzMTYsImV4cCI6MTc2NDQ5ODExNn0.Dsf_SagpKxPLKn7s6FjpY4Ox_rA4C0o9OdThdBIDleg';

const SORT_COMBINATIONS = [
    { sort: null, order: null },
    { sort: 'RECENT', order: 'ASC' },
    { sort: 'NAME', order: 'AALPH' },
    { sort: 'NAME', order: 'DALPH' },
    { sort: 'BALANCE', order: 'ASC' },
    { sort: 'BALANCE', order: 'DESC' },
    { sort: 'EXPIRATION', order: 'EARLIEST' },
    { sort: 'EXPIRATION', order: 'LATEST' },
    { sort: 'LIMIT', order: 'ASC' },
    { sort: 'LIMIT', order: 'DESC' },
];

const params = {
    headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${JWT_TOKEN}`,
    },
    timeout: '10s',
};

export default function () {
    const combo = SORT_COMBINATIONS[Math.floor(Math.random() * SORT_COMBINATIONS.length)];
    const offset = Math.floor(Math.random() * 51);
    const limit = Math.floor(Math.random() * 20) + 10;

    const queryParams = [];
    if (combo.sort) queryParams.push(`sort=${combo.sort}`);
    if (combo.order) queryParams.push(`order=${combo.order}`);
    queryParams.push(`offset=${offset}`);
    queryParams.push(`limit=${limit}`);

    const url = `${BASE_URL}/cards?${queryParams.join('&')}`;
    http.get(url, params);
    sleep(0.1);
}