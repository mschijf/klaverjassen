package com.cards.game.fourplayercardgame.klaverjassen

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RoemTest {
    @Test
    fun test50low() {
        val trick = listOf("7H", "8H", "9H", "10H").map { str -> Card.of(str) }
        assertEquals(50, trick.bonusValue(CardColor.CLUBS))
        assertEquals(50, trick.bonusValue(CardColor.HEARTS))
    }

    @Test
    fun test50high() {
        val trick = listOf("JH", "AH", "KH", "QH").map { str -> Card.of(str) }
        assertEquals(50, trick.bonusValue(CardColor.CLUBS))
        assertEquals(70, trick.bonusValue(CardColor.HEARTS))
    }

    @Test
    fun test20_left() {
        val trick = listOf("7H", "8H", "9H", "JH").map { str -> Card.of(str) }
        assertEquals(20, trick.bonusValue(CardColor.CLUBS))
        assertEquals(20, trick.bonusValue(CardColor.HEARTS))
    }

    @Test
    fun test20_right() {
        val trick = listOf("7H", "10H", "JH", "QH").map { str -> Card.of(str) }
        assertEquals(20, trick.bonusValue(CardColor.CLUBS))
        assertEquals(20, trick.bonusValue(CardColor.HEARTS))
    }

    @Test
    fun test40() {
        val trick = listOf("7H", "JH", "QH", "KH").map { str -> Card.of(str) }
        assertEquals(20, trick.bonusValue(CardColor.CLUBS))
        assertEquals(40, trick.bonusValue(CardColor.HEARTS))
    }

    @Test
    fun test20_stuk() {
        val trick = listOf("7C", "JC", "QH", "KH").map { str -> Card.of(str) }
        assertEquals(0, trick.bonusValue(CardColor.CLUBS))
        assertEquals(20, trick.bonusValue(CardColor.HEARTS))
    }

    @Test
    fun testNoBonus() {
        val trick = listOf("7C", "8C", "9H", "10C").map { str -> Card.of(str) }
        assertEquals(0, trick.bonusValue(CardColor.CLUBS))
        assertEquals(0, trick.bonusValue(CardColor.HEARTS))
    }

    @Test
    fun testOneMore20() {
        val trick = listOf("7C", "8C", "9H", "9C").map { str -> Card.of(str) }
        assertEquals(20, trick.bonusValue(CardColor.CLUBS))
        assertEquals(20, trick.bonusValue(CardColor.HEARTS))
    }

    @Test
    fun test100King() {
        val trick = listOf("KC", "KH", "KD", "KS").map { str -> Card.of(str) }
        assertEquals(100, trick.bonusValue(CardColor.CLUBS))
        assertEquals(100, trick.bonusValue(CardColor.HEARTS))
    }

    @Test
    fun test100Seven() {
        val trick = listOf("7C", "7H", "7D", "7S").map { str -> Card.of(str) }
        assertEquals(100, trick.bonusValue(CardColor.CLUBS))
        assertEquals(100, trick.bonusValue(CardColor.HEARTS))
    }

    @Test
    fun test100Jack() {
        val trick = listOf("JC", "JH", "JD", "JS").map { str -> Card.of(str) }
        assertEquals(100, trick.bonusValue(CardColor.CLUBS))
        assertEquals(100, trick.bonusValue(CardColor.HEARTS))
    }

    @Test
    fun beatsTwoNoTrump() {
        val thisCard = Card(CardColor.CLUBS, CardRank.TEN)
        val otherCard = Card(CardColor.CLUBS, CardRank.JACK)
        assertEquals(true, thisCard.beats(otherCard, CardColor.HEARTS))
        assertEquals(false, otherCard.beats(thisCard, CardColor.HEARTS))
    }
    @Test
    fun beatsTwoBothTrumpJack() {
        val thisCard = Card(CardColor.CLUBS, CardRank.TEN)
        val otherCard = Card(CardColor.CLUBS, CardRank.JACK)
        assertEquals(false, thisCard.beats(otherCard, CardColor.CLUBS))
        assertEquals(true, otherCard.beats(thisCard, CardColor.CLUBS))
    }
    @Test
    fun beatsTwoBothTrumpNine() {
        val thisCard = Card(CardColor.CLUBS, CardRank.ACE)
        val otherCard = Card(CardColor.CLUBS, CardRank.NINE)
        assertEquals(false, thisCard.beats(otherCard, CardColor.CLUBS))
        assertEquals(true, otherCard.beats(thisCard, CardColor.CLUBS))
    }
    @Test
    fun trumpBeatsNoTrump() {
        val thisCard = Card(CardColor.CLUBS, CardRank.SEVEN)
        val otherCard = Card(CardColor.HEARTS, CardRank.ACE)
        assertEquals(true, thisCard.beats(otherCard, CardColor.CLUBS))
        assertEquals(false, otherCard.beats(thisCard, CardColor.CLUBS))
    }
    @Test
    fun firstOfDifferentcolorNoTrumpBeatsOther() {
        val thisCard = Card(CardColor.CLUBS, CardRank.SEVEN)
        val otherCard = Card(CardColor.HEARTS, CardRank.ACE)
        assertEquals(true, thisCard.beats(otherCard, CardColor.DIAMONDS))
        assertEquals(true, otherCard.beats(thisCard, CardColor.DIAMONDS))
    }
}