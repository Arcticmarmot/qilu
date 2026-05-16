import { SharedArray } from 'k6/data';

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
export const USERS_FILE = __ENV.USERS_FILE || './seed/data/users.json';

export const SUCCESS = 0;
export const BAD_REQUEST = 40000;
export const UNAUTHORIZED = 40100;
export const FORBIDDEN = 40300;
export const NOT_FOUND = 40400;
export const CONFLICT = 40900;
export const SYSTEM_ERROR = 50000;

export function loadUsers() {
    return new SharedArray('users', function () {
        return JSON.parse(open(USERS_FILE));
    });
}

export function parseJson(res) {
    try {
        return res.json();
    } catch (e) {
        return null;
    }
}

export function pickRandom(list) {
    return list[Math.floor(Math.random() * list.length)];
}

export function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export function authHeaders(user) {
    return {
        Authorization: `Bearer ${user.token}`,
    };
}

export function recordApiCode(body, counters) {
    if (body === null || typeof body.code !== 'number') {
        counters.invalidResponseCount.add(1);
        return;
    }

    if (body.code === SUCCESS) {
        counters.successCount.add(1);
        return;
    }

    if (body.code === BAD_REQUEST) {
        counters.badRequestCount.add(1);
        return;
    }

    if (body.code === UNAUTHORIZED) {
        counters.unauthorizedCount.add(1);
        return;
    }

    if (body.code === FORBIDDEN) {
        counters.forbiddenCount.add(1);
        return;
    }

    if (body.code === NOT_FOUND) {
        counters.notFoundCount.add(1);
        return;
    }

    if (body.code === CONFLICT && counters.conflictCount) {
        counters.conflictCount.add(1);
        return;
    }

    if (body.code === SYSTEM_ERROR) {
        counters.systemErrorCount.add(1);
        return;
    }

    counters.invalidResponseCount.add(1);
}

export function apiResponseIsValid(body) {
    return body !== null
        && typeof body.code === 'number'
        && typeof body.message === 'string';
}

export function noBadRequest(body) {
    return body !== null && body.code !== BAD_REQUEST;
}

export function tokenIsValid(body) {
    return body !== null && body.code !== UNAUTHORIZED;
}

export function noForbidden(body) {
    return body !== null && body.code !== FORBIDDEN;
}

export function noNotFound(body) {
    return body !== null && body.code !== NOT_FOUND;
}

export function noSystemError(body) {
    return body !== null && body.code !== SYSTEM_ERROR;
}