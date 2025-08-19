package com.cards.game.fourplayercardgame.hearts

import com.cards.game.fourplayercardgame.basic.Game
import com.cards.game.fourplayercardgame.basic.Round
import com.cards.game.fourplayercardgame.basic.TableSide
import com.cards.game.fourplayercardgame.basic.Trick
import kotlin.math.max

class GameHearts private constructor(): Game() {

    fun isGoingUp() = getRounds().size < goingDownFromRoundNumber()

    override fun createTrick(sideToLead: TableSide): Trick {
        return TrickHearts(sideToLead)
    }

    override fun createRound(): Round {
        return RoundHearts()
    }

    override fun isFinished() = !isGoingUp() && (getTotalScore().minValue() <= VALUE_TO_FINISH)

    fun getCumulativeScorePerRound(): List<ScoreHearts> {
        return getRounds()
            .filter { it.isComplete() }
            .map { round ->  getGameScoreForRound(round)}
            .runningFold(ScoreHearts.ZERO) { acc, sc -> acc.plus(sc) }.drop(1)
    }

    private fun getTotalScore(): ScoreHearts {
        return getCumulativeScorePerRound().lastOrNull()?: ScoreHearts.ZERO
    }

    private var goingDownRoundNumber: Int? = null
    private fun goingDownFromRoundNumber(): Int {
        if (goingDownRoundNumber != null)
            return goingDownRoundNumber!!

        var score = ScoreHearts.ZERO
        getRounds().forEachIndexed { idx, round ->
            score = score.plus((round as RoundHearts).getScore())
            if (score.maxValue() >= VALUE_TO_GO_DOWN) {
                goingDownRoundNumber = idx+1
                return idx + 1
            }
        }
        return Int.MAX_VALUE
    }

    private fun getGameScoreForRound(round: Round): ScoreHearts {
        val score = (round as RoundHearts).getScore()
        val roundNumber = max(0, getRounds().indexOf(round))

        val goingUp = roundNumber < goingDownFromRoundNumber()
        return if (goingUp) {
            if (score.maxValue() == ALL_POINTS_FOR_PIT) {
                ScoreHearts(
                    westValue = if (score.westValue == 0) ALL_POINTS_FOR_PIT else 0,
                    northValue = if (score.northValue == 0) ALL_POINTS_FOR_PIT else 0,
                    eastValue = if (score.eastValue == 0) ALL_POINTS_FOR_PIT else 0,
                    southValue = if (score.southValue == 0) ALL_POINTS_FOR_PIT else 0
                )
            } else {
                score
            }
        } else {
            ScoreHearts.ZERO.minus(score)
        }
    }

    companion object {
        fun startNewGame(startSide: TableSide = TableSide.WEST): GameHearts {
            val game = GameHearts()
            game.start(startSide)
            return game
        }
    }

}
