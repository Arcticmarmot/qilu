import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { Counter } from 'k6/metrics';
import {
    BASE_URL,
    SUCCESS,
    loadUsers,
    parseJson,
    pickRandom,
    authHeaders,
    recordApiCode,
    apiResponseIsValid,
    noBadRequest,
    tokenIsValid,
    noForbidden,
    noNotFound,
    noSystemError,
} from './common.js';

const counters = {
    successCount: new Counter('search_posts_success_count'),
    badRequestCount: new Counter('search_posts_bad_request_count'),
    unauthorizedCount: new Counter('search_posts_unauthorized_count'),
    forbiddenCount: new Counter('search_posts_forbidden_count'),
    notFoundCount: new Counter('search_posts_not_found_count'),
    systemErrorCount: new Counter('search_posts_system_error_count'),
    invalidResponseCount: new Counter('search_posts_invalid_response_count'),
};

const users = loadUsers();

const keywords = new SharedArray('keywords', function () {
    return [
        '草地',
        '野餐',
        '城市',
        '水岸',
        '山野',
        '露营',
        '晴天',
        '生活',
        '平衡',
        '自然',
        '运动',
        '厨房',
        '阅读',
        '工作',
        '旅行',
        '情绪',
        '风景',
        '人物',
        '阳光',
        '慢时光',
    ];
});

export const options = {
    scenarios: {
        search_posts_read: {
            executor: 'constant-vus',
            vus: Number(__ENV.VUS || 30),
            duration: __ENV.DURATION || '30s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],

        // 搜索通常比热榜更重，因为会访问搜索索引。
        http_req_duration: ['p(95)<500', 'p(99)<1000'],

        checks: ['rate>0.99'],
        search_posts_system_error_count: ['count==0'],
        search_posts_unauthorized_count: ['count==0'],
        search_posts_forbidden_count: ['count==0'],
    },
};

export default function () {
    const user = pickRandom(users);
    const keyword = pickRandom(keywords);

    const res = http.get(
        `${BASE_URL}/search/posts?keyword=${encodeURIComponent(keyword)}`,
        {
            headers: authHeaders(user),
            tags: {
                api: 'search_posts',
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

    sleep(0.5);
}