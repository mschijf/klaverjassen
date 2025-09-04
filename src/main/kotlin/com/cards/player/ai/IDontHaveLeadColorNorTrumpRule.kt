package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.card.CardRank
import com.cards.game.klaverjassen.beats
import com.cards.game.klaverjassen.bonusValue
import com.cards.game.klaverjassen.cardValue
import com.cards.game.klaverjassen.toBonusRankNumber
import tool.mylambdas.collectioncombination.mapCombinedItems
import kotlin.math.max
import kotlin.math.sign

/*

//    ======================================
//    IK KAN NIET BIJLOPEN EN HEB GEEN TROEF
//    ======================================

    *** Slag aan tegenstander - kon geen onze slag worden (ik ben speler 3 of 4)
        ==> gooi kale kaart bij, als weinig punten
        ==> gooi lage kaart bij van > 2 aanwezig van kleur, roem ontwijkend voor volgende slag
        bijvoorbeeld 7,8,9 -> gooi 9
        b,v,h --> gooi b
        ==> maar maak 10 niet kaal! (aas liever ook niet)
        ==> vbd 7,v,a  en 9,b (beide kleuren nog niet gespeeld). Dan 7 (want aas in handen van die kleur, en bij andere kleur speel je jezelf kaal en geef je kans op roem weg)
        ==> b,v,9 ==> hoge kans op roem kaarten, 8,h mid-kans op roem, 7: kleine kans op roem
        Dus: minste punten, kale kaart, 10 niet kaal maken, huidge roem ontwijkend, toekomstige roem ontwijkend

    *** Slag aan maat (en andere partij gaat) en zeker weten dat slag aan maat blijft:
        ==> gooi kale kaart op, die niet hoogste is en kans op slag halen met die kaart is aanwezig
        ==> of laag als je wilt seinen (maar dan moet troef op zijn (of kleiner dan 1)
        ==> gooi 10 op (als geen aas)
        ==> gooi aas op (als wel 10 en >= 3 van de kleur)


    *** Slag aan maat (en maat gaat) en niet zeker dat slag aan maat blijft
        ==> hou 10 gedekt
        ==> seinen?
        ==> hou aas vast
        ==> als kans groot dat slag naar tegenstander gaat, dan 9,8,7, b,v,h, 10,a
        ==> als kans groot dat slag bij maat blijft dan b,h,v, 9,8,7 , 10,a
        (let wel: gedekte 10 van niet gespeelde kleur is meer waard dan vrije aas.)

    *** Slag aan maat en maat gaat en slag blijft bij maat
        ==> als meerdere kleuren en hoogste van een kleur en maat heeft die kleur ook, dan seinen (lage kaart vd kleur)
        ==> als meerdere kleuren en hoogste van een kleur en maat heeft die kleur ook, dan seinen (hoogste kaart vd kleur, als ook een na hoogste en 'lange kaart', of meerdere hoogste kleuren)
        ==> als niks te seinen: kale of dubbel gedekte 10, tenzij 10 de hoogste is en kans op een slag halen met die 10 nog kan
        : zoveel mogelijk punten
        : toekomstig roem ontwijkend
        :

 */

class IkHebGeenLeadColorEnGeenTroefRule(player: GeniusPlayerKlaverjassen, brainDump: BrainDump): AbstractPlayerRules(player, brainDump) {

    override fun chooseCard(): Card {
        if (currentTrick.getWinningSide()!!.isPartner()){
            if (partnerWillWinThisTrick()) {
                if (brainDump.theyOwnContract) {
                    return slagAanMaatEnAnderePartijGaatEnZekerWetenDatSlagAanMaatBlijft()
                } else {
                    return slagAanMaatEnMaatGaatEnZekerWetenDatSlagAanMaatBlijft()
                }
            } else {
                return slagAanMaatEnMaatGaatEnNietZekerDatSlagAanMaatBlijft()
            }
        } else {
            if (weCannotWinThisTrick()) {
                return slagAanTegenstanderEnHetKanOnzeSlagNietWorden()
            } else {
//                Slag aan tegenstander - maar we kunnen deze slag nog winnen (i am 2,3 or 4)
                return playFallbackCard()
            }
        }
    }

    private fun weCannotWinThisTrick() = currentTrick.getWinningSide()!!.isOtherParty() && (brainDump.iAmThirdPlayer || brainDump.iAmFourthPlayer)

    private fun partnerWillWinThisTrick() =
        currentTrick.getWinningSide()!!.isPartner() &&
                when {
                    brainDump.iAmSecondPlayer ->
                        throw Exception("i am second player and partner has winning card is not possible")
                    brainDump.iAmThirdPlayer ->
                        brainDump.player1.legalCards.none { it.beats(currentTrick.getWinningCard()!!, brainDump.trump) }
                    brainDump.iAmFourthPlayer ->
                        true
                    else ->
                        false
                }

    //------------------------------------------------------------------------------------------------------------------

    private fun slagAanMaatEnAnderePartijGaatEnZekerWetenDatSlagAanMaatBlijft(): Card {
//                    Slag aan maat (en andere partij gaat) en zeker weten dat slag aan maat blijft:
//                    ==> gooi kale kaart op, tenzij die de hoogste is en kans op slag halen met die kaart is nog aanwezig
//                    ==> of laag als je wilt seinen (maar dan moet troef op zijn (of kleiner dan 1)
//                    ==> gooi 10 op (als zelf geen aas en niet hoogste)
//                    ==> gooi aas op (als wel 10 en >= 3(?) van de kleur in hand) afhankelijk hoe ver in spel


        //(1a)
        val bareTenCardCandidates = myLegalCards.filter { it.isKaal() && it.isTen() && !it.isHighestInPlay() }
        if (bareTenCardCandidates.isNotEmpty())
            return bareTenCardCandidates.last()

        //(3)
        val tenCardCandidates = myLegalCards.filter { it.isTen() && !it.isHighestInPlay() }
        if (tenCardCandidates.isNotEmpty())
            return tenCardCandidates.maxByOrNull { myLegalCardsByColor[it.color]!!.size }!!

        //(4a): note: aas opgooien is ook seinen
        val aceCardColors = myLegalCards.filter { it.isAce() }.map { it.color }.toSet()
        val aceCardCandidates = myLegalCards.filter { it.color in aceCardColors && !it.isAce() && it.isHighestInPlay() }
        if (aceCardCandidates.isNotEmpty())
            return aceCardCandidates.maxByOrNull { myLegalCardsByColor[it.color]!!.size }!!

        //(4b)
        val tenCardColors = myLegalCards.filter { it.color !in aceCardColors && it.isTen() && it.isHighestInPlay() }.map { it.color }.toSet()
        val tenCardHighCandidates = myLegalCards.filter { it.color in tenCardColors && !it.isTen() && it.isHighestInPlay() }
        if (tenCardHighCandidates.isNotEmpty())
            return tenCardHighCandidates.maxByOrNull { myLegalCardsByColor[it.color]!!.size }!!

//                    //(2) todo: seinen!!
//                    val highCardColors = legalCards.filter { !it.isKaal() && it.isHighestInPlay() }.map { it.color }.toSet()
//                    val signalCandidates = legalCards.filter { it.color in highCardColors && it.isLowCard() }
//                    if (signalCandidates.isNotEmpty()) //sein kortste kaart (of beter langste kaart?)
//                        return signalCandidates.minByOrNull { legalCardsByColor[it.color]!!.size }!!

        //(1b)
        val bareCardsNotHighest = myLegalCards.filter { it.isKaal() && !it.isHighestInPlay() }
        val bareCardCandidates = bareCardsNotHighest.sortedBy{ 10 * it.cardValue(brainDump.trump) + if (it.isVrij()) 0 else 1 }
        if (bareCardCandidates.isNotEmpty())
            return bareCardCandidates.last() //let op, je gooit evt nu een kale 7 weg

        return playFallbackCard("Slag aan maat (en andere partij gaat) en zeker weten dat slag aan maat blijft:")
    }


    private fun slagAanMaatEnMaatGaatEnZekerWetenDatSlagAanMaatBlijft(): Card {
//                    Slag aan maat (en maat gaat) en slag blijft bij maat
//                    ==> als meerdere kleuren en hoogste van een kleur en maat heeft die kleur ook, dan seinen (hoogste kaart vd kleur, als ook een na hoogste en 'lange kaart', of meerdere hoogste kleuren)
//                    ==> als meerdere kleuren en hoogste van een kleur en maat heeft die kleur ook, dan seinen (lage kaart vd kleur)
//                    ==> als niks te seinen:
//                         ==> kale 10, tenzij 10 de hoogste is en kans op een slag halen met die 10 nog kan
//                             of dubbel gedekte 10, tenzij 10 de hoogste is en kans op een slag halen met die 10 nog kan
//                         ==> zoveel mogelijk punten
//                         ==> toekomstig roem ontwijkend

        val highestInPlayColors = myLegalCards.filter { it.isHighestInPlay() }.map { it.color }.toSet()
        val seinCards = myLegalCards.filter { it.color in brainDump.partnerCardColors && it.color in highestInPlayColors }

        //(1a)
        val aceCardColors = seinCards.filter { it.isAce()  }.map { it.color }.toSet()
        val aceCardCandidates = seinCards.filter { it.color in aceCardColors && !it.isAce() && it.isHighestInPlay() }
        if (aceCardCandidates.isNotEmpty())
            return aceCardCandidates.maxByOrNull { myLegalCardsByColor[it.color]!!.size }!!

        //(1b)
        val tenCardColors = seinCards.filter { it.color !in aceCardColors && it.isTen() && it.isHighestInPlay() && it.color in brainDump.partnerCardColors }.map { it.color }.toSet()
        val tenCardHighCandidates = seinCards.filter { it.color in tenCardColors && !it.isTen() && it.isHighestInPlay() }
        if (tenCardHighCandidates.isNotEmpty())
            return tenCardHighCandidates.maxByOrNull { myLegalCardsByColor[it.color]!!.size }!!

        //(2)
        val highCardColors = seinCards.filter { !it.isKaal() && it.isHighestInPlay() }.map { it.color }.toSet()
        val signalCandidates = seinCards.filter { it.color in highCardColors && it.isLowCard() }
        if (signalCandidates.isNotEmpty()) //sein kortste kaart (of beter langste kaart?)
            return signalCandidates.minByOrNull { myLegalCardsByColor[it.color]!!.size }!!

        //(3a)
        val bareTenCardCandidates = myLegalCards.filter { it.isKaal() && it.isTen() && !it.isHighestInPlay() }
        if (bareTenCardCandidates.isNotEmpty())
            return bareTenCardCandidates.last()

        //(3b)
        val coveredTenCardCandidates = myLegalCards.filter { !it.isKaal() && it.isTen() && !it.isHighestInPlay() && myLegalCardsByColor[it.color]!!.size >= 3 }
        if (coveredTenCardCandidates.isNotEmpty())
            return coveredTenCardCandidates.last()

        return myLegalCards.filter{ !it.isAce() && !it.isTen() }.maxByOrNull { card ->
            2 * card.cardValue(brainDump.trump) +
                    -1 * card.kaalMakendeKaartPenalty() +
                    1 * roemSureThisTrickByCandidate(card) +
                    1 * (if (roemPossibleThisTrickByCandidate(card) > 0) 10 else 0) +
                    -1 * (if (isRoemPossibleNextTrick(card)) 5 else 0)
        } ?: playFallbackCard("Slag aan maat (en maat gaat) en slag blijft bij maat")
    }

    private fun slagAanMaatEnMaatGaatEnNietZekerDatSlagAanMaatBlijft(): Card {
//                Slag aan maat (en maat gaat) en niet zeker dat slag aan maat blijft
//                    ==> hou 10 gedekt
//                    ==> seinen?
//                    ==> hou aas vast
//                    ==> als kans groot dat slag naar tegenstander gaat, dan 9,8,7, b,v,h, 10,a
//                    ==> als kans groot dat slag bij maat blijft dan b,h,v, 9,8,7 , 10,a
//                    (let wel: gedekte 10 van niet gespeelde kleur is meer waard dan vrije aas.)

        val evaluationRankOrder = listOf(CardRank.NINE, CardRank.EIGHT, CardRank.SEVEN, CardRank.JACK, CardRank.QUEEN, CardRank.KING, CardRank.TEN, CardRank.ACE)
        return myLegalCards.minBy { card ->
            2 * evaluationRankOrder.indexOf(card.rank)
            1 * card.kaalMakendeKaartPenalty() +
                    1 * roemSureThisTrickByCandidate(card) +
                    1 * (if (isRoemPossibleNextTrick(card)) 5 else 0)
        }
    }

    private fun slagAanTegenstanderEnHetKanOnzeSlagNietWorden():Card {
//                Slag aan tegenstander - kon geen onze slag worden (ik ben speler 3 of 4)
//                     ==> gooi kale kaart bij, als weinig punten
//                     ==> gooi lage kaart bij van > 2 aanwezig van kleur, roem ontwijkend voor volgende slag
//                     bijvoorbeeld 7,8,9 -> gooi 9
//                     b,v,h --> gooi b
//                     ==> maar maak 10 niet kaal! (aas liever ook niet)
//                     ==> vbd 7,v,a  en 9,b (beide kleuren nog niet gespeeld). Dan 7 (want aas in handen van die kleur,
//                          en bij andere kleur speel je jezelf kaal en geef je kans op roem weg)
//                     ==> b,v,9 ==> hoge kans op roem kaarten, 8,h mid-kans op roem, 7: kleine kans op roem
//                     Dus: minste punten, kale kaart, 10 niet kaal maken, huidige roem ontwijkend, toekomstige roem ontwijkend
//
//                     to loose values:
//                     initially: value of card (i.e. 0,2,3,4,10,11) * 2
//                     extra: maak 10 kaal en aas in spel en aas kan bij tegenstander: +10 * 2
//                     extra maak aas kaal: +5
//                     extra maak b,v,h kaal: + 2,3,4
//                     extra direct roem makende kaart deze trick: + 20
//                     extra kans op roem makende kaart deze trick: + 10
//                     extra kans op roem achtergebleven kaart volgende trick: +5

        return myLegalCards.minBy { card ->
            2 * card.cardValue(brainDump.trump) +
                    card.kaalMakendeKaartPenalty() +
                    roemSureThisTrickByCandidate(card) +
                    (if (roemPossibleThisTrickByCandidate(card) > 0) 10 else 0) +
                    (if (isRoemPossibleNextTrick(card)) 5 else 0)
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun Card.kaalMakendeKaartPenalty(): Int {
        val kaleKaart = (myLegalCards - this).singleOrNull { it.color == this.color }
        return if (kaleKaart != null) {
            if (!kaleKaart.isHighestInPlay()) {
                when (kaleKaart.rank) {
                    CardRank.TEN -> 2*10
                    else -> kaleKaart.cardValue(brainDump.trump)
                }
            } else {
                0
            }
        } else {
            0
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    //todo: als dubbele kans op roem, dan die anders beoordelen, dan enkele kans op roem
    //      bijv. 7,9 ==> dan is er met 8 kans op 20 roem
    //            8,9 ==> dan is er met 7 en 10 kans op 20 roem

    private fun roemSureThisTrickByCandidate(candidate: Card): Int {
        return (currentTrick.getCardsPlayed() + candidate).bonusValue(brainDump.trump)
    }

    private fun roemPossibleThisTrickByCandidate(candidate: Card): Int {

        val listOfTrickPossibilities = if (brainDump.iAmSecondPlayer) {
            val cardsPlayer1 = brainDump.player1.legalCards
            val cardsPlayer2 = brainDump.player2.legalCards
            if (cardsPlayer1.isNotEmpty() && cardsPlayer2.isNotEmpty()) {
                (cardsPlayer1 + cardsPlayer2).toList().mapCombinedItems { card1, card2 -> (currentTrick.getCardsPlayed() + candidate + card1 + card2) }
            } else {
                (cardsPlayer1 + cardsPlayer2).map { card1 -> (currentTrick.getCardsPlayed() + candidate + card1) }
            }
        } else if (brainDump.iAmThirdPlayer) {
            val cardsPlayer1 = brainDump.player1.legalCards
            (cardsPlayer1).map { card1 -> (currentTrick.getCardsPlayed() + candidate + card1) }
        } else { //iAmFourthPlayer
            listOf((currentTrick.getCardsPlayed() + candidate))
        }
        return listOfTrickPossibilities.maxOf { poss -> poss.bonusValue(brainDump.trump) }
    }

    private fun isRoemPossibleNextTrick(candidate: Card): Boolean {
        val p1 = brainDump.player1.allAssumeCards.filterTo(HashSet()) { it.color == candidate.color }
        val p2 = brainDump.player2.allAssumeCards.filterTo(HashSet()) { it.color == candidate.color }
        val p3 = brainDump.player3.allAssumeCards.filterTo(HashSet()) { it.color == candidate.color }
        val doHave = p1.size.sign + p2.size.sign + p3.size.sign
        if (doHave <= 1)
            return false
        val all = p1 + p2 + p3

        var currentSequence = 0
        var maxSequence = 0
        var lastCardRank = -1000
        all.sortedBy { card -> card.rank }.forEach { c ->
            if (c.toBonusRankNumber() == lastCardRank+1) {
                currentSequence++
            } else {
                maxSequence = max(maxSequence, currentSequence)
                currentSequence = 1
            }
            lastCardRank = c.toBonusRankNumber()
        }
        return (maxSequence > 2)
    }

    //------------------------------------------------------------------------------------------------------------------

}