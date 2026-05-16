import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';
import {
    BASE_URL,
    SUCCESS,
    loadUsers,
    parseJson,
    pickRandom,
    randomInt,
    authHeaders,
    recordApiCode,
    apiResponseIsValid,
    noBadRequest,
    tokenIsValid,
    noForbidden,
    noNotFound,
    noSystemError,
} from './common.js';

const PAGE_SIZE = Number(__ENV.PAGE_SIZE || 10);
const MAX_PAGE = Number(__ENV.MAX_PAGE || 20);

const counters = {
    successCount: new Counter('post_page_success_count'),
    badRequestCount: new Counter('post_page_bad_request_count'),
    unauthorizedCount: new Counter('post_page_unauthorized_count'),
    forbiddenCount: new Counter('post_page_forbidden_count'),
    notFoundCount: new Counter('post_page_not_found_count'),
    systemErrorCount: new Counter('post_page_system_error_count'),
    invalidResponseCount: new Counter('post_page_invalid_response_count'),
};

const users = loadUsers();

export const options = {
    scenarios: {
        post_page_read: {
            executor: 'constant-vus',
            vus: Number(__ENV.VUS || 50),
            duration: __ENV.DURATION || '30s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],

        // p95 表示 95% 的请求耗时小于该值；p99 表示 99% 的请求耗时小于该值。
        // p99 更能反映少数慢请求，也就是尾延迟。
        http_req_duration: ['p(95)<500', 'p(99)<1000'],

        checks: ['rate>0.99'],
        post_page_system_error_count: ['count==0'],
        post_page_unauthorized_count: ['count==0'],
        post_page_forbidden_count: ['count==0'],
    },
};

export default function () {
    const user = pickRandom(users);
    const current = randomInt(1, MAX_PAGE);

    const res = http.get(
        `${BASE_URL}/posts?current=${current}&size=${PAGE_SIZE}`,
        {
            headers: authHeaders(user),
            tags: {
                api: 'post_page',
            },
        }
    );

    const body = parseJson(res);
    recordApiCode(body, counters);

    check(res, {
        'http status is 200': (r) => r.status === 200,
        'api response is valid': () => apiResponseIsValid(body),
        'api code is success': () => body !== null && body.code === SUCCESS,
        'page data is valid': () => {
            return body !== null
                && body.data !== null
                && typeof body.data.current === 'number'
                && typeof body.data.size === 'number'
                && typeof body.data.total === 'number'
                && Array.isArray(body.data.records);
        },
        'no bad request': () => noBadRequest(body),
        'token is valid': () => tokenIsValid(body),
        'no forbidden': () => noForbidden(body),
        'no not found': () => noNotFound(body),
        'no system error': () => noSystemError(body),
    });

    sleep(0.3);
}