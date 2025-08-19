package com.cards.game.fourplayercardgame.basic

import com.cards.game.fourplayercardgame.klaverjassen.NUMBER_OF_TRICKS_PER_ROUND

abstract class Round() {

    private val trickList = mutableListOf<Trick>()

    private fun getLastTrick() = trickList.lastOrNull()?:throw Exception("We do not have a last trick")
    fun hasNotStarted(): Boolean = trickList.size == 1 && trickList.first().hasNotStarted()
    fun getTrickOnTable() = if (getLastTrick().isActive()) getLastTrick() else throw Exception("We do not have a current trick on table")
    fun getLastCompletedTrickWinner(): TableSide? = getLastCompletedTrick()?.getWinningSide()
    fun getTrickList() = trickList.toList()
    fun isComplete() = getTrickList().size == NUMBER_OF_TRICKS_PER_ROUND && getTrickList().last().isComplete()
    fun isLastTrick(trick: Trick) = getTrickList().size == NUMBER_OF_TRICKS_PER_ROUND && getTrickList().last() == trick

    fun getLastCompletedTrick(): Trick? {
        if (trickList.isEmpty())
            return null
        if (!trickList.last().isActive())
            return trickList.last()
        if (trickList.size <= 1)
            return null
        return trickList[trickList.size - 2]
    }

    fun addTrick(trick: Trick) {
        if (isComplete())
            throw Exception("Trying to add more tricks to a round than the maximum allowed")
        trickList.add(trick)
    }
}