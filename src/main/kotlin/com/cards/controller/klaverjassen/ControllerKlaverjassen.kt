package com.cards.controller.klaverjassen

import com.cards.controller.basic.model.CardPlayedModel
import com.cards.controller.basic.model.CardPlayedResponse
import com.cards.controller.klaverjassen.model.GameStatusModelKlaverjassen
import com.cards.controller.klaverjassen.model.ScoreModelKlaverjassen
import com.cards.controller.klaverjassen.model.TrumpChoiceModel
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import com.cards.game.fourplayercardgame.basic.TableSide
import com.cards.tools.Log
import org.springframework.web.bind.annotation.*

const val REQUESTPATH_BASE_KLAVERJJASSEN = "/api/v1/klaverjassen/"

@RestController
@RequestMapping(REQUESTPATH_BASE_KLAVERJJASSEN)
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
    fun computeTrumpCardChoice(@PathVariable(name = "tablePosition") tableSide: TableSide): TrumpChoiceModel {
        return gameService.computeTrumpCardChoice(tableSide)
    }

    @PostMapping("/executeTrumpCardChoice/{cardColor}/{tablePosition}")
    fun executeTrumpCardChoice(
        @PathVariable(name = "cardColor") color: CardColor,
        @PathVariable(name = "tablePosition") tableSide: TableSide): TrumpChoiceModel {
        return gameService.executeTrumpCardChoice(color, tableSide)
    }


    private fun createCardPlayResponse(response: CardPlayedModel?): CardPlayedResponse {
        return CardPlayedResponse(response!= null, response)
    }

    @GetMapping("/log")
    fun getLog(): String {
        return Log.get()
    }

}


