package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.player.Player

class IAmLeadPlayerTheyOwnContractRule(player: Player): AbstractChooseCardLeaderRule(player) {
    override fun chooseCard(): Card {
        if (iAmContractOwner && iHaveCard(trumpJack) )
            return trumpJack

        return playFallbackCard()
    }
}