package main

import java.io.File
import java.util.*

fun main(args: Array<String>) {
//    var horizontal = 0
//    var vertical = 0
//    var random = Random()
//
//    for (i in 0..10000) {
//        var split = random.nextFloat() >= 0.5f
//        when (split) {
//            true -> horizontal++
//            false -> vertical++
//        }
//    }
//
//    println("Horizontal: $horizontal, Vertical: $vertical")
    var start = System.currentTimeMillis()
    File("automata.txt").writeText("")
    File("absplitlevel.txt").writeText("")
    val dungeon = ABSplit.Dungeon(100, 100)
    val array = dungeon.dungeonMap
    var end = System.currentTimeMillis() - start
    println("Time spent = $end")

//    var xx = -1
//    var yy = -1
//
//    loop@
//    for (y in 0..array.size - 1) {
//        for (x in 0..array[y].size - 1) {
//            if (array[y][x] == ' ') {
//                xx = x
//                yy = y
//                break@loop
//            }
//        }
//    }
//
//    flood(array,xx,yy,' ', '*')

    writeToFile(array, File("absplitlevel.txt"), true)
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

        enum class Direction {
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
        val dungeonMap: Array<Array<Char>>

        constructor(sizeX: Int, sizeY: Int) {
            this.sizeX = sizeX
            this.sizeY = sizeY
            dungeonMap = Array(sizeY, { y -> Array(sizeX, { x -> '*' }) })
            main = DungeonLeaf(this, 1, 0, sizeX, 0, sizeY, ABSplit.Companion.Direction.NONE)
            main.connectChildren()

        }
    }

    class DungeonLeaf {

        data class Children(val achild: DungeonLeaf, val bchild: DungeonLeaf, val verticalSplit: Boolean)
        data class FloorPair(val floorA: CellularAutomata.FloorTile, val floorB: CellularAutomata.FloorTile)

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

        constructor(rootParent: Dungeon, depth: Int, startX: Int, endX: Int, startY: Int, endY: Int, edge: Direction) {
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

                println("Split ${if(verticalSplit) "vertical" else "horizontal"}")
                this.contents = null
            } else {
                this.contents = CellularAutomata(endX - startX, endY - startY)
                for (y in startY..endY - 1) {
                    for (x in startX..endX - 1) {
                        root.dungeonMap[y][x] =
                                contents.getArray()[y - startY][x - startX]
                    }
                }
                verticalSplit = false
                aChild = null
                bChild = null
            }
        }

        fun createChildren(): Children {
            var verticalSplit: Boolean

            verticalSplit = random.nextFloat() >= 0.5f

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


        fun connectChildren() {
            aChild?.connectChildren()
            bChild?.connectChildren()

            if (aChild != null && bChild != null) {
                if (verticalSplit) {
                    val mostEastFloors = ArrayList<CellularAutomata.FloorTile>()
                    val mostWestFloors = ArrayList<CellularAutomata.FloorTile>()

                    for (y in aChild.startY..aChild.endY - 1) {
                        for (x in aChild.endX - 1 downTo aChild.startX) {
                            if (root.dungeonMap[y][x] == ' ') {
                                mostEastFloors.add(CellularAutomata.FloorTile(x, y))
                                break
                            }
                        }
                    }

                    for (y in 0..bChild.endY - 1) {
                        for (x in bChild.startX..bChild.endX - 1) {
                            if (root.dungeonMap[y][x] == ' ') {
                                mostWestFloors.add(CellularAutomata.FloorTile(x, y))
                                break
                            }
                        }
                    }

                    val floorPairs = ArrayList<FloorPair>()

                    for (e in mostEastFloors) {
                        for (w in mostWestFloors) {
                            if (e.y == w.y) {
                                floorPairs.add(FloorPair(e, w))
                            }
                        }
                    }

                    val randomFloortile = floorPairs[random.nextInt(floorPairs.size)]

                    connectRoomsEastWest(randomFloortile.floorA, randomFloortile.floorB)
                }
                if (!verticalSplit) {
                    val mostSouthFloors = ArrayList<CellularAutomata.FloorTile>()
                    val mostNorthFloors = ArrayList<CellularAutomata.FloorTile>()

                    for (x in aChild.startX..aChild.endX - 1) {
                        for (y in aChild.endY - 1 downTo startY) {
                            if (root.dungeonMap[y][x] == ' ') {
                                mostSouthFloors.add(CellularAutomata.FloorTile(x, y))
                                break
                            }
                        }
                    }

                    for (x in bChild.startX..bChild.endX - 1) {
                        for (y in bChild.startY..bChild.endY - 1) {
                            if (root.dungeonMap[y][x] == ' ') {
                                mostNorthFloors.add(CellularAutomata.FloorTile(x, y))
                                break
                            }
                        }
                    }

                    val floorPairs = ArrayList<FloorPair>()

                    for (s in mostSouthFloors) {
                        for (n in mostNorthFloors) {
                            if (s.x == n.x) {
                                floorPairs.add(FloorPair(s, n))
                            }
                        }
                    }

                    val randomFloorTile = floorPairs[random.nextInt(floorPairs.size)]

                    connectRoomsNorthSouth(randomFloorTile.floorA, randomFloorTile.floorB)
                }
            }
        }

        fun connectRoomsEastWest(floorA: CellularAutomata.FloorTile, floorB: CellularAutomata.FloorTile) {
            for (x in floorA.x..floorB.x) {
                root.dungeonMap[floorA.y][x] = ' '
            }
        }

        fun connectRoomsNorthSouth(floorA: CellularAutomata.FloorTile, floorB: CellularAutomata.FloorTile) {
            for (y in floorA.y..floorB.y) {
                root.dungeonMap[y][floorA.x] = ' '
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


