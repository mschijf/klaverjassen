package com.cards.controller

import com.cards.controller.model.CardPlayedModel
import com.cards.controller.model.CardPlayedResponse
import com.cards.controller.model.GameStatusModelKlaverjassen
import com.cards.controller.model.ScoreModelKlaverjassen
import com.cards.controller.model.TrumpChoiceModel
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import com.cards.game.klaverjassen.TableSide
import com.cards.tools.Log
import org.springframework.web.bind.annotation.*

const val REQUEST_PATH_BASE_KLAVERJASSEN = "/api/v1/klaverjassen/"

@RestController
@RequestMapping(REQUEST_PATH_BASE_KLAVERJASSEN)
class ControllerKlaverjassen(private val gameService: ServiceKlaverjassen) {

    @PostMapping("/new-game")
    fun newGame(): GameStatusModelKlaverjassen {
        return gameService.newGame()
    }

    @GetMapping("/game-status")
    fun getGameStatus(): GameStatusModelKlaverjassen {
        return gameService.getGameStatus()
    }

    @GetMapping("/score-list/")
    fun getScoreListKlaverjassen(): ScoreModelKlaverjassen {
        return gameService.getScoreCard()
    }

    @PostMapping("/computeMove")
    fun computeMove(): CardPlayedResponse {
        return createCardPlayResponse(gameService.computeMove())
    }

    @PostMapping("/executeMove/{color}/{rank}")
    fun executeMove(@PathVariable(name = "color") color: CardColor,
                    @PathVariable(name = "rank") rank: CardRank): CardPlayedResponse {
        return createCardPlayResponse(gameService.executeMove(color, rank))
    }

    @PostMapping("/computeTrumpCardChoice/{tablePosition}")
    fun computeTrumpCardChoice(@PathVariable(name = "tablePosition") tableSide: TableSide): GameStatusModelKlaverjassen {
        return gameService.computeTrumpCardChoice(tableSide)
    }

    @PostMapping("/executeTrumpCardChoice/{cardColor}/{tablePosition}")
    fun executeTrumpCardChoice(
        @PathVariable(name = "cardColor") color: CardColor,
        @PathVariable(name = "tablePosition") tableSide: TableSide): GameStatusModelKlaverjassen {
        return gameService.executeTrumpCardChoice(color, tableSide)
    }

    @PostMapping("/takeBackTrick/")
    fun requestTakeBackTrick(): GameStatusModelKlaverjassen {
        return gameService.TakeBackTrick()
    }

    private fun createCardPlayResponse(response: CardPlayedModel?): CardPlayedResponse {
        return CardPlayedResponse(response!= null, response)
    }

    @GetMapping("/log")
    fun getLog(): String {
        return Log.get()
    }

    @PostMapping("/printgame")
    fun printGame() {
        return gameService.printGame()
    }

    @GetMapping("/asynctest")
    fun asynctest():  TrumpChoiceModel {
        println("START")
        Thread.sleep(5_000)
        println("STOP")
        return TrumpChoiceModel(CardColor.DIAMONDS, TableSide.NORTH)
    }

}


