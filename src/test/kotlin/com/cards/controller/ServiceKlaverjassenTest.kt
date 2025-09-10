package com.cards.controller

import com.cards.game.card.Card
import com.cards.game.klaverjassen.Game
import com.cards.game.klaverjassen.ScoreKlaverjassen
import com.cards.game.klaverjassen.ScoreType
import com.cards.game.klaverjassen.TableSide
import com.cards.player.Player
import com.cards.player.PlayerGroup
import com.cards.player.ai.AbstractChooseCardRule
import com.cards.player.ai.GeniusPlayerKlaverjassen
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
            val scoresPerRound = testOneGame(game, playerGroup)
            scoresPerRound.reduce { acc, roundScore -> acc.plus(roundScore) }
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

        AbstractChooseCardRule.doPrintFallBack = false

        val startTime = System.currentTimeMillis()
        var natCountNS = 0
        var natCountEW = 0
        var pitCountNS = 0
        var pitCountEW = 0

        RANDOMIZER.unsetSeed()
        RANDOMIZER.setFixedSequence(true)
        val numberOfTests = 1000
        val serie = (1..numberOfTests).map {
//            if (it % 10 == 0) println(it)
            val game = Game()
            val playerGroup = PlayerGroup(
                listOf(Player(TableSide.WEST, game), GeniusPlayerKlaverjassen(TableSide.NORTH, game),
                    Player(TableSide.EAST, game), GeniusPlayerKlaverjassen(TableSide.SOUTH, game),)
            )
            val scoresPerRound = testOneGame(game, playerGroup)
            pitCountNS += scoresPerRound.count { it.getNorthSouthTotal() > 0 && it.scoreType == ScoreType.PIT }
            pitCountEW += scoresPerRound.count { it.getEastWestTotal() > 0 && it.scoreType == ScoreType.PIT }
            natCountNS += scoresPerRound.count { it.getNorthSouthTotal() == 0 && it.scoreType == ScoreType.NAT }
            natCountEW += scoresPerRound.count { it.getEastWestTotal() == 0 && it.scoreType == ScoreType.NAT }
            scoresPerRound.reduce { acc, roundScore -> acc.plus(roundScore) }
        }

        println()
        println("----------------------------------------------------------------")
        println("%7d runs           WIJ        ZIJ".format(numberOfTests))
        val winsNS = serie.count { it.getNorthSouthTotal() > it.getEastWestTotal() }
        val winsEW = serie.count { it.getNorthSouthTotal() < it.getEastWestTotal() }
        println("number of wins: %10d %10d".format(winsNS,winsEW))
        val total = serie.reduce { acc, score -> acc.plus(score) }
        println("Total           %10d %10d".format(total.getNorthSouthTotal(), total.getEastWestTotal()))
        println("Points          %10d %10d".format(total.northSouthPoints, total.eastWestPoints))
        println("Bonus           %10d %10d".format(total.northSouthBonus, total.eastWestBonus))
        println("Pit             %10d %10d".format(pitCountNS, pitCountEW))
        println("Nat             %10d %10d".format(natCountNS, natCountEW))
        val timePassed = System.currentTimeMillis() - startTime
        println("Total time passed: %d.%03d sec".format(timePassed / 1000, timePassed % 1000))
        val max = GeniusPlayerKlaverjassen.maxTiming
        println("maxTime choose card: %d.%03d sec".format(max / 1000, max  % 1000))
        val avg = GeniusPlayerKlaverjassen.avgTiming()
        println("avg time choose card: %d.%03d sec".format(avg / 1000, avg  % 1000))

//        val totalTiming = GeniusPlayerKlaverjassen.totalTiming
//        println("total time choose card: %d.%03d sec".format(totalTiming / 1000, totalTiming  % 1000))
//        val timingCount = GeniusPlayerKlaverjassen.timingCount
//        println("total timing count    : %d".format(timingCount))
//        println("Cards examined during analysis: ${KlaverjassenAnalyzer.t}")

    }

    private fun testOneGame(game: Game, playerGroup: PlayerGroup): List<ScoreKlaverjassen> {
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
        return game.getAllScoresPerRound()
    }


    private fun playCard(playerToMove: Player, game: Game, cardToPlay: Card) {
        playerToMove.removeCard(cardToPlay)
        game.playCard(cardToPlay)
    }

}
