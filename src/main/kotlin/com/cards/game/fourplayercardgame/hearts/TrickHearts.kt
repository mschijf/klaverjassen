package com.cards.game.fourplayercardgame.hearts

import com.cards.game.card.Card
import com.cards.game.fourplayercardgame.basic.TableSide
import com.cards.game.fourplayercardgame.basic.Trick

class TrickHearts(sideToLead: TableSide): Trick(sideToLead) {

    override fun getWinningSide(): TableSide? {
        return getSideThatPlayedCard(getWinningCard())
    }

    override fun getWinningCard(): Card? {
        return getCardsPlayed()
            .filter { card -> isLeadColor(card.color) }
            .maxByOrNull { card -> card.toRankNumber() }
    }

    fun getScore(): ScoreHearts {
        return if (!isComplete()) {
            ScoreHearts.ZERO
        } else {
            ScoreHearts.scoreForPlayer(getWinningSide()!!, getCardsPlayed().sumOf { card -> card.cardValue() })
        }
    }

}