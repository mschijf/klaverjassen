package com.cards.player.ai

import com.cards.game.card.Card

/*

IK KAN NIET BIJLOPEN EN HEB WEL TROEF
=====================================
als boer er nog in: 2 x de nel: gooi nel
                  : 3 x de nel: bewaar nel
(als maat gaat en hij heeft (waarschijnlijk) de boer), dan wel nel opgooien - en later met andere troef zo roemvol terugkomen.)

 */

class IkHebGeenLeadColorMaarWelTroefRule(player: GeniusPlayerKlaverjassen, brainDump: BrainDump): AbstractPlayerRules(player, brainDump) {


    //------------------------------------------------------------------------------------------------------------------

    override fun chooseCard(): Card {
        return playFallbackCard()
    }

    //------------------------------------------------------------------------------------------------------------------
}