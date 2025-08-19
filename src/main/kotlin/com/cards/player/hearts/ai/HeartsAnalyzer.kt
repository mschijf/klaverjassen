package com.cards.player.hearts.ai

import com.cards.game.card.CARDDECK
import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.fourplayercardgame.hearts.toRankNumber

class HeartsAnalyzer(
    private val cardsInHand : List<Card>,
    private val cardsPlayed: List<Card>,
    private val cardsStillInPlay: List<Card>) {

    val metaCardList = cardsInHand.map { card -> MetaCardInfo(card, 0) }

    private fun higher (card1: Card, card2: Card) = card1.toRankNumber() > card2.toRankNumber()
    private fun lower (card1: Card, card2: Card) = card1.toRankNumber() < card2.toRankNumber()

    fun getCardAnalysisValue(card: Card): Int? = metaCardList.firstOrNull { metacard -> metacard.card == card }?.value
    
    constructor(cardsInHand: List<Card>):this(cardsInHand, emptyList<Card>(), emptyList<Card>())

    class MetaCardInfo(val card: Card, var value: Int)

    fun evaluateSpecificCard(card: Card, value: Int): HeartsAnalyzer {
        metaCardList
            .filter { metaCardInfo -> metaCardInfo.card == card }
            .forEach { metaCardInfo -> metaCardInfo.value += value }
        return this
    }

    fun evaluateSpecificColor(cardColor: CardColor, value: Int): HeartsAnalyzer {
        metaCardList
            .filter { metaCardInfo -> metaCardInfo.card.color == cardColor }
            .forEach { metaCardInfo -> metaCardInfo.value += value }
        return this
    }

    fun evaluateHighestCardsInColor(value: Int): HeartsAnalyzer {
        metaCardList
            .filter { metaCardInfo -> isHighestCardOfColor(metaCardInfo.card) }
            .forEach { metaCardInfo -> metaCardInfo.value += value }
        return this
    }

    fun evaluateByRank(rankStepValue: Int): HeartsAnalyzer {
        metaCardList
            .forEach { metaCardInfo -> metaCardInfo.value += rankStepValue * metaCardInfo.card.toRankNumber() }
        return this
    }
    

    fun evaluateByRankLowerThanOtherCard(otherCard: Card, baseValue: Int, rankStepValue: Int): HeartsAnalyzer {
        metaCardList
            .filter { metaCardInfo -> metaCardInfo.card.color == otherCard.color }
            .filter { metaCardInfo -> metaCardInfo.card.toRankNumber() < otherCard.toRankNumber() }
            .sortedBy { mc -> mc.card.toRankNumber() }
            .forEachIndexed { index, metaCardInfo -> metaCardInfo.value += baseValue + (index+1) * rankStepValue }
        return this
    }

    fun evaluateByRankHigherThanOtherCard(otherCard: Card, baseValue: Int, rankStepValue: Int): HeartsAnalyzer {
        metaCardList
            .filter { metaCardInfo -> metaCardInfo.card.color == otherCard.color }
            .filter { metaCardInfo -> metaCardInfo.card.toRankNumber() > otherCard.toRankNumber() }
            .sortedBy { mc -> mc.card.toRankNumber() }
            .forEachIndexed { index, metaCardInfo -> metaCardInfo.value += baseValue + (index+1) * rankStepValue }
        return this
    }

    fun evaluateSpecificCardLowerThanOtherCard(card: Card, value: Int, otherCard: Card): HeartsAnalyzer {
        metaCardList
            .filter { metaCardInfo -> metaCardInfo.card == card }
            .filter { metaCardInfo ->
                metaCardInfo.card.toRankNumber() < otherCard.toRankNumber()
            }
            .forEach { metaCardInfo -> metaCardInfo.value += value }
        return this
    }

    fun evaluateSingleCardOfColor(value: Int, higherThanAvailableCard: Card): HeartsAnalyzer {
        val color = higherThanAvailableCard.color
        if (cardsInHand.count { c -> c.color == color } == 1) {
            val single = cardsInHand.first { c -> c.color == color }
            if (cardsStillInPlay.contains(higherThanAvailableCard) ){
                if (single.toRankNumber() > higherThanAvailableCard.toRankNumber()) {
                    metaCardList
                        .filter { metaCardInfo -> metaCardInfo.card.color == color }
                        .forEach { metaCardInfo -> metaCardInfo.value += value }
                }
            }
        }
        return this
    }

    fun evaluateSingleCardOfColor(value: Int, color: CardColor): HeartsAnalyzer {
        if (cardsInHand.count { c -> c.color == color } == 1) {
            val single = cardsInHand.first { c -> c.color == color }

            val countHigher = cardsStillInPlay
                .filter { c -> c.color == color }
                .count { c -> c.toRankNumber() > single.toRankNumber() }
            val countLower = cardsStillInPlay
                .filter { c -> c.color == color }
                .count { c -> c.toRankNumber() < single.toRankNumber() }
            if (countHigher <= 3 && countLower >= 1) {
                metaCardList
                    .filter { metaCardInfo -> metaCardInfo.card.color == color }
                    .forEach { metaCardInfo -> metaCardInfo.value += value }
            }
        }
        return this
    }

    fun evaluateFreeCards(value: Int): HeartsAnalyzer {
        CardColor.values().forEach { color ->
            if (cardsStillInPlay.none { c -> c.color == color }) {
                metaCardList
                    .filter { mc -> mc.card.color == color }
                    .forEach { mc -> mc.value += value }
            }
        }
        return this
    }

    //------------------------------------------------------------------------------------------------------------------

    fun evaluateLeadPlayerByColor(color: CardColor): HeartsAnalyzer {
        val cardsOfColorInPlay = cardsStillInPlay.count { it.color == color }
        val cardsOfColorInHand = cardsInHand.count { it.color == color }

        if (cardsOfColorInHand == 0)
            return this

        if (cardsOfColorInPlay == 0) {
            metaCardList
                .filter { mc -> mc.card.color == color }
                .forEach { mc -> mc.value += -100}
            return this
        }

        if (cardsOfColorInHand == 1) {
            //todo: check queen of spades, jack of clubs?
            val onlyCard = cardsInHand.first { it.color == color }
            val cardsInPlayHigher = cardsStillInPlay.filter { it.color == color }.count{ higher(it, onlyCard)}
            val cardsInPlayLower = cardsStillInPlay.filter { it.color == color }.count{ lower(it, onlyCard)}

            if (cardsInPlayLower == 0) {
                metaCardList
                    .filter { mc -> mc.card == onlyCard }
                    .forEach { mc -> mc.value += 100 }
            }else if (cardsInPlayHigher == 0) {
                metaCardList
                    .filter { mc -> mc.card == onlyCard }
                    .forEach { mc -> mc.value += -100}
            } else if (cardsInPlayLower >= 3) {
                metaCardList
                    .filter { mc -> mc.card == onlyCard }
                    .forEach { mc -> mc.value += -20*(cardsInPlayLower - cardsInPlayHigher) }
            } else {
                metaCardList
                    .filter { mc -> mc.card == onlyCard }
                    .forEach { mc -> mc.value += 20*(cardsInPlayHigher-cardsInPlayLower) }
            }
            return this
        }

        if (cardsOfColorInHand == 2) {
            //todo: better evaluating
            //todo: check queen of spades, jack of clubs
            val lowestCardInHand = cardsInHand.filter { it.color == color }.minByOrNull{it.toRankNumber()}!!
            val cardsInPlayHigherLowest = cardsInHand.filter { it.color == color }.count{ higher(it, lowestCardInHand)}
            val cardsInPlayLowerLowest = cardsInHand.filter { it.color == color }.count{ lower(it, lowestCardInHand)}

            metaCardList
                .filter { it.card.color == color }
                .sortedByDescending { it.card.toRankNumber() }
                .forEachIndexed { index, it -> it.value += 20 + (index+1) }

            if (cardsInPlayLowerLowest <= 2 && cardsInPlayHigherLowest >= 1) {
                metaCardList
                    .filter { it.card.color == color }
                    .minByOrNull { it.card.toRankNumber() }!!
                    .value += cardsInPlayHigherLowest * 20
            }
            return this
        }

        if (cardsOfColorInHand == 3) {
            //todo: better evaluating
            //todo: check queen of spades, jack of clubs

            val lowestCardInHand = cardsInHand.filter { it.color == color }.minByOrNull{it.toRankNumber()}!!
            val cardsInPlayHigherLowest = cardsInHand.filter { it.color == color }.count{ higher(it, lowestCardInHand)}
            val cardsInPlayLowerLowest = cardsInHand.filter { it.color == color }.count{ lower(it, lowestCardInHand)}

            metaCardList
                .filter { it.card.color == color }
                .sortedByDescending { it.card.toRankNumber() }
                .forEachIndexed { index, it -> it.value += 30 + (index+1) }

            if (cardsInPlayLowerLowest <= 2 && cardsInPlayHigherLowest >= 1) {
                metaCardList
                    .filter { it.card.color == color }
                    .minByOrNull { it.card.toRankNumber() }!!
                    .value += cardsInPlayHigherLowest * 20
            }
            return this
        }

        if (cardsOfColorInHand >= 4) {
            val lowestCardInHand = cardsInHand.filter { it.color == color }.minByOrNull{it.toRankNumber()}!!
            val cardsInPlayHigherLowest = cardsInHand.filter { it.color == color }.count{ higher(it, lowestCardInHand)}
            val cardsInPlayLowerLowest = cardsInHand.filter { it.color == color }.count{ lower(it, lowestCardInHand)}

            metaCardList
                .filter { it.card.color == color }
                .sortedByDescending { it.card.toRankNumber() }
                .forEachIndexed { index, it -> it.value += 50 + (index+1) }

            if (cardsInPlayLowerLowest <= 2 && cardsInPlayHigherLowest >= 1) {
                metaCardList
                    .filter { it.card.color == color }
                    .minByOrNull { it.card.toRankNumber() }!!
                    .value += cardsInPlayHigherLowest * 20
            }
            return this
        }

        return this
    }

    //------------------------------------------------------------------------------------------------------------------

    private fun higherCardsThen(card: Card): List<Card> {
        return CARDDECK.baseDeckCardsSevenAndHigher
            .filter { crd -> crd.color  ==  card.color }
            .filter { crd -> crd.toRankNumber() > card.toRankNumber() }
    }

    private fun isHighestCardOfColor(card: Card): Boolean {
        val nCardsHigherPlayed = (cardsInHand union cardsPlayed)
            .filter { cardPlayed -> cardPlayed.color == card.color }
            .count { cardPlayed -> cardPlayed.toRankNumber() > card.toRankNumber() }
        val nCardsHigher = higherCardsThen(card).count()

        return nCardsHigher == nCardsHigherPlayed
    }

    fun hasOnlyLowerCardsThanLeader(winningCard: Card): Boolean {
        return cardsInHand
            .filter{crd -> crd.color == winningCard.color}
            .all { crd -> crd.toRankNumber() < winningCard.toRankNumber() }
    }
    fun hasOnlyHigherCardsThanLeader(winningCard: Card): Boolean {
        return cardsInHand
            .filter{crd -> crd.color == winningCard.color}
            .all { crd -> crd.toRankNumber() > winningCard.toRankNumber() }
    }
    fun hasAllCardsOfColor(color: CardColor): Boolean {
        return (cardsInHand.count { cp -> cp.color == color } + cardsPlayed.count { cp -> cp.color == color } == 8)
    }
    fun canStopBeingSideToStart(leadColor: CardColor): Boolean {
        return hasLowestCardOfColorInHandAndHigherInPLayExists(leadColor)
        //todo: mag ook een een-na-laagste kaart zijn, mits er nog steeds een hoogste is
        // en kleuren verdeeld over verschillende spelers
    }

    private fun hasLowestCardOfColorInHandAndHigherInPLayExists(color: CardColor): Boolean {
        val lowestCardInHand = lowestCardOfColorInCardList(cardsInHand, color)
        val lowestCardStillInPlay = lowestCardOfColorInCardList(cardsStillInPlay, color)
        if (lowestCardStillInPlay == null || lowestCardInHand == null)
            return false
        return lowestCardInHand.toRankNumber() < lowestCardStillInPlay.toRankNumber()
    }
    private fun lowestCardOfColorInCardList(cardList: List<Card>, color: CardColor): Card? {
        return cardList
            .filter { c -> c.color == color}
            .minByOrNull { c -> c.toRankNumber() }
    }

    //------------------------------------------------------------------------------------------------------------------



}




