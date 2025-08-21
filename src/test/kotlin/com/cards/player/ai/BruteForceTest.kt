package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.klaverjassen.Game
import com.cards.game.klaverjassen.TableSide
import com.cards.player.PlayerGroup
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class BruteForceTest {

    @Test
    fun mostValuableCardToPlay() {
        val game = Game.startNewGame(TableSide.WEST)
        val playerGroup = PlayerGroup(
            listOf(
                GeniusPlayerKlaverjassen(TableSide.WEST, game),
                GeniusPlayerKlaverjassen(TableSide.NORTH, game),
                GeniusPlayerKlaverjassen(TableSide.EAST, game),
                GeniusPlayerKlaverjassen(TableSide.SOUTH, game),
            )
        )
        val analyzer: KlaverjassenAnalyzer = mockk()
//        val trick = Trick(TableSide.EAST)
        setUp(analyzer)

        println(analyzer.playerCanHaveCards(TableSide.WEST))
        println(analyzer.playerSureHasCards(TableSide.WEST))
    }

    fun setUp(analyzer: KlaverjassenAnalyzer) {
        every { analyzer.playerSureHasCards(TableSide.WEST) } returns emptySet()
        every { analyzer.playerSureHasCards(TableSide.NORTH) } returns emptySet()
        every { analyzer.playerSureHasCards(TableSide.EAST) } returns emptySet()
        every { analyzer.playerSureHasCards(TableSide.SOUTH) } returns emptySet()

        every { analyzer.playerCanHaveCards(TableSide.WEST) } returns
                listOf("8C", "9C", "10C", "JC", "QC").map{Card.of(it)}.toSet()
        every { analyzer.playerCanHaveCards(TableSide.NORTH) } returns
                listOf("8C", "9C", "10C", "JC", "QC").map{Card.of(it)}.toSet()
        every { analyzer.playerCanHaveCards(TableSide.EAST) } returns
                listOf("8C", "9C", "10C", "JC", "QC").map{Card.of(it)}.toSet()
        every { analyzer.playerCanHaveCards(TableSide.SOUTH) } returns
                listOf("8C", "9C", "10C", "JC", "QC").map{Card.of(it)}.toSet()
    }
}