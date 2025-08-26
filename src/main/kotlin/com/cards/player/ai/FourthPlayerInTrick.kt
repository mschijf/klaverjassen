package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.klaverjassen.Trick
import com.cards.game.klaverjassen.cardValue

class FourthPlayerInTrick(player: GeniusPlayerKlaverjassen): AbstractPlayerInTrick(player) {

    private val leadColor = leadColor()!!

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