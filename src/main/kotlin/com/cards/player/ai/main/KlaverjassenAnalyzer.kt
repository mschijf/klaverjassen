package com.cards.player.ai.main

import com.cards.game.card.*
import com.cards.game.klaverjassen.Round
import com.cards.game.klaverjassen.TableSide
import com.cards.game.klaverjassen.Trick
import com.cards.game.klaverjassen.beats
import com.cards.game.klaverjassen.toRankNumberNoTrump
import com.cards.game.klaverjassen.toRankNumberTrump
import com.cards.player.Player

class KlaverjassenAnalyzer(
    private val playerForWhichWeAnalyse: Player) {

    companion object {
        var t: Long = 0
    }

    private lateinit var currentRound: Round
    private lateinit var trumpColor: CardColor

    private val allSides = TableSide.values().toSet()
    private val mySide = playerForWhichWeAnalyse.tableSide
    private val otherSides = allSides - mySide
    private val allCards = CARDDECK.baseDeckCardsSevenAndHigher
    private val cardsPlayedDuringAnalysis = mutableSetOf<Card>()

    val playerCanHave: Map<TableSide, MutableSet<Card>> = allSides.associateWith { mutableSetOf() }
    val playerMustHave: Map<TableSide, MutableSet<Card>> = allSides.associateWith { mutableSetOf() }
    val playerProbablyHas: Map<TableSide, MutableSet<Card>> = allSides.associateWith { mutableSetOf() }
    val playerProbablyHasNot: Map<TableSide, MutableSet<Card>> = allSides.associateWith { mutableSetOf() }
    val playerHeeftGeseind: MutableMap<TableSide, Card?> = allSides.associateWith { null }.toMutableMap()

    private fun cardsInHandForSide(side: TableSide): Int {
        if (side == mySide)
            return playerForWhichWeAnalyse.getCardsInHand().size

        val playersPlayedInLastTrick = currentRound.getTrickOnTable().getSidesPlayed()
        val numberOfCardsInHandOtherPlayer = playerForWhichWeAnalyse.getCardsInHand().size - if (side in playersPlayedInLastTrick) 1 else 0
        return numberOfCardsInHandOtherPlayer
    }

    fun refreshAnalysis()  {
        t++

        initGlobals()
        determinePlayerCanHaveCards()
        updateAfterAnalysis()
    }

    private fun initGlobals() {
        currentRound = playerForWhichWeAnalyse.game.getCurrentRound()
        trumpColor = currentRound.getTrumpColor()
        cardsPlayedDuringAnalysis.clear()

        playerCanHave.values.forEach { it.clear() }
        playerMustHave.values.forEach { it.clear() }
        playerProbablyHas.values.forEach { it.clear() }
        playerProbablyHasNot.values.forEach { it.clear() }
        playerHeeftGeseind.keys.forEach { playerHeeftGeseind[it] = null }
        val cardsInOtherHands = allCards - playerForWhichWeAnalyse.getCardsInHand()
        otherSides.forEach { other -> playerCanHave[other]!!.addAll(cardsInOtherHands) }
    }


    private fun updateAfterAnalysis() {

        //canHave -- no assumptions
        //mustHave -- no assumptions (geen overlap met welke canhave dan ook)

        // probablyHas    -- assumption moet ook in canHave maar niet in SureHas zitten
        // probablyHasNot -- assumption moet ook in canHave maar niet in SureHas zitten
        //                -- zit niet ook in probablyHas
        //
        // als een speler heeft probablyHas, dan andere players hebben probablyHasNot
        // als twee spelers hebben probablyHas, dan verdwijnen beiden.

        // 1. als speler enige is met een kaart in can have, dan is het een sure have
        //        remove it from canhave and add it to mustHave
        // 2. remove all cards in probablys that are not in canhave/surehas
        // 3. if number of canhaves+surehas == cards in hand, then everything = surehas

        updateMine()
        equalizeSureHasWithCanHave()
        removeImpossibles()
    }

    private fun updateMine() {
        playerCanHave[mySide]!!.clear()
        playerMustHave[mySide]!!.addAll(playerForWhichWeAnalyse.getCardsInHand())
        playerProbablyHas[mySide]!!.clear()
        playerProbablyHasNot[mySide]!!.clear()
    }

    private fun equalizeSureHasWithCanHave() {

        do {
            val sumSureHas0 = playerMustHave.values.fold(emptySet<Card>()) { acc, mustHaveCards -> acc + mustHaveCards }

            //update mustHave --> if a player has cards in canhave that all other players don't have, then it becomes a mustHave
            otherSides.forEach { otherSide ->
                val otherCanHave = (allSides - otherSide).flatMap { playerCanHave[it]!! + playerMustHave[it]!! }.toSet()
                val unique = playerCanHave[otherSide]!!.filterNot{card -> card in otherCanHave}
                playerMustHave[otherSide]!! += unique
                playerCanHave[otherSide]!! -= unique
            }

            //a mustHave can not appear in any other canHaves:
            val sumSureHas1 = playerMustHave.values.fold(emptySet<Card>()) { acc, mustHaveCards -> acc + mustHaveCards }
            allSides.forEach { player -> playerCanHave[player]!!.removeAll(sumSureHas1) }

            //if number of canHave + SureHas == number of cardsInHand
            otherSides.forEach { otherSide ->
                val numberOfCardsInHandOtherSide = cardsInHandForSide(otherSide)
                if (playerMustHave[otherSide]!!.size == numberOfCardsInHandOtherSide) {
                    playerCanHave[otherSide]!!.clear()
                } else if ((playerMustHave[otherSide]!! + playerCanHave[otherSide]!!).size == numberOfCardsInHandOtherSide) {
                    playerMustHave[otherSide]!! += playerCanHave[otherSide]!!
                    playerCanHave[otherSide]!!.clear()
                }
            }
            //a mustHave can not appear in any other canHaves:
            val sumSureHas2 = playerMustHave.values.fold(emptySet<Card>()) { acc, mustHaveCards -> acc + mustHaveCards }
            allSides.forEach { player -> playerCanHave[player]!!.removeAll(sumSureHas2) }
        } while (sumSureHas0.size != sumSureHas2.size)
    }

    private fun removeImpossibles() {
        allSides.forEach { side ->
            playerProbablyHasNot[side]!! -= playerMustHave[side]!!
            val impossibleCards = playerProbablyHasNot[side]!!.filter { card -> card !in (playerCanHave[side]!!) }
            playerProbablyHasNot[side]!! -= impossibleCards
        }

        allSides.forEach { side ->
            playerProbablyHas[side]!! -= playerMustHave[side]!!
            val impossibleCards = playerProbablyHas[side]!!.filter { card -> card !in (playerCanHave[side]!!) }
            playerProbablyHas[side]!! -= impossibleCards
        }

        allSides.forEach { side ->
            val other = playerProbablyHas[side.clockwiseNext(1)]!! + playerProbablyHas[side.clockwiseNext(2)]!! + playerProbablyHas[side.clockwiseNext(3)]!!
//            val impossibleCards = playerProbablyHas[side]!!.filter { card -> card in other }
//            if (impossibleCards.isNotEmpty())
//                println("komt voor $side --> $impossibleCards")
        }

    }

    //-----------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------------------------------

    private fun processCard(trick: Trick, cardPlayed: Card, highestTrumpUpTillNow: Card?) {
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
        determineAssumptions(trick, cardPlayed)
        cardsPlayedDuringAnalysis.add(cardPlayed)
    }

    private fun processTrick(trick: Trick) {
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
        val allTricks = currentRound.getTrickList()
        allTricks.filterNot { trick -> trick.hasNotStarted() }.forEach { trick ->
            processTrick(trick)
        }
    }

    private fun determineAssumptions(trick: Trick, cardJustPlayed: Card) {
        val playerJustMoved = trick.getSideThatPlayedCard(cardJustPlayed)!!

        //seinen
        //todo: alleen seinen als het zeker is dat maat de slag haalt
        val highestCard = highestOfColorStillAvailable(cardJustPlayed.color)
        var heeftGeseind = false
        if (playerJustMoved.isOppositeOf(trick.getWinningSide())) {
            if (cardJustPlayed.color != trick.getLeadColor() && cardJustPlayed.color != trick.getWinningCard()!!.color && cardJustPlayed.color != trumpColor) {
                if (highestCard != null && highestCard.toRankNumberNoTrump() >= Card(cardJustPlayed.color, CardRank.NINE).toRankNumberNoTrump()) {
                    if (cardJustPlayed.toRankNumberNoTrump() <= Card(cardJustPlayed.color, CardRank.NINE).toRankNumberNoTrump()) {
                        addProbablyHas(playerJustMoved, highestCard)
                        setHeeftGeseind(playerJustMoved, cardJustPlayed)
                        heeftGeseind = true
                    } else if (cardJustPlayed == highestCard) {
                        val secondHighest = secondHighestOfColorStillAvailable(cardJustPlayed.color)
                        if (secondHighest != null) {
                            addProbablyHas(playerJustMoved, secondHighest)
                            setOthersCannotHebbenGeseind(playerJustMoved, cardJustPlayed)
                            setHeeftGeseind(playerJustMoved, cardJustPlayed)
                            heeftGeseind = true
                        }
                    }
                }
            }
        }

        //afSeinen
        if (cardJustPlayed == highestCard) {
            if (trick.isSideToLead(playerJustMoved.opposite()) && trick.isLeadColor(cardJustPlayed.color)) {
                if (playerHeeftGeseind[playerJustMoved]?.color == cardJustPlayed.color) {
                    //maat is met kleur gekomen, nadat deze speler dat geseind heeft. Het sein geldt niet meer
                    setHeeftAfGeseind(playerJustMoved)
                    setOthersCannotHebbenGeseind(playerJustMoved, cardJustPlayed)
                }
            }
            if (playerJustMoved.others().any { playerHeeftGeseind[it]?.color == cardJustPlayed.color }) {
                setOthersCannotHebbenGeseind(playerJustMoved, cardJustPlayed)
            }
        }

//        if (trick.isSideToLead(playerJustMoved) && currentRound.isContractOwningSide(playerJustMoved)) {
//            if (noRealTrumpsPlayed()) {
//                if (cardJustPlayed.color == trumpColor) {
//                    if (!cardJustPlayed.isJack(trumpColor)) {
//                        if (cardJustPlayed.isNine(trumpColor)) {
//                            addProbablyHas(playerJustMoved, Card(trumpColor, CardRank.JACK))
//                        } else {
//                            //speler speelt geen boer en geen negen: probeert boer er uit te krijgen, om eigen nel hoog te maken
//                            addProbablyHasNot(playerJustMoved, Card(trumpColor, CardRank.JACK))
//                            addProbablyHas(playerJustMoved, Card(trumpColor, CardRank.NINE))
//                        }
//                    }
//                } else {
//                    //speler komt met andere kaart dan de boer en ook geen troef
//                    //todo: wat als het een aas is? (poging eerst roem te spelen, voordat er troef wordt getrokken)?
//                    addProbablyHasNot(playerJustMoved, Card(trumpColor, CardRank.JACK))
//                }
//            }
//        }
//
//        //todo: check op 10 weggegeven --> is die kaal? of noodzaak vanwege roem ontwijken
//        if (trick.isComplete()) { //playerToMove is last player in this trick that played a card
//            if (trick.getWinningSide() != playerJustMoved && trick.getWinningSide() != playerJustMoved.opposite()) {
//                if (roemWeggegevenDoorLastPlayer(trick)) { //roem weggegeven
//                    val bonusAfter = trick.getScore().getBonusForPlayer(playerJustMoved.clockwiseNext())
//                    trick.removeLastCard()
//                    playerCanHave[playerJustMoved]!!.filter { it.color == cardJustPlayed.color }.forEach { otherCard ->
//                        if (otherCard != cardJustPlayed) {
//                            trick.addCard(otherCard)
//                            if (trick.getScore().getBonusForPlayer(playerJustMoved.clockwiseNext()) < bonusAfter) {
//                                addProbablyHasNot(playerJustMoved, otherCard)
//                            }
//                            trick.removeLastCard()
//                        }
//                    }
//                    trick.addCard(cardJustPlayed)
//                }
//            } else {
//                if (roemOntwekenDoorLastPlayer(trick)) { //roem niet gemaakt
//                    val bonusAfter = trick.getScore().getBonusForPlayer(playerJustMoved)
//                    trick.removeLastCard()
//                    playerCanHave[playerJustMoved]!!.filter { it.color == cardJustPlayed.color }.forEach { otherCard ->
//                        if (otherCard != cardJustPlayed) {
//                            trick.addCard(otherCard)
//                            if (trick.getScore().getBonusForPlayer(playerJustMoved) > bonusAfter) {
//                                addProbablyHasNot(playerJustMoved, otherCard)
//                            }
//                            trick.removeLastCard()
//                        }
//                    }
//                    trick.addCard(cardJustPlayed)
//                }
//            }
//        }
    }

    private fun roemWeggegevenDoorLastPlayer(trickSoFar: Trick): Boolean {
        val cardJustPlayed = trickSoFar.getCardsPlayed().last()
        val playerJustMoved = trickSoFar.getSideThatPlayedCard(cardJustPlayed)!!

        val bonusAfter = trickSoFar.getScore().getBonusForPlayer(playerJustMoved.clockwiseNext())
        trickSoFar.removeLastCard()
        val bonusBefore = trickSoFar.getScore().getBonusForPlayer(playerJustMoved.clockwiseNext())
        trickSoFar.addCard(cardJustPlayed)
        return bonusAfter > bonusBefore
    }

    private fun roemOntwekenDoorLastPlayer(trickSoFar: Trick): Boolean {
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
        val cardsStillAvailable = (allCards - cardsPlayedDuringAnalysis)
        return if (cardColor == trumpColor)
            cardsStillAvailable.filter { it.color == cardColor }.maxByOrNull { it.toRankNumberTrump() }
        else
            cardsStillAvailable.filter { it.color == cardColor }.maxByOrNull { it.toRankNumberNoTrump() }
    }

    private fun secondHighestOfColorStillAvailable(cardColor: CardColor): Card? {
        val cardsStillAvailable = (allCards - cardsPlayedDuringAnalysis)
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
    private fun setHeeftGeseind(side: TableSide, card: Card) {
        if (playerHeeftGeseind[side] == null)
            playerHeeftGeseind[side] = card
    }
    private fun setHeeftAfGeseind(side: TableSide) {
        playerHeeftGeseind[side] = null
    }
    private fun setOthersCannotHebbenGeseind(side: TableSide, card: Card) {
        side.others().forEach { if (playerHeeftGeseind[it]?.color == card.color) playerHeeftGeseind[it] = null  }
    }

}