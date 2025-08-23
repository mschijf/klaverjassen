package com.cards.game.klaverjassen

import com.cards.game.card.Card
import com.cards.game.card.CardColor

class Game(
    private val startSide: TableSide = GAME_START_PLAYER) {

    private val roundList = mutableListOf<Round>()
    fun getLastTrickWinner(): TableSide? = getCurrentRound().getLastCompletedTrickWinner()

    fun getRounds() = roundList.toList()
    fun getCurrentRound() = getCurrentRoundOrNull()?:throw Exception("We do not have a current round")
    fun getSideToMove() =
        if (newRoundToBeStarted())
            roundList.lastOrNull()?.getFirstTrickLead()?.clockwiseNext() ?: startSide
        else
            getCurrentRound().getTrickOnTable().getSideToPlay()
    fun getTrickLead() = getCurrentRoundOrNull()?.getTrickOnTableOrNull()?.getSideToLead()
    fun getNewRoundLead() = if (newRoundToBeStarted()) getSideToMove() else null

    private fun getCurrentRoundOrNull() = roundList.lastOrNull()

    fun newRoundToBeStarted() = !isFinished() && (roundList.lastOrNull()?.isComplete()?:true)

    fun playCard(card: Card): GameStatus {
        if (isFinished())
            throw Exception("Trying to play a card, but the game is already over")

        val currentRound = getCurrentRound()
        val trickOnTable = currentRound.getTrickOnTable()
        trickOnTable.addCard(card)

        val status = if (isFinished()) {
            GameStatus(gameFinished = true, roundFinished = true, trickFinished = true)
        } else if (currentRound.isComplete()) {
            GameStatus(gameFinished = false, roundFinished = true, trickFinished = true)
        } else if (trickOnTable.isComplete()) {
            createAndAddNewTrickToCurrentRound(trickOnTable.getWinningSide()!!)
            GameStatus(gameFinished = false, roundFinished = false, trickFinished = true)
        } else {
            GameStatus(gameFinished = false, roundFinished = false, trickFinished = false)
        }
        return status
    }

    fun takeLastCardBack(): GameStatus {
        //todo: implement
        val status = GameStatus(gameFinished = false, roundFinished = false, trickFinished = false)
        return status
    }


    //------------------------------------------------------------------------------------------------------------------
    // Klaverjassen specific
    //------------------------------------------------------------------------------------------------------------------

    fun startNewRound(trumpColor: CardColor, contractOwningSide: TableSide): GameStatus {
        if (isFinished())
            throw Exception("Trying to add a round to a finished game")

        val sideToLead = getSideToMove()

        val newRound = Round(trumpColor, contractOwningSide)
        roundList.add(newRound)
        createAndAddNewTrickToCurrentRound(sideToLead)
        val status = GameStatus(gameFinished = false, roundFinished = false, trickFinished = false)
        return status
    }

    private fun createAndAddNewTrickToCurrentRound(sideToLead: TableSide) {
        val trick = Trick(
            sideToLead = sideToLead,
            trumpColor = getCurrentRound().getTrumpColor(),
            lastTrickInRound = getCurrentRound().getTrickList().size == NUMBER_OF_TRICKS_PER_ROUND-1)
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





