package com.cards.player.ai

import com.cards.game.card.Card

class LeadPlayerInTrick(player: GeniusPlayerKlaverjassen, analyzer: KlaverjassenAnalyzer): AbstractPlayerInTrick(player, analyzer) {
    override fun chooseCard(): Card {
        if (firstTrick() && isContractOwner() && isLeadPlayer() && hasTrumpJack())
            return trumpJack()

        return player.getCardsInHand().first()
    }
}