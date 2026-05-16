import fs from 'fs/promises';
import path from 'path';
import { fileURLToPath } from 'url';

export const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';

export const SUCCESS = 0;
export const BAD_REQUEST = 40000;
export const UNAUTHORIZED = 40100;
export const FORBIDDEN = 40300;
export const NOT_FOUND = 40400;
export const CONFLICT = 40900;
export const SYSTEM_ERROR = 50000;

export const VISIBILITY_PUBLIC = 1;

export function currentDir(importMetaUrl) {
    const filename = fileURLToPath(importMetaUrl);
    return path.dirname(filename);
}

export function resolvePath(baseDir, relativePath) {
    return path.resolve(baseDir, relativePath);
}

export async function readJsonFile(baseDir, filePath) {
    const raw = await fs.readFile(resolvePath(baseDir, filePath), 'utf-8');
    return JSON.parse(raw);
}

export async function writeJsonFile(baseDir, filePath, data) {
    const outputPath = resolvePath(baseDir, filePath);
    await fs.mkdir(path.dirname(outputPath), { recursive: true });
    await fs.writeFile(outputPath, JSON.stringify(data, null, 2));
}

export async function requestJson(pathname, options = {}) {
    const url = `${BASE_URL}${pathname}`;
    const response = await fetch(url, options);

    let body;

    try {
        body = await response.json();
    } catch (e) {
        throw new Error(`invalid json response, url=${url}, httpStatus=${response.status}`);
    }

    return {
        httpStatus: response.status,
        body,
    };
}

export function assertApiSuccess(result, action) {
    if (result.httpStatus !== 200) {
        throw new Error(`${action} failed, httpStatus=${result.httpStatus}`);
    }

    if (!result.body || result.body.code !== SUCCESS) {
        throw new Error(
            `${action} failed, code=${result.body?.code}, message=${result.body?.message}`
        );
    }
}

export function getCreatedId(result, action) {
    assertApiSuccess(result, action);

    const id = result.body.data;

    if (!Number.isInteger(id) || id <= 0) {
        throw new Error(`${action} failed, created id is invalid, id=${id}`);
    }

    return id;
}

export function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export function pickRandom(list) {
    return list[randomInt(0, list.length - 1)];
}

export function getContentType(filename) {
    const lower = filename.toLowerCase();

    if (lower.endsWith('.png')) {
        return 'image/png';
    }

    if (lower.endsWith('.webp')) {
        return 'image/webp';
    }

    return 'image/jpeg';
}

export async function readUsers(baseDir, usersFile) {
    const users = await readJsonFile(baseDir, usersFile);

    if (!Array.isArray(users) || users.length === 0) {
        throw new Error(`users file is empty, file=${usersFile}`);
    }

    for (const user of users) {
        if (!user.token) {
            throw new Error(`user token is missing, email=${user.email}`);
        }
    }

    return users;
}

export function pickActor(users, authorEmail) {
    const candidates = users.filter((user) => user.email !== authorEmail);

    if (candidates.length === 0) {
        return pickRandom(users);
    }

    return pickRandom(candidates);
}

export function flattenPosts(seedResult) {
    const posts = [];

    function walk(node) {
        if (!node || !node.id) {
            return;
        }

        posts.push({
            id: node.id,
            sourceIndex: node.sourceIndex,
            title: node.title,
            category: node.category,
            authorEmail: node.authorEmail,
            depth: node.depth,
        });

        if (Array.isArray(node.children)) {
            for (const child of node.children) {
                walk(child);
            }
        }
    }

    for (const root of seedResult.roots || []) {
        walk(root);
    }

    return posts;
}