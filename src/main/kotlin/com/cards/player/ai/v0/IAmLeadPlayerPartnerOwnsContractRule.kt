package com.cards.player.ai.v0

import com.cards.game.card.Card
import com.cards.player.Player

/*

maat nog niet aan slag geweest
  ==> kom troef




 */
class IAmLeadPlayerPartnerOwnsContractRule(player: Player): AbstractChooseCardLeaderRule(player) {
    override fun chooseCard(): Card {
        if (iHaveCard(trumpJack) )
            return trumpJack

        return playFallbackCard()
    }
}