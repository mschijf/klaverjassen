package com.cards.game.fourplayercardgame.hearts

import com.cards.game.fourplayercardgame.basic.TableSide
import com.github.jknack.handlebars.internal.lang3.math.NumberUtils

data class ScoreHearts(val westValue: Int, val northValue: Int, val eastValue: Int, val southValue: Int) {
    fun plus(score: ScoreHearts): ScoreHearts {
        return ScoreHearts(
            westValue + score.westValue,
            northValue + score.northValue,
            eastValue + score.eastValue,
            southValue + score.southValue
        )
    }

    fun minus(score: ScoreHearts): ScoreHearts {
        return ScoreHearts(
            westValue - score.westValue,
            northValue - score.northValue,
            eastValue - score.eastValue,
            southValue - score.southValue
        )
    }

    fun minValue() = NumberUtils.min(westValue, northValue, eastValue, southValue)
    fun maxValue() = NumberUtils.max(westValue, northValue, eastValue, southValue)

    companion object {
        val ZERO = ScoreHearts(0,0,0,0)

        fun scoreForPlayer(tableSide: TableSide, value: Int): ScoreHearts {
            return ScoreHearts(
                if (tableSide == TableSide.WEST) value else 0,
                if (tableSide == TableSide.NORTH) value else 0,
                if (tableSide == TableSide.EAST) value else 0,
                if (tableSide == TableSide.SOUTH) value else 0
            )
        }
    }
}