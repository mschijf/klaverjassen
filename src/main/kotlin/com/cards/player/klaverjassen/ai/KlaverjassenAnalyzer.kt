package com.cards.player.klaverjassen.ai

import com.cards.game.card.*
import com.cards.game.fourplayercardgame.basic.TableSide
import com.cards.game.fourplayercardgame.basic.Trick
import com.cards.game.fourplayercardgame.klaverjassen.*
import java.util.Currency

class KlaverjassenAnalyzer(
    private val playerForWhichWeAnalyse: GeniusPlayerKlaverjassen) {

    private var currentRound = playerForWhichWeAnalyse.getCurrentRound()
    private var trumpColor = currentRound.getTrumpColor()

    private val allSides = TableSide.values().toSet()
    private val mySide = playerForWhichWeAnalyse.tableSide
    private val otherSides = allSides - mySide

    private val playerCanHave: Map<TableSide, MutableSet<Card>> = allSides.associateWith { mutableSetOf() }
    private val playerSureHas: Map<TableSide, MutableSet<Card>> = allSides.associateWith { mutableSetOf() }
    private val playerProbablyHas: Map<TableSide, MutableSet<Card>> = allSides.associateWith { mutableSetOf() }
    private val playerProbablyHasNot: Map<TableSide, MutableSet<Card>> = allSides.associateWith { mutableSetOf() }


    private val cardsPlayedDuringAnalysis = mutableListOf<Card>()

    fun refreshAnalysis() {
        determinePlayerCanHaveCards()
        updateAfterAnalysis()
    }

    fun playerCanHaveCards(side: TableSide): Set<Card> = playerCanHave[side]!!
    fun playerSureHasCards(side: TableSide): Set<Card> = playerSureHas[side]!!

    private fun updateAfterAnalysis() {

        //canHave -- no assumptions
        //sureHas -- no assumptions (geen overlap met welke canhave dan ook)

        //probablyHas -- assumption moet ook in canHave of SureHas zitten
        //probablyHasNot -- assumption moet ook in canHave of SureHas zitten (en heeft geen overlap met probablyHas)

        // 1. als speler enige is met een kaart in can have, dan is het een sure have
        //        remove it from canhave and add it to sureHas
        // 2. remove all cards in probablys that are not in canhave/surehas
        // 3. if number of canhaves+surehas == cards in hand, then everything = surehas

        updateMine()
        equalizeSureHasWithCanHave()
        removeImpossibles()
    }

    private fun updateMine() {
        playerCanHave[mySide]!!.clear()
        playerSureHas[mySide]!!.addAll(playerForWhichWeAnalyse.getCardsInHand())
        playerProbablyHas[mySide]!!.clear()
        playerProbablyHasNot[mySide]!!.clear()
    }

    private fun equalizeSureHasWithCanHave() {

        do {
            val sumSureHas0 = playerSureHas.values.fold(emptySet<Card>()) { acc, sureHasCards -> acc + sureHasCards }

            //update sureHas --> if a player has cards in canhave that all other players don't have, then it becomes a sureHas
            otherSides.forEach { otherSide ->
                val otherCanHave = (allSides - otherSide).flatMap { playerCanHave[it]!! + playerSureHas[it]!! }.toSet()
                val unique = playerCanHave[otherSide]!!.filterNot{card -> card in otherCanHave}
                playerSureHas[otherSide]!! += unique
                playerCanHave[otherSide]!! -= unique
            }

            //a sureHas can not appear in any other canHaves:
            val sumSureHas1 = playerSureHas.values.fold(emptySet<Card>()) { acc, sureHasCards -> acc + sureHasCards }
            allSides.forEach { player -> playerCanHave[player]!!.removeAll(sumSureHas1) }

            //if number of canHave + SureHas == number of cardsInHand
            otherSides.forEach { otherSide ->
                val numberOfCardsInHandOtherSide = cardsInHandForSide(otherSide)
                if (playerSureHas[otherSide]!!.size == numberOfCardsInHandOtherSide) {
                    playerCanHave[otherSide]!!.clear()
                } else if ((playerSureHas[otherSide]!! + playerCanHave[otherSide]!!).size == numberOfCardsInHandOtherSide) {
                    playerSureHas[otherSide]!! += playerCanHave[otherSide]!!
                    playerCanHave[otherSide]!!.clear()
                }
            }
            //a sureHas can not appear in any other canHaves:
            val sumSureHas2 = playerSureHas.values.fold(emptySet<Card>()) { acc, sureHasCards -> acc + sureHasCards }
            allSides.forEach { player -> playerCanHave[player]!!.removeAll(sumSureHas2) }
        } while (sumSureHas0.size != sumSureHas2.size)
    }

    private fun removeImpossibles() {
        allSides.forEach { side ->
            val impossibleCards = playerProbablyHasNot[side]!!.filterNot { card -> card in (playerCanHave[side]!! + playerSureHas[side]!!) }
            playerProbablyHas[side]!! -= impossibleCards
            playerProbablyHasNot[side]!! -= impossibleCards
        }
    }

    //-----------------------------------------------------------------------------------------------------------------

    private fun processCard(trick: Trick, cardPlayed: Card, highestTrumpUpTillNow: Card?) {

        val allCards = CARDDECK.baseDeckCardsSevenAndHigher

        val sideThatPlayed = trick.getSideThatPlayedCard(cardPlayed)!!

        otherSides.forEach {
                otherSide -> playerCanHave[otherSide]!! -= cardPlayed
        }

        if (trick.getCardsPlayed().first() != cardPlayed) {

            if (cardPlayed.color != trick.getLeadColor()) {
                playerCanHave[sideThatPlayed]!! -= allCards.ofColor(trick.getLeadColor()!!)
                if (cardPlayed.color != trumpColor) {
                    playerCanHave[sideThatPlayed]!! -= allCards.ofColor(trumpColor)
                } else {
                    if (!cardPlayed.beats(highestTrumpUpTillNow, trumpColor)) {
                        playerCanHave[sideThatPlayed]!! -= allCards.filter {
                            it.beats(highestTrumpUpTillNow, trumpColor)
                        }
                    }
                }
            } else if (cardPlayed.color == trumpColor && highestTrumpUpTillNow!!.beats(cardPlayed, trumpColor)) {
                playerCanHave[sideThatPlayed]!! -= allCards.filter {
                    it.beats(highestTrumpUpTillNow, trumpColor)
                }
            } else {
                //player just follows, we can not conclude anything yet
            }
        }
        determineAssumptions(trick as TrickKlaverjassen)
        cardsPlayedDuringAnalysis.add(cardPlayed)
    }

    private fun processTrick(trick: Trick) {
        val allCards = CARDDECK.baseDeckCardsSevenAndHigher

        val firstCard = trick.getCardsPlayed().first()
        processCard(trick, firstCard, null)
        var highestTrumpUpTillNow = if (firstCard.color == trumpColor) firstCard else null

        trick.getCardsPlayed().drop(1).forEach { cardPlayed ->
            processCard(trick, cardPlayed, highestTrumpUpTillNow)
            if (cardPlayed.color == trumpColor && cardPlayed.beats(highestTrumpUpTillNow, trumpColor))
                highestTrumpUpTillNow = cardPlayed
        }
    }

    private fun determinePlayerCanHaveCards() {
        currentRound = playerForWhichWeAnalyse.getCurrentRound()
        trumpColor = currentRound.getTrumpColor()
        cardsPlayedDuringAnalysis.clear()

        playerCanHave.values.forEach { it.clear() }
        playerSureHas.values.forEach { it.clear() }
        playerProbablyHas.values.forEach { it.clear() }
        playerProbablyHasNot.values.forEach { it.clear() }

        val allCards = CARDDECK.baseDeckCardsSevenAndHigher

        otherSides.forEach { otherSide ->
            playerCanHave[otherSide]!!.addAll(allCards - playerForWhichWeAnalyse.getCardsInHand())
        }

        val allTricks = playerForWhichWeAnalyse.getCurrentRound().getTrickList()
        allTricks.filterNot { trick -> trick.hasNotStarted() }.forEach { trick ->
            processTrick(trick)
        }
    }

    private fun determineAssumptions(trickSoFar: TrickKlaverjassen) {
        val cardJustPlayed = trickSoFar.getCardsPlayed().last()
        val playerJustMoved = trickSoFar.getSideThatPlayedCard(cardJustPlayed)!!

        if (trickSoFar.isSideToLead(playerJustMoved) && currentRound.isContractOwningSide(playerJustMoved)) {
            if (noRealTrumpsPlayed()) {
                if (cardJustPlayed.color == trumpColor) {
                    if (cardJustPlayed.isJack(trumpColor)) {
                        if (cardJustPlayed.isNine(trumpColor)) {
                            addProbablyHas(playerJustMoved, Card(trumpColor, CardRank.JACK))
                        } else {
                            //speler speelt geen boer en geen negen: probeert boer er uit te krijgen, om eigen nel hoog te maken
                            addProbablyHasNot(playerJustMoved, Card(trumpColor, CardRank.JACK))
                            addProbablyHas(playerJustMoved, Card(trumpColor, CardRank.NINE))
                        }
                    }
                } else {
                    //speler komt met andere kaart dan de boer en ook geen troef
                    //todo: wat als het een aas is? (poging eerst roem te spelen, voordat er troef wordt getrokken)?
                    addProbablyHasNot(playerJustMoved, Card(trumpColor, CardRank.JACK))
                }
            }
        }

        //seinen
        if (playerJustMoved.isOppositeOf(trickSoFar.getWinningSide())) {
            if (cardJustPlayed.color != trickSoFar.getLeadColor() && cardJustPlayed.color != trickSoFar.getWinningCard()!!.color && cardJustPlayed.color != trumpColor) {
                val highestCard = highestOfColorStillAvailable(cardJustPlayed.color)
                if (cardJustPlayed.toRankNumberNoTrump() <= Card(cardJustPlayed.color, CardRank.NINE).toRankNumberNoTrump()) {
                    if (highestCard != null)
                        addProbablyHas(playerJustMoved, highestCard)
                } else if (cardJustPlayed == highestCard) {
                    val secondHighest = secondHighestOfColorStillAvailable(cardJustPlayed.color)
                    if (secondHighest != null)
                        addProbablyHas(playerJustMoved, secondHighest)
                }
            }
        }

        if (!trickSoFar.isLeadColor(trumpColor) && trickSoFar.isLeadColor(cardJustPlayed.color) && !playerJustMoved.isOppositeOf(trickSoFar.getWinningSide())) {
            if (!cardJustPlayed.isTrumpCard() && cardJustPlayed.isTen() )
                if (playerCanHave[playerJustMoved]!!.count { it.color == cardJustPlayed.color } > 1) {
                    //kale 10 --> dus heeft die kleur verder niet meer (of roem ontwijken ==> nog checken)
                    //todo: (of roem ontwijken ==> nog checken)
                    addProbablyHasNot(playerJustMoved, playerCanHave[playerJustMoved]!!.filter { it.color == cardJustPlayed.color })
                }
        }

        if (trickSoFar.isComplete()) { //playerToMove is last player in this trick that played a card
            if (trickSoFar.getWinningSide() != playerJustMoved && trickSoFar.getWinningSide() != playerJustMoved.opposite()) {
                if (roemWeggegevenDoorLastPlayer(trickSoFar)) { //roem weggegeven
                    val bonusAfter = trickSoFar.getScore().getBonusForPlayer(playerJustMoved.clockwiseNext())
                    trickSoFar.removeLastCard()
                    playerCanHave[playerJustMoved]!!.filter { it.color == cardJustPlayed.color }.forEach { otherCard ->
                        if (otherCard != cardJustPlayed) {
                            trickSoFar.addCard(otherCard)
                            if (trickSoFar.getScore().getBonusForPlayer(playerJustMoved.clockwiseNext()) < bonusAfter) {
                                addProbablyHasNot(playerJustMoved, otherCard)
                            }
                            trickSoFar.removeLastCard()
                        }
                    }
                    trickSoFar.addCard(cardJustPlayed)
                }
            } else {
                if (roemOntwekenDoorLastPlayer(trickSoFar)) { //roem niet gemaakt
                    val bonusAfter = trickSoFar.getScore().getBonusForPlayer(playerJustMoved)
                    trickSoFar.removeLastCard()
                    playerCanHave[playerJustMoved]!!.filter { it.color == cardJustPlayed.color }.forEach { otherCard ->
                        if (otherCard != cardJustPlayed) {
                            trickSoFar.addCard(otherCard)
                            if (trickSoFar.getScore().getBonusForPlayer(playerJustMoved) > bonusAfter) {
                                addProbablyHasNot(playerJustMoved, otherCard)
                            }
                            trickSoFar.removeLastCard()
                        }
                    }
                    trickSoFar.addCard(cardJustPlayed)
                }
            }
        }
    }

    private fun roemWeggegevenDoorLastPlayer(trickSoFar: TrickKlaverjassen): Boolean {
        val cardJustPlayed = trickSoFar.getCardsPlayed().last()
        val playerJustMoved = trickSoFar.getSideThatPlayedCard(cardJustPlayed)!!

        val bonusAfter = trickSoFar.getScore().getBonusForPlayer(playerJustMoved.clockwiseNext())
        trickSoFar.removeLastCard()
        val bonusBefore = trickSoFar.getScore().getBonusForPlayer(playerJustMoved.clockwiseNext())
        trickSoFar.addCard(cardJustPlayed)
        return bonusAfter > bonusBefore
    }

    private fun roemOntwekenDoorLastPlayer(trickSoFar: TrickKlaverjassen): Boolean {
        val cardJustPlayed = trickSoFar.getCardsPlayed().last()
        val playerJustMoved = trickSoFar.getSideThatPlayedCard(cardJustPlayed)!!

        val bonusAfter = trickSoFar.getScore().getBonusForPlayer(playerJustMoved)
        trickSoFar.removeLastCard()
        val bonusBefore = trickSoFar.getScore().getBonusForPlayer(playerJustMoved)
        trickSoFar.addCard( cardJustPlayed)
        return bonusAfter == bonusBefore
    }


    private fun Card.isTrumpCard(): Boolean = this.color == trumpColor

    private fun noRealTrumpsPlayed() =
        (cardsPlayedDuringAnalysis.count { it.color == trumpColor } == 0)

    private fun highestOfColorStillAvailable(cardColor: CardColor): Card? {
        val cardsStillAvailable = (CARDDECK.baseDeckCardsSevenAndHigher - cardsPlayedDuringAnalysis)
        return if (cardColor == trumpColor)
            cardsStillAvailable.filter { it.color == cardColor }.maxByOrNull { it.toRankNumberTrump() }
        else
            cardsStillAvailable.filter { it.color == cardColor }.maxByOrNull { it.toRankNumberNoTrump() }
    }

    private fun secondHighestOfColorStillAvailable(cardColor: CardColor): Card? {
        val cardsStillAvailable = (CARDDECK.baseDeckCardsSevenAndHigher - cardsPlayedDuringAnalysis)
        val sortedCards = if (cardColor == trumpColor)
            cardsStillAvailable.filter { it.color == cardColor }.sortedByDescending { it.toRankNumberTrump() }
        else
            cardsStillAvailable.filter { it.color == cardColor }.sortedByDescending { it.toRankNumberNoTrump() }

        return if (sortedCards.size > 1)
            sortedCards[1]
        else
            null
    }

    private fun addProbablyHas(side: TableSide, card: Card) {
        playerProbablyHas[side]!! += card
        playerProbablyHasNot[side]!! -= card
    }
    private fun addProbablyHasNot(side: TableSide, card: Card) {
        playerProbablyHas[side]!! -= card
        playerProbablyHasNot[side]!! += card
    }
    private fun addProbablyHasNot(side: TableSide, cardList: List<Card>) {
        playerProbablyHas[side]!! -= cardList
        playerProbablyHasNot[side]!! += cardList
    }

    fun cardsInHandForSide(side: TableSide): Int {
        if (side == mySide)
            return playerForWhichWeAnalyse.getCardsInHand().size

        val playersPlayedInLastTrick = currentRound.getTrickOnTable().getSidesPlayed()
        val numberOfCardsInHandOtherPlayer = playerForWhichWeAnalyse.getCardsInHand().size - if (side in playersPlayedInLastTrick) 1 else 0
        return numberOfCardsInHandOtherPlayer
    }

}