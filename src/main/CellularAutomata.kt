package main

import java.io.File
import java.util.*

private var WALL_PROB = 0.45f

fun main(args: Array<String>) {
    var automata = CellularAutomata()

    var array = automata.generateMap(80, 50)

    writeToFile(array, File("automata.txt"))
}

class CellularAutomata {

    private var random: Random

    constructor() {
        random = Random()
    }

    fun generateMap(sizeX: Int, sizeY: Int, floodFill: Boolean = true): Array<Array<Char>> {
        var currentIteration = Array(sizeY, { y -> Array(sizeX, { x -> getRandomMapThing(sizeX, sizeY, x, y) }) })

        for (iteration in 1..4) {
            currentIteration = generateNextIteration(currentIteration, 5, 2)
        }

        for (iteration in 1..3) {
            currentIteration = generateNextIteration(currentIteration, 5, -1)
        }

//        writeToFile(currentIteration, File("automata.txt"))

        if (floodFill) {
           floodFill(currentIteration)
        }

        writeToFile(currentIteration, File("automata.txt"), true)

        connectIsolatedCaves(currentIteration)

//        writeToFile(currentIteration, File("automata.txt"), true)

        return currentIteration
    }


    fun generateNextIteration(currentMap: Array<Array<Char>>, closeWalls: Int, farWalls: Int): Array<Array<Char>> {
        var nextIteration = Array(currentMap.size, { Array(currentMap[0].size, { '.' }) })

        val yLength = 0..currentMap.size - 1
        val xLength = 0..currentMap[0].size - 1

        for (y in yLength) {
            for (x in xLength) {
                val wallsClose = checkNeighbours(currentMap, x, y, closeWalls)
                var wallsFar = false
                if (farWalls != -1) {
                    wallsFar = checkFarNeighbours(currentMap, x, y, farWalls)
                }
                if (wallsClose || wallsFar) {
                    nextIteration[y][x] = '#'
                }
            }
        }

        return nextIteration
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

    fun floodFill(array: Array<Array<Char>>, oldChar: Char = '.', newChar: Char = ' ') {

        var rand = Random()
        var xLength = array[0].size
        var yLength = array.size
        var x: Int
        var y: Int

        do {
            x = rand.nextInt(xLength - 2) + 1
            y = rand.nextInt(yLength - 2) + 1
        } while (array[y][x] != oldChar)

        flood(array, x, y, oldChar, newChar)
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

    fun connectIsolatedCaves(array: Array<Array<Char>>) {
        while (hasUnconnectedFloor(array)) {

            floodFill(array, '.', '0')
            var isolatedFloors = ArrayList<IsolatedFloor>()

            for (y in 0..array.size - 1) {
                for (x in 0..array[y].size - 1) {
                    if (array[y][x] == '0') {
                        var floor = IsolatedFloor(x, y)
                        findShortestPath(array, floor)
                        isolatedFloors.add(floor)
                    }
                }
            }

            var shortestSoFar = if (array.size > array[0].size) array.size else array[0].size
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
                connectFloor(array, closestFloor, isNorth = true)
            }
            if (closestFloor.dSouth <= closestFloor.dWest && closestFloor.dSouth <= closestFloor.dEast && closestFloor.dSouth <= closestFloor.dNorth) {
                connectFloor(array, closestFloor, isSouth = true)
            }
            if (closestFloor.dEast <= closestFloor.dWest && closestFloor.dEast <= closestFloor.dNorth && closestFloor.dEast <= closestFloor.dSouth) {
                connectFloor(array, closestFloor, isEast = true)
            }
            if (closestFloor.dWest <= closestFloor.dEast && closestFloor.dWest <= closestFloor.dNorth && closestFloor.dWest <= closestFloor.dSouth) {
                connectFloor(array, closestFloor, isWest = true)
            }

            floodFill(array, '0')
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

    fun hasUnconnectedFloor(array: Array<Array<Char>>): Boolean {
        for (xline in array) {
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

    data class IsolatedFloor(val x: Int, val y: Int, var dNorth: Int = 1000, var dSouth: Int = 1000, var dWest: Int = 1000, var dEast: Int = 1000)

    fun printCurrentMap(currentMap: Array<Array<Char>>) {
        println()
        println()

        for (currentArray in currentMap) {
            for (currentChar in currentArray) {
                print(currentChar)
            }
            println()
        }
    }

}


