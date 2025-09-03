package com.cards.player.ai

import com.cards.game.card.Card

class LeadPlayerInTrick(player: GeniusPlayerKlaverjassen, analysisResult: KlaverjassenAnalysisResult): AbstractPlayerInTrick(player, analysisResult) {
    override fun chooseCard(): Card {
        if (analysis.iAmContractOwner && hasCard(trumpJack()) )
            return trumpJack()

        return playFallbackCard()
    }

    private fun playFallbackCard(): Card {
        return player.getCardsInHand().first()
    }
}