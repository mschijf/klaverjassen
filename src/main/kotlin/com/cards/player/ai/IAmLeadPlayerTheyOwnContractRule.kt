package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.player.Player

/*

    kom terug wat maat vorige slag kwam

    kom geen troef

    bewaar aas
        - tenzij troef op en alleen hoge en waarschijnlijk alle slagen
        - tenzij vier kaart, dan één keer komen en de volgende keer kleine kaart (hopen op introeven)
        - tenzij alleen azen:: speel aas van langste kaart
                               - tenzij minder dan 3 in spel elke andere speler
        -> van andere kleuren: gooi laagste (roem en zo rekeninghoudend roem)

     gooi kleur die troef bij tegenstander kan trekken

    laat maat introeven (als nog veel troef in spel en je denkt dat maat kan troeven)


 */
class IAmLeadPlayerTheyOwnContractRule(player: Player): AbstractChooseCardLeaderRule(player) {
    override fun chooseCard(): Card {
        return playFallbackCard()
    }
}