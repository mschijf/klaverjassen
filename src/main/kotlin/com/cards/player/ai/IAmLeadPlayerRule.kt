package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.player.Player

class IAmLeadPlayerRule(player: Player): AbstractChooseCardLeaderRule(player) {
    override fun chooseCard(): Card {
        if (iAmContractOwner && iHaveCard(trumpJack) )
            return trumpJack

        return playFallbackCard()
    }

    private fun playFallbackCard(): Card {
        return player.getCardsInHand().first()
    }
}