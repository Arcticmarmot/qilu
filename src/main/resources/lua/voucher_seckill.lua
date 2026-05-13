local stockKey = KEYS[1]
local userKey = KEYS[2]

local userUuid = ARGV[1]

local stock = redis.call('get', stockKey)
if stock == false then
    return 3
end

stock = tonumber(stock)
if stock <= 0 then
    return 1
end

if redis.call('sismember', userKey, userUuid) == 1 then
    return 2
end

redis.call('decr', stockKey)
redis.call('sadd', userKey, userUuid)

return 0