package com.cards.player.ai

import com.cards.game.card.Card

class SecondPlayerInTrick(player: GeniusPlayerKlaverjassen): AbstractPlayerInTrick(player) {

    private val leadColor = leadColor()!!

    override fun chooseCard(): Card {
        return cardGivingHighestValue(trick, player.tableSide).card
    }

}