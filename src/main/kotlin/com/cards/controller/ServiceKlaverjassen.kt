package com.cards.controller

import com.cards.controller.model.*
import com.cards.game.card.Card
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import com.cards.game.klaverjassen.GAME_START_PLAYER
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
    private var game = Game()

    private fun createInitialPlayerList(): List<Player> {
        return listOf(
            GeniusPlayerKlaverjassen(TableSide.WEST, game),
            GeniusPlayerKlaverjassen(TableSide.NORTH, game),
            GeniusPlayerKlaverjassen(TableSide.EAST, game),
            GeniusPlayerKlaverjassen(TableSide.SOUTH, game),
        )
    }

    private var playerGroup = PlayerGroup(createInitialPlayerList())
        .also {
//            RANDOMIZER.setSeed(1426494019)
            it.dealCards()
        }

    fun newGame(): GameStatusModelKlaverjassen {
        game = Game()
        playerGroup = PlayerGroup(createInitialPlayerList())
        playerGroup.dealCards()
        return getGameStatus()
    }

    fun getGameStatus(): GameStatusModelKlaverjassen {
        val trickOnTable = if (game.newRoundToBeStarted()) {
            null
        } else {
            game.getCurrentRound().getTrickOnTable()
        }

        val cardsOnTable = TableModel(
            trickOnTable?.getCardPlayedBy(TableSide.SOUTH),
            trickOnTable?.getCardPlayedBy(TableSide.WEST),
            trickOnTable?.getCardPlayedBy(TableSide.NORTH),
            trickOnTable?.getCardPlayedBy(TableSide.EAST)
        )
        val sideToMove = game.getSideToMove()
        val sideToLead = game.getTrickLeadOrNull()?: game.getNewRoundLeadOrNull()

        val playerSouth = makePlayerCardListModel(TableSide.SOUTH)
        val playerNorth = makePlayerCardListModel(TableSide.NORTH)
        val playerWest = makePlayerCardListModel(TableSide.WEST)
        val playerEast = makePlayerCardListModel(TableSide.EAST)

        val gameJsonString = ""
        val newRoundToBeStarted = game.newRoundToBeStarted()

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
            trumpChoice = if (!game.newRoundToBeStarted())
                TrumpChoiceModel(
                    game.getCurrentRound().getTrumpColor(),
                    game.getCurrentRound().getContractOwningSide()
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
        val bc = if (geniusPlayerKlaverjassen.tableSide == TableSide.SOUTH && game.getSideToMove() == TableSide.SOUTH)
            geniusPlayerKlaverjassen.chooseCard()
        else
            null
        return if (card == bc) "X" else " "
    }

    fun computeMove(): CardPlayedModel? {
        val playerToMove = playerGroup.getPlayer(game.getSideToMove())
        val suggestedCardToPlay = playerToMove.chooseCard()
        return executeMove(suggestedCardToPlay.color, suggestedCardToPlay.rank)
    }

    fun executeMove(color: CardColor, rank: CardRank): CardPlayedModel? {
        val playerToMove = playerGroup.getPlayer(game.getSideToMove())
        val suggestedCardToPlay = Card(color, rank)
        if (!isLegalCardToPlay(playerToMove, suggestedCardToPlay)) {
            println("LOG.WARNING: try to play illegal card")
            return null
        }

        val cardsStillInHand = playerToMove.getNumberOfCardsInHand()

        val gameStatus = playCard(suggestedCardToPlay)

        val trickCompleted = if (gameStatus.trickFinished)
            TrickCompletedModel(
                game.getLastTrickWinner()!!,
                gameStatus.roundFinished,
                gameStatus.gameFinished,
            )
        else
            null

        val nextSideToPlay = if (gameStatus.gameFinished) GAME_START_PLAYER else game.getSideToMove()

        if (gameStatus.gameFinished)
            printGame()

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
            game.getAllScoresPerRound()
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
        game.startNewRound(trumpColor, tableSide)
//        return TrumpChoiceModel(trumpColor, tableSide)
        return getGameStatus()
    }

    fun TakeBackTrick() : GameStatusModelKlaverjassen {
        do {
            takeBackCard()
        } while (game.getSideToMove() != TableSide.SOUTH && !game.veryBeginning())
        return getGameStatus()
    }

    private fun Game.veryBeginning(): Boolean {
        return this.getCurrentRound().getTrickList().size == 1 && this.getCurrentRound().getTrickOnTable().hasNotStarted()
    }

    fun printGame() {
        println("Random seed  : ${RANDOMIZER.getLastSeedUsed()}")
        println("RoundLead    : ${game.getCurrentRound().getFirstTrickLead()}")
        println("ContractOwner: ${game.getCurrentRound().getContractOwningSide()}")
        println("Trump        : ${game.getCurrentRound().getTrumpColor()}")
        println("PlayerToMove : ${game.getCurrentRound().getTrickOnTable().getSideToPlay()}")
        println(game.getCurrentRound().getTrickList().flatMap { it.getCardsPlayed() })
    }

    //======================================================================================================
    // added from GameMaster
    //======================================================================================================

    private fun isLegalCardToPlay(player: Player, card: Card): Boolean {
        return player.getLegalPlayableCards().contains(card)
    }

    private fun playCard(card: Card): GameStatus {
        val playerToMove = playerGroup.getPlayer(game.getSideToMove())
        playerToMove.removeCard(card)
        val gameStatus = game.playCard(card)
        if (playerGroup.allEmptyHanded())
            playerGroup.dealCards()
        return gameStatus
    }

    private fun takeBackCard() {
        val card = game.takeLastCardBack()
        val playerToMove = playerGroup.getPlayer(game.getSideToMove())
        playerToMove.addCard(card)
    }


}