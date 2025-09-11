package com.cards.player.ai.main

import com.cards.game.card.CARDDECK
import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.klaverjassen.*
import com.cards.player.Player

data class Memory(
    val cardsPlayed: List<Card>,
    val allCardsInPlay: List<Card>,
    val cardsInPlayOtherPlayers: List<Card>,

    val playerList: List<OtherPlayer>,
    val numberOfTricksWonByUs: Int,) {

    //------------------------------------------------------------------------------------------------------------------

    companion object {
        fun refresh(player: Player): Memory {

            val analyzer = KlaverjassenAnalyzer(player)
            analyzer.refreshAnalysis()

            val currentTrick = player.game.getCurrentRound().getTrickOnTable()
            val trump = player.game.getCurrentRound().getTrumpColor()

            val myCards = player.getCardsInHand()
            val nCardsInHand = myCards.size
            val sidesPlayedInTrick = currentTrick.getSidesPlayed()

            val cardsPlayed = player.game.getCurrentRound().getTrickList().flatMap { it.getCardsPlayed() }
            val allCardsInPlay = CARDDECK.baseDeckCardsSevenAndHigher - cardsPlayed
            fun numberOfCardsInHandForSide(side: TableSide) = nCardsInHand - if (side in sidesPlayedInTrick) 1 else 0

            fun otherPlayerCanHaveLegalCards(playerSide: TableSide): Set<Card> {
                return (analyzer.playerCanHave[playerSide]!! + analyzer.playerMustHave[playerSide]!!)
                    .legalPlayable(currentTrick, trump)
                    .toSet()
            }

            val playerList = (1..3).map { idx ->
                val playerSide = player.tableSide.clockwiseNext(idx)
                OtherPlayer(
                    playerSide,
                    numberOfCardsInHandForSide(playerSide),
                    analyzer.playerCanHave[playerSide]!!,
                    analyzer.playerMustHave[playerSide]!!,
                    analyzer.playerHeeftGeseind[playerSide]?.color,
                    otherPlayerCanHaveLegalCards(playerSide)
                )
            }

            val us = setOf(player.tableSide, player.tableSide.opposite())
            val numberOfTricksWonByUs = player.game.getCurrentRound().getTrickList().filter{it.isComplete()}.count {it.getWinningSide() in us }

            return Memory (
                cardsPlayed = cardsPlayed,
                allCardsInPlay = allCardsInPlay,
                cardsInPlayOtherPlayers = allCardsInPlay - myCards,
                playerList = playerList,
                numberOfTricksWonByUs = numberOfTricksWonByUs
            )
        }

    }

    fun printAnalyzer() {
        TableSide.values().forEach {
            val player = playerList.firstOrNull { player -> player.tableSide == it }
            if (player == null) {
                print(String.format("%-5s ", it.toString().lowercase()))
            } else {
                val playerCanHaveCards = player.canHave
                print(String.format("%-5s ", it.toString().lowercase()))
                print(String.format("(%2d): ", playerCanHaveCards.size))
                CardColor.values().forEach { color ->
                    print(
                        String.format(
                            "%-8s: %-25s  ",
                            color,
                            playerCanHaveCards.filter { card -> card.color == color }
                                .map { card -> card.rank.rankString })
                    )
                }
                println()
                val playerMustHaveCards = player.mustHave
                print(String.format("%-5s ", it.toString().lowercase()))
                print(String.format("(%2d): ", playerMustHaveCards.size))
                CardColor.values().forEach { color ->
                    print(
                        String.format(
                            "%-8s: %-25s  ",
                            " ",
                            playerMustHaveCards.filter { card -> card.color == color }
                                .map { card -> card.rank.rankString })
                    )
                }
            }
            println()
        }
    }

}

data class OtherPlayer(
    val tableSide: TableSide,
    val numberOfCardsInHand: Int,
    val canHave: Set<Card>,
    val mustHave: Set<Card>,
    val geseindeKleur: CardColor?,
    val legalCards: Set<Card>,) {
    val allAssumeCards = canHave + mustHave
}