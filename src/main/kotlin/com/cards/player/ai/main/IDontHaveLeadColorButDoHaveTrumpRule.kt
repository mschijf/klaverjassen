package com.cards.player.ai.main

import com.cards.game.card.Card
import com.cards.game.klaverjassen.beats
import com.cards.game.klaverjassen.toRankNumberTrump
import com.cards.player.Player

/*

=====================================
IK KAN NIET BIJLOPEN EN HEB WEL TROEF
=====================================

als boer er nog in: 2 x de nel: gooi nel
                  : 3 x de nel: bewaar nel

anders
--> gooi hoogste kaart uit hand op die niet de hoogste kaart in het spel is
--> is die er niet dan heb je alleen hoogste kaarten: gooi die kaart op die meeste roem over laat houden


(als maat gaat en hij heeft (waarschijnlijk) de boer), dan wel nel opgooien - en later met andere troef zo roemvol terugkomen.)

 */

class IDontHaveLeadColorButDoHaveTrumpRule(player: Player): AbstractChooseCardFollowerRule(player) {


    //------------------------------------------------------------------------------------------------------------------

    override fun chooseCard(): Card {
        if (trumpJack in myLegalCards) {
            return if (trumpNine in myLegalCards) {
                myLegalCards
                    .filter{ trumpAce.beats(it, trump)}
                    .maxByOrNull { it.cardValue() }
                    ?:trumpNine
            } else {
                val highestAtOthers = memory.cardsInPlayOtherPlayers
                    .filter { it.isTrump() }
                    .maxByOrNull { it.toRankNumberTrump() }

                if (highestAtOthers != null) {
                    myLegalCards
                        .filter { highestAtOthers.beats(it, trump) }
                        .maxByOrNull { it.cardValue() }
                        ?:myLegalCards
                            .filter { !it.
                            isHigherThanOtherInPlay() }
                            .maxByOrNull { it.cardValue() }
                        ?:myLegalCards.first()
                } else { //ik ben de enige nog met troef, maakt niet uit welke je opgooit
                    myLegalCards.minBy { it.cardValue() }
                }
            }
        }

        if (trumpJack in memory.cardsInPlayOtherPlayers) {
            if (trumpNine in myLegalCards) {
                if (myLegalCards.size == 2)
                    return trumpNine
                if (myLegalCards.size >= 3) //hoogste onder de nel
                    return myLegalCards.filter{ trumpNine.beats(it, trump)}.maxBy { it.cardValue() }
            }
        }

        //todo: net als bij de nel rule: hou hoogste kaart vast als je 3 of meer van die kaart heb ??

        //gooi hoogste kaart uit hand op die niet de hoogste kaart in het spel is
        val candidate = myLegalCards.filter { !it.isHigherThanOtherInPlay() }.maxByOrNull { it.cardValue() }
        if (candidate != null) {
            return candidate
        }

        //alles is hoogste. Kunnen we nog roem halen? Zo ja, bewaar die kaart(en)
        val candidate2 = myLegalCards.firstOrNull { !isRoemPossibleNextTrick(it) }
        if (candidate2 != null) {
            return candidate2
        }

        return myLegalCards.first()
    }

    //------------------------------------------------------------------------------------------------------------------


}