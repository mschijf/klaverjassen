package com.cards.game.klaverjassen.basic

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.klaverjassen.klaverjassen.NUMBER_OF_ROUNDS_PER_GAME
import com.cards.game.klaverjassen.klaverjassen.ScoreKlaverjassen

class Game() {

    private val roundList = mutableListOf<Round>()

    fun start(startSide: TableSide ) {
        createNewRoundAndTrick(startSide)
    }

    fun getLastTrickWinner(): TableSide?  =
        if (getCurrentRound().hasNotStarted())
            getPreviousRound()?.getLastCompletedTrickWinner()
        else
            getCurrentRound().getLastCompletedTrickWinner()

    fun getLastCompletedTrick(): Trick?  =
        if (getCurrentRound().hasNotStarted())
            getPreviousRound()?.getLastCompletedTrick()
        else
            getCurrentRound().getLastCompletedTrick()

    fun getRounds() = roundList.toList()
    fun getCurrentRound() = roundList.lastOrNull()?:throw Exception("We do not have a current round")
    fun getPreviousRound() = if (roundList.size >= 2) roundList[roundList.size - 2] else null
    fun getSideToMove() = getCurrentRound().getTrickOnTable().getSideToPlay()

    private fun createNewRoundAndTrick(sideToLead: TableSide) {
        if (isFinished())
            throw Exception("Trying to add a round to a finished game")
        roundList.add(createRound())
        createNewTrick(sideToLead)
    }

    private fun createNewTrick(sideToLead: TableSide) {
        getCurrentRound().addTrick(createTrick(sideToLead))
    }

    fun hasNewRoundStarted() = getCurrentRound().hasNotStarted()

    fun playCard(card: Card): GameStatus {
        if (isFinished())
            throw Exception("Trying to play a card, but the game is already over")

        val currentRound = getCurrentRound()
        val trickOnTable = currentRound.getTrickOnTable()
        val sideToPlay = trickOnTable.getSideToPlay()
        trickOnTable.addCard(card)

        val gameStatus = if (isFinished()) {
            GameStatus(gameFinished = true, roundFinished = true, trickFinished = true)
        } else if (currentRound.isComplete()) {
            val previousLeadStart = currentRound.getTrickList().first().getSideToLead()
            createNewRoundAndTrick(previousLeadStart.clockwiseNext())
            GameStatus(gameFinished = false, roundFinished = true, trickFinished = true)
        } else if (trickOnTable.isComplete()) {
            createNewTrick(trickOnTable.getWinningSide()!!)
            GameStatus(gameFinished = false, roundFinished = false, trickFinished = true)
        } else {
            GameStatus(gameFinished = false, roundFinished = false, trickFinished = false)
        }
        return gameStatus
    }

    //------------------------------------------------------------------------------------------------------------------
    // Klaverjassen specific
    //------------------------------------------------------------------------------------------------------------------

    fun createTrick(sideToLead: TableSide) =
        Trick(
            sideToLead,
            getCurrentRound()
        )
    fun createRound() = Round()

    fun isFinished(): Boolean {
        return getRounds().size == NUMBER_OF_ROUNDS_PER_GAME && getRounds().last().isComplete()
    }

    fun setTrumpColorAndContractOwner(trumpColor: CardColor, side: TableSide) {
        getCurrentRound().setTrumpColorAndContractOwner(trumpColor, side)
    }

    fun getAllScoresPerRound(): List<ScoreKlaverjassen> {
        return getRounds()
            .map { round ->  round.getScore()}
    }

    companion object {
        fun startNewGame(startSide: TableSide = TableSide.WEST): Game {
            val game = Game()
            game.start(startSide)
            return game
        }
    }

}





