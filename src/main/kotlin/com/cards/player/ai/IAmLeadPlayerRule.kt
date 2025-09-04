package com.cards.player.ai

import com.cards.game.card.Card

class IkBenLeadPlayerRule(player: GeniusPlayerKlaverjassen, analysisResult: BrainDump): AbstractPlayerRules(player, analysisResult) {
    override fun chooseCard(): Card {
        if (brainDump.iAmContractOwner && hasCard(trumpJack()) )
            return trumpJack()

        return playFallbackCard()
    }

    private fun playFallbackCard(): Card {
        return player.getCardsInHand().first()
    }
}