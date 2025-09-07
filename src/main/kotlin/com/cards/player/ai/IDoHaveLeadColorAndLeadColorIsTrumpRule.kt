package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.klaverjassen.beats
import com.cards.game.klaverjassen.toRankNumberTrump

/*


TROEF BIJLOPEN
==============
*** slag (blijft) aan andere partij:
       ==> zo laag mogelijk, liever geen roem.
                             hoogste vasthouden, tenzij veel roem (>= 50),
                                                 tenzij hele slechte kaart (geen azen, aantal troef <= 2 en nog geen troef gespeeld, anders dan in de trick)

***   ==> tegenstander slag (boer), eerste ronde. Geen nel in handen: ontwijk roem

*** ==>  maar kan slag pakken
        ==> zo laag mogelijk pakken (als 4e speler), tenzij je nel hebt en boer zit er nog in, dan de nel.
                                                   , tenzij je aas hebt en boer e/o nel zit er nog in, dan de aas.
                                                   , tenzij je 10 hebt en aas, boer e/o nel zit er nog in, dan de 10.

*** tweede ronde, tegenstander gaat en komt laag uit.
    je hebt de hoogste maar ook 2-na hoogste --> gooi die
    gooi laagste

*** 1e ronde tegenstander gaat en komt met boer
    vbd trick: b   zelf: 10,v  dan 10 (kans op 9 bij tegenpartij is groot en kans op 40,70 met vrouw te groot)


 */

class IDoHaveLeadColorAndLeadColorIsTrumpRule(player: GeniusPlayerKlaverjassen, brainDump: BrainDump): AbstractChooseCardFollowerRule(player, brainDump) {

    //------------------------------------------------------------------------------------------------------------------

    override fun chooseCard(): Card {
        if (brainDump.contractOwner.isOtherParty() && currentTrick.getSideToLead().isContractOwner() && brainDump.trump.playedBefore()  ) {
            val myHighest = myLegalCards.highestTrumpCard()!!
            if (myHighest.isHigherThanOtherInPlay() && myHighest.beats(winningCard, brainDump.trump)) {
                val secondHighest = (myLegalCards-myHighest).highestTrumpCard()!!
                if (brainDump.cardsInPlayOtherPlayers.count { it.color.isTrump() && it.beats(secondHighest, brainDump.trump)} == 1) {
                    return secondHighest
                } else {
                    return myLegalCards.minBy { it.toRankNumberTrump() }
                }
            }
        }

        if (brainDump.contractOwner.isOtherParty() && brainDump.iAmFourthPlayer && myLegalCards.first().beats(currentTrick.getWinningCard(), brainDump.trump)) {
            if (trumpNine in myLegalCards && trumpJack in brainDump.cardsInPlayOtherPlayers)
                return trumpNine
            if (trumpAce in myLegalCards && (trumpJack in brainDump.cardsInPlayOtherPlayers || trumpNine in brainDump.cardsInPlayOtherPlayers))
                return trumpAce
            if (trumpTen in myLegalCards && (trumpJack in brainDump.cardsInPlayOtherPlayers || trumpNine in brainDump.cardsInPlayOtherPlayers || trumpAce in brainDump.cardsInPlayOtherPlayers))
                return trumpTen
            return myLegalCards.minBy { it.toRankNumberTrump() }
        }


        if (winningSide.isOtherParty() && winningCard.isJack()) {
            return if (trumpNine !in myLegalCards) {
                myLegalCards.minBy { card ->
                    2 * card.cardValue() +
                            1 * roemSureThisTrickByCandidate(card) +
                            1 * (if (roemPossibleThisTrickByCandidate(card) > 0) 10 else 0) +
                            1 * (if (isRoemPossibleNextTrick(card)) 5 else 0)
                }
            } else {
                val chooseFrom = if (keepCardInHandToPreventPit(trumpNine)) myLegalCards-trumpNine else myLegalCards
                chooseFrom.minBy { card ->
                    2 * card.cardValue() +
                            1 * roemSureThisTrickByCandidate(card) +
                            1 * (if (roemPossibleThisTrickByCandidate(card) >= 50) 10 else 0) +
                            1 * (if (isRoemPossibleNextTrick(card)) 5 else 0)
                }
            }
        }

        if (winningSide.isOtherParty() && weCannotWinThisTrick()) {
            val myHighest = myLegalCards.highestTrumpCard()!!
            val chooseFrom = if (myHighest.beats(brainDump.cardsInPlayOtherPlayers.highestTrumpCard(), brainDump.trump)) {
                if (keepCardInHandToPreventPit(trumpNine)) myLegalCards-myHighest else myLegalCards
            } else {
                myLegalCards
            }

            return chooseFrom.minBy { card ->
                2 * card.cardValue() +
                        1 * roemSureThisTrickByCandidate(card) +
                        1 * (if (roemPossibleThisTrickByCandidate(card) > 0) 10 else 0) +
                        1 * (if (isRoemPossibleNextTrick(card)) 5 else 0)
            }
        }

        return playFallbackCard()
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun Collection<Card>.highestTrumpCard(): Card? {
        return this.maxBy { it.toRankNumberTrump() }
    }

    private fun weCannotWinThisTrick(): Boolean {
        if (!winningSide.isOtherParty())
            return false

        return if (brainDump.iAmThirdPlayer || brainDump.iAmFourthPlayer) {
            myLegalCards.none { it.beats(winningCard, brainDump.trump) }
        } else {
            brainDump.player2.legalCards.none { it.beats(winningCard, brainDump.trump) }
        }
    }


    private fun keepCardInHandToPreventPit(card: Card) = (brainDump.numberOfTricksWonByUs == 0) && !denkIkNogEenSlagTeHalenZonder(card)

    private fun denkIkNogEenSlagTeHalenZonder(card: Card): Boolean {
        //tenzij hele slechte kaart (geen azen, aantal troef <= 2 en nog geen troef gespeeld, anders dan in de trick)
        val checkCards = (myCardsInHand-card).filter { !it.isTrump() }
        if (checkCards.any { it.isAce()})
            return true
        if (checkCards.filter { it.isTen()}.any {ten -> checkCards.count{it.color == ten.color} > 1})
            return true //todo: check op kale 10 en 10 high en niet vrij)
        if (checkCards.count {it.isTrump()} > 1)
            return true
        return false
    }
}