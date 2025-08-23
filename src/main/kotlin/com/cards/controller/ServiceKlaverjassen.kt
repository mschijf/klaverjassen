package com.cards.controller

import com.cards.controller.model.*
import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import com.cards.game.klaverjassen.Game
import com.cards.game.klaverjassen.GameStatus
import com.cards.game.klaverjassen.ScoreType
import com.cards.game.klaverjassen.TableSide
import com.cards.player.Player
import com.cards.player.PlayerGroup
import com.cards.player.ai.GeniusPlayerKlaverjassen
import com.cards.tools.RANDOMIZER
import org.springframework.stereotype.Service

@Service
class ServiceKlaverjassen {
    private var gameKlaverjassen = Game()

    private fun createInitialPlayerList(): List<Player> {
        return listOf(
            GeniusPlayerKlaverjassen(TableSide.WEST, gameKlaverjassen),
            GeniusPlayerKlaverjassen(TableSide.NORTH, gameKlaverjassen),
            GeniusPlayerKlaverjassen(TableSide.EAST, gameKlaverjassen),
            GeniusPlayerKlaverjassen(TableSide.SOUTH, gameKlaverjassen),
        )
    }

    private var playerGroup = PlayerGroup(createInitialPlayerList())
        .also {
            RANDOMIZER.setSeed(141471740)
            it.dealCards()
        }

    fun newGame(): GameStatusModelKlaverjassen {
        gameKlaverjassen = Game()
        playerGroup = PlayerGroup(createInitialPlayerList())
        playerGroup.dealCards()
        return getGameStatus()
    }

    fun getGameStatus(): GameStatusModelKlaverjassen {
        val trickOnTable = if (gameKlaverjassen.newRoundToBeStarted()) {
            null
        } else {
            gameKlaverjassen.getCurrentRound().getTrickOnTable()
        }

        val cardsOnTable = TableModel(
            trickOnTable?.getCardPlayedBy(TableSide.SOUTH),
            trickOnTable?.getCardPlayedBy(TableSide.WEST),
            trickOnTable?.getCardPlayedBy(TableSide.NORTH),
            trickOnTable?.getCardPlayedBy(TableSide.EAST)
        )
        val sideToMove = gameKlaverjassen.getSideToMove()
        val sideToLead = gameKlaverjassen.getTrickLead()?: gameKlaverjassen.getNewRoundLead()

        val playerSouth = makePlayerCardListModel(TableSide.SOUTH)
        val playerNorth = makePlayerCardListModel(TableSide.NORTH)
        val playerWest = makePlayerCardListModel(TableSide.WEST)
        val playerEast = makePlayerCardListModel(TableSide.EAST)

        val gameJsonString = ""
        val newRoundToBeStarted = gameKlaverjassen.newRoundToBeStarted()

        println("====================================================================================================")
        println("To Move: $sideToMove")
        (playerGroup.getPlayer(TableSide.SOUTH) as GeniusPlayerKlaverjassen).printAnalyzer()

        return GameStatusModelKlaverjassen(
            generic = GameStatusModel(
                cardsOnTable,
                sideToMove,
                sideToLead,
                newRoundToBeStarted,
                playerSouth,
                playerWest,
                playerNorth,
                playerEast,
                gameJsonString,
                RANDOMIZER.getLastSeedUsed()
            ),
            trumpChoice = if (!gameKlaverjassen.newRoundToBeStarted())
                TrumpChoiceModel(
                    gameKlaverjassen.getCurrentRound().getTrumpColor(),
                    gameKlaverjassen.getCurrentRound().getContractOwningSide()
                )
            else
                null
        )
    }

    private fun makePlayerCardListModel(tableSide: TableSide): List<CardInHandModel> {
        val player = playerGroup.getPlayer(tableSide)
        val xx = player
            .getCardsInHand()
            .sortedBy { card -> 100 * card.color.ordinal + card.rank.ordinal }
            .map { card ->
                CardInHandModel(
                    card,
                    isLegalCardToPlay(player, card),
                    getGeniusCardValue(player as GeniusPlayerKlaverjassen, card)
                )
            }
        return xx
    }

    private fun getGeniusCardValue(geniusPlayerKlaverjassen: GeniusPlayerKlaverjassen, card: Card): String {
//        val valueList = geniusPlayerKlaverjassen.getCardPlayedValueList()
//        return valueList.firstOrNull{card == it.card}?.value?.toString()?:"x"
        return if (geniusPlayerKlaverjassen.tableSide == TableSide.SOUTH) " " else if (card.isJack()) "x" else " "
    }

    fun computeMove(): CardPlayedModel? {
        val playerToMove = playerGroup.getPlayer(gameKlaverjassen.getSideToMove())
        val suggestedCardToPlay = playerToMove.chooseCard()
        return executeMove(suggestedCardToPlay.color, suggestedCardToPlay.rank)
    }

    fun executeMove(color: CardColor, rank: CardRank): CardPlayedModel? {
        val playerToMove = playerGroup.getPlayer(gameKlaverjassen.getSideToMove())
        val suggestedCardToPlay = Card(color, rank)
        if (!isLegalCardToPlay(playerToMove, suggestedCardToPlay)) {
            println("LOG.WARNING: try to play illegal card")
            return null
        }

        val cardsStillInHand = playerToMove.getNumberOfCardsInHand()

        val gameStatus = playCard(suggestedCardToPlay)

        val trickCompleted = if (gameStatus.trickFinished)
            TrickCompletedModel(
                gameKlaverjassen.getLastTrickWinner()!!,
                gameStatus.roundFinished,
                gameStatus.gameFinished,
            )
        else
            null

        val nextSideToPlay = gameKlaverjassen.getSideToMove()

        return CardPlayedModel(
            playerToMove.tableSide,
            suggestedCardToPlay,
            nextSideToPlay,
            cardsStillInHand,
            trickCompleted,
        )
    }

    fun getScoreCard(): ScoreModelKlaverjassen {
        return ScoreModelKlaverjassen(
            gameKlaverjassen.getAllScoresPerRound()
                .map { roundScore ->
                    RoundScoreKlaverjassen(
                        if (roundScore.northSouthPoints == 0) {
                            when (roundScore.scoreType) {
                                ScoreType.NAT -> "NAT"
                                ScoreType.PIT -> "PIT"
                                ScoreType.REGULAR -> "0"
                            }
                        } else {
                            roundScore.northSouthPoints.toString()
                        },

                        if (roundScore.eastWestPoints == 0) {
                            when (roundScore.scoreType) {
                                ScoreType.NAT -> "NAT"
                                ScoreType.PIT -> "PIT"
                                ScoreType.REGULAR -> "0"
                            }
                        } else {
                            roundScore.eastWestPoints.toString()
                        },

                        if (roundScore.northSouthBonus == 0) "" else roundScore.northSouthBonus.toString(),
                        if (roundScore.eastWestBonus == 0) "" else roundScore.eastWestBonus.toString()
                    )
                }
        )
    }

    fun computeTrumpCardChoice(tableSide: TableSide): GameStatusModelKlaverjassen {
        val choosingPlayer = playerGroup.getPlayer(tableSide)
        val trumpColor = choosingPlayer.chooseTrumpColor()
        return executeTrumpCardChoice(trumpColor, tableSide)
    }

    fun executeTrumpCardChoice(trumpColor: CardColor, tableSide: TableSide): GameStatusModelKlaverjassen {
        gameKlaverjassen.startNewRound(trumpColor, tableSide)
//        return TrumpChoiceModel(trumpColor, tableSide)
        return getGameStatus()
    }

    //======================================================================================================
    // added from GameMaster
    //======================================================================================================

    private fun isLegalCardToPlay(player: Player, card: Card): Boolean {
        return player.getLegalPlayableCards().contains(card)
    }

    fun playCard(card: Card): GameStatus {
        val playerToMove = playerGroup.getPlayer(gameKlaverjassen.getSideToMove())
        playerToMove.removeCard(card)
        val gameStatus = gameKlaverjassen.playCard(card)
        if (playerGroup.allEmptyHanded())
            playerGroup.dealCards()
        return gameStatus
    }

}