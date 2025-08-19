package com.cards.game.fourplayercardgame.basic

import com.cards.game.card.Card

abstract class Game() {

    private val roundList = mutableListOf<Round>()

    abstract fun createTrick(sideToLead: TableSide): Trick
    abstract fun createRound(): Round
    abstract fun isFinished(): Boolean

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
}





