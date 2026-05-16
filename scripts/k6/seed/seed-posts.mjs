import fs from 'fs/promises';
import path from 'path';
import {
    currentDir,
    resolvePath,
    readJsonFile,
    writeJsonFile,
    readUsers,
    requestJson,
    assertApiSuccess,
    getCreatedId,
    randomInt,
    getContentType,
    VISIBILITY_PUBLIC,
} from './common.mjs';

const __dirname = currentDir(import.meta.url);

const USERS_FILE = process.env.USERS_FILE || './data/users.json';
const POSTS_FILE = process.env.POSTS_FILE || './data/posts.json';
const ASSETS_DIR = process.env.ASSETS_DIR || './assets';
const OUTPUT_FILE = process.env.OUTPUT_FILE || './results/seed-posts-result.json';

const ROOT_POST_COUNT = Number(process.env.ROOT_POST_COUNT || 100);

const MIN_BRANCH_COUNT = Number(process.env.MIN_BRANCH_COUNT || 1);
const MAX_BRANCH_COUNT = Number(process.env.MAX_BRANCH_COUNT || 3);

const MIN_TREE_DEPTH = Number(process.env.MIN_TREE_DEPTH || 2);
const MAX_TREE_DEPTH = Number(process.env.MAX_TREE_DEPTH || 5);

// 越深越不容易继续生长。这个值是基础生长概率。
const BRANCH_CONTINUE_RATE = Number(process.env.BRANCH_CONTINUE_RATE || 0.65);

async function readSeedPosts() {
    const posts = await readJsonFile(__dirname, POSTS_FILE);

    if (!Array.isArray(posts) || posts.length === 0) {
        throw new Error(`posts file is empty, file=${POSTS_FILE}`);
    }

    for (const post of posts) {
        if (!post.index || !post.title || !post.content || !post.branchPrompt || !post.category) {
            throw new Error(`seed post is invalid, index=${post.index}`);
        }

        if (!Array.isArray(post.url)) {
            post.url = [];
        }
    }

    return posts;
}

function shouldCreateNextLevelBranches(depth) {
    const rate = Math.max(0.15, BRANCH_CONTINUE_RATE - depth * 0.12);
    return Math.random() < rate;
}

async function uploadImage(token, filename) {
    const filePath = path.join(resolvePath(__dirname, ASSETS_DIR), filename);
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

    return mediaId;
}

async function uploadPostImages(token, sourcePost, state) {
    const mediaIds = [];

    for (const filename of sourcePost.url) {
        const mediaId = await uploadImage(token, filename);

        mediaIds.push(mediaId);
        state.uploadedMediaCount += 1;

        console.log(`upload image success, filename=${filename}, mediaId=${mediaId}`);
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

function pickBranchSources(seedPosts, category, count, usedIndexes) {
    const sameCategoryPosts = seedPosts.filter((post) => {
        return post.category === category && !usedIndexes.has(post.index);
    });

    const fallbackPosts = seedPosts.filter((post) => {
        return !usedIndexes.has(post.index);
    });

    const candidates = sameCategoryPosts.length >= count ? sameCategoryPosts : fallbackPosts;
    const selected = [];

    while (selected.length < count && candidates.length > 0) {
        const offset = randomInt(0, candidates.length - 1);
        const [post] = candidates.splice(offset, 1);

        selected.push(post);
        usedIndexes.add(post.index);
    }

    return selected;
}

async function createBranchNode({ user, parentPostId, sourcePost, depth, state }) {
    const mediaIds = await uploadPostImages(user.token, sourcePost, state);
    const postId = await createBranchPost(user, parentPostId, sourcePost, mediaIds);

    state.totalCreated += 1;

    console.log(
        `create branch post success, postId=${postId}, parentPostId=${parentPostId}, sourceIndex=${sourcePost.index}, depth=${depth}`
    );

    return {
        id: postId,
        sourceIndex: sourcePost.index,
        category: sourcePost.category,
        title: sourcePost.title,
        branchPrompt: sourcePost.branchPrompt,
        depth,
        mediaIds,
        children: [],
    };
}

async function createBranchChildren({
                                        user,
                                        parentNode,
                                        parentSource,
                                        seedPosts,
                                        usedIndexes,
                                        depth,
                                        maxDepth,
                                        state,
                                    }) {
    if (depth > maxDepth) {
        return;
    }

    const branchCount = randomInt(MIN_BRANCH_COUNT, MAX_BRANCH_COUNT);

    const branchSources = pickBranchSources(
        seedPosts,
        parentSource.category,
        branchCount,
        usedIndexes
    );

    for (const branchSource of branchSources) {
        const branchNode = await createBranchNode({
            user,
            parentPostId: parentNode.id,
            sourcePost: branchSource,
            depth,
            state,
        });

        parentNode.children.push(branchNode);

        if (depth < maxDepth && shouldCreateNextLevelBranches(depth)) {
            await createBranchChildren({
                user,
                parentNode: branchNode,
                parentSource: branchSource,
                seedPosts,
                usedIndexes,
                depth: depth + 1,
                maxDepth,
                state,
            });
        }
    }
}

async function main() {
    const users = await readUsers(__dirname, USERS_FILE);
    const seedPosts = await readSeedPosts();

    const rootSources = seedPosts.slice(0, Math.min(ROOT_POST_COUNT, seedPosts.length));

    const state = {
        totalCreated: 0,
        uploadedMediaCount: 0,
    };

    const seedResult = {
        usersFile: USERS_FILE,
        postsFile: POSTS_FILE,
        assetsDir: ASSETS_DIR,
        rootPostCount: rootSources.length,
        minBranchCount: MIN_BRANCH_COUNT,
        maxBranchCount: MAX_BRANCH_COUNT,
        minTreeDepth: MIN_TREE_DEPTH,
        maxTreeDepth: MAX_TREE_DEPTH,
        branchContinueRate: BRANCH_CONTINUE_RATE,
        roots: [],
    };

    console.log(`seed posts started, rootPostCount=${rootSources.length}`);

    const maxDepth = MAX_TREE_DEPTH;

    for (let i = 0; i < rootSources.length; i++) {
        const user = users[i % users.length];
        const rootSource = rootSources[i];

        // 根节点忽略 branchPrompt，统一创建公开帖子。
        const mediaIds = await uploadPostImages(user.token, rootSource, state);
        const rootPostId = await createRootPost(user, rootSource, mediaIds);

        state.totalCreated += 1;

        const rootNode = {
            id: rootPostId,
            sourceIndex: rootSource.index,
            category: rootSource.category,
            title: rootSource.title,
            depth: 1,
            maxDepth,
            mediaIds,
            authorEmail: user.email,
            children: [],
        };

        seedResult.roots.push(rootNode);

        console.log(
            `create root post success, index=${i + 1}, postId=${rootPostId}, sourceIndex=${rootSource.index}, category=${rootSource.category}, maxDepth=${maxDepth}`
        );

        await createBranchChildren({
            user,
            parentNode: rootNode,
            parentSource: rootSource,
            seedPosts,
            usedIndexes: new Set([rootSource.index]),
            depth: 2,
            maxDepth,
            state,
        });
    }

    seedResult.totalCreated = state.totalCreated;
    seedResult.uploadedMediaCount = state.uploadedMediaCount;

    await writeJsonFile(__dirname, OUTPUT_FILE, seedResult);

    console.log('');
    console.log(
        `seed posts success, totalCreated=${state.totalCreated}, uploadedMediaCount=${state.uploadedMediaCount}, output=${OUTPUT_FILE}`
    );
}

main().catch((error) => {
    console.error('');
    console.error(error.message);
    process.exit(1);
});