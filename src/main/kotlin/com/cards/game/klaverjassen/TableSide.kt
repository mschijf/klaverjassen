package com.cards.game.klaverjassen

enum class TableSide {
    WEST,
    NORTH,
    EAST,
    SOUTH;

    fun clockwiseNext(n: Int=1) = values()[(this.ordinal + n) % values().size]
    fun opposite(): TableSide = clockwiseNext(2)
    fun isOppositeOf(other: TableSide?) = this.opposite() == other
    fun clockwiseDistanceFrom(other: TableSide) = (values().size + other.ordinal - this.ordinal) % values().size
    fun clockwiseDistanceTo(other: TableSide) = (values().size + this.ordinal - other.ordinal) % values().size

    fun others() = setOf (clockwiseNext(1), clockwiseNext(2), clockwiseNext(3) )
}