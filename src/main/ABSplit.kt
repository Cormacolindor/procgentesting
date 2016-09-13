package main

import java.io.File
import java.util.*

fun main(args: Array<String>) {
    val dungeon = ABSplit.Dungeon(100, 100)
    val array = dungeon.printString()

    val file = File("absplitlevel.txt")
    writeToFile(array, file)
}

class ABSplit {

    companion object {
        val random = Random()

        fun getSplitCoord(start: Int, end: Int): Int {
            var span = end - start

            val halfSplit = span / 2
            var randomCoord: Int
            var counter = 0

            randomCoord = (span / 3) + random.nextInt(span / 3)

            return randomCoord + start
        }

        fun getRandomChar(): Char {
            var number = random.nextInt(5)
            when (number) {
                0 -> return '#'
                1 -> return ' '
                2 -> return '.'
                3 -> return '-'
                4 -> return '0'
                else -> return '*'
            }
        }

        fun getGeneratedAutomata(sizeX: Int, sizeY: Int): Array<Array<Char>> {
            val automataGenerator = CellularAutomata()
            return automataGenerator.generateMap(sizeX, sizeY)
        }
    }


    class Dungeon {

        var main: DungeonLeaf
        val sizeX: Int
        val sizeY: Int

        constructor(sizeX: Int, sizeY: Int) {
            this.sizeX = sizeX
            this.sizeY = sizeY
            main = DungeonLeaf(this, 1, 0, sizeX, 0, sizeY)
        }

        fun printString(): Array<Array<Char>> {
            val charArray = Array(sizeY, { Array(sizeX, { ' ' }) })
            main.createString(charArray)

            return charArray
        }
    }

    class DungeonLeaf {

        val root: Dungeon
        val depth: Int
        var aChild: DungeonLeaf? = null
        var bChild: DungeonLeaf? = null
        var contents: Array<Array<Char>>? = null

        var startX = 0
        var endX = 0
        var startY = 0
        var endY = 0

        constructor(rootParent: Dungeon, depth: Int, startX: Int, endX: Int, startY: Int, endY: Int) {
            this.root = rootParent
            this.depth = depth
            this.startX = startX
            this.endX = endX
            this.startY = startY
            this.endY = endY

            if (depth <= 3) {
                createChildren()
            } else {
                this.contents = getGeneratedAutomata(endX - startX, endY - startY)
            }
        }

        fun createChildren() {
            var xToYRatio = (endX - startX).toFloat() / (endY - startY).toFloat()
            var horizontalSplit: Boolean

            if (xToYRatio >= 2) {
                horizontalSplit = random.nextFloat() > 0.15f
            } else if (xToYRatio <= 0.5f) {
                horizontalSplit = random.nextFloat() > 0.85f
            } else {
                horizontalSplit = random.nextFloat() > 0.5f
            }

            if (horizontalSplit && (endX - startX) < 20 || !horizontalSplit && (endY - startY) < 20) {
                horizontalSplit = !horizontalSplit
            }

            var splitCoord: Int

            if (horizontalSplit) {
                splitCoord = getSplitCoord(startX, endX)

                aChild = DungeonLeaf(root, depth + 1, startX, splitCoord, startY, endY)
                bChild = DungeonLeaf(root, depth + 1, splitCoord, endX, startY, endY)
            } else {
                splitCoord = getSplitCoord(startY, endY)

                aChild = DungeonLeaf(root, depth + 1, startX, endX, startY, splitCoord)
                bChild = DungeonLeaf(root, depth + 1, startX, endX, splitCoord, endY)
            }
        }

        fun createString(array: Array<Array<Char>>) {
            aChild?.createString(array)
            bChild?.createString(array)

            if (aChild == null && bChild == null && contents != null && (contents is Array<Array<Char>>)) {
                val currentContents = contents as Array<Array<Char>>
                for (currentY in startY..(endY - 1)) {
                    for (currentX in startX..(endX - 1)) {
                        array[currentY][currentX] = currentContents[currentY - startY][currentX - startX]
                    }
                }
            }
//            println("Depth: $depth [$startX, $endX] , [$startY, $endY]")
        }
    }
}

fun writeToFile(currentMap: Array<Array<Char>>, file: File, append: Boolean = false) {
    val writer = StringBuffer()

    if (!append) {
        file.writeText("")
    }

    for (array in currentMap) {
        for (char in array) {
            writer.append(char + " ")
        }
        writer.append("\n")
    }

    writer.append("\n")
    file.appendText(writer.toString())
}


