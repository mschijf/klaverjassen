package com.cards.player.ai

import com.cards.game.card.Card

class ThirdPlayerInTrick(player: GeniusPlayerKlaverjassen): AbstractPlayerInTrick(player) {

    private val leadColor = leadColor()!!

    override fun chooseCard(): Card {
        return cardGivingHighestValue(trick, player.tableSide).card
    }

}