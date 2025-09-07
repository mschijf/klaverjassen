package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import com.cards.game.klaverjassen.beats
import com.cards.game.klaverjassen.legalPlayable
import com.cards.player.Player

/*

KLEUR BIJLOPEN
==============
*** slag is (en blijft) aan tegenstander (bijv hoogste van kleur uitgekomen. kans op troeven door maat is nihil
      ==> gooi laagste van kleur op die huidige kans op roem zo klein mogelijk maakt
      ==> vbd a, h, b in trick, zelf: v,10 :: beide kaarten zijn na afloop van trick de hoogste. v veel roem, 10 geen roem, dus 10
      ==> vbd trick is 7,10  zelf: 9,b ==> dan b, want 9 heeft kans op 50 en b kans op max 20

*** heb de hoogste van kleur en kleur nog niet gespeeld
      ==> kans op een tweede troefloze (door tegenstander) ronde aanwezig en 10 nog in spel, dan overweeg duiken
      ==> tenzij met duiken kans op roem naar tegenstander groot.
      ==> als 3 kaarten in hand en maat is gekomen, dan gooi aas
      ==> vbd trick 7,v   zelf:8,a  dan toch aas niet duiken, zelfs bij allereerste slag, vooral als maat is gekomen.

*** heb de hoogste van kleur en kleur is wel al gespeeld
       ==> hoogste kans op roem doen, hoogste kaart, tenzij kans op troeven
       ==> als kan op nog een keer duiken zou lonen, dan nog een keer duiken
              voorwaarde: geen troeven meer.
              vrij zeker dat 10 bij voorloper is (niet in de achterhand)
                  de rule hiervoor is: voorloper is eerder met deze kleur uitgekomen.

*** laatste speler, heb 10, en aas er nog in
     ==> gooi 10 (beter: gooi kaart die hooste trickwaarde oplevert)

 *** eerste ronde van de kleur, en a, 10 nog niet gegooid en zelf geen A,10 en onduidelijk waar a is
     vbd trick is v :: zelf h,9,8 ==> speel op safe: ontwijk roem (dus 8), speel risky: op de roem (dus h)
                                  ==> complexer: weet je dat je nat gaat,dan ontwijken, als enige kans op niet nat, dan doen
                                  ==> ook: als maat gaan, voorzichtiger zijn.
     vbd trick is b :: 7,8        ==>  maakt niet veel uit. 8 geeft kleine kans op nu roem, maar toekomstige kans op roem is nul door eigen kaart
                                                            7 geeft geen kans op roem (door eigen toedoen), maar wel toekomstige kans op roem
                                                            ik zou nu kiezen voor de 8

 *** eerste ronde van de kleur, en a, 10 nog niet gegooid heb zelf geen A, maar wel 10 en onduidelijk waar a is
     zo laag mogelijk, ontwijk roem.
     vbd: trick is h, zelf 10,v ==> toch de v

*** slag aan maat, naast jouw kaarten alles van die kleur gespeeld:
     ==> gooi hoogste vd kleur, zoveel mogelijk roem

*** slag aan maat, en blijft aan maat, eerste ronde van die kleur
    ==> gooi hoogste van de kleur, zeker als je daarna de hoogste nog over houdt
    ==> maak kans op roem in huidge trick zo groot mogelijk.
        vbd: trick: a,9    zelf 10,v,h ==> gooi 10 (en hoogste kaart en hoogste roem mogelijkheid)



 ALGEMEEN:
 hoogste roem mogelijkheid: 20 zeker is altijd hoger dan 50 misschien.
 overweeg duiken: Veel van de kleur nog in spel (bij tegenstanders).
                  Zelf niet heel veel van de kleur.
                  kans op pit niet meer aanwezig (na 4 rondjes?)

 */

class IDoHaveLeadColorAndLeadColorIsNotTrumpRule(player: Player): AbstractChooseCardFollowerRule(player) {

    private val leadColorAce = Card(leadColor, CardRank.ACE)
    private val leadColorTen = Card(leadColor, CardRank.TEN)

    private fun Collection<Card>.hasAce() = this.contains(leadColorAce)
    private fun Collection<Card>.hasTen() = this.contains(leadColorTen)

    //------------------------------------------------------------------------------------------------------------------

    override fun chooseCard(): Card {
        if (iAmFourthPlayer) {
            return playFourthPLayer()
        }

        if (weCannotWinThisTrick())
            return playWeCannotWinThisTrick()

        if (leadColor.playedForFirstTime()) {
            if (leadColor.iHaveHighest())
                return playColorForFirstTimeAndIHaveHighest()

            if (winningSide.isPartner() && winningCard.isHigherThanAllInPlayIncludingMine())
                playColorForFirstTimeAndPartnerHasHighestAndIHaveNotHigher()

            if (winningCard != leadColor.highestInPlayOrOnTable())
                return playColorForFirstTimeAndIDoNotHaveHighestAndIHighestStillInPlay()
        }

        if (leadColor.playedForSecondTime())
            if (leadColor.iHaveHighest())
                return playColorSecondTimeAndIHaveHighest()

        if (winningSide.isPartner() && myLegalCards.all{it.isVrij()})
            playNoMoreOfColorAndPartnerIsWinning()

        //todo: if weWillWin --> throw highestValue
        //      if we can loose --> throw lowest
        return playFallbackCard()
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun playWeCannotWinThisTrick(): Card {
//            ==> gooi laagste van kleur op die huidige kans op roem zo klein mogelijk maakt
//            ==> vbd a, h, b in trick, zelf: v,10 :: beide kaarten zijn na afloop van trick de hoogste. v veel roem, 10 geen roem, dus 10
//            ==> vbd trick is 7,10  zelf: 9,b ==> dan b, want 9 heeft kans op 50 en b kans op max 20
        return myLegalCards.minBy { card ->
            2 * card.cardValue() +
                    roemSureThisTrickByCandidate(card) +
                    roemPossibleThisTrickByCandidate(card)/2 +
                    (if (isRoemPossibleNextTrick(card)) 5 else 0)
        }
    }

    //todo: check this routine:: combinatie van weCanLoose en roemSure kan verkeerd uitvallen
    private fun playColorForFirstTimeAndIHaveHighest(): Card {
//    ==> kans op een tweede troefloze (door tegenstander) ronde aanwezig en 10 nog in spel, dan overweeg duiken
//                         tenzij met duiken kans op roem naar tegenstander groot.
//    ==> als 3 kaarten in hand en maat is gekomen, dan gooi aas
//    ==> vbd trick 7,v   zelf:8,a  dan toch aas niet duiken, zelfs bij allereerste slag, vooral als maat is gekomen.

        val myHighest = leadColor.myHighest()!!
        val duikenOptie = leadColor.playedForFirstTime()
                && myLegalCards.hasAce()
                && (player1.allAssumeCards.hasTen() || player3.allAssumeCards.hasTen())

        if (currentTrick.getSideToLead().isPartner() ) {
            if ((myLegalCards - myHighest).none { weCanLooseThisTrickIfIPlayCard(it) }) {
                val best = myLegalCards.maxBy { roemSureThisTrickByCandidate(it) }
                if (roemSureThisTrickByCandidate(best) > 0)
                    return best
                val best2 = myLegalCards.maxBy { roemPossibleThisTrickByCandidate(it) }
                if (roemSureThisTrickByCandidate(best2) > 0)
                    return best2
            }
            if (myLegalCards.size >= 3)
                return myHighest
            if (leadColor.playedBefore())
                return myHighest
            return (myLegalCards - myHighest).first()
        }

        if (myLegalCards.size == 2) {
            val otherCard = (myLegalCards - myHighest).first()
            if (otherCard.isKing() && duikenOptie)
                return otherCard
            if (roemPossibleThisTrickByCandidate(otherCard) > 0 && weCanLooseThisTrickIfIPlayCard(otherCard))
                return myHighest
            if (duikenOptie)
                return otherCard
            return myHighest
        }

        if (myLegalCards.size == 3) {
            val otherCard = (myLegalCards - myHighest).maxBy { it.cardValue() }
            if (otherCard.isKing() && duikenOptie)
                return otherCard
            return myHighest
        }

        if (myLegalCards.size >= 4)
            return myHighest

        return playFallbackCard()
    }

    //todo: meer kijken naar de mogelijkheden met wel of niet roem ontwijken
    //      H of B bij V leggen is meer kans op roem dan 9 bij V leggen
    private fun playColorForFirstTimeAndIDoNotHaveHighestAndIHighestStillInPlay(): Card {
//    eerste ronde van de kleur, en a, 10 nog niet gegooid en zelf geen A,10 en onduidelijk waar a is
//       vbd trick is v :: zelf h,9,8 ==> speel op safe: ontwijk roem (dus 8), speel risky: op de roem (dus h)
//       ==> complexer: weet je dat je nat gaat,dan ontwijken, als enige kans op niet nat, dan doen
//       ==> ook: als maat gaan, voorzichtiger zijn.
//       vbd trick is b :: 7,8        ==>  maakt niet veel uit. 8 geeft kleine kans op nu roem, maar toekomstige kans op roem is nul door eigen kaart
//       7 geeft geen kans op roem (door eigen toedoen), maar wel toekomstige kans op roem
//       ik zou nu kiezen voor de 8
//
//    eerste ronde van de kleur, en a, 10 nog niet gegooid heb zelf geen A, maar wel 10 en onduidelijk waar a is
//    zo laag mogelijk, ontwijk roem.
//    vbd: trick is h, zelf 10,v ==> toch de v

        val playSafe = true

        if (!leadColor.highestInPlayOrOnTable()!!.isAce()) {
            return myLegalCards.minBy { card ->
                2 * card.cardValue() +
                        roemSureThisTrickByCandidate(card) +
                        roemPossibleThisTrickByCandidate(card)/2 +
                        (if (isRoemPossibleNextTrick(card)) 5 else 0)
            }
        }

        if (myLegalCards.hasTen()) {
            return myLegalCards.minBy { card ->
                2 * card.cardValue() +
                        roemSureThisTrickByCandidate(card) +
                        roemPossibleThisTrickByCandidate(card)/2 +
                        (if (isRoemPossibleNextTrick(card)) 5 else 0)
            }
        }

        return if (playSafe) {
            myLegalCards.minBy { card ->
                2 * card.cardValue() +
                        roemSureThisTrickByCandidate(card) +
                        roemPossibleThisTrickByCandidate(card)/2 +
                        (if (isRoemPossibleNextTrick(card)) 5 else 0)
            }
        } else {
            myLegalCards.maxBy { card ->
                2 * card.cardValue() +
                        roemSureThisTrickByCandidate(card) +
                        roemPossibleThisTrickByCandidate(card)/2 +
                        (if (isRoemPossibleNextTrick(card)) 5 else 0)
            }
        }
    }


    private fun playColorSecondTimeAndIHaveHighest(): Card {
//        kleur bijlopen, maar heb de hoogste van kleur en kleur is wel al gespeeld
//        ==> hoogste kans op roem doen, hoogste kaart, tenzij kans op troeven
//        ==> als kans op nog een keer duiken zou lonen, dan nog een keer duiken
//            voorwaarde: geen troeven meer.
//                        vrij zeker dat 10 bij voorloper is (niet in de achterhand)
//                                       de rule hiervoor is: voorloper is eerder met deze kleur uitgekomen.

        val myHighest = leadColor.myHighest()!!

        if (iAmSecondPlayer || iAmThirdPlayer ) {
            if (player1.hasColorProbabilityPercentage(leadColor) > 0.49) { //kans dat volgende speler meeloopt is groot
                var highestRoemCandidate: Card? = null
                var mostRoem = 0

                myLegalCards.forEach { candidate ->
                    val roemPossible = roemPossibleThisTrickByCandidate(candidate)
                    if (roemPossible > mostRoem && !weCanLooseThisTrickIfIPlayCard(candidate)) {
                        highestRoemCandidate = candidate
                        mostRoem = roemPossible
                    }
                }
                if (highestRoemCandidate != null)
                    return highestRoemCandidate!!

                //todo: overweeg nog een keer duiken (alleen als secondPLayer)
                return myHighest
            } else if (player1.hasColorProbabilityPercentage(trump) > 0.32) { //kans op introven te groot
                return myLegalCards.minBy { card ->
                    2 * card.cardValue() + roemPossibleThisTrickByCandidate(card)/2
                }
            } else { //kans  op introeven klein
                //todo: overweeg nog een keer duiken (alleen als secondPLayer)
                return myLegalCards.maxBy { card ->
                    2 * card.cardValue() + roemPossibleThisTrickByCandidate(card)/2
                }
            }
        }

        return myLegalCards.maxBy { card ->
            2 * card.cardValue() + roemSureThisTrickByCandidate(card)
        }
    }

    private fun playFourthPLayer(): Card {
        return myLegalCards.maxBy { card ->
            if (card.weWinThisTrickIfIPlayCardAsFourthPlayer())
                2 * card.cardValue() + roemSureThisTrickByCandidate(card)
            else
                -1 * (2 * card.cardValue() + roemSureThisTrickByCandidate(card))

        }
    }

    private fun playColorForFirstTimeAndPartnerHasHighestAndIHaveNotHigher(): Card {
//        slag aan maat, en blijft aan maat, eerste ronde van die kleur
//            ==> gooi hoogste van de kleur, zeker als je daarna de hoogste nog over houdt
//            ==> maak kans op roem in huidige trick zo groot mogelijk.
//            vbd: trick: a,9    zelf 10,v,h ==> gooi 10 (en hoogste kaart en hoogste roem mogelijkheid)

        return myLegalCards.maxBy { card ->
            2 * card.cardValue() +
                    roemSureThisTrickByCandidate(card) +
                    roemPossibleThisTrickByCandidate(card)/2 +
                    (if (isRoemPossibleNextTrick(card)) 5 else 0)
        }
    }

    private fun playNoMoreOfColorAndPartnerIsWinning(): Card {
//        slag aan maat, naast jouw kaarten alles van die kleur gespeeld:
//        ==> gooi hoogste vd kleur, zoveel mogelijk roem
//        (maar check of er nog troef kan komen)

        if (iAmFourthPlayer)
            return myLegalCards.maxBy { card ->
                2 * card.cardValue() +
                        roemSureThisTrickByCandidate(card) +
                        roemPossibleThisTrickByCandidate(card)/2 +
                        (if (isRoemPossibleNextTrick(card)) 5 else 0)
            }

        if (player1.allAssumeCards.any { it.color == trump })
            return myLegalCards.minBy { card ->
                2 * card.cardValue() +
                        roemSureThisTrickByCandidate(card) +
                        roemPossibleThisTrickByCandidate(card)/2 +
                        (if (isRoemPossibleNextTrick(card)) 5 else 0)
            }

        return myLegalCards.maxBy { card ->
            2 * card.cardValue() +
                    roemSureThisTrickByCandidate(card) +
                    roemPossibleThisTrickByCandidate(card)/2 +
                    (if (isRoemPossibleNextTrick(card)) 5 else 0)
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    private fun weCannotWinThisTrick(): Boolean {
        if (!winningSide.isOtherParty())
            return false

        return if (iAmThirdPlayer || iAmFourthPlayer) {
            myLegalCards.none { it.beats(winningCard, trump) }
        } else {
            player2.legalCards.none { it.beats(winningCard, trump) }
        }
    }

    private fun OtherPlayer.hasColorProbabilityPercentage(color: CardColor): Double {
        if (this.sureHas.count{it.color == color} > 0)
            return 100.0
        if (this.canHave.count{it.color == color} == 0)
            return 0.0
        val cardCountInPlayAtOthers = 8 - color.colorPlayedCount() + myLegalCards.size
        val otherPlayersCanHaveThisColor = listOf(player1, player2, player3).count { pl ->
            pl.allAssumeCards.count {it.color == color} > 0 }
        return cardCountInPlayAtOthers.toDouble() / otherPlayersCanHaveThisColor
    }


    private fun weCanLooseThisTrickIfIPlayCard(card: Card): Boolean {
        when {
            iAmSecondPlayer -> {
                if ( (player1.allAssumeCards.count{it.color == currentTrick.getLeadColor()} == 0))
                    return (player1.allAssumeCards.count{it.color == trump} > 0)

                if (player1.allAssumeCards.legalPlayable(currentTrick, trump).any { it.beats(card, trump) })
                    return false

                return true
            }
            iAmThirdPlayer ->
                if (winningSide.isOtherParty()) {
                    if (card.beats(winningCard, trump)) {
                        if (player3.allAssumeCards.legalPlayable(currentTrick, trump).none { it.beats(card, trump) })
                            return false
                    }
                    return true
                } else {
                    val highestTrickCard = if (card.beats(winningCard, trump)) card else winningCard
                    if (player3.allAssumeCards.legalPlayable(currentTrick, trump).none { it.beats(highestTrickCard, trump) })
                        return false
                    return true
                }

            iAmFourthPlayer ->
                return winningSide.isOtherParty() &&
                        !card.beats(winningCard, trump)
            else ->
                throw Exception("Not Possible")
        }
    }

    private fun Card.weWinThisTrickIfIPlayCardAsFourthPlayer(): Boolean {
        if (winningSide.isPartner())
            return true
        if (winningCard.isTrump())
            return false
        return currentTrick.getCardsPlayed().none{it.beats(this, trump)}
    }

}