import fs from 'fs/promises';

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';
const USER_COUNT = Number(process.env.USER_COUNT || 100);
const OUTPUT_FILE = process.env.OUTPUT_FILE || './data/users.json';

const EMAIL_PREFIX = process.env.EMAIL_PREFIX || 'qilu_k6_user';
const EMAIL_DOMAIN = process.env.EMAIL_DOMAIN || 'example.com';
const PASSWORD = process.env.PASSWORD || '123456';

const SUCCESS_CODE = 0;
const CONFLICT_CODE = 40900;

function buildUser(index) {
    const no = String(index).padStart(3, '0');

    return {
        nickname: `k6_user_${no}`,
        email: `${EMAIL_PREFIX}_${no}@${EMAIL_DOMAIN}`,
        password: PASSWORD,
    };
}

async function postJson(path, body) {
    const url = `${BASE_URL}${path}`;

    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(body),
    });

    let apiResponse;

    try {
        apiResponse = await response.json();
    } catch (e) {
        throw new Error(`invalid json response, url=${url}, httpStatus=${response.status}`);
    }

    return {
        httpStatus: response.status,
        code: apiResponse.code,
        message: apiResponse.message,
        data: apiResponse.data,
    };
}

async function registerUser(user) {
    const result = await postJson('/users', {
        nickname: user.nickname,
        email: user.email,
        password: user.password,
    });

    if (result.httpStatus !== 200) {
        throw new Error(
            `register user failed, email=${user.email}, httpStatus=${result.httpStatus}`
        );
    }

    if (result.code === SUCCESS_CODE) {
        console.log(`register user success, email=${user.email}`);
        return;
    }

    if (result.code === CONFLICT_CODE) {
        console.log(`register user skipped, email already exists, email=${user.email}`);
        return;
    }

    throw new Error(
        `register user failed, email=${user.email}, code=${result.code}, message=${result.message}`
    );
}

async function loginUser(user) {
    const result = await postJson('/auth/login', {
        email: user.email,
        password: user.password,
    });

    if (result.httpStatus !== 200) {
        throw new Error(
            `login user failed, email=${user.email}, httpStatus=${result.httpStatus}`
        );
    }

    if (result.code !== SUCCESS_CODE) {
        throw new Error(
            `login user failed, email=${user.email}, code=${result.code}, message=${result.message}`
        );
    }

    if (!result.data || !result.data.token) {
        throw new Error(`login user failed, token is missing, email=${user.email}`);
    }

    console.log(`login user success, email=${user.email}`);

    return {
        uuid: result.data.uuid,
        nickname: result.data.nickname,
        email: result.data.email,
        password: user.password,
        token: result.data.token,
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

    await fs.writeFile(OUTPUT_FILE, JSON.stringify(preparedUsers, null, 2));

    console.log('');
    console.log(`prepare k6 users success, count=${preparedUsers.length}, output=${OUTPUT_FILE}`);
}

main().catch((error) => {
    console.error('');
    console.error(error.message);
    process.exit(1);
});