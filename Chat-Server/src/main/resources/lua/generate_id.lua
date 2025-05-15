local function is_leap_year(year)
    return year % 4 == 0 and (year % 100 ~= 0 or year % 400 == 0)
end

local function days_in_month(year, month)
    local month_days = {31,28,31,30,31,30,31,31,30,31,30,31}
    if month == 2 and is_leap_year(year) then
        return 29
    else
        return month_days[month]
    end
end

local function timestamp_to_date(ts)
    local seconds_per_day = 86400
    local year = 1970
    local day = math.floor(ts / seconds_per_day)

    while true do
        local days_this_year = is_leap_year(year) and 366 or 365
        if day < days_this_year then break end
        day = day - days_this_year
        year = year + 1
    end

    local month = 1
    while true do
        local dim = days_in_month(year, month)
        if day < dim then break end
        day = day - dim
        month = month + 1
    end

    return {
        year = year,
        month = month,
        day = day + 1
    }
end

-- 计算距离当天24点的秒数
local function seconds_until_end_of_day(ts)
    local seconds_in_day = 86400
    local offset = ts % seconds_in_day
    return seconds_in_day - offset
end

-- 主逻辑
local time = redis.call('TIME')
local ts = tonumber(time[1])
local date = timestamp_to_date(ts)
local date_str = string.format("%04d%02d%02d", date.year, date.month, date.day)

local key = 'id:' .. date_str
local seq = redis.call('INCR', key)

if seq == 1 then
    -- 只有第一次创建才设置过期
    local expire_seconds = seconds_until_end_of_day(ts)
    redis.call('EXPIRE', key, expire_seconds)
end

return tostring(date_str) .. "-" .. string.format('%06d', seq)
