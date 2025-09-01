package com.cards.player.ai

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import com.cards.game.klaverjassen.TableSide
import com.cards.game.klaverjassen.Trick
import com.cards.game.klaverjassen.legalPlayable
import com.cards.player.Player

abstract class AbstractPlayerInTrick(protected val player: Player,
                                     val analyzer: KlaverjassenAnalyzer) {
    abstract fun chooseCard(): Card

    protected val game = player.game

    protected fun canFollow() = player.getCardsInHand().any{game.getCurrentRound().getTrickOnTable().isLeadColor(it.color)}
    protected fun hasTroef() = hasColor(trump())
    protected fun mustTroeven() = !canFollow() && hasTroef()

    protected fun hasColor(cardColor: CardColor) = player.getCardsInHand().any{it.color == cardColor}
    protected fun hasCard(card: Card) = card in player.getCardsInHand()

    protected fun firstTrick() = game.getCurrentRound().getTrickList().size == 1
    protected fun leadColor() = game.getCurrentRound().getTrickOnTable().getLeadColor()
    protected fun CardColor.isTrump() = this == trump()
    protected fun Card.isTrump() = this.color.isTrump()

    protected fun isLeadPlayer() = game.getCurrentRound().getTrickOnTable().isSideToLead(player.tableSide)
    protected fun isContractOwner() = game.getCurrentRound().isContractOwningSide(player.tableSide)
    protected fun isContractOwnersPartner() = game.getCurrentRound().isContractOwningSide(player.tableSide.opposite())
    protected fun TableSide.isPartner() = this.opposite() == player.tableSide

    protected fun trump() = game.getCurrentRound().getTrumpColor()

    protected fun hasTrumpCard(rank: CardRank) = hasCard(Card(trump(), rank))
    protected fun hasTrumpJack() = hasCard(trumpJack())
    protected fun trumpJack() = Card(trump(), CardRank.JACK)
    protected fun trumpNine() = Card(trump(), CardRank.NINE)
    protected fun ace(color: CardColor) = Card.ace(color)
    protected fun ten(color: CardColor) = Card.ten(color)
    protected fun king(color: CardColor) = Card.ten(color)

    protected fun List<Card>.hasAce(color: CardColor? = null) = this.any{it.rank == CardRank.ACE && (if (color != null) it.color == color else true)}
    protected fun List<Card>.hasTen(color: CardColor? = null) = this.any{it.rank == CardRank.TEN && (if (color != null) it.color == color else true)}
    protected fun List<Card>.hasKing(color: CardColor? = null) = this.any{it.rank == CardRank.KING && (if (color != null) it.color == color else true)}

    //------------------------------------------------------------------------------------------------------------------

    private val dummyCard = Card(CardColor.CLUBS, CardRank.THREE)

    fun cardGivingHighestValue(trick: Trick, sideToMove: TableSide): CardValue {
        if (trick.isComplete())
            return CardValue(dummyCard, trick.getScore().getDeltaForPlayer(player.tableSide))

        val checkCards = if (sideToMove == player.tableSide) {
            player.getLegalPlayableCards()
        }  else {
            (analyzer.playerSureHasCards(sideToMove) + analyzer.playerCanHaveCards(sideToMove) - trick.getCardsPlayed())
            .toList()
            .legalPlayable(trick, trump())
        }

        if (sideToMove == player.tableSide || sideToMove.opposite() == player.tableSide) {
            var best = CardValue(dummyCard, Int.MIN_VALUE)
            checkCards.forEach { card ->
                trick.addCard(card)
                val cv = cardGivingHighestValue(trick, sideToMove.clockwiseNext())
                trick.removeLastCard()
                if (cv.value > best.value)
                    best = CardValue(card, cv.value)
            }
            return best
        } else {
            var best = CardValue(dummyCard, Int.MAX_VALUE)
            checkCards.forEach { card ->
                trick.addCard(card)
                val cv = cardGivingHighestValue(trick, sideToMove.clockwiseNext())
                trick.removeLastCard()
                if (cv.value < best.value)
                    best = CardValue(card, cv.value)
            }
            return best
        }
    }

}