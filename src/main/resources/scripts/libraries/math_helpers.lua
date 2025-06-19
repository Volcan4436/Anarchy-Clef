--[[
@name Math Helpers
@description Common mathematical utility functions for scripts
@version 1.0.0
@author Hearty
@category Library
@dependencies none
]]--

local mathHelpers = {}

-- Calculate distance between two 3D points
function mathHelpers.distance3D(x1, y1, z1, x2, y2, z2)
    local dx = x2 - x1
    local dy = y2 - y1
    local dz = z2 - z1
    return math.sqrt(dx*dx + dy*dy + dz*dz)
end

-- Calculate distance between two 2D points (ignore Y coordinate)
function mathHelpers.distance2D(x1, z1, x2, z2)
    local dx = x2 - x1
    local dz = z2 - z1
    return math.sqrt(dx*dx + dz*dz)
end

-- Clamp a value between min and max
function mathHelpers.clamp(value, min, max)
    if value < min then return min end
    if value > max then return max end
    return value
end

-- Linear interpolation between two values
function mathHelpers.lerp(a, b, t)
    return a + (b - a) * mathHelpers.clamp(t, 0, 1)
end

-- Check if a number is within a range
function mathHelpers.inRange(value, center, range)
    return math.abs(value - center) <= range
end

-- Round a number to specified decimal places
function mathHelpers.round(num, decimals)
    local mult = 10 ^ (decimals or 0)
    return math.floor(num * mult + 0.5) / mult
end

-- Calculate angle between two points (in radians)
function mathHelpers.angleBetween(x1, z1, x2, z2)
    return math.atan2(z2 - z1, x2 - x1)
end

-- Convert radians to degrees
function mathHelpers.toDegrees(radians)
    return radians * (180 / math.pi)
end

-- Convert degrees to radians
function mathHelpers.toRadians(degrees)
    return degrees * (math.pi / 180)
end

-- Find the closest point in a list to a target point
function mathHelpers.findClosest(targetX, targetY, targetZ, points)
    if not points or #points == 0 then
        return nil
    end
    
    local closestPoint = points[1]
    local closestDistance = mathHelpers.distance3D(
        targetX, targetY, targetZ,
        closestPoint.x, closestPoint.y, closestPoint.z
    )
    
    for i = 2, #points do
        local point = points[i]
        local distance = mathHelpers.distance3D(
            targetX, targetY, targetZ,
            point.x, point.y, point.z
        )
        
        if distance < closestDistance then
            closestDistance = distance
            closestPoint = point
        end
    end
    
    return closestPoint, closestDistance
end

-- Generate a random number within a range
function mathHelpers.randomRange(min, max)
    return min + math.random() * (max - min)
end

-- Check if a point is within a rectangular area
function mathHelpers.pointInRectangle(px, pz, x1, z1, x2, z2)
    local minX, maxX = math.min(x1, x2), math.max(x1, x2)
    local minZ, maxZ = math.min(z1, z2), math.max(z1, z2)
    return px >= minX and px <= maxX and pz >= minZ and pz <= maxZ
end

function onLoad()
    AltoClef.log("Math Helpers library loaded - Functions available for other scripts")
end

function onEnable()
    AltoClef.log("Math Helpers library enabled")
end

function onDisable()
    AltoClef.log("Math Helpers library disabled")
end

function onCleanup()
    AltoClef.log("Math Helpers library cleaning up")
end

return mathHelpers 