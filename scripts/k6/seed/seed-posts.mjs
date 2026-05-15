import fs from 'fs/promises';

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';
const USERS_FILE = process.env.USERS_FILE || './data/users.json';
const OUTPUT_FILE = process.env.OUTPUT_FILE || './results/seed-posts-result.json';

const ROOT_POST_COUNT = Number(process.env.ROOT_POST_COUNT || 100);
const MIN_BRANCH_COUNT = Number(process.env.MIN_BRANCH_COUNT || 1);
const MAX_BRANCH_COUNT = Number(process.env.MAX_BRANCH_COUNT || 3);

const SUCCESS = 0;
const VISIBILITY_PUBLIC = 1;

const seedTag = process.env.SEED_TAG || `qilu-seed-${formatNow()}`;

const topics = [
    {
        scene: '大龙山徒步',
        titles: [
            '大龙山半日徒步路线记录',
            '从安庆师大出发的一条安静山路',
            '适合新手的大龙山轻徒步攻略',
            '雨后大龙山的泥路和风',
        ],
        branchPrompts: [
            '选择轻装路线',
            '绕去看日落',
            '带新手朋友同行',
            '走一条更安静的小路',
        ],
        rootContents: [
            '这条路线不追求强度，更适合想在周末把脑子清空的人。上午从校门附近出发，先经过一段平缓的水泥路，再进入树荫比较密的山路。路上补给不多，建议提前带水。',
            '大龙山最适合的不是打卡，而是慢慢走。一路上坡不算猛烈，但连续走半小时后身体会慢慢热起来。风从林子里穿过来的时候，会有一种从城市边缘暂时退出的感觉。',
            '如果只是想体验徒步，不建议一开始就选太长路线。可以把目标设成两个小时以内，重点是鞋子舒服、水够、不要硬冲。山不大，但认真走完也会有成就感。',
        ],
        branchContents: [
            '轻装路线的关键是不要背太多东西。一瓶水、一包纸巾、一件薄外套基本够用。真正影响体验的往往不是装备不够，而是鞋子磨脚。',
            '如果想看日落，时间要留得更宽一点。下山不要太晚，尤其是对路线不熟的人，宁愿早点结束，也不要摸黑找路。',
            '带新手同行时，不要用自己的体力标准要求别人。更好的节奏是每二十分钟停一下，顺便看看路边的树、石头和远处的城市。',
        ],
    },
    {
        scene: '安庆城市散步',
        titles: [
            '安庆江边散步小记',
            '从老城区走到江边的一下午',
            '适合一个人发呆的安庆路线',
            '长江边的风和慢生活',
        ],
        branchPrompts: [
            '走向江边',
            '拐进老街',
            '找一家咖啡店坐下',
            '晚上再来一次',
        ],
        rootContents: [
            '安庆的江边不一定热闹，但很适合慢慢走。傍晚风会变大，货船从远处经过，城市的声音被水面压低，整个人会安静很多。',
            '这条路线适合没有明确目的的时候走。从老城区出来，穿过几条普通街道，再慢慢接近江边。路上没有强烈的景点感，但日常生活本身就很好看。',
            '一个人散步时，最重要的是不要着急。可以沿着江边走一段，再折回街区吃点东西。这样的路线不贵，也不需要特别计划。',
        ],
        branchContents: [
            '江边路线建议在傍晚走，阳光不刺眼，风也更明显。可以找一个长椅坐十分钟，看船慢慢过去。',
            '老街更适合白天去。很多店铺不一定精致，但有生活气。买一瓶水或者一份小吃，路线就自然继续下去了。',
            '咖啡店不一定要选网红店。对这种散步来说，一个安静的座位、稳定的灯光和能放下包的桌子就够了。',
        ],
    },
    {
        scene: '户外装备选择',
        titles: [
            '新手登山杖到底有没有必要买',
            '遮阳帽和防晒衣怎么选才不踩坑',
            '短途徒步装备清单',
            '第一次户外不要买太多东西',
        ],
        branchPrompts: [
            '预算有限怎么选',
            '更看重轻便',
            '更看重耐用',
            '线下试用再决定',
        ],
        rootContents: [
            '新手最容易犯的错误是一次性买太多装备。真正影响体验的前三项通常是鞋、水和防晒。登山杖有用，但不是每条路线都必须。',
            '遮阳帽的作用不只是遮太阳，也能减少长时间暴晒带来的疲惫。短途路线可以选轻便透气的款式，不一定要追求很专业的型号。',
            '短途徒步装备应该遵循够用原则。水、纸巾、充电宝、简单药品、轻便外套，比很多看起来酷的装备更实际。',
        ],
        branchContents: [
            '预算有限时，优先买舒服的鞋和靠谱的袜子。登山杖、帽子、手套这些可以根据路线逐步补。',
            '轻便路线里，每多一件东西都会增加负担。装备不是越多越安全，而是要和路线强度匹配。',
            '耐用装备适合长期使用，但第一次购买不必一步到位。先用普通装备完成几次短线，再决定是否升级。',
        ],
    },
    {
        scene: '树状内容创作',
        titles: [
            '如果帖子不是一条线，而是一棵树',
            '用分支记录一次旅行的不同选择',
            '把攻略写成对话树会怎样',
            '树状内容适合哪些社区场景',
        ],
        branchPrompts: [
            '选择实用攻略分支',
            '选择故事叙述分支',
            '选择争议讨论分支',
            '选择个人经验分支',
        ],
        rootContents: [
            '线性帖子适合表达一个确定观点，但很多经验本身不是线性的。一次旅行、一次购物、一次学习路线，往往都有多个选择节点。',
            '树状内容的价值在于把选择显式呈现出来。读者不必从头读到尾，而是根据自己的兴趣进入某个分支。',
            '如果把攻略写成对话树，作者就不只是输出结论，而是在模拟一个决策过程。比如预算高低、时间长短、同行人数不同，路线也会不同。',
        ],
        branchContents: [
            '实用攻略分支应该尽量短，直接告诉读者路线、成本、风险和适合人群。',
            '故事叙述分支可以更慢一点，保留细节和情绪，让读者知道这个选择背后的具体场景。',
            '争议讨论分支适合容纳不同看法，比如贵不贵、值不值、适不适合新手。',
        ],
    },
    {
        scene: '后端工程实践',
        titles: [
            '一个内容社区后端应该先做什么',
            'Redis 热榜为什么适合社区项目',
            'Kafka 通知系统的取舍',
            '搜索、热榜和通知如何串起来',
        ],
        branchPrompts: [
            '先做 MVP',
            '先保证一致性',
            '先做可观测性',
            '先准备压测数据',
        ],
        rootContents: [
            '内容社区后端最重要的是先把核心链路跑通：用户、帖子、评论、点赞、通知。功能不一定复杂，但接口语义和数据状态要稳定。',
            'Redis 热榜适合用来承接高频读和实时互动分数更新。它不是数据库的替代品，而是把读热点和排序压力从 MySQL 中拆出来。',
            'Kafka 通知系统的价值在于把互动行为和通知落库解耦。点赞、评论、回复不应该被通知写入速度拖慢，但异步链路也需要幂等和失败处理。',
        ],
        branchContents: [
            'MVP 阶段不要过度设计，但要把异常、日志和接口响应统一好。否则功能越多，后面越难维护。',
            '一致性问题要分层看。用户请求链路要尽量清晰，异步通知和热榜可以接受短暂最终一致。',
            '压测数据不是装饰。没有足够的帖子、互动和用户，就看不出分页、索引、缓存和热榜的真实表现。',
        ],
    },
];

function formatNow() {
    const now = new Date();
    const pad = (value) => String(value).padStart(2, '0');

    return `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`;
}

function pick(list, index) {
    return list[index % list.length];
}

function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function buildRootPost(index) {
    const topic = pick(topics, index);
    const title = pick(topic.titles, index);
    const rootContent = pick(topic.rootContents, index);

    return {
        topicIndex: index % topics.length,
        title: `${title}｜${seedTag}-${String(index + 1).padStart(4, '0')}`,
        content: `${rootContent}\n\n这是一条用于 qilu 本地压测和展示的数据。主题是「${topic.scene}」，内容尽量模拟真实社区里的经验分享，而不是随机字符串。`,
        visibility: VISIBILITY_PUBLIC,
        mediaIds: [],
    };
}

function buildBranchPost(rootPost, branchIndex) {
    const topic = topics[rootPost.topicIndex];
    const branchPrompt = pick(topic.branchPrompts, branchIndex);
    const branchContent = pick(topic.branchContents, branchIndex);
    const rootTitle = rootPost.title.split('｜')[0];

    return {
        branchPrompt,
        title: `${branchPrompt}｜${rootTitle}`,
        content: `${branchContent}\n\n这个分支承接根帖「${rootTitle}」，用于模拟读者在不同选择下继续阅读的内容。`,
        mediaIds: [],
    };
}

async function readUsers() {
    const raw = await fs.readFile(USERS_FILE, 'utf-8');
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

async function requestJson(path, options = {}) {
    const url = `${BASE_URL}${path}`;

    const response = await fetch(url, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {}),
        },
    });

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

async function postJson(path, token, payload) {
    return requestJson(path, {
        method: 'POST',
        headers: {
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
    });
}

function getCreatedId(result, action) {
    if (result.httpStatus !== 200) {
        throw new Error(`${action} failed, httpStatus=${result.httpStatus}`);
    }

    if (!result.body || result.body.code !== SUCCESS) {
        throw new Error(
            `${action} failed, code=${result.body?.code}, message=${result.body?.message}`
        );
    }

    const id = result.body.data;

    if (!Number.isInteger(id) || id <= 0) {
        throw new Error(`${action} failed, created id is invalid, id=${id}`);
    }

    return id;
}

async function createRootPost(user, rootPost) {
    const result = await postJson('/posts', user.token, {
        title: rootPost.title,
        content: rootPost.content,
        visibility: rootPost.visibility,
        mediaIds: rootPost.mediaIds,
    });

    return getCreatedId(result, `create root post, title=${rootPost.title}`);
}

async function createBranchPost(user, parentPostId, branchPost) {
    const result = await postJson(`/posts/${parentPostId}/branches`, user.token, {
        branchPrompt: branchPost.branchPrompt,
        title: branchPost.title,
        content: branchPost.content,
        mediaIds: branchPost.mediaIds,
    });

    return getCreatedId(
        result,
        `create branch post, parentPostId=${parentPostId}, branchPrompt=${branchPost.branchPrompt}`
    );
}

async function main() {
    const users = await readUsers();

    const seedResult = {
        seedTag,
        baseUrl: BASE_URL,
        rootPostCount: ROOT_POST_COUNT,
        minBranchCount: MIN_BRANCH_COUNT,
        maxBranchCount: MAX_BRANCH_COUNT,
        roots: [],
    };

    console.log(`seed posts started, seedTag=${seedTag}, rootPostCount=${ROOT_POST_COUNT}`);

    for (let i = 0; i < ROOT_POST_COUNT; i++) {
        const user = users[i % users.length];
        const rootPost = buildRootPost(i);

        // 创建根帖，并直接使用返回的 id 继续创建分支。
        const rootPostId = await createRootPost(user, rootPost);

        const branchCount = randomInt(MIN_BRANCH_COUNT, MAX_BRANCH_COUNT);
        const branches = [];

        for (let j = 0; j < branchCount; j++) {
            const branchPost = buildBranchPost(rootPost, j);

            // 创建分支帖，并记录分支 id，方便后续补点赞、评论、搜索测试。
            const branchPostId = await createBranchPost(user, rootPostId, branchPost);

            branches.push({
                id: branchPostId,
                branchPrompt: branchPost.branchPrompt,
                title: branchPost.title,
            });
        }

        seedResult.roots.push({
            id: rootPostId,
            title: rootPost.title,
            authorEmail: user.email,
            branchCount,
            branches,
        });

        console.log(
            `seed root post success, index=${i + 1}, rootPostId=${rootPostId}, branchCount=${branchCount}`
        );
    }

    await fs.writeFile(OUTPUT_FILE, JSON.stringify(seedResult, null, 2));

    console.log('');
    console.log(
        `seed posts success, rootPostCount=${seedResult.roots.length}, output=${OUTPUT_FILE}`
    );
}

main().catch((error) => {
    console.error('');
    console.error(error.message);
    process.exit(1);
});