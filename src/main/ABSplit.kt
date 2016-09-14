package main

import java.io.File
import java.util.*

fun main(args: Array<String>) {
    File("automata.txt").writeText("")
    val dungeon = ABSplit.Dungeon(100, 100)
    val array = dungeon.printString()

    writeToFile(array, File("absplitlevel.txt"))
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

        enum class Direction{
            NORTH,
            SOUTH,
            EAST,
            WEST,
            NONE
        }
    }


    class Dungeon {

        var main: DungeonLeaf
        val sizeX: Int
        val sizeY: Int

        constructor(sizeX: Int, sizeY: Int) {
            this.sizeX = sizeX
            this.sizeY = sizeY
            main = DungeonLeaf(this, 1, 0, sizeX, 0, sizeY, ABSplit.Companion.Direction.NONE)
            main.connectChildren()
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
        val aChild: DungeonLeaf?
        val bChild: DungeonLeaf?
        val contents: CellularAutomata?
        val edgeDirection: Direction
        val verticalSplit: Boolean

        var startX = 0
        var endX = 0
        var startY = 0
        var endY = 0

        constructor(rootParent: Dungeon, depth: Int, startX: Int, endX: Int, startY: Int, endY: Int, edge:Direction) {
            this.root = rootParent
            this.depth = depth
            this.startX = startX
            this.endX = endX
            this.startY = startY
            this.endY = endY
            this.edgeDirection = edge

            if (depth <= 3) {
                val children = createChildren()
                aChild = children.achild
                bChild = children.bchild
                verticalSplit = children.verticalSplit
                this.contents = null
            } else {
                this.contents = CellularAutomata(endX - startX, endY - startY)
                verticalSplit = false
                aChild = null
                bChild = null
            }
        }

        fun createChildren(): Children {
            var xToYRatio = (endX - startX).toFloat() / (endY - startY).toFloat()
            var verticalSplit: Boolean

            if (xToYRatio >= 2) {
                verticalSplit = random.nextFloat() > 0.15f
            } else if (xToYRatio <= 0.5f) {
                verticalSplit = random.nextFloat() > 0.85f
            } else {
                verticalSplit = random.nextFloat() > 0.5f
            }

            if (verticalSplit && (endX - startX) < 20 || !verticalSplit && (endY - startY) < 20) {
                verticalSplit = !verticalSplit
            }

            var splitCoord: Int

            if (verticalSplit) {
                splitCoord = getSplitCoord(startX, endX)

                return Children(DungeonLeaf(root, depth + 1, startX, splitCoord, startY, endY, Direction.EAST),
                        DungeonLeaf(root, depth + 1, splitCoord, endX, startY, endY, Direction.WEST), verticalSplit)
            } else {
                splitCoord = getSplitCoord(startY, endY)

                return Children(DungeonLeaf(root, depth + 1, startX, endX, startY, splitCoord, Direction.SOUTH),
                        DungeonLeaf(root, depth + 1, startX, endX, splitCoord, endY, Direction.NORTH), verticalSplit)
            }
        }

        data class Children(val achild: DungeonLeaf, val bchild: DungeonLeaf, val verticalSplit: Boolean)

        fun connectChildren() {
            aChild?.connectChildren()
            bChild?.connectChildren()

            if (aChild != null && bChild != null) {

                println("Depth: $depth, Horizontal split: $verticalSplit, ChildA: ${aChild.contents?.getRoomList()?.size}, ChildB: ${bChild.contents?.getRoomList()?.size}")
            }
        }


        fun createString(array: Array<Array<Char>>) {
            aChild?.createString(array)
            bChild?.createString(array)

            if (aChild == null && bChild == null && contents != null) {
                val charArray = contents.getArray()
                for (currentY in startY..(endY - 1)) {
                    for (currentX in startX..(endX - 1)) {
                        array[currentY][currentX] = charArray[currentY - startY][currentX - startX]
                    }
                }
            }
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


