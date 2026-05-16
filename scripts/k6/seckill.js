import http from 'k6/http';
import { check } from 'k6';
import exec from 'k6/execution';
import { Counter } from 'k6/metrics';
import {
    BASE_URL,
    SUCCESS,
    BAD_REQUEST,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    CONFLICT,
    SYSTEM_ERROR,
    loadUsers,
    parseJson,
    authHeaders,
    recordApiCode,
    apiResponseIsValid,
} from './common.js';

const SECKILL_ID = __ENV.SECKILL_ID || '1';

const counters = {
    successCount: new Counter('seckill_success_count'),
    conflictCount: new Counter('seckill_conflict_count'),
    badRequestCount: new Counter('seckill_bad_request_count'),
    unauthorizedCount: new Counter('seckill_unauthorized_count'),
    forbiddenCount: new Counter('seckill_forbidden_count'),
    notFoundCount: new Counter('seckill_not_found_count'),
    systemErrorCount: new Counter('seckill_system_error_count'),
    invalidResponseCount: new Counter('seckill_invalid_response_count'),
};

const users = loadUsers();

export const options = {
    scenarios: {
        seckill_once: {
            executor: 'shared-iterations',
            vus: Number(__ENV.VUS || 100),
            iterations: Number(__ENV.ITERATIONS || 100),
            maxDuration: __ENV.MAX_DURATION || '10s',
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
            headers: authHeaders(user),
        }
    );

    const body = parseJson(res);
    recordApiCode(body, counters);

    check(res, {
        'http status is 200': (r) => r.status === 200,
        'api response is valid': () => apiResponseIsValid(body),
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
        'activity is available': () => {
            return body !== null
                && body.code !== FORBIDDEN
                && body.code !== NOT_FOUND;
        },
        'no system error': () => body !== null && body.code !== SYSTEM_ERROR,
    });
}