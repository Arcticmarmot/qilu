import {
    currentDir,
    readJsonFile,
    writeJsonFile,
    readUsers,
    flattenPosts,
    requestJson,
    getCreatedId,
    pickRandom,
    SUCCESS,
    CONFLICT,
} from '../lib/common.mjs';

const __dirname = currentDir(import.meta.url);

const USERS_FILE = process.env.USERS_FILE || './data/users.json';
const SEED_POSTS_FILE = process.env.SEED_POSTS_FILE || './results/seed-posts-result.json';
const OUTPUT_FILE = process.env.OUTPUT_FILE || './results/seed-interactions-result.json';

const LIKE_COUNT = Number(process.env.LIKE_COUNT || 2000);
const COMMENT_COUNT = Number(process.env.COMMENT_COUNT || 600);
const REPLY_COUNT = Number(process.env.REPLY_COUNT || 800);

const commentContents = [
    '确实，说得太对了。',
    '这个角度很有意思，之前没这么想过。',
    '真实，有时候就是这种感觉。',
    '这段写得很有画面感。',
    '我也有类似的经历，看完很有共鸣。',
    '这个分支很适合继续展开。',
    '感觉这里可以再补充一点细节。',
    '这种表达挺自然的，不像硬凑出来的内容。',
    '我喜欢这个标题，有一种慢慢走进去的感觉。',
    '这个场景很适合做成树状内容。',
    '看完有点想出去走走。',
    '这一段很松弛，但又不是空的。',
    '内容挺真实的，有生活感。',
    '这个选择分支很有代入感。',
    '感觉平台有这种内容之后会更像真实社区。',
    '这个点可以收藏一下。',
    '有些地方说得很克制，但很准确。',
    '这类内容很适合慢慢读。',
    '我觉得这里的情绪很稳。',
    '这一条放在热榜里应该挺自然的。',
    '这张图和文字还挺搭的。',
    '如果后面能继续写分支，会更有意思。',
    '这个主题适合继续讨论。',
    '确实不是所有内容都适合线性阅读。',
    '这让我想到很多日常里的小选择。',
    '有时候简单一点反而更舒服。',
    '这个故事感挺好。',
    '这里的分支提示很自然。',
    '看起来像真实用户会发的内容。',
    '这种内容比纯测试数据好多了。',
];

const replyContents = [
    '我也是这么想的。',
    '对，这里最重要的其实是真实感。',
    '哈哈，这个说法挺准确。',
    '这个分支可以继续往下写。',
    '感觉你抓住了重点。',
    '确实，细节比结论更有意思。',
    '我觉得这里还可以补一段个人经历。',
    '这个角度挺适合展开成另一个分支。',
    '同意，读起来很自然。',
    '是的，真实社区就需要这种轻一点的互动。',
    '这个评论让我又想回去看原文。',
    '我觉得这类内容适合放在首页。',
    '如果配图再多一点，观感会更好。',
    '这个节点很适合继续探索。',
    '对，树状内容的优势就在这里。',
    '我觉得这里不是简单的图文，而是选择感。',
    '这种慢节奏内容挺适合歧路。',
    '确实，平台感一下子出来了。',
    '这里可以作为一个典型样例。',
    '有道理，继续往下读会更自然。',
];

async function readSeedPosts() {
    const seedResult = await readJsonFile(__dirname, SEED_POSTS_FILE);

    if (!seedResult || !Array.isArray(seedResult.roots) || seedResult.roots.length === 0) {
        throw new Error(`seed posts result is empty, file=${SEED_POSTS_FILE}`);
    }

    return seedResult;
}

async function likePost(user, post) {
    const result = await requestJson(`/posts/${post.id}/likes`, {
        method: 'POST',
        headers: {
            Authorization: `Bearer ${user.token}`,
        },
    });

    if (result.httpStatus !== 200) {
        throw new Error(`like post failed, postId=${post.id}, httpStatus=${result.httpStatus}`);
    }

    if (!result.body || ![SUCCESS, CONFLICT].includes(result.body.code)) {
        throw new Error(
            `like post failed, postId=${post.id}, code=${result.body?.code}, message=${result.body?.message}`
        );
    }

    return result.body.code === SUCCESS;
}

async function createComment(user, post, content) {
    const result = await requestJson(`/posts/${post.id}/comments`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${user.token}`,
        },
        body: JSON.stringify({
            content,
        }),
    });

    const commentId = getCreatedId(result, `create comment, postId=${post.id}`);

    return {
        id: commentId,
        postId: post.id,
        content,
        actorEmail: user.email,
    };
}

async function createReply(user, comment, content) {
    const result = await requestJson(`/posts/${comment.postId}/comments/${comment.id}/replies`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${user.token}`,
        },
        body: JSON.stringify({
            parentReplyId: null,
            content,
        }),
    });

    const replyId = getCreatedId(
        result,
        `create reply, postId=${comment.postId}, commentId=${comment.id}`
    );

    return {
        id: replyId,
        postId: comment.postId,
        commentId: comment.id,
        content,
        actorEmail: user.email,
    };
}

async function seedLikes(users, posts, state, seedResult) {
    const likedPairs = new Set();
    let attempts = 0;

    while (state.likeCreated < LIKE_COUNT) {
        attempts += 1;

        const post = pickRandom(posts);
        const user = pickRandom(users);
        const pairKey = `${user.email}:${post.id}`;

        if (likedPairs.has(pairKey)) {
            continue;
        }

        likedPairs.add(pairKey);

        const created = await likePost(user, post);

        if (created) {
            state.likeCreated += 1;
        } else {
            state.likeSkipped += 1;
        }

        if ((state.likeCreated + state.likeSkipped) % 100 === 0) {
            console.log(
                `seed likes progress, created=${state.likeCreated}, skipped=${state.likeSkipped}`
            );
        }
    }

    seedResult.likes = {
        target: LIKE_COUNT,
        created: state.likeCreated,
        skipped: state.likeSkipped,
        attempts,
    };
}

async function seedComments(users, posts, state, seedResult) {
    for (let i = 0; i < COMMENT_COUNT; i++) {
        const post = pickRandom(posts);
        const user = pickRandom(users);
        const content = pickRandom(commentContents);

        const comment = await createComment(user, post, content);

        state.commentCreated += 1;
        state.comments.push(comment);

        if (state.commentCreated % 100 === 0) {
            console.log(`seed comments progress, created=${state.commentCreated}`);
        }
    }

    seedResult.comments = {
        target: COMMENT_COUNT,
        created: state.commentCreated,
    };
}

async function seedReplies(users, state, seedResult) {
    if (state.comments.length === 0) {
        seedResult.replies = {
            target: REPLY_COUNT,
            created: 0,
            skipped: REPLY_COUNT,
            reason: 'no comments available',
        };
        return;
    }

    for (let i = 0; i < REPLY_COUNT; i++) {
        const comment = pickRandom(state.comments);
        const user = pickRandom(users);
        const content = pickRandom(replyContents);

        const reply = await createReply(user, comment, content);

        state.replyCreated += 1;
        state.replies.push(reply);

        if (state.replyCreated % 100 === 0) {
            console.log(`seed replies progress, created=${state.replyCreated}`);
        }
    }

    seedResult.replies = {
        target: REPLY_COUNT,
        created: state.replyCreated,
    };
}

async function main() {
    const users = await readUsers(__dirname, USERS_FILE);
    const seedPosts = await readSeedPosts();
    const posts = flattenPosts(seedPosts);

    if (posts.length === 0) {
        throw new Error('flatten posts result is empty');
    }

    const state = {
        likeCreated: 0,
        likeSkipped: 0,
        commentCreated: 0,
        replyCreated: 0,
        comments: [],
        replies: [],
    };

    const seedResult = {
        usersFile: USERS_FILE,
        seedPostsFile: SEED_POSTS_FILE,
        postCount: posts.length,
        userCount: users.length,
        likeCount: LIKE_COUNT,
        commentCount: COMMENT_COUNT,
        replyCount: REPLY_COUNT,
    };

    console.log(
        `seed interactions started, users=${users.length}, posts=${posts.length}, likes=${LIKE_COUNT}, comments=${COMMENT_COUNT}, replies=${REPLY_COUNT}`
    );

    await seedLikes(users, posts, state, seedResult);
    await seedComments(users, posts, state, seedResult);
    await seedReplies(users, state, seedResult);

    seedResult.commentSamples = state.comments.slice(0, 20);
    seedResult.replySamples = state.replies.slice(0, 20);

    await writeJsonFile(__dirname, OUTPUT_FILE, seedResult);

    console.log('');
    console.log(
        `seed interactions success, likes=${state.likeCreated}, comments=${state.commentCreated}, replies=${state.replyCreated}, output=${OUTPUT_FILE}`
    );
}

main().catch((error) => {
    console.error('');
    console.error(error.message);
    process.exit(1);
});