package com.cards.player.ai.main

import com.cards.game.card.Card
import com.cards.game.klaverjassen.beats
import com.cards.game.klaverjassen.toRankNumberTrump
import com.cards.player.Player

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

*** 1e ronde tegenstander gaat en komt laag (wil boer er uit halen)
    je hebt naast boer nog een keuze
    --> gooi andere (als 2e en 4e speler).
    --> check roem (als 3e speler)

 */

class IDoHaveLeadColorAndLeadColorIsTrumpRule(player: Player): AbstractChooseCardFollowerRule(player) {

    override fun chooseCard(): Card {
        if (contractOwner.isOtherParty() && currentTrick.getSideToLead().isContractOwner() && trump.playedBefore()  ) {
            val myHighest = myLegalCards.highestTrumpCard()!!
            if (myHighest.isHigherThanOtherInPlay() && myHighest.beats(winningCard, trump)) {
                val secondHighest = (myLegalCards-myHighest).highestTrumpCard()!!
                if (memory.cardsInPlayOtherPlayers.count { it.isTrump() && it.beats(secondHighest, trump)} == 1) {
                    return secondHighest
                } else {
                    return myLegalCards.minBy { it.toRankNumberTrump() }
                }
            }
        }

        if (contractOwner.isOtherParty() && myLegalCards.first().beats(currentTrick.getWinningCard(), trump)) {
            if (iAmFourthPlayer) {
                if (trumpJack in myLegalCards && trumpNine in memory.cardsInPlayOtherPlayers) {
                    val extraTrickWinByJack = 2 * (trumpJack.cardValue() + 3)
                    val maxPointsOther = (myLegalCards - trumpJack).maxOf { card -> 2 * card.cardValue() + roemSureThisTrickByCandidate(card) } + extraTrickWinByJack
                    val maxPointsJack = 2 * trumpJack.cardValue() + roemSureThisTrickByCandidate(trumpJack)
                    if (maxPointsJack > maxPointsOther)
                        return trumpJack
                    return (myLegalCards - trumpJack).maxBy { card -> 2 * card.cardValue() + roemSureThisTrickByCandidate(card) }
                }
                if (trumpNine in myLegalCards && trumpJack in memory.cardsInPlayOtherPlayers)
                    return trumpNine
                if (trumpAce in myLegalCards && (trumpJack in memory.cardsInPlayOtherPlayers || trumpNine in memory.cardsInPlayOtherPlayers))
                    return trumpAce
                if (trumpTen in myLegalCards && (trumpJack in memory.cardsInPlayOtherPlayers || trumpNine in memory.cardsInPlayOtherPlayers || trumpAce in memory.cardsInPlayOtherPlayers))
                    return trumpTen
                return myLegalCards.minBy { it.toRankNumberTrump() }
            }
            if (iAmSecondPlayer) {
                if (leadPlayer == contractOwner && trumpJack in myLegalCards && trumpNine in memory.cardsInPlayOtherPlayers) {
                    return (myLegalCards - trumpJack).maxBy { it.toRankNumberTrump() }
                }
                if (leadPlayer == player3Side && trumpJack in myLegalCards && trumpNine in memory.cardsInPlayOtherPlayers) {
                    return (myLegalCards - trumpJack).minBy { it.toRankNumberTrump() }
                }
            }
            if (iAmThirdPlayer) {
                if (trumpJack in myLegalCards && trumpNine in memory.cardsInPlayOtherPlayers) {
                    return trumpJack
                }
            }
        }

        if (winningSide.isOtherParty() && winningCard.isJack()) {
            return if (trumpNine !in myLegalCards) {
                myLegalCards.minBy { card ->
                    2 * card.cardValue() +
                            1 * roemSureThisTrickByCandidate(card) +
                            1 * (if (roemPossibleThisTrickByCandidate(card) > 0) 10 else 0) +
                            1 * (if (isRoemPossibleNextTrick(card)) ROEM_POSSIBLE_NEXT_TRICK_VALUE_MIN else 0)
                }
            } else {
                val chooseFrom = if (keepCardInHandToPreventPit(trumpNine)) myLegalCards-trumpNine else myLegalCards
                chooseFrom.minBy { card ->
                    2 * card.cardValue() +
                            1 * roemSureThisTrickByCandidate(card) +
                            1 * (if (roemPossibleThisTrickByCandidate(card) >= 50) 10 else 0) +
                            1 * (if (isRoemPossibleNextTrick(card)) ROEM_POSSIBLE_NEXT_TRICK_VALUE_MIN else 0)
                }
            }
        }

        if (winningSide.isOtherParty() && weCannotWinThisTrick()) {
            val myHighest = myLegalCards.highestTrumpCard()!!
            val chooseFrom = if (myHighest.beats(memory.cardsInPlayOtherPlayers.highestTrumpCard(), trump)) {
                if (keepCardInHandToPreventPit(trumpNine)) myLegalCards-myHighest else myLegalCards
            } else {
                myLegalCards
            }

            return chooseFrom.minBy { card ->
                2 * card.cardValue() +
                        1 * roemSureThisTrickByCandidate(card) +
                        1 * (if (roemPossibleThisTrickByCandidate(card) > 0) 10 else 0) +
                        1 * (if (isRoemPossibleNextTrick(card)) ROEM_POSSIBLE_NEXT_TRICK_VALUE_MIN else 0)
            }
        }

        if (contractOwner.isPartner() && winningSide.isPartner()) {
            if (winningCard.isNine()) //assume partner has jack as well
                return myLegalCards.maxBy { card ->
                    1 * roemSureThisTrickByCandidate(card) +
                            1 * (if (roemPossibleThisTrickByCandidate(card) > 0) 15 else 0) +
                            -1 * (if (isRoemPossibleNextTrick(card)) ROEM_POSSIBLE_NEXT_TRICK_VALUE_MAX else 0)
                }
            if (player1.legalCards.none{it.beats(winningCard, trump)}) {
                return myLegalCards.maxBy { card ->
                    1 * roemSureThisTrickByCandidate(card) +
                            1 * (if (roemPossibleThisTrickByCandidate(card) > 0) 15 else 0) +
                            1 * (if (isRoemPossibleNextTrick(card)) ROEM_POSSIBLE_NEXT_TRICK_VALUE_MAX else 0)
                }
            }
        }

        if (iAmThirdPlayer || iAmFourthPlayer)
            return cardGivingBestValueByPlayingFullTrick()

//        return cardGivingBestValueByPlayingFullTrick()
        return playFallbackCard(this.javaClass.simpleName)
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun Collection<Card>.highestTrumpCard(): Card? {
        return this.maxBy { it.toRankNumberTrump() }
    }

    private fun weCannotWinThisTrick(): Boolean {
        if (!winningSide.isOtherParty())
            return false

        return if (iAmThirdPlayer || iAmFourthPlayer) {
            myLegalCards.none { it.beats(winningCard, trump) }
        } else {
            player2.legalCards.none { it.beats(winningCard, trump) }
        }
    }



    private fun keepCardInHandToPreventPit(card: Card) = (memory.numberOfTricksWonByUs == 0) && !denkIkNogEenSlagTeHalenZonder(card)

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