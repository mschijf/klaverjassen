package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import com.cards.game.klaverjassen.*
import tool.mylambdas.collectioncombination.mapCombinedItems
import kotlin.math.max
import kotlin.math.sign

/*

IK KAN NIET BIJLOPEN EN HEB GEEN TROEF
======================================
Slag aan tegenstander - kon geen onze slag worden (ik ben speler 3 of 4)
   ==> gooi kale kaart bij, als weinig punten
   ==> gooi lage kaart bij van > 2 aanwezig van kleur, roem ontwijkend voor volgende slag
        bijvoorbeeld 7,8,9 -> gooi 9
        b,v,h --> gooi b
        ==> maar maak 10 niet kaal! (aas liever ook niet)
        ==> vbd 7,v,a  en 9,b (beide kleuren nog niet gespeeld). Dan 7 (want aas in handen van die kleur, en bij andere kleur speel je jezelf kaal en geef je kans op roem weg)
        ==> b,v,9 ==> hoge kans op roem kaarten, 8,h mid-kans op roem, 7: kleine kans op roem
   Dus: minste punten, kale kaart, 10 niet kaal maken, huidge roem ontwijkend, toekomstige roem ontwijkend

Slag aan maat (en andere partij gaat) en zeker weten dat slag aan maat blijft:
   ==> gooi kale kaart op, die niet hoogste is en kans op slag halen met die kaart is aanwezig
   ==> of laag als je wilt seinen (maar dan moet troef op zijn (of kleiner dan 1)
   ==> gooi 10 op (als geen aas)
   ==> gooi aas op (als wel 10 en >= 3 van de kleur)


Slag aan maat (en maat gaat) en niet zeker dat slag aan maat blijft
  ==> hou 10 gedekt
  ==> seinen?
  ==> hou aas vast
  ==> als kans groot dat slag naar tegenstander gaat, dan 9,8,7, b,v,h, 10,a
  ==> als kans groot dat slag bij maat blijft dan b,h,v, 9,8,7 , 10,a
  (let wel: gedekte 10 van niet gespeelde kleur is meer waard dan vrije aas.)



Slag aan maat en maat gaat en slag blijft bij maat
==> als meerdere kleuren en hoogste van een kleur en maat heeft die kleur ook, dan seinen (lage kaart vd kleur)
==> als meerdere kleuren en hoogste van een kleur en maat heeft die kleur ook, dan seinen (hoogste kaart vd kleur, als ook een na hoogste en 'lange kaart', of meerdere hoogste kleuren)
==> als niks te seinen: kale of dubbel gedekte 10, tenzij 10 de hoogste is en kans op een slag halen met die 10 nog kan
                      : zoveel mogelijk punten
                      : toekomstig roem ontwijkend
                      :


IK KAN NIET BIJLOPEN EN HEB WEL TROEF
=====================================
als boer er nog in: 2 x de nel: gooi nel
                  : 3 x de nel: bewaar nel
(als maat gaat en hij heeft (waarschijnlijk) de boer), dan wel nel opgooien - en later met andere troef zo roemvol terugkomen.)


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


KLEUR BIJLOPEN
==============
kleur bijlopen, maar slag is (en blijft) aan tegenstander (bijv hoogste van kleur uitgekomen. kans op troeven door maat is nihil
  ==> gooi laagste van kleur op die huidige kans op roem zo klein mogelijk maakt
  ==> vbd a, h, b in trick, zelf: v,10 :: beide kaarten zijn na afloop van trick de hoogste. v veel roem, 10 geen roem, dus 10
  ==> vbd trick is 7,10  zelf: 9,b ==> dan b, want 9 heeft kans op 50 en b kans op max 20

kleur bijlopen, maar heb de hoogste van kleur en kleur nog niet gespeeld
  ==> kans op een tweede troefloze (door tegenstander) ronde aanwezig en 10 nog in spel, dan overweeg duiken
  ==> tenzij met duiken kans op roem naar tegenstander groot.
  ==> als 3 kaarten in hand en maat is gekomen, dan gooi aas
  ==> vbd trick 7,v   zelf:8,a  dan toch aas niet duiken, zelfs bij allereerste slag, vooral als maat is gekomen.

kleur bijlopen, maar heb de hoogste van kleur en kleur is wel al gespeeld
   ==> hoogste kans op roem doen, hoogste kaart, tenzij kans op troeven
   ==> als kan op nog een keer duiken zou lonen, dan nog een keer duiken
          voorwaarde: geen troeven meer.
          vrij zeker dat 10 bij voorloper is (niet in de achterhand)
              de rule hiervoor is: voorloper is eerder met deze kleur uitgekomen.

kleur bijlopen, laatste speler, heb 10, en aas er nog in
  ==> gooi 10

 eerste ronde van de kleur, en a, 10 nog niet gegooid en zelf geen A,10 en onduidelijk waar a is
 vbd trick is v :: zelf h,9,8 ==> speel op safe: ontwijk roem (dus 8), speel risky: op de roem (dus h)
                              ==> complexer: weet je dat je nat gaat,dan ontwijken, als enige kans op niet nat, dan doen
                              ==> ook: als maat gaan, voorzichtiger zijn.
 vbd trick is b :: 7,8        ==>  maakt niet veel uit. 8 geeft kleine kans op nu roem, maar toekomstige kans op roem is nul door eigen kaart
                                                        7 geeft geen kans op roem (door eigen toedoen), maar wel toekomstige kans op roem
                                                        ik zou nu kiezen voor de 8

 eerste ronde van de kleur, en a, 10 nog niet gegooid heb zelf geen A, maar wel 10 en onduidelijk waar a is
 zo laag mogelijk, ontwijk roem.
 vbd: trick is h, zelf 10,v ==> toch de v

 kleur bijlopen, slag aan maat, naast jouw kaarten alles van die kleur gespeeld:
 ==> gooi hoogste vd kleur, zoveel mogelijk roem

kleur bijlopen, slag aan maat, en blijft aan maat, eerste ronde van die kleur
 ==> gooi hoogste van de kleur, zeker als je daarna de hoogste nog over houdt
 ==> maak kans op roem in huidge trick zo grot mogelijk.
 vbd: trick: a,9    zelf 10,v,h ==> gooi 10 (en hoogste kaart en hoogste roem mogelijkheid)



 ALGEMEEN:
 hoogste roem mogelijkheid: 20 zeker is altijd hoger dan 50 misschien.
 overweeg duiken: Veel van de kleur nog in spel (bij tegenstanders).
                  Zelf niet heel veel van de kleur.
                  kans op pit niet meer aanwezig (na 4 rondjes?)

 */



class FollowPlayerInTrick(player: GeniusPlayerKlaverjassen, analysisResult: Brain): AbstractPlayerInTrick(player, analysisResult) {

    private val leadColor = brain.leadColor

    private val currentTrick = player.game.getCurrentRound().getTrickOnTable()

    private val legalCards = brain.myLegalCards
    private val legalCardsByColor = brain.legalCardsByColor
    private val partnerCardColors = brain.partnerCardColors

    private fun Card.isKaal() = legalCardsByColor[this.color]!!.size == 1
    private fun Card.isVrij() = brain.cardsInPlayOtherPlayers().none { it.color == this.color }
    private fun Card.isHighestInPlay() = brain.cardsInPlayOtherPlayers().none { it.color == this.color && it.beats(this, brain.trump)}

    //------------------------------------------------------------------------------------------------------------------

    private fun ikHebGeenLeadColorEnGeenTroef() = legalCards.none{it.color == leadColor} && legalCards.none{it.color == brain.trump }
    private fun ikHebGeenLeadColorMaarWelTroef() = legalCards.none{it.color == leadColor} && legalCards.any{it.color == brain.trump }
    private fun ikHebWelLeadColorEnDatIsTroef() = legalCards.any{it.color == leadColor} && leadColor == brain.trump
    private fun ikHebWelLeadColorEnDatIsGeenTroef() = legalCards.any{it.color == leadColor} && leadColor != brain.trump

    override fun chooseCard(): Card {
        return when {
            ikHebGeenLeadColorEnGeenTroef() -> rulesForIkHebGeenLeadColorEnGeenTroef()
            ikHebGeenLeadColorMaarWelTroef() -> rulesForIkHebGeenLeadColorMaarWelTroef()
            ikHebWelLeadColorEnDatIsTroef() -> rulesForIkHebWelLeadColorEnDatIsTroef()
            ikHebWelLeadColorEnDatIsGeenTroef() -> rulesForIkHebWelLeadColorEnDatIsGeenTroef()
            else -> playFallbackCard("Main level follow player in trick")
        }
    }

//    ======================================
//    IK KAN NIET BIJLOPEN EN HEB GEEN TROEF
//    ======================================

    /*
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

    private fun rulesForIkHebGeenLeadColorEnGeenTroef(): Card {
        if (currentTrick.getWinningSide()!!.isPartner()){
            if (partnerWillWinThisTrick()) {
                if (brain.theyOwnContract) {
//                    Slag aan maat (en andere partij gaat) en zeker weten dat slag aan maat blijft:
//                    ==> gooi kale kaart op, tenzij die de hoogste is en kans op slag halen met die kaart is nog aanwezig
//                    ==> of laag als je wilt seinen (maar dan moet troef op zijn (of kleiner dan 1)
//                    ==> gooi 10 op (als zelf geen aas en niet hoogste)
//                    ==> gooi aas op (als wel 10 en >= 3(?) van de kleur in hand) afhankelijk hoe ver in spel


                    //(1a)
                    val bareTenCardCandidates = legalCards.filter { it.isKaal() && it.isTen() && !it.isHighestInPlay() }
                    if (bareTenCardCandidates.isNotEmpty())
                        return bareTenCardCandidates.last()

                    //(3)
                    val tenCardCandidates = legalCards.filter { it.isTen() && !it.isHighestInPlay() }
                    if (tenCardCandidates.isNotEmpty())
                        return tenCardCandidates.maxByOrNull { legalCardsByColor[it.color]!!.size }!!

                    //(4a): note: aas opgooien is ook seinen
                    val aceCardColors = legalCards.filter { it.isAce() }.map { it.color }.toSet()
                    val aceCardCandidates = legalCards.filter { it.color in aceCardColors && !it.isAce() && it.isHighestInPlay() }
                    if (aceCardCandidates.isNotEmpty())
                        return aceCardCandidates.maxByOrNull { legalCardsByColor[it.color]!!.size }!!

                    //(4b)
                    val tenCardColors = legalCards.filter { it.color !in aceCardColors && it.isTen() && it.isHighestInPlay() }.map { it.color }.toSet()
                    val tenCardHighCandidates = legalCards.filter { it.color in tenCardColors && !it.isTen() && it.isHighestInPlay() }
                    if (tenCardHighCandidates.isNotEmpty())
                        return tenCardHighCandidates.maxByOrNull { legalCardsByColor[it.color]!!.size }!!

//                    //(2) todo: seinen!!
//                    val highCardColors = legalCards.filter { !it.isKaal() && it.isHighestInPlay() }.map { it.color }.toSet()
//                    val signalCandidates = legalCards.filter { it.color in highCardColors && it.isLowCard() }
//                    if (signalCandidates.isNotEmpty()) //sein kortste kaart (of beter langste kaart?)
//                        return signalCandidates.minByOrNull { legalCardsByColor[it.color]!!.size }!!

                    //(1b)
                    val bareCardsNotHighest = legalCards.filter { it.isKaal() && !it.isHighestInPlay() }
                    val bareCardCandidates = bareCardsNotHighest.sortedBy{ 10 * it.cardValue(brain.trump) + if (it.isVrij()) 0 else 1 }
                    if (bareCardCandidates.isNotEmpty())
                        return bareCardCandidates.last() //let op, je gooit evt nu een kale 7 weg

                    return playFallbackCard("Slag aan maat (en andere partij gaat) en zeker weten dat slag aan maat blijft:")

                } else {
//                    Slag aan maat (en maat gaat) en slag blijft bij maat
//                    ==> als meerdere kleuren en hoogste van een kleur en maat heeft die kleur ook, dan seinen (hoogste kaart vd kleur, als ook een na hoogste en 'lange kaart', of meerdere hoogste kleuren)
//                    ==> als meerdere kleuren en hoogste van een kleur en maat heeft die kleur ook, dan seinen (lage kaart vd kleur)
//                    ==> als niks te seinen:
//                         ==> kale 10, tenzij 10 de hoogste is en kans op een slag halen met die 10 nog kan
//                             of dubbel gedekte 10, tenzij 10 de hoogste is en kans op een slag halen met die 10 nog kan
//                         ==> zoveel mogelijk punten
//                         ==> toekomstig roem ontwijkend

                    val highestInPlayColors = legalCards.filter { it.isHighestInPlay() }.map { it.color }.toSet()
                    val seinCards = legalCards.filter { it.color in partnerCardColors && it.color in highestInPlayColors }

                    //(1a)
                    val aceCardColors = seinCards.filter { it.isAce()  }.map { it.color }.toSet()
                    val aceCardCandidates = seinCards.filter { it.color in aceCardColors && !it.isAce() && it.isHighestInPlay() }
                    if (aceCardCandidates.isNotEmpty())
                        return aceCardCandidates.maxByOrNull { legalCardsByColor[it.color]!!.size }!!

                    //(1b)
                    val tenCardColors = seinCards.filter { it.color !in aceCardColors && it.isTen() && it.isHighestInPlay() && it.color in partnerCardColors }.map { it.color }.toSet()
                    val tenCardHighCandidates = seinCards.filter { it.color in tenCardColors && !it.isTen() && it.isHighestInPlay() }
                    if (tenCardHighCandidates.isNotEmpty())
                        return tenCardHighCandidates.maxByOrNull { legalCardsByColor[it.color]!!.size }!!

                    //(2)
                    val highCardColors = seinCards.filter { !it.isKaal() && it.isHighestInPlay() }.map { it.color }.toSet()
                    val signalCandidates = seinCards.filter { it.color in highCardColors && it.isLowCard() }
                    if (signalCandidates.isNotEmpty()) //sein kortste kaart (of beter langste kaart?)
                        return signalCandidates.minByOrNull { legalCardsByColor[it.color]!!.size }!!

                    //(3a)
                    val bareTenCardCandidates = legalCards.filter { it.isKaal() && it.isTen() && !it.isHighestInPlay() }
                    if (bareTenCardCandidates.isNotEmpty())
                        return bareTenCardCandidates.last()

                    //(3b)
                    val coveredTenCardCandidates = legalCards.filter { !it.isKaal() && it.isTen() && !it.isHighestInPlay() && legalCardsByColor[it.color]!!.size >= 3 }
                    if (coveredTenCardCandidates.isNotEmpty())
                        return coveredTenCardCandidates.last()

                    return legalCards.filter{ !it.isAce() && !it.isTen() }.maxByOrNull { card ->
                        2 * card.cardValue(brain.trump) +
                                -1 * card.kaalMakendeKaartPenalty() +
                                 1 * roemSureThisTrickByCandidate(card) +
                                 1 * (if (roemPossibleThisTrickByCandidate(card) > 0) 10 else 0) +
                                -1 * (if (isRoemPossibleNextTrick(card)) 5 else 0)
                    } ?: playFallbackCard("Slag aan maat (en maat gaat) en slag blijft bij maat")
                }
            } else { //partner has contract, has currently winning card, but not sure that we keep it that way (dus ik ben 3e speler en next kan hogere hebben of gaat treoven)

//                Slag aan maat (en maat gaat) en niet zeker dat slag aan maat blijft
//                    ==> hou 10 gedekt
//                    ==> seinen?
//                    ==> hou aas vast
//                    ==> als kans groot dat slag naar tegenstander gaat, dan 9,8,7, b,v,h, 10,a
//                    ==> als kans groot dat slag bij maat blijft dan b,h,v, 9,8,7 , 10,a
//                    (let wel: gedekte 10 van niet gespeelde kleur is meer waard dan vrije aas.)

                val evaluationRankOrder = listOf(CardRank.NINE, CardRank.EIGHT, CardRank.SEVEN, CardRank.JACK, CardRank.QUEEN, CardRank.KING, CardRank.TEN, CardRank.ACE)
                return legalCards.minBy { card ->
                            2 * evaluationRankOrder.indexOf(card.rank)
                            1 * card.kaalMakendeKaartPenalty() +
                            1 * roemSureThisTrickByCandidate(card) +
                            1 * (if (isRoemPossibleNextTrick(card)) 5 else 0)
                }
            }
        } else {
            if (weCannotWinThisTrick()) {
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

                return legalCards.minBy { card ->
                    2 * card.cardValue(brain.trump) +
                            card.kaalMakendeKaartPenalty() +
                            roemSureThisTrickByCandidate(card) +
                            (if (roemPossibleThisTrickByCandidate(card) > 0) 10 else 0) +
                            (if (isRoemPossibleNextTrick(card)) 5 else 0)
                }

            } else {
//                Slag aan tegenstander - maar we kunnen deze slag nog winnen (i am 2,3 or 4)
                return playFallbackCard()
            }
        }
    }

    //    Slag aan tegenstander - kon geen onze slag worden (ik ben speler 3 of 4)
    private fun weCannotWinThisTrick() = currentTrick.getWinningSide()!!.isOtherParty() && (brain.iAmThirdPlayer || brain.iAmFourthPlayer)

    //    Slag aan maat en zeker weten dat slag aan maat blijft:
    private fun partnerWillWinThisTrick() =
        currentTrick.getWinningSide()!!.isPartner() &&
                when {
                    brain.iAmSecondPlayer ->
                        throw Exception("i am second player and partner has winning card is not possible")
                    brain.iAmThirdPlayer ->
                        brain.player1.legalCards
                            .none { it.beats(currentTrick.getWinningCard()!!, brain.trump) }
                    brain.iAmFourthPlayer ->
                        true
                    else ->
                        false
                }

    private fun Card.kaalMakendeKaartPenalty(): Int {
        val kaleKaart = (legalCards - this).singleOrNull { it.color == this.color }
        return if (kaleKaart != null) {
            if (!kaleKaart.isHighestInPlay()) {
                when (kaleKaart.rank) {
                    CardRank.TEN -> 2*10
                    else -> kaleKaart.cardValue(brain.trump)
                }
            } else {
                0
            }
        } else {
            0
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun rulesForIkHebGeenLeadColorMaarWelTroef(): Card {
        return playFallbackCard()
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun rulesForIkHebWelLeadColorEnDatIsTroef(): Card {
        return playFallbackCard()
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun rulesForIkHebWelLeadColorEnDatIsGeenTroef(): Card {
        return playFallbackCard()
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun playFallbackCard(info: String? = null): Card {
//        if (info != null)
//            println("FALL BACK NOTE: Fallback card info: $info")
        return legalCards.first()
    }

    //------------------------------------------------------------------------------------------------------------------

    //todo: als dubbele kans op roem, dan die anders beoordelen, dan enkele kans op roem
    //      bijv. 7,9 ==> dan is er met 8 kans op 20 roem
    //            8,9 ==> dan is er met 7 en 10 kans op 20 roem

    private fun roemSureThisTrickByCandidate(candidate: Card): Int {
        return (currentTrick.getCardsPlayed() + candidate).bonusValue(brain.trump)
    }

    private fun roemPossibleThisTrickByCandidate(candidate: Card): Int {

        val listOfTrickPossibilities = if (brain.iAmSecondPlayer) {
            val cardsPlayer1 = brain.player1.legalCards
            val cardsPlayer2 = brain.player2.legalCards
            if (cardsPlayer1.isNotEmpty() && cardsPlayer2.isNotEmpty()) {
                (cardsPlayer1 + cardsPlayer2).toList().mapCombinedItems { card1, card2 -> (currentTrick.getCardsPlayed() + candidate + card1 + card2) }
            } else {
                (cardsPlayer1 + cardsPlayer2).map { card1 -> (currentTrick.getCardsPlayed() + candidate + card1) }
            }
        } else if (brain.iAmThirdPlayer) {
            val cardsPlayer1 = brain.player1.legalCards
            (cardsPlayer1).map { card1 -> (currentTrick.getCardsPlayed() + candidate + card1) }
        } else { //iAmFourthPlayer
            listOf((currentTrick.getCardsPlayed() + candidate))
        }
        return listOfTrickPossibilities.maxOf { poss -> poss.bonusValue(brain.trump) }
    }

    private fun isRoemPossibleNextTrick(candidate: Card): Boolean {
        val p1 = brain.player1.allAssumeCards.filterTo(HashSet()) { it.color == candidate.color }
        val p2 = brain.player2.allAssumeCards.filterTo(HashSet()) { it.color == candidate.color }
        val p3 = brain.player3.allAssumeCards.filterTo(HashSet()) { it.color == candidate.color }
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

    private fun List<Card>.cardGivingBestValue(): CardValue {
        var best = Int.MIN_VALUE
        var bestCard: Card? = null
        this.forEach { card ->
            val v = card.trickValueAfterPlayed()
            if (v > best) {
                best = v
                bestCard = card
            }
        }
        return CardValue(bestCard!!, best)
    }

    private fun Card.trickValueAfterPlayed(): Int {
        currentTrick.addCard(this)
        val v = currentTrick.getScore().getDeltaForPlayer(player.tableSide)
        currentTrick.removeLastCard()
        return v
    }


    private val dummyCard = Card(CardColor.CLUBS, CardRank.THREE)
    private fun cardGivingBestValueByPlayingFullTrick(trick: Trick, sideToMove: TableSide): CardValue {
        if (trick.isComplete())
            return CardValue(dummyCard, trick.getScore().getDeltaForPlayer(player.tableSide))

        val checkCards = if (sideToMove == player.tableSide) {
            player.getLegalPlayableCards()
        }  else {
            (brain.player(sideToMove).allAssumeCards - trick.getCardsPlayed())
                .toList()
                .legalPlayable(trick, brain.trump)
        }

        if (sideToMove == player.tableSide || sideToMove.opposite() == player.tableSide) {
            var best = CardValue(dummyCard, Int.MIN_VALUE)
            checkCards.forEach { card ->
                trick.addCard(card)
                val cv = cardGivingBestValueByPlayingFullTrick(trick, sideToMove.clockwiseNext())
                trick.removeLastCard()
                if (cv.value > best.value)
                    best = CardValue(card, cv.value)
            }
            return best
        } else {
            var best = CardValue(dummyCard, Int.MAX_VALUE)
            checkCards.forEach { card ->
                trick.addCard(card)
                val cv = cardGivingBestValueByPlayingFullTrick(trick, sideToMove.clockwiseNext())
                trick.removeLastCard()
                if (cv.value < best.value)
                    best = CardValue(card, cv.value)
            }
            return best
        }
    }

}