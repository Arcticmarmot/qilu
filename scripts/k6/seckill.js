import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';
import exec from 'k6/execution';
import { Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const SECKILL_ID = __ENV.SECKILL_ID || '1';

const SUCCESS = 0;
const BAD_REQUEST = 40000;
const UNAUTHORIZED = 40100;
const FORBIDDEN = 40300;
const NOT_FOUND = 40400;
const CONFLICT = 40900;
const SYSTEM_ERROR = 50000;

const successCount = new Counter('seckill_success_count');
const conflictCount = new Counter('seckill_conflict_count');
const badRequestCount = new Counter('seckill_bad_request_count');
const unauthorizedCount = new Counter('seckill_unauthorized_count');
const forbiddenCount = new Counter('seckill_forbidden_count');
const notFoundCount = new Counter('seckill_not_found_count');
const systemErrorCount = new Counter('seckill_system_error_count');
const invalidResponseCount = new Counter('seckill_invalid_response_count');

const users = new SharedArray('users', function () {
    return JSON.parse(open('./users.json'));
});

export const options = {
    scenarios: {
        seckill_once: {
            executor: 'shared-iterations',
            vus: 100,
            iterations: 100,
            maxDuration: '10s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
        checks: ['rate>0.95'],
        seckill_system_error_count: ['count==0'],
        seckill_unauthorized_count: ['count==0'],
        seckill_not_found_count: ['count==0'],
        seckill_forbidden_count: ['count==0'],
    },
};

export default function () {
    const index = exec.scenario.iterationInTest;
    const user = users[index % users.length];

    const res = http.post(
        `${BASE_URL}/voucher-seckills/${SECKILL_ID}/orders`,
        null,
        {
            headers: {
                Authorization: `Bearer ${user.token}`,
            },
        }
    );

    const body = parseJson(res);

    recordCode(body);

    check(res, {
        'http status is 200': (r) => r.status === 200,

        'api response is valid': () => {
            return body !== null
                && typeof body.code === 'number'
                && typeof body.message === 'string';
        },

        'api code is acceptable': () => {
            return body !== null
                && (body.code === SUCCESS || body.code === CONFLICT);
        },

        'success response status is processing': () => {
            if (body === null) {
                return false;
            }

            if (body.code !== SUCCESS) {
                return true;
            }

            return body.data !== null
                && body.data !== undefined
                && body.data.status === 'processing';
        },

        'no bad request': () => body !== null && body.code !== BAD_REQUEST,
        'token is valid': () => body !== null && body.code !== UNAUTHORIZED,
        'activity is available': () => body !== null && body.code !== FORBIDDEN && body.code !== NOT_FOUND,
        'no system error': () => body !== null && body.code !== SYSTEM_ERROR,
    });
}

function parseJson(res) {
    try {
        return res.json();
    } catch (e) {
        return null;
    }
}

function recordCode(body) {
    if (body === null || typeof body.code !== 'number') {
        invalidResponseCount.add(1);
        return;
    }

    if (body.code === SUCCESS) {
        successCount.add(1);
        return;
    }

    if (body.code === CONFLICT) {
        conflictCount.add(1);
        return;
    }

    if (body.code === BAD_REQUEST) {
        badRequestCount.add(1);
        return;
    }

    if (body.code === UNAUTHORIZED) {
        unauthorizedCount.add(1);
        return;
    }

    if (body.code === FORBIDDEN) {
        forbiddenCount.add(1);
        return;
    }

    if (body.code === NOT_FOUND) {
        notFoundCount.add(1);
        return;
    }

    if (body.code === SYSTEM_ERROR) {
        systemErrorCount.add(1);
        return;
    }

    invalidResponseCount.add(1);
}