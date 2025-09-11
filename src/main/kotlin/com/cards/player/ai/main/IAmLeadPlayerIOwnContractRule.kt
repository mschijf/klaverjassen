package com.cards.player.ai.main

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.klaverjassen.toRankNumber
import com.cards.player.Player


/*

troef getrokken en maat geseind en ik heb die kleur en zelf geen bijkaart, maar nog wel troef en nog vier slagen te gaan
  -- kans dat maat alle slagen ( = aantal - troef aantal) haalt is groot zeg aantal - troef <= 2, bij lage sein
                                                                         zeg aantal - troef <= 3, bij hoge sein
                                                                         evt bijstellen als pit nog mogelijk is
           ==> trek troef door en kom dan de sein kaart
           ==> anders: troef vast houden en direct seinkaart komen
           (of derde kleur en kans op laatste slag groot houden)
  vbd: troef 8H, kale 10D (geseind), JC+8C ==> 8C (twee kansen op laatste slag, 1 vd troef, 1 van maat)





========================================================================================================================
bepaal tactiek:
troef trekken door hoogste (algemeen, eerste slag)
troef trekken door laagste (hoogste troef er uit willen hebben, zodat je zelf de hoogste troeven over houdt)
troef uit spel halen, want ik heb goede bijkaart
troef uit spel halen want maat heeft geseind en heeft dus een bijkaart
troef, maar ook eigen bijkaart doortrekken, want maat heeft geseind
troef doortrekken en één troef vasthouden om terug aan slag te komen, want ik heb goede bijkaart
laatste slag willen halen

 */
class IAmLeadPlayerIOwnContractRule(player: Player): AbstractChooseCardLeaderRule(player) {
    override fun chooseCard(): Card {

            if (myTrumpCards.isNotEmpty()) {
            if (firstTrickIAmLead()) {
                val candidate = trekTroefDoor()
                if (candidate != null)
                    return candidate
            }

            if (secondTrickIAmLead()) {
                if (opponentCanHaveTroef()) {
                    val candidate = trekTroefDoor()
                    if (candidate != null)
                        return candidate
                }
            }
        }

        if (!opponentCanHaveTroef()) {
            val potentialTricksToWinByMe = potentialTricksToWinByMe()
            val potentialTricksToWinByPartner = potentialTricksToWinByPartner()
            if (potentialTricksToWinByMe == myCardsInHand.size) {
                return if (myTrumpCards.size < myCardsInHand.size)
                    (myCardsInHand - myTrumpCards)
                        .filter { it.isHigherThanOtherInPlay() }
                        .maxBy { roemPossibleThisTrickByCandidate(it) }
                else
                    myTrumpCards.maxBy {
                        //if (it.isHigherThanOtherInPlay()) 10 else 0 +
                        roemPossibleThisTrickByCandidate(it)
                    }
            } else if (myCardsInHand.size - potentialTricksToWinByMe >= 1 && potentialTricksToWinByPartner >= 1) {
                val highBijkaart = (myCardsInHand - myTrumpCards)
                    .filter { it.isHigherThanOtherInPlay() }
                    .maxByOrNull { roemPossibleThisTrickByCandidate(it) }
                if (highBijkaart != null)
                    return highBijkaart
                val trumpCard = myTrumpCards
                    .filter { it.isHigherThanOtherInPlay() }
                    .maxByOrNull { roemPossibleThisTrickByCandidate(it) }
                if (trumpCard != null)
                    return trumpCard
                val seinKaart = myCardsInHand
                    .filter {it.color == player2.geseindeKleur}
                    .maxByOrNull { roemPossibleThisTrickByCandidate(it) }
                if (seinKaart != null)
                    return seinKaart
            }
        }

        if (bijKaartByColor().isNotEmpty()) {
            val longestBijKaartColor = bijKaartByColor().maxBy { it.value }.key
            val bijKaartCards = myCardsInHand.filter { it.color == longestBijKaartColor }
            return bijKaartCards
                .filter { it.isHigherThanOtherInPlay() }
                .maxBy { roemPossibleThisTrickByCandidate(it) }
        }

        if (player2.geseindeKleur != null && !opponentCanHaveTroef()) {
            val seinCards = myCardsInHand.filter { it.color == player2.geseindeKleur }
            if (seinCards.isNotEmpty()) {

                return seinCards.maxBy { roemPossibleThisTrickByCandidate(it) }
            }
        }


//        return playFallbackCard(this.javaClass.simpleName+ ": chooseCard")
        return playFallbackCard()
    }

    protected fun bijKaartByColor() = CardColor.values().filter { it != trump && it.iHaveHighest() }.associateWith{ color -> myCardsInHand.count { it.color == color} }
    protected fun firstTrickIAmLead() = currentRound.getTrickList().count { it.isSideToLead(mySide ) } == 1
    protected fun secondTrickIAmLead() = currentRound.getTrickList().count { it.isSideToLead(mySide ) } == 2

    //tactic questions
    protected fun aantalZekereSlagenAlsTroefEruit() = myCardsInHand.filter { !it.isTrump() }.count { it.isHigherThanOtherInPlay() }
    private fun ikWilDeHoogsteTroefEruitHebben() = myTrumpCards.size > 2 || aantalZekereSlagenAlsTroefEruit() > 1 || myCardsInHand.any {it.color == player2.geseindeKleur}

    //tactic actions
    private fun trekTroefDoor(): Card? {
        if (trump.iHaveHighest()) {
            return myTrumpCards
                .filter { it.isHigherThanOtherInPlay() }
                .maxBy { roemPossibleThisTrickByCandidate(it) }
        } else if (ikWilDeHoogsteTroefEruitHebben() && opponentCanHaveTroef()) {
            return myTrumpCards.minBy { card ->
                2 * card.cardValue() + roemPossibleThisTrickByCandidate(card) / 2
            }
        }
        return null
    }

    private fun potentialTricksToWinByMe(): Int {
        if (opponentCanHaveTroef())
            return -1
        return myTrumpCards.size + trump.otherColors().sumOf {it.tricksToWinIfNoTrump()}
    }

    private fun CardColor.tricksToWinIfNoTrump(): Int {
        val numberOfColor = myLegalCardsByColor[this]?.size?:0
        if (numberOfColor == 0)
            return 0

        if (!this.iHaveHighest()) //todo: improve when gedekte 10?
            return 0

        val mySecondHighest = myLegalCardsByColor[this]?.minus(myHighest())?.maxByOrNull { it?.toRankNumber(trump)?:-1 }
        val mySecondHighestIsAlsoHigh = mySecondHighest?.isHigherThanOtherInPlay()?:false

        val numberOfColorStillInPlay = memory.cardsInPlayOtherPlayers.count { it.color == this }
        if (numberOfColorStillInPlay == 0)
            return numberOfColor

        return when (numberOfColor) {
            5,6,7,8 -> numberOfColor
            4 -> when (numberOfColorStillInPlay) {
                4 -> if (mySecondHighestIsAlsoHigh) 4 else 1
                3 -> if (mySecondHighestIsAlsoHigh) 4 else 1
                2,
                1 -> return 4
                else -> throw Exception("not possible")
            }
            3 -> if (mySecondHighestIsAlsoHigh) 3 else 1
            2 -> if (mySecondHighestIsAlsoHigh) 2 else 1
            1 -> return 1
            else -> throw Exception("not possible")
        }
    }

    //todo: gebruik mustHave voor verdere verfijning
    private fun potentialTricksToWinByPartner(): Int {
        if (opponentCanHaveTroef())
            return -1
        val trumpCards = player2.allAssumeCards.count { it.isTrump() }
        return if (myCardsInHand.any { it.color == player2.geseindeKleur })
            1 + trumpCards
        else
            0
    }

}