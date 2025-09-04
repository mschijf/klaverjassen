package com.cards.player.ai

import com.cards.game.card.Card

/*


TROEF BIJLOPEN
==============
troef bijlopen, slag (blijft) aan andere partij:
   ==> zo laag mogelijk, liever geen roem. hoogste vasthouden, tenzij veel roem (>= 50), tenzij hele slechte kaart (geen azen, aantal troef <= 2 en nog geen troef gespeeld, anders dan in de trick)

troef bijlopen, tegenstander slag (boer), eerste ronde. Geen nel in handen: ontwijk roem

troef bijlopen, maar kan slag pakken
   ==> zo laag mogelijk pakken (als 4e speler), tenzij je nel hebt en boer zit er nog in, dan de nel.
                                              , tenzij je aas hebt en boer e/o nel zit er nog in, dan de aas.
                                              , tenzij je 10 hebt en aas, boer e/o nel zit er nog in, dan de 10.

tweede ronde bijlopen, tegenstander gaat en komt laag uit.
je hebt de hoogste maar ook 2-na hoogste --> gooi die
gooi laagste

1e ronde tegenstander gaat en komt met boer
vbd trick: b   zelf: 10,v  dan 10 (kans op 9 bij tegenpartij is groot en kans op 40,70 met vrouw te groot)


 */

class IkHebWelLeadColorEnDatIsTroefRule(player: GeniusPlayerKlaverjassen, brainDump: BrainDump): AbstractPlayerRules(player, brainDump) {

    //------------------------------------------------------------------------------------------------------------------

    override fun chooseCard(): Card {
        return playFallbackCard()
    }

    //------------------------------------------------------------------------------------------------------------------
}