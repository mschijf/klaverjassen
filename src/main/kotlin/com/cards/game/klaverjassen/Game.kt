package com.cards.game.klaverjassen

import com.cards.game.card.Card
import com.cards.game.card.CardColor

class Game(private val startSide: TableSide = GAME_START_PLAYER) {

    private val roundList = mutableListOf<Round>()

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
    fun getSideToMove() =
        if (newRoundToBeStarted())
            roundList.lastOrNull()?.getFirstTrickLead()?.clockwiseNext()?:startSide
        else
            getCurrentRound().getTrickOnTable().getSideToPlay()

    fun newRoundToBeStarted() = !isFinished() && (roundList.lastOrNull()?.isComplete()?:true)

    fun playCard(card: Card): GameStatus {
        if (isFinished())
            throw Exception("Trying to play a card, but the game is already over")

        val currentRound = getCurrentRound()
        val trickOnTable = currentRound.getTrickOnTable()
        trickOnTable.addCard(card)

        val gameStatus = if (isFinished()) {
            GameStatus(gameFinished = true, roundFinished = true, trickFinished = true)
        } else if (currentRound.isComplete()) {
            GameStatus(gameFinished = false, roundFinished = true, trickFinished = true)
        } else if (trickOnTable.isComplete()) {
            createAndAddNewTrickToCurrentRound(trickOnTable.getWinningSide()!!)
            GameStatus(gameFinished = false, roundFinished = false, trickFinished = true)
        } else {
            GameStatus(gameFinished = false, roundFinished = false, trickFinished = false)
        }
        return gameStatus
    }

    fun takeLastCardBack(): GameStatus {
        //todo: implement
        return GameStatus(gameFinished = false, roundFinished = false, trickFinished = false)
    }


    //------------------------------------------------------------------------------------------------------------------
    // Klaverjassen specific
    //------------------------------------------------------------------------------------------------------------------

    fun startNewRound(trumpColor: CardColor, contractOwningSide: TableSide) {
        if (isFinished())
            throw Exception("Trying to add a round to a finished game")

        val sideToLead = getSideToMove()

        val newRound = Round(trumpColor, contractOwningSide)
        roundList.add(newRound)
        createAndAddNewTrickToCurrentRound(sideToLead)
    }

    private fun createAndAddNewTrickToCurrentRound(sideToLead: TableSide) {
        val trick = Trick(
            sideToLead = sideToLead,
            trumpColor = getCurrentRound().getTrumpColor(),
            lastTrickInRound = roundList.size == 8)
        getCurrentRound().addTrick(trick)
    }

    fun isFinished(): Boolean {
        return getRounds().size == NUMBER_OF_ROUNDS_PER_GAME && getRounds().last().isComplete()
    }

    fun getAllScoresPerRound(): List<ScoreKlaverjassen> {
        return getRounds()
            .map { round ->  round.getScore()}
    }

}





