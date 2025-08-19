package com.cards.game.fourplayercardgame.klaverjassen

import com.cards.game.card.CardColor
import com.cards.game.fourplayercardgame.basic.Round
import com.cards.game.fourplayercardgame.basic.TableSide


class RoundKlaverjassen() : Round() {

    private var trumpColor: CardColor = CardColor.CLUBS
    private var contractOwningSide: TableSide = TableSide.WEST

    fun getTrumpColor() = trumpColor
    fun getContractOwningSide() = contractOwningSide
    fun isContractOwningSide(tableSide: TableSide) = (contractOwningSide == tableSide)

    fun setTrumpColorAndContractOwner(trumpColor: CardColor, contractOwner: TableSide) {
        this.trumpColor= trumpColor
        this.contractOwningSide = contractOwner
    }

    private fun allTricksWonByTeam(team: Set<TableSide>): Boolean {
        return getTrickList().all { trick -> trick.getWinningSide()!! in team }
    }

    fun getScore(): ScoreKlaverjassen {
        val roundScore= getTrickList()
            .fold(ScoreKlaverjassen.ZERO){acc, trick -> acc.plus((trick as TrickKlaverjassen).getScore())}

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