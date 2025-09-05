package com.cards.game.klaverjassen

import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KlaverjassenToolsTest {
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

    @Test
    fun maxSequenceTestAll() {
        val cardList = Card.ofList("7C 8C 9C 10C JC QC KC AC")
        assertEquals(8, cardList.maxSequence())
    }

    @Test
    fun maxSequenceTestLast() {
        val cardList = Card.ofList("7C 8C 9C JC QC KC AC")
        assertEquals(4, cardList.maxSequence())
    }

    @Test
    fun maxSequenceTestFirst() {
        val cardList = Card.ofList("7C 8C 9C 10C  QC KC AC")
        assertEquals(4, cardList.maxSequence())
    }

    @Test
    fun maxSequenceTestEmpty() {
        val cardList = emptyList<Card>()
        assertEquals(0, cardList.maxSequence())
    }

    @Test
    fun maxSequenceTestOne() {
        val cardList = Card.ofList("7C")
        assertEquals(1, cardList.maxSequence())
    }

    @Test
    fun maxSequenceTestUsingCardAll() {
        val cardList = Card.ofList("7C 8C 9C 10C JC QC KC AC")
        assertEquals(8, cardList.maxSequenceUsing(Card.queen(CardColor.CLUBS)))
    }

    @Test
    fun maxSequenceTestUsingCardLast() {
        val cardList = Card.ofList("7C 8C 10C JC QC")
        assertEquals(3, cardList.maxSequenceUsing(Card.queen(CardColor.CLUBS)))
    }

    @Test
    fun maxSequenceTestUsingCardFirst() {
        val cardList = Card.ofList("7C 8C 9C JC QC KC AC")
        assertEquals(3, cardList.maxSequenceUsing(Card.seven(CardColor.CLUBS)))
    }

    @Test
    fun maxSequenceTestUsingCardLastOfShorterSequence() {
        val cardList = Card.ofList("7C 8C 9C JC QC KC AC")
        assertEquals(3, cardList.maxSequenceUsing(Card.nine(CardColor.CLUBS)))
    }

    @Test
    fun maxSequenceTestUsingCardMiddletOfShorterSequence() {
        val cardList = Card.ofList("7C 8C 9C JC QC KC AC")
        assertEquals(3, cardList.maxSequenceUsing(Card.eight(CardColor.CLUBS)))
    }

}