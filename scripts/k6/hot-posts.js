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
const MAX_PAGE = Number(__ENV.MAX_PAGE || 10);

const counters = {
    successCount: new Counter('hot_posts_success_count'),
    badRequestCount: new Counter('hot_posts_bad_request_count'),
    unauthorizedCount: new Counter('hot_posts_unauthorized_count'),
    forbiddenCount: new Counter('hot_posts_forbidden_count'),
    notFoundCount: new Counter('hot_posts_not_found_count'),
    systemErrorCount: new Counter('hot_posts_system_error_count'),
    invalidResponseCount: new Counter('hot_posts_invalid_response_count'),
};

const users = loadUsers();

export const options = {
    scenarios: {
        hot_posts_read: {
            executor: 'constant-vus',
            vus: Number(__ENV.VUS || 50),
            duration: __ENV.DURATION || '30s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],

        // 热榜通常应该比普通分页更快，因为理想情况下主要读 Redis 热榜结果。
        http_req_duration: ['p(95)<300', 'p(99)<800'],

        checks: ['rate>0.99'],
        hot_posts_system_error_count: ['count==0'],
        hot_posts_unauthorized_count: ['count==0'],
        hot_posts_forbidden_count: ['count==0'],
    },
};

export default function () {
    const user = pickRandom(users);
    const current = randomInt(1, MAX_PAGE);

    const res = http.get(
        `${BASE_URL}/hot/posts?current=${current}&size=${PAGE_SIZE}`,
        {
            headers: authHeaders(user),
            tags: {
                api: 'hot_posts',
            },
        }
    );

    const body = parseJson(res);
    recordApiCode(body, counters);

    check(res, {
        'http status is 200': (r) => r.status === 200,
        'api response is valid': () => apiResponseIsValid(body),
        'api code is success': () => body !== null && body.code === SUCCESS,
        'hot page data is valid': () => {
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