local stock_key = KEYS[1]
local purchased_key = KEYS[2]
local user_id = ARGV[1]

if redis.call('sismember', purchased_key, user_id) == 1 then
    return -2
end

local stock = tonumber(redis.call('get', stock_key))
if stock == nil or stock <= 0 then
    return -1
end

redis.call('decr', stock_key)
redis.call('sadd', purchased_key, user_id)
return 1
