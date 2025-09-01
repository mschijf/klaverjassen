package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.klaverjassen.Trick
import com.cards.game.klaverjassen.bonusValue
import com.cards.game.klaverjassen.cardValue
import tool.mylambdas.collectioncombination.mapCombinedItems
import kotlin.math.absoluteValue

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



class FollowPlayerInTrick(player: GeniusPlayerKlaverjassen, analyzer: KlaverjassenAnalyzer): AbstractPlayerInTrick(player, analyzer) {

    private val leadColor = leadColor()!!

    private val player1 = player.tableSide.clockwiseNext(1)
    private val player2 = player.tableSide.clockwiseNext(2)
    private val player3 = player.tableSide.clockwiseNext(3)

    private val iAmSecondPlayer = player.game.getCurrentRound().getTrickOnTable().getCardsPlayed().size == 1
    private val iAmThirdPlayer = player.game.getCurrentRound().getTrickOnTable().getCardsPlayed().size == 2
    private val iAmFourthPlayer = player.game.getCurrentRound().getTrickOnTable().getCardsPlayed().size == 3

    private val trick = game.getCurrentRound().getTrickOnTable()
    private val legalCards = player.getLegalPlayableCards()

    override fun chooseCard(): Card {
        if (canFollow() && !leadColor.isTrump()) {
            if (!trick.getWinningCard()!!.color.isTrump()) { //no trump in trick
                return when {
                    legalCards.hasAce() -> canFollowAceHighNoTrumpInTrick()
                    legalCards.hasTen() -> canFollowTenHighNoTrumpInTrick()
                    else -> canFollowOtherNoTrumpInTrick()
                }
            } else { //er is ingetroefd
                return canFollowButTrumpInTrick()
            }
        } else {
            //trump is lead color
        }

        return legalCards.first()
    }




    //------------------------------------------------------------------------------------------------------------------

    private fun canFollowAceHighNoTrumpInTrick(): Card {
        val ace = ace(leadColor)
        val plentyForNextRound = analyzer.cardsInPlayOtherPlayers().count{ it.color == leadColor } >= 4

        val (candidateCard, candidateValue) = (legalCards - ace(leadColor)).cardGivingHighestValue(trick)
        val valueAce = ace.trickValueAfterPlayed(trick)

        return if (valueAce - candidateValue > ace.cardValue(trump()) - candidateCard.cardValue(trump()) )
            ace
        else if (plentyForNextRound)
            candidateCard
        else
            ace
    }

    private fun canFollowTenHighNoTrumpInTrick(): Card {

        val ten = ten(leadColor)
        if (trick.getWinningCard()!!.isAce()) {
            if (player.tableSide.opposite() == trick.getWinningSide()) {
                val (candidateCard, candidateValue) = (legalCards - ten(leadColor)).cardGivingHighestValue(trick)
                val valueTen = ten.trickValueAfterPlayed(trick)
                return if (valueTen - candidateValue > ten.cardValue(trump()) - candidateCard.cardValue(trump()) )
                    ten
                else
                    candidateCard
            } else {
                return legalCards.cardGivingHighestValue(trick).card
            }
        } else {
            return legalCards.cardGivingHighestValue(trick).card
        }
    }

    private fun canFollowOtherNoTrumpInTrick(): Card {
        return legalCards.cardGivingHighestValue(trick).card
    }

    private fun canFollowButTrumpInTrick(): Card {
        if (player.tableSide.opposite() != trick.getWinningSide())
            return legalCards.cardGivingHighestValue(trick).card

        if (legalCards.hasAce()) {
            val plentyForNextRound = analyzer.cardsInPlayOtherPlayers().count{ it.color == leadColor } >= 4
            val noMoreTrumpInOtherHands = analyzer.cardsInPlayOtherPlayers().count{ it.color == trump() } == 0
            val noMoreOfColorInOtherHands = analyzer.cardsInPlayOtherPlayers().count{ it.color == leadColor } == 0
            return when {
                noMoreOfColorInOtherHands -> legalCards.cardGivingHighestValue(trick).card
                noMoreTrumpInOtherHands -> (legalCards - ace(leadColor)).cardGivingHighestValue(trick).card
                plentyForNextRound  -> (legalCards - ace(leadColor)).cardGivingHighestValue(trick).card
                else -> legalCards.cardGivingHighestValue(trick).card
            }
        } else if (legalCards.hasTen()) {
            return legalCards.cardGivingHighestValue(trick).card
        } else {
            return legalCards.cardGivingHighestValue(trick).card
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun roemSureThisTrickByCandidate(trick: Trick, candidate: Card): Int {
        return (trick.getCardsPlayed() + candidate).bonusValue(trump())
    }

    private fun roemPossibleThisTrickByCandidate(trick: Trick, candidate: Card): Int {

        val listOfTrickPossibilities = if (iAmSecondPlayer) {
            val cardsPlayer1 = (analyzer.playerSureHasCards(player1) + analyzer.playerCanHaveCards(player1)).filter { it.color == leadColor }
            val cardsPlayer2 = (analyzer.playerSureHasCards(player2) + analyzer.playerCanHaveCards(player2)).filter { it.color == leadColor }
            if (cardsPlayer1.isNotEmpty() && cardsPlayer2.isNotEmpty()) {
                (cardsPlayer1 + cardsPlayer2).mapCombinedItems { card1, card2 -> (trick.getCardsPlayed() + candidate + card1 + card2) }
            } else {
                (cardsPlayer1 + cardsPlayer2).map { card1 -> (trick.getCardsPlayed() + candidate + card1) }
            }
        } else if (iAmThirdPlayer) {
            val cardsPlayer1 = (analyzer.playerSureHasCards(player1) + analyzer.playerCanHaveCards(player1)).filter { it.color == leadColor }
            (cardsPlayer1).map { card1 -> (trick.getCardsPlayed() + candidate + card1) }
        } else { //fourthPlayer
            listOf((trick.getCardsPlayed() + candidate))
        }
        return listOfTrickPossibilities.maxOf { poss -> poss.bonusValue(trump()) }
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun List<Card>.cardGivingHighestValue(trick: Trick): CardValue {
        var best = Int.MIN_VALUE
        var bestCard: Card? = null
        this.forEach { card ->
            val v = card.trickValueAfterPlayed(trick)
            if (v > best) {
                best = v
                bestCard = card
            }
        }
        return CardValue(bestCard!!, best)
    }

    private fun Card.trickValueAfterPlayed(trick: Trick): Int {
        trick.addCard(this)
        val v = trick.getScore().getDeltaForPlayer(player.tableSide)
        trick.removeLastCard()
        return v
    }


}