package com.cards.player.ai

import com.cards.game.card.Card

class IAmLeadPlayerRule(player: GeniusPlayerKlaverjassen, brainDump: BrainDump): AbstractChooseCardLeaderRule(player, brainDump) {
    override fun chooseCard(): Card {
        if (brainDump.iAmContractOwner && hasCard(trumpJack) )
            return trumpJack

        return playFallbackCard()
    }

    private fun playFallbackCard(): Card {
        return player.getCardsInHand().first()
    }
}