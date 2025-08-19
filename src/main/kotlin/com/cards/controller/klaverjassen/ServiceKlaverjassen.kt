package com.cards.controller.klaverjassen

import com.cards.controller.basic.model.*
import com.cards.controller.klaverjassen.model.GameStatusModelKlaverjassen
import com.cards.controller.klaverjassen.model.RoundScoreKlaverjassen
import com.cards.controller.klaverjassen.model.ScoreModelKlaverjassen
import com.cards.controller.klaverjassen.model.TrumpChoiceModel
import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import com.cards.game.fourplayercardgame.basic.GameStatus
import com.cards.game.fourplayercardgame.basic.TableSide
import com.cards.game.fourplayercardgame.klaverjassen.GameKlaverjassen
import com.cards.game.fourplayercardgame.klaverjassen.RoundKlaverjassen
import com.cards.game.fourplayercardgame.klaverjassen.ScoreType
import com.cards.game.fourplayercardgame.klaverjassen.legalPlayable
import com.cards.player.Player
import com.cards.player.PlayerGroup
import com.cards.player.klaverjassen.PlayerKlaverjassen
import com.cards.player.klaverjassen.ai.GeniusPlayerKlaverjassen
import com.cards.tools.RANDOMIZER
import org.springframework.stereotype.Service

@Service
class ServiceKlaverjassen {
    private var gameKlaverjassen = GameKlaverjassen.startNewGame()

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
        gameKlaverjassen = GameKlaverjassen.startNewGame()
        playerGroup = PlayerGroup(createInitialPlayerList())
        playerGroup.dealCards()
        return getGameStatus()
    }

    fun getGameStatus(): GameStatusModelKlaverjassen {
        val trickOnTable = gameKlaverjassen.getCurrentRound().getTrickOnTable()
        val onTableSide = TableModel(
            trickOnTable.getCardPlayedBy(TableSide.SOUTH),
            trickOnTable.getCardPlayedBy(TableSide.WEST),
            trickOnTable.getCardPlayedBy(TableSide.NORTH),
            trickOnTable.getCardPlayedBy(TableSide.EAST)
        )
        val sideToMove = gameKlaverjassen.getSideToMove()
        val sideToLead = trickOnTable.getSideToLead()

        val playerSouth = makePlayerCardListModel(TableSide.SOUTH)
        val playerNorth = makePlayerCardListModel(TableSide.NORTH)
        val playerWest = makePlayerCardListModel(TableSide.WEST)
        val playerEast = makePlayerCardListModel(TableSide.EAST)

        val gameJsonString = "" //Gson().toJson(gm)
        val newRoundStarted = gameKlaverjassen.getCurrentRound().hasNotStarted()

        println("====================================================================================================")
        println("$sideToMove")
        (playerGroup.getPlayer(TableSide.NORTH) as GeniusPlayerKlaverjassen).printAnalyzer()

        return GameStatusModelKlaverjassen(
            generic = GameStatusModel(
                onTableSide,
                sideToMove,
                sideToLead,
                newRoundStarted,
                playerSouth,
                playerWest,
                playerNorth,
                playerEast,
                gameJsonString,
                RANDOMIZER.getLastSeedUsed()
            ),
            trumpChoice = TrumpChoiceModel(
                    (gameKlaverjassen.getCurrentRound() as RoundKlaverjassen).getTrumpColor(),
                    (gameKlaverjassen.getCurrentRound() as RoundKlaverjassen).getContractOwningSide()
                )
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
                    ""//getGeniusCardValue(player as GeniusPlayerKlaverjassen, card)
                )
            }
        return xx
    }

    private fun getGeniusCardValue(geniusPlayerKlaverjassen: GeniusPlayerKlaverjassen, card: Card): String {
//        val valueList = geniusPlayerKlaverjassen.getCardPlayedValueList()
//        return valueList.firstOrNull{card == it.card}?.value?.toString()?:"x"
        return "x"
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

    fun computeTrumpCardChoice(tableSide: TableSide): TrumpChoiceModel {
        val choosingPlayer = (playerGroup.getPlayer(tableSide) as PlayerKlaverjassen)
        val trumpColor = choosingPlayer.chooseTrumpColor()
        return executeTrumpCardChoice(trumpColor, tableSide)
    }

    fun executeTrumpCardChoice(trumpColor: CardColor, tableSide: TableSide): TrumpChoiceModel {
        (gameKlaverjassen.getCurrentRound() as RoundKlaverjassen).setTrumpColorAndContractOwner(trumpColor, tableSide)
        return TrumpChoiceModel(trumpColor, tableSide)
    }

    //======================================================================================================
    // added from GameMaster
    //======================================================================================================

    private fun isLegalCardToPlay(player: Player, card: Card): Boolean {
        val trickOnTable = gameKlaverjassen.getCurrentRound().getTrickOnTable()

        val legalCards = player
            .getCardsInHand()
            .legalPlayable(
                trickOnTable,
                (gameKlaverjassen.getCurrentRound() as RoundKlaverjassen).getTrumpColor()
            )
        return legalCards.contains(card)
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