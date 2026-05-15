import fs from 'fs/promises';
import path from 'path';
import { fileURLToPath } from 'url';

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';
const USERS_FILE = process.env.USERS_FILE || './data/users.json';
const POSTS_FILE = process.env.POSTS_FILE || './data/posts.json';
const ASSETS_DIR = process.env.ASSETS_DIR || './assets';
const OUTPUT_FILE = process.env.OUTPUT_FILE || './results/seed-posts-result.json';

const ROOT_POST_COUNT = Number(process.env.ROOT_POST_COUNT || 100);
const MIN_BRANCH_COUNT = Number(process.env.MIN_BRANCH_COUNT || 1);
const MAX_BRANCH_COUNT = Number(process.env.MAX_BRANCH_COUNT || 3);
const MIN_TREE_DEPTH = Number(process.env.MIN_TREE_DEPTH || 1);
const MAX_TREE_DEPTH = Number(process.env.MAX_TREE_DEPTH || 5);

// 防止 100 个根帖在 5 层树下爆炸式创建，默认最多创建 600 个帖子节点。
const MAX_TOTAL_POST_COUNT = Number(process.env.MAX_TOTAL_POST_COUNT || 600);

// 控制分支是否继续向下生长。越大树越深，节点越多。
const BRANCH_CONTINUE_RATE = Number(process.env.BRANCH_CONTINUE_RATE || 0.65);

const SUCCESS = 0;
const VISIBILITY_PUBLIC = 1;

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const state = {
    totalCreated: 0,
    uploadedMediaCount: 0,
};

function resolvePath(relativePath) {
    return path.resolve(__dirname, relativePath);
}

function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function pickRandom(list) {
    return list[randomInt(0, list.length - 1)];
}

function pickRandomItems(list, count, excludeIndexes = new Set()) {
    const candidates = list.filter((item) => !excludeIndexes.has(item.index));
    const result = [];

    while (result.length < count && candidates.length > 0) {
        const offset = randomInt(0, candidates.length - 1);
        const [item] = candidates.splice(offset, 1);
        result.push(item);
    }

    return result;
}

function getContentType(filename) {
    const lower = filename.toLowerCase();

    if (lower.endsWith('.png')) {
        return 'image/png';
    }

    if (lower.endsWith('.webp')) {
        return 'image/webp';
    }

    return 'image/jpeg';
}

async function readUsers() {
    const raw = await fs.readFile(resolvePath(USERS_FILE), 'utf-8');
    const users = JSON.parse(raw);

    if (!Array.isArray(users) || users.length === 0) {
        throw new Error(`users file is empty, file=${USERS_FILE}`);
    }

    for (const user of users) {
        if (!user.token) {
            throw new Error(`user token is missing, email=${user.email}`);
        }
    }

    return users;
}

async function readSeedPosts() {
    const raw = await fs.readFile(resolvePath(POSTS_FILE), 'utf-8');
    const posts = JSON.parse(raw);

    if (!Array.isArray(posts) || posts.length === 0) {
        throw new Error(`posts file is empty, file=${POSTS_FILE}`);
    }

    for (const post of posts) {
        if (!post.index || !post.title || !post.content) {
            throw new Error(`seed post is invalid, index=${post.index}`);
        }

        if (!Array.isArray(post.url)) {
            post.url = [];
        }
    }

    return posts;
}

async function requestJson(pathname, options = {}) {
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

function assertApiSuccess(result, action) {
    if (result.httpStatus !== 200) {
        throw new Error(`${action} failed, httpStatus=${result.httpStatus}`);
    }

    if (!result.body || result.body.code !== SUCCESS) {
        throw new Error(
            `${action} failed, code=${result.body?.code}, message=${result.body?.message}`
        );
    }
}

function getCreatedId(result, action) {
    assertApiSuccess(result, action);

    const id = result.body.data;

    if (!Number.isInteger(id) || id <= 0) {
        throw new Error(`${action} failed, created id is invalid, id=${id}`);
    }

    return id;
}

async function uploadImage(token, filename) {
    const filePath = path.join(resolvePath(ASSETS_DIR), filename);
    const fileBuffer = await fs.readFile(filePath);

    const form = new FormData();
    const blob = new Blob([fileBuffer], {
        type: getContentType(filename),
    });

    form.append('file', blob, filename);

    const result = await requestJson('/media/images', {
        method: 'POST',
        headers: {
            Authorization: `Bearer ${token}`,
        },
        body: form,
    });

    assertApiSuccess(result, `upload image, filename=${filename}`);

    const mediaId = result.body.data?.mediaId;

    if (!Number.isInteger(mediaId) || mediaId <= 0) {
        throw new Error(`upload image failed, media id is invalid, filename=${filename}`);
    }

    console.log(`upload image success, filename=${filename}, mediaId=${mediaId}`);

    return mediaId;
}

async function uploadPostImages(token, sourcePost) {
    const mediaIds = [];

    for (const filename of sourcePost.url) {
        const mediaId = await uploadImage(token, filename);
        mediaIds.push(mediaId);
    }

    return mediaIds;
}

async function createRootPost(user, sourcePost, mediaIds) {
    const result = await requestJson('/posts', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${user.token}`,
        },
        body: JSON.stringify({
            title: sourcePost.title,
            content: sourcePost.content,
            visibility: VISIBILITY_PUBLIC,
            mediaIds,
        }),
    });

    return getCreatedId(result, `create root post, sourceIndex=${sourcePost.index}`);
}

async function createBranchPost(user, parentPostId, sourcePost, mediaIds) {
    const result = await requestJson(`/posts/${parentPostId}/branches`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${user.token}`,
        },
        body: JSON.stringify({
            branchPrompt: sourcePost.branchPrompt,
            title: sourcePost.title,
            content: sourcePost.content,
            mediaIds,
        }),
    });

    return getCreatedId(
        result,
        `create branch post, parentPostId=${parentPostId}, sourceIndex=${sourcePost.index}`
    );
}

function shouldContinue(depth, maxDepth) {
    if (depth >= maxDepth) {
        return false;
    }

    return Math.random() < BRANCH_CONTINUE_RATE;
}

async function createBranchTree(context) {
    const {
        user,
        allPosts,
        parentPostId,
        depth,
        maxDepth,
        pathIndexes,
        createdNode,
        state,
    } = context;

    if (state.totalCreated >= MAX_TOTAL_POST_COUNT) {
        return;
    }

    if (!shouldContinue(depth, maxDepth)) {
        return;
    }

    const branchCount = randomInt(MIN_BRANCH_COUNT, MAX_BRANCH_COUNT);
    const branchSources = pickRandomItems(allPosts, branchCount, pathIndexes);

    for (const branchSource of branchSources) {
        if (state.totalCreated >= MAX_TOTAL_POST_COUNT) {
            return;
        }

        const nextPathIndexes = new Set(pathIndexes);
        nextPathIndexes.add(branchSource.index);

        const mediaIds = await uploadPostImages(user.token, branchSource);
        const branchPostId = await createBranchPost(user, parentPostId, branchSource, mediaIds);

        state.totalCreated += 1;

        const branchNode = {
            id: branchPostId,
            sourceIndex: branchSource.index,
            title: branchSource.title,
            branchPrompt: branchSource.branchPrompt,
            depth,
            mediaIds,
            children: [],
        };

        createdNode.children.push(branchNode);

        console.log(
            `create branch post success, postId=${branchPostId}, parentPostId=${parentPostId}, sourceIndex=${branchSource.index}, depth=${depth}`
        );

        await createBranchTree({
            user,
            allPosts,
            parentPostId: branchPostId,
            depth: depth + 1,
            maxDepth,
            pathIndexes: nextPathIndexes,
            createdNode: branchNode,
            state,
        });
    }
}

async function main() {
    const users = await readUsers();
    const seedPosts = await readSeedPosts();

    const rootSources = seedPosts.slice(0, Math.min(ROOT_POST_COUNT, seedPosts.length));

    const state = {
        totalCreated: 0,
    };

    const seedResult = {
        baseUrl: BASE_URL,
        postsFile: POSTS_FILE,
        assetsDir: ASSETS_DIR,
        rootPostCount: rootSources.length,
        maxTotalPostCount: MAX_TOTAL_POST_COUNT,
        roots: [],
    };

    console.log(
        `seed posts started, rootPostCount=${rootSources.length}, maxTotalPostCount=${MAX_TOTAL_POST_COUNT}`
    );

    for (let i = 0; i < rootSources.length; i++) {
        if (state.totalCreated >= MAX_TOTAL_POST_COUNT) {
            break;
        }

        const user = users[i % users.length];
        const rootSource = rootSources[i];
        const maxDepth = randomInt(MIN_TREE_DEPTH, MAX_TREE_DEPTH);

        // 根节点忽略 branchPrompt，只使用 title/content/mediaIds 创建根帖。
        const mediaIds = await uploadPostImages(user.token, rootSource);
        const rootPostId = await createRootPost(user, rootSource, mediaIds);

        state.totalCreated += 1;

        const rootNode = {
            id: rootPostId,
            sourceIndex: rootSource.index,
            title: rootSource.title,
            depth: 1,
            maxDepth,
            mediaIds,
            authorEmail: user.email,
            children: [],
        };

        seedResult.roots.push(rootNode);

        console.log(
            `create root post success, index=${i + 1}, postId=${rootPostId}, sourceIndex=${rootSource.index}, maxDepth=${maxDepth}`
        );

        const pathIndexes = new Set([rootSource.index]);

        await createBranchTree({
            user,
            allPosts: seedPosts,
            parentPostId: rootPostId,
            depth: 2,
            maxDepth,
            pathIndexes,
            createdNode: rootNode,
            state,
        });
    }

    seedResult.totalCreated = state.totalCreated;

    await fs.writeFile(resolvePath(OUTPUT_FILE), JSON.stringify(seedResult, null, 2));

    console.log('');
    console.log(
        `seed posts success, totalCreated=${state.totalCreated}, output=${OUTPUT_FILE}`
    );
}

main().catch((error) => {
    console.error('');
    console.error(error.message);
    process.exit(1);
});