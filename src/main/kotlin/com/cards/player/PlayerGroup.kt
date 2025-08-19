package com.cards.player

import com.cards.game.card.CARDDECK
import com.cards.game.fourplayercardgame.basic.TableSide
import com.cards.tools.RANDOMIZER
import kotlin.collections.chunked

class PlayerGroup(private val playerList: List<Player>) {

    fun dealCards() {
        val cardDeck = CARDDECK.baseDeckCardsSevenAndHigher.shuffled(RANDOMIZER.getShuffleRandomizer())
        val cardPiles = cardDeck.chunked(cardDeck.size/ playerList.size)
        playerList.forEachIndexed { idx, player -> player.setCardsInHand(cardPiles[idx]) }
    }

    fun allEmptyHanded() = playerList.all { player -> player.getCardsInHand().isEmpty() }

    fun getPlayer(side: TableSide) = playerList.first { cardPlayer -> cardPlayer.tableSide == side }
}