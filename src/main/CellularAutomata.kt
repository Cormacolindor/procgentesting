package main

import java.io.File
import java.util.*

private var WALL_PROB = 0.45f

fun main(args: Array<String>) {
    File("automata.txt").writeText("")

    var automata = CellularAutomata(20, 20)
    var array = automata.generateMap()

}

class CellularAutomata {

    private var random: Random
    private var currentIteration: Array<Array<Char>>
    private var roomList: ArrayList<Room>

    constructor(sizeX: Int, sizeY: Int) {
        random = Random()
        currentIteration = generatePreMap(sizeX, sizeY)
        roomList = ArrayList<Room>()
        generateMap()
    }

    fun getArray(): Array<Array<Char>> {
        return currentIteration
    }

    fun getRoomList(): List<Room> {
        return roomList
    }

    fun generatePreMap(sizeX: Int, sizeY: Int): Array<Array<Char>> {
        var preMap = Array(sizeY, { y -> Array(sizeX, { x -> getRandomMapThing(sizeX, sizeY, x, y) }) })

//        for (y in 0..preMap.size - 1) {
//            preMap[y][10] = '.'
//            preMap[y][11] = '.'
//        }

        return preMap
    }

    fun generateMap(): Array<Array<Char>> {
        writeToFile(currentIteration, File("automata.txt"), true)

        for (iteration in 1..4) {
            generateNextIteration(5, 2)
        }

        for (iteration in 1..3) {
            generateNextIteration(5, -1)
        }

        countAndMeasureRooms()

        floodFill()

        writeToFile(currentIteration, File("automata.txt"), true)

        connectIsolatedCaves()

        writeToFile(currentIteration, File("automata.txt"), true)

        return currentIteration
    }


    fun generateNextIteration(closeWalls: Int, farWalls: Int) {
        var nextIteration = Array(currentIteration.size, { Array(currentIteration[0].size, { '.' }) })

        val yLength = 0..currentIteration.size - 1
        val xLength = 0..currentIteration[0].size - 1

        for (y in yLength) {
            for (x in xLength) {
                val wallsClose = checkNeighbours(currentIteration, x, y, closeWalls)
                var wallsFar = false
                if (farWalls != -1) {
                    wallsFar = checkFarNeighbours(currentIteration, x, y, farWalls)
                }
                if (wallsClose || wallsFar) {
                    nextIteration[y][x] = '#'
                }
            }
        }
        currentIteration = nextIteration
    }

    fun checkNeighbours(currentMap: Array<Array<Char>>, currentX: Int, currentY: Int, allowedWalls: Int): Boolean {
        var numberOfWalls = 0

        val yRange = (currentY - 1)..(currentY + 1)
        val xRange = (currentX - 1)..(currentX + 1)

        for (y in yRange) {
            for (x in xRange) {
                if (y < 0 || x < 0 || y >= currentMap.size || x >= currentMap[0].size) {
                    numberOfWalls++
                } else {
                    if (currentMap[y][x] == '#') {
                        numberOfWalls++
                    }
                }
            }
        }

        var result = false
        if (numberOfWalls >= allowedWalls) {
            result = true
        }

        return result
    }

    fun checkFarNeighbours(currentMap: Array<Array<Char>>, currentX: Int, currentY: Int, allowedWalls: Int = 0): Boolean {
        var numberOfWalls = 0

        val yRange = (currentY - 2)..(currentY + 2)
        val xRange = (currentX - 2)..(currentX + 2)

        for (y in yRange) {
            for (x in xRange) {
                if (y < 0 || x < 0 || y >= currentMap.size || x >= currentMap[0].size) {
                    numberOfWalls++
                } else {
                    if (currentMap[y][x] == '#') {
                        numberOfWalls++
                    }
                }
            }
        }

        var result = false
        if (numberOfWalls <= allowedWalls) {
            result = true
        }

        return result
    }

    fun getRandomMapThing(sizeX: Int, sizeY: Int, x: Int, y: Int): Char {
        val result = random.nextFloat()

        if (x == 0 || x == sizeX - 1 || y == 0 || y == sizeY - 1) {
            return '#'
        }

        if (result > WALL_PROB) {
            return '.'
        } else {
            return '#'
        }
    }

    fun floodFill(oldChar: Char = '.', newChar: Char = ' ') {

        var rand = Random()
        var xLength = currentIteration[0].size
        var yLength = currentIteration.size
        var x: Int
        var y: Int

        do {
            x = rand.nextInt(xLength - 2) + 1
            y = rand.nextInt(yLength - 2) + 1
        } while (currentIteration[y][x] != oldChar)

        flood(currentIteration, x, y, oldChar, newChar)
    }

    fun flood(array: Array<Array<Char>>, x: Int, y: Int, origChar: Char, newChar: Char) {
        if (origChar == newChar) {
            return
        }
        if (array[y][x] != origChar) {
            return
        }

        array[y][x] = newChar

        if (x > 0) {
            flood(array, x - 1, y, origChar, newChar)
        }
        if (x < array[0].size - 1) {
            flood(array, x + 1, y, origChar, newChar)
        }
        if (y > 0) {
            flood(array, x, y - 1, origChar, newChar)
        }
        if (y < array.size - 1) {
            flood(array, x, y + 1, origChar, newChar)
        }
    }

    fun connectIsolatedCaves() {
        while (hasUnconnectedFloor()) {

            floodFill('.', '0')
            var isolatedFloors = ArrayList<IsolatedFloor>()

            for (y in 0..currentIteration.size - 1) {
                for (x in 0..currentIteration[y].size - 1) {
                    if (currentIteration[y][x] == '0') {
                        var floor = IsolatedFloor(x, y)
                        findShortestPath(currentIteration, floor)
                        isolatedFloors.add(floor)
                    }
                }
            }

            var shortestSoFar = if (currentIteration.size > currentIteration[0].size) currentIteration.size else currentIteration[0].size
            var shortestCandidates = ArrayList<IsolatedFloor>()

            for (floor in isolatedFloors) {

                var shortestDirection = floor.dWest
                shortestDirection = if (floor.dEast < shortestDirection) floor.dEast else shortestDirection
                shortestDirection = if (floor.dNorth < shortestDirection) floor.dNorth else shortestDirection
                shortestDirection = if (floor.dSouth < shortestDirection) floor.dSouth else shortestDirection

                if (shortestDirection < shortestSoFar) {
                    shortestSoFar = shortestDirection
                    shortestCandidates.clear()
                    shortestCandidates.add(floor)
                } else if (shortestDirection == shortestSoFar) {
                    shortestCandidates.add(floor)
                }
            }

            var closestFloor: IsolatedFloor
            if (shortestCandidates.size == 1) {
                closestFloor = shortestCandidates[0]
            } else {
                closestFloor = shortestCandidates[random.nextInt(shortestCandidates.size - 1)]
            }

            if (closestFloor.dNorth <= closestFloor.dWest && closestFloor.dNorth <= closestFloor.dEast && closestFloor.dNorth <= closestFloor.dSouth) {
                connectFloor(currentIteration, closestFloor, isNorth = true)
            }
            if (closestFloor.dSouth <= closestFloor.dWest && closestFloor.dSouth <= closestFloor.dEast && closestFloor.dSouth <= closestFloor.dNorth) {
                connectFloor(currentIteration, closestFloor, isSouth = true)
            }
            if (closestFloor.dEast <= closestFloor.dWest && closestFloor.dEast <= closestFloor.dNorth && closestFloor.dEast <= closestFloor.dSouth) {
                connectFloor(currentIteration, closestFloor, isEast = true)
            }
            if (closestFloor.dWest <= closestFloor.dEast && closestFloor.dWest <= closestFloor.dNorth && closestFloor.dWest <= closestFloor.dSouth) {
                connectFloor(currentIteration, closestFloor, isWest = true)
            }

            floodFill('0')
        }
    }

    fun connectFloor(array: Array<Array<Char>>, floorTile: IsolatedFloor, isNorth: Boolean = false, isSouth: Boolean = false, isWest: Boolean = false, isEast: Boolean = false) {

        if (isNorth) {
            for (d in 1..floorTile.dNorth) {
                array[floorTile.y - d][floorTile.x] = ' '
            }
        }
        if (isSouth) {
            for (d in 1..floorTile.dSouth) {
                array[floorTile.y + d][floorTile.x] = ' '
            }
        }
        if (isWest) {
            for (d in 1..floorTile.dWest) {
                array[floorTile.y][floorTile.x - d] = ' '
            }
        }
        if (isEast) {
            for (d in 1..floorTile.dEast) {
                array[floorTile.y][floorTile.x + d] = ' '
            }
        }
    }

    fun hasUnconnectedFloor(): Boolean {
        for (xline in currentIteration) {
            for (x in xline) {
                if (x == '.') {
                    return true
                }
            }
        }
        return false
    }

    fun findShortestPath(array: Array<Array<Char>>, floorTile: IsolatedFloor) {

        var distance = 0
        for (x in floorTile.x - 1 downTo 0) {
            distance++
            if (array[floorTile.y][x] == ' ') {
                floorTile.dWest = distance
                break
            }
        }

        distance = 0
        for (x in (floorTile.x + 1)..(array[floorTile.y].size - 1)) {
            distance++
            if (array[floorTile.y][x] == ' ') {
                floorTile.dEast = distance
                break
            }
        }

        distance = 0
        for (y in (floorTile.y - 1) downTo 0) {
            distance++
            if (array[y][floorTile.x] == ' ') {
                floorTile.dNorth = distance
                break
            }
        }

        distance = 0
        for (y in (floorTile.y + 1)..(array.size - 1)) {
            distance++
            if (array[y][floorTile.x] == ' ') {
                floorTile.dSouth = distance
                break
            }
        }
    }

    fun countAndMeasureRooms() {
        val WORKING = 'w'
        val DONE = 'd'

        while (hasUnconnectedFloor()) {
            var x = 0
            var y = 0

            for (currentY in 0..currentIteration.size - 1) {
                for (currentX in 0..currentIteration[currentY].size - 1) {
                    if (currentIteration[currentY][currentX] == '.') {
                        x = currentX
                        y = currentY
                        break
                    }
                }
            }

            flood(currentIteration, x, y, '.', WORKING)

            val floors = ArrayList<FloorTile>()
            var size = 0

            for (currentY in 0..currentIteration.size - 1) {
                for (currentX in 0..currentIteration[currentY].size - 1) {
                    if (currentIteration[currentY][currentX] == WORKING) {
                        val floorTile = FloorTile(currentX, currentY)
                        floors.add(floorTile)
                        size++
                    }
                }
            }

            val newRoom = Room(size, floors)
            roomList.add(newRoom)

            flood(currentIteration, x,y, WORKING, DONE)
        }

        for (currentY in 0..currentIteration.size - 1) {
            for (currentX in 0..currentIteration[currentY].size - 1) {
                if (currentIteration[currentY][currentX] == DONE) {
                    currentIteration[currentY][currentX] = '.'
                }
            }
        }

    }


    data class IsolatedFloor(val x: Int, val y: Int, var dNorth: Int = Int.MAX_VALUE, var dSouth: Int = Int.MAX_VALUE, var dWest: Int = Int.MAX_VALUE, var dEast: Int = Int.MAX_VALUE)

    data class FloorTile(val x: Int, val y: Int)

    data class Room(val size: Int, val tiles: List<FloorTile>)

}


