package com.cards.controller

import com.cards.game.card.Card
import com.cards.game.klaverjassen.Game
import com.cards.game.klaverjassen.ScoreKlaverjassen
import com.cards.game.klaverjassen.TableSide
import com.cards.player.Player
import com.cards.player.PlayerGroup
import com.cards.player.ai.GeniusPlayerKlaverjassen
import com.cards.player.ai.KlaverjassenAnalyzer
import com.cards.tools.RANDOMIZER
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ServiceKlaverjassenTest {
    @Test
    fun runTest() {
        val startTime = System.currentTimeMillis()

        RANDOMIZER.unsetSeed()
        RANDOMIZER.setFixedSequence(true)
        val numberOfTests = 1000

        val serie = (1..numberOfTests).map {
            val game = Game()
            val playerGroup = PlayerGroup(
                listOf(Player(TableSide.WEST, game), Player(TableSide.NORTH, game),
                    Player(TableSide.EAST, game), Player(TableSide.SOUTH, game),)
            )
            testOneGame(game, playerGroup)
        }
        println()
        println("----------------------------------------------------------------")
        println("%7d runs           WIJ        ZIJ".format(numberOfTests))
        val winsNS = serie.count { it.getNorthSouthTotal() > it.getEastWestTotal() }
        val winsEW = serie.count { it.getNorthSouthTotal() < it.getEastWestTotal() }
        val equal = serie.count { it.getNorthSouthTotal() == it.getEastWestTotal() }
        println("number of wins: %10d %10d".format(winsNS,winsEW))
        val total = serie.reduce { acc, score -> acc.plus(score) }
        println("Points          %10d %10d".format(total.getNorthSouthTotal(), total.getEastWestTotal()))

        val timePassed = System.currentTimeMillis() - startTime
        print("%d.%03d sec".format(timePassed / 1000, timePassed % 1000))

        assertEquals(501, winsNS)
        assertEquals(498, winsEW)
        assertEquals(1, equal)
        assertEquals(1592297, total.getNorthSouthTotal())
        assertEquals(1601543, total.getEastWestTotal())
    }

    @Test
    fun runTestGenius() {

        val startTime = System.currentTimeMillis()

        RANDOMIZER.unsetSeed()
        RANDOMIZER.setFixedSequence(true)
        val numberOfTests = 1000

        val serie = (1..numberOfTests).map {
//            println(it)
            val game = Game()
            val playerGroup = PlayerGroup(
                listOf(Player(TableSide.WEST, game), GeniusPlayerKlaverjassen(TableSide.NORTH, game),
                    Player(TableSide.EAST, game), GeniusPlayerKlaverjassen(TableSide.SOUTH, game),)
            )
            testOneGame(game, playerGroup)
        }


        println()
        println("----------------------------------------------------------------")
        println("%7d runs           WIJ        ZIJ".format(numberOfTests))
        val winsNS = serie.count { it.getNorthSouthTotal() > it.getEastWestTotal() }
        val winsEW = serie.count { it.getNorthSouthTotal() < it.getEastWestTotal() }
        println("number of wins: %10d %10d".format(winsNS,winsEW))
        val total = serie.reduce { acc, score -> acc.plus(score) }
        println("Points          %10d %10d".format(total.getNorthSouthTotal(), total.getEastWestTotal()))
        println("Cards examined during analysis: ${KlaverjassenAnalyzer.t}")

        val timePassed = System.currentTimeMillis() - startTime
        print("%d.%03d sec".format(timePassed / 1000, timePassed % 1000))

//        println()
//        println("----------------------------------------------------------------")
//        println("EXPECTED:")
//        println("----------------------------------------------------------------")
//        println("%7d runs           WIJ        ZIJ".format(1000))
//        println("number of wins: %10d %10d".format(978, 22))
//        println("Points          %10d %10d".format(2124123, 1059727))
    }

    private fun testOneGame(game: Game, playerGroup: PlayerGroup): ScoreKlaverjassen {
        while (!game.isFinished()) {
            val sideToMove = game.getSideToMove()
            val playerToMove = playerGroup.getPlayer(sideToMove)

            if (game.newRoundToBeStarted()) {
                playerGroup.dealCards()
                val trumpColor = playerToMove.chooseTrumpColor()
                game.startNewRound(trumpColor, playerToMove.tableSide)
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
