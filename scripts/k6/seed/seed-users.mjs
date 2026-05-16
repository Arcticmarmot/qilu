import {
    currentDir,
    writeJsonFile,
    requestJson,
    SUCCESS,
    CONFLICT,
} from '../lib/common.mjs';

const __dirname = currentDir(import.meta.url);

const USER_COUNT = Number(process.env.USER_COUNT || 100);
const OUTPUT_FILE = process.env.OUTPUT_FILE || './data/users.json';

const EMAIL_PREFIX = process.env.EMAIL_PREFIX || 'qilu_k6_user';
const EMAIL_DOMAIN = process.env.EMAIL_DOMAIN || 'example.com';
const PASSWORD = process.env.PASSWORD || '123456';

function buildUser(index) {
    const no = String(index).padStart(3, '0');

    return {
        nickname: `k6_user_${no}`,
        email: `${EMAIL_PREFIX}_${no}@${EMAIL_DOMAIN}`,
        password: PASSWORD,
    };
}

async function registerUser(user) {
    const result = await requestJson('/users', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            nickname: user.nickname,
            email: user.email,
            password: user.password,
        }),
    });

    if (result.httpStatus !== 200) {
        throw new Error(
            `register user failed, email=${user.email}, httpStatus=${result.httpStatus}`
        );
    }

    if (result.body.code === SUCCESS) {
        console.log(`register user success, email=${user.email}`);
        return;
    }

    if (result.body.code === CONFLICT) {
        console.log(`register user skipped, email already exists, email=${user.email}`);
        return;
    }

    throw new Error(
        `register user failed, email=${user.email}, code=${result.body.code}, message=${result.body.message}`
    );
}

async function loginUser(user) {
    const result = await requestJson('/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            email: user.email,
            password: user.password,
        }),
    });

    if (result.httpStatus !== 200) {
        throw new Error(
            `login user failed, email=${user.email}, httpStatus=${result.httpStatus}`
        );
    }

    if (result.body.code !== SUCCESS) {
        throw new Error(
            `login user failed, email=${user.email}, code=${result.body.code}, message=${result.body.message}`
        );
    }

    if (!result.body.data || !result.body.data.token) {
        throw new Error(`login user failed, token is missing, email=${user.email}`);
    }

    console.log(`login user success, email=${user.email}`);

    return {
        uuid: result.body.data.uuid,
        nickname: result.body.data.nickname,
        email: result.body.data.email,
        password: user.password,
        token: result.body.data.token,
    };
}

async function main() {
    const preparedUsers = [];

    for (let i = 1; i <= USER_COUNT; i++) {
        const user = buildUser(i);

        await registerUser(user);

        const loggedInUser = await loginUser(user);
        preparedUsers.push(loggedInUser);
    }

    await writeJsonFile(__dirname, OUTPUT_FILE, preparedUsers);

    console.log('');
    console.log(`prepare k6 users success, count=${preparedUsers.length}, output=${OUTPUT_FILE}`);
}

main().catch((error) => {
    console.error('');
    console.error(error.message);
    process.exit(1);
});