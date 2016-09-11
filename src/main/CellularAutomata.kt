package main

import java.io.File
import java.util.*

private var WALL_PROB = 0.45f

fun main(args: Array<String>) {
    var automata = CellularAutomata()

    automata.generateMap(15, 30)
}

class CellularAutomata {

    private var random: Random

    constructor() {
        random = Random()
    }

    fun generateMap(sizeX: Int, sizeY: Int) : Array<Array<Char>> {
//        var file = File("level.txt")
//        file.writeText("")

        var currentIteration = Array(sizeY,  { y -> Array(sizeX, { x -> getRandomMapThing(sizeX, sizeY, x, y) }) })
//        writeToFile(currentIteration, file)

        for (iteration in 1..4) {
            currentIteration = generateNextIteration(currentIteration, 5, 2)
//            writeToFile(currentIteration, file)
        }

        for (iteration in 1..3) {
            currentIteration = generateNextIteration(currentIteration, 5, -1)
//            writeToFile(currentIteration,file)
        }

        return currentIteration
    }

    fun writeToFile(currentMap: Array<Array<Char>>, file: File) {
        val writer = StringBuffer()

        for (array in currentMap) {
            for (char in array) {
                writer.append(char + " ")
            }
            writer.append("\n")
        }

        writer.append("\n")
        file.appendText(writer.toString())
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


