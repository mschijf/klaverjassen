package com.cards.game.fourplayercardgame.klaverjassen

import com.cards.game.card.CardColor
import com.cards.game.fourplayercardgame.basic.Game
import com.cards.game.fourplayercardgame.basic.TableSide

class GameKlaverjassen private constructor(): Game()  {

    override fun createTrick(sideToLead: TableSide) =
        TrickKlaverjassen(
            sideToLead,
            getCurrentRound() as RoundKlaverjassen
        )
    override fun createRound() = RoundKlaverjassen()

    override fun isFinished(): Boolean {
        return getRounds().size == NUMBER_OF_ROUNDS_PER_GAME && getRounds().last().isComplete()
    }

    fun setTrumpColorAndContractOwner(trumpColor: CardColor, side: TableSide) {
        (getCurrentRound() as RoundKlaverjassen).setTrumpColorAndContractOwner(trumpColor, side)
    }

    fun getAllScoresPerRound(): List<ScoreKlaverjassen> {
        return getRounds()
            .map { round ->  (round as RoundKlaverjassen).getScore()}
    }

    companion object {
        fun startNewGame(startSide: TableSide = TableSide.WEST): GameKlaverjassen {
            val game = GameKlaverjassen()
            game.start(startSide)
            return game
        }
    }
}