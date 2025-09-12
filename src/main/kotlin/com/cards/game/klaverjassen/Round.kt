package com.cards.game.klaverjassen

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.tools.RANDOMIZER

class Round(
    private val trumpColor: CardColor,
    private val contractOwningSide: TableSide) {

    private val trickList = mutableListOf<Trick>()

    private fun getFirstTrick() = trickList.firstOrNull()?:throw Exception("We do not have a first trick")
    fun getTrickOnTable() = trickList.lastOrNull()?:throw Exception("We do not have a trick on table")

    fun getLastCompletedTrickWinner(): TableSide? = getLastCompletedTrick()?.getWinningSide()
    fun getTrickList() = trickList.toList()
    fun isComplete() = getTrickList().size == NUMBER_OF_TRICKS_PER_ROUND && getTrickList().last().isComplete()
    fun getFirstTrickLead() = getFirstTrick().getSideToLead()

    private fun getLastCompletedTrick(): Trick? {
        if (trickList.isEmpty())
            return null
        if (trickList.last().isComplete())
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

    fun removeLastTrick() {
        if (trickList.isEmpty())
            throw Exception("Trying to remove a trick from an empty trick list")
        if (trickList.last().getCardsPlayed().isNotEmpty())
            throw Exception("Trying to remove a trick with cards played")
        trickList.removeLast()
    }


    //------------------------------------------------------------------------------------------------------------------
    // Klaverjassen specific
    //------------------------------------------------------------------------------------------------------------------

    fun getTrumpColor() = trumpColor
    fun getContractOwningSide() = contractOwningSide

    private fun allTricksWonByTeam(team: Set<TableSide>): Boolean {
        return getTrickList().all { trick -> trick.getWinningSide()!! in team }
    }

    fun getScore(): ScoreKlaverjassen {
//        if (isComplete())
//            return trickList[6].getScore().plus(trickList[7].getScore())

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

    fun header(): String {
        return "Seed\tWest\tNorth\tEast\tSouth\tLead\tContract\tTrump\ttrick1\ttrick2\ttrick3\ttrick4\ttrick5\ttrick6\ttrick7\ttrick8" +
        "\tNS-points\tNS-roem\tEW-points\tEW-roem\ttype"
    }

    private fun determineCardsForPlayer(side: TableSide) : List<Card> {
        if (!isComplete())
            return emptyList()
        return trickList
            .map {it.getCardPlayedBy(side)!!}
            .sortedBy { card -> 100 * card.color.ordinal + card.rank.ordinal }
    }

    override fun toString(): String {
        val score = getScore()
        return "" +
                "${RANDOMIZER.getLastSeedUsed()}\t" +
                "${determineCardsForPlayer(TableSide.WEST)}\t" +
                "${determineCardsForPlayer(TableSide.NORTH)}\t" +
                "${determineCardsForPlayer(TableSide.EAST)}\t" +
                "${determineCardsForPlayer(TableSide.SOUTH)}\t" +
                "${getFirstTrickLead()}\t" +
                "${getContractOwningSide()}\t" +
                "${getTrumpColor()}\t" +
                trickList.joinToString("\t"){it.toString()} + "\t" +
                "\t".repeat(8-trickList.size) +
                "${score.northSouthPoints}\t${score.northSouthBonus}\t" +
                "${score.eastWestPoints}\t${score.eastWestBonus}\t" +
                "${score.scoreType}"
    }

    fun toPrettyString(): String {
        val score = getScore()
        return "" +
                "Random seed  : ${RANDOMIZER.getLastSeedUsed()}\n" +
                "RoundLead    : ${getFirstTrickLead()}\n" +
                "ContractOwner: ${getContractOwningSide()}\n" +
                "Trump        : ${getTrumpColor()}\n" +
                "Tricks       : " + trickList.joinToString("\t"){it.toString()} + "\n" +
                "NS-Points    : ${score.northSouthPoints} + ${score.northSouthBonus}\n" +
                "EW-Points    : ${score.eastWestPoints} + ${score.eastWestBonus}\n" +
                "Score type   : ${score.scoreType}"
    }
}