package com.cards.controller

import com.cards.game.card.Card
import com.cards.game.klaverjassen.basic.Game
import com.cards.game.klaverjassen.basic.TableSide
import com.cards.game.klaverjassen.klaverjassen.ScoreKlaverjassen
import com.cards.player.Player
import com.cards.player.PlayerGroup
import com.cards.player.klaverjassen.PlayerKlaverjassen
import com.cards.player.klaverjassen.ai.GeniusPlayerKlaverjassen
import com.cards.tools.RANDOMIZER
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ServiceKlaverjassenTest {
    @Test
    fun runTest() {
        RANDOMIZER.setFixedSequence(true)
        val numberOfTests = 1000
        val serie = (1..numberOfTests).map { testOneGame(it) }
        println()
        println("----------------------------------------------------------------")
        println("%7d runs           WIJ        ZIJ".format(numberOfTests))
        val winsNS = serie.count { it.getNorthSouthTotal() > it.getEastWestTotal() }
        val winsEW = serie.count { it.getNorthSouthTotal() < it.getEastWestTotal() }
        val equal = serie.count { it.getNorthSouthTotal() == it.getEastWestTotal() }
        println("number of wins: %10d %10d".format(winsNS,winsEW))
        val total = serie.reduce { acc, score -> acc.plus(score) }
        println("Points          %10d %10d".format(total.getNorthSouthTotal(), total.getEastWestTotal()))

        assertEquals(501, winsNS)
        assertEquals(498, winsEW)
        assertEquals(1, equal)
        assertEquals(1592297, total.getNorthSouthTotal())
        assertEquals(1601543, total.getEastWestTotal())
    }

    private fun testOneGame(index: Int): ScoreKlaverjassen {
        val game = Game.startNewGame(TableSide.WEST)
        val playerGroup = PlayerGroup(
            listOf(PlayerKlaverjassen(TableSide.WEST, game), PlayerKlaverjassen(TableSide.NORTH, game), PlayerKlaverjassen(TableSide.EAST, game), PlayerKlaverjassen(TableSide.SOUTH, game),)
        )

        while (!game.isFinished()) {
            val sideToMove = game.getSideToMove()
            val playerToMove = playerGroup.getPlayer(sideToMove) as PlayerKlaverjassen

            if (game.hasNewRoundStarted()) {
                playerGroup.dealCards()
                val trumpColor = playerToMove.chooseTrumpColor()
                game.setTrumpColorAndContractOwner(trumpColor, playerToMove.tableSide)
            }

            val suggestedCardToPlay = playerToMove.chooseCard()
            playCard(playerToMove, game, suggestedCardToPlay)
        }
        return game.getAllScoresPerRound().reduce { acc, roundScore -> acc.plus(roundScore) }
    }

    @Test
    fun runTestGenius() {
        RANDOMIZER.setFixedSequence(true)
        val numberOfTests = 1000
        val serie = (1..numberOfTests).map { testOneGameGenius(it) }
        println()
        println("----------------------------------------------------------------")
        println("%7d runs           WIJ        ZIJ".format(numberOfTests))
        val winsNS = serie.count { it.getNorthSouthTotal() > it.getEastWestTotal() }
        val winsEW = serie.count { it.getNorthSouthTotal() < it.getEastWestTotal() }
        println("number of wins: %10d %10d".format(winsNS,winsEW))
        val total = serie.reduce { acc, score -> acc.plus(score) }
        println("Points          %10d %10d".format(total.getNorthSouthTotal(), total.getEastWestTotal()))

        println()
        println("----------------------------------------------------------------")
        println("EXPECTED:")
        println("----------------------------------------------------------------")
        println("%7d runs           WIJ        ZIJ".format(1000))
        println("number of wins: %10d %10d".format(978, 22))
        println("Points          %10d %10d".format(2124123, 1059727))
    }

    private fun testOneGameGenius(index: Int): ScoreKlaverjassen {
        val game = Game.startNewGame(TableSide.WEST)
        val playerGroup = PlayerGroup(
            listOf(
                PlayerKlaverjassen(TableSide.WEST, game),
                GeniusPlayerKlaverjassen(TableSide.NORTH, game),
                PlayerKlaverjassen(TableSide.EAST, game),
                GeniusPlayerKlaverjassen(TableSide.SOUTH, game),
            )
        )

        while (!game.isFinished()) {
            val sideToMove = game.getSideToMove()
            val playerToMove = playerGroup.getPlayer(sideToMove) as PlayerKlaverjassen

            if (game.hasNewRoundStarted()) {
                playerGroup.dealCards()
                val trumpColor = playerToMove.chooseTrumpColor()
                game.setTrumpColorAndContractOwner(trumpColor, playerToMove.tableSide)
            }

            val suggestedCardToPlay = playerToMove.chooseCard()
            playCard(playerToMove, game, suggestedCardToPlay)
        }
        return game.getAllScoresPerRound().reduce { acc, roundScore -> acc.plus(roundScore) }
    }

    private fun playCard(playerToMove: Player, game: Game, cardToPlay: Card) {
        playerToMove.removeCard(cardToPlay)
        game.playCard(cardToPlay)
    }

}
