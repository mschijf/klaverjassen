package com.cards.game.klaverjassen

import com.cards.game.card.CardColor

class Round(
    private val trumpColor: CardColor,
    private val contractOwningSide: TableSide) {

    private val trickList = mutableListOf<Trick>()

    fun getTrickOnTableOrNull() = if (getLastTrick().isActive()) getLastTrick() else null
    fun getTrickOnTable() = getTrickOnTableOrNull()?:throw Exception("We do not have a current trick on table")


    fun getLastCompletedTrickWinner(): TableSide? = getLastCompletedTrick()?.getWinningSide()
    fun getTrickList() = trickList.toList()
    fun isComplete() = getTrickList().size == NUMBER_OF_TRICKS_PER_ROUND && getTrickList().last().isComplete()
    fun getFirstTrickLead() = getFirstTrick().getSideToPlay()

    private fun getLastTrick() = trickList.lastOrNull()?:throw Exception("We do not have a last trick")
    private fun getFirstTrick() = trickList.firstOrNull()?:throw Exception("We do not have a first trick")

    private fun getLastCompletedTrick(): Trick? {
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

    //------------------------------------------------------------------------------------------------------------------
    // Klaverjassen specific
    //------------------------------------------------------------------------------------------------------------------

    fun getTrumpColor() = trumpColor
    fun getContractOwningSide() = contractOwningSide
    fun isContractOwningSide(tableSide: TableSide) = (contractOwningSide == tableSide)

    private fun allTricksWonByTeam(team: Set<TableSide>): Boolean {
        return getTrickList().all { trick -> trick.getWinningSide()!! in team }
    }

    fun getScore(): ScoreKlaverjassen {
        val roundScore= getTrickList()
            .fold(ScoreKlaverjassen.ZERO){acc, trick -> acc.plus(trick.getScore())}
        if (!isComplete())
            return roundScore

        return if (getContractOwningSide() in setOf(TableSide.NORTH, TableSide.SOUTH)) {
            if (roundScore.getNorthSouthTotal() <= roundScore.getEastWestTotal())
                roundScore.changeNorthSouthToNat()
            else if (allTricksWonByTeam(setOf(TableSide.NORTH, TableSide.SOUTH)))
                roundScore.plusPitBonus()
            else
                roundScore
        } else {
            if (roundScore.getEastWestTotal() <= roundScore.getNorthSouthTotal())
                roundScore.changeEastWestToNat()
            else if (allTricksWonByTeam(setOf(TableSide.EAST, TableSide.WEST)))
                roundScore.plusPitBonus()
            else
                roundScore
        }
    }
}