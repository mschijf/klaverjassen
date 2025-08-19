package com.cards.controller.hearts

import com.cards.controller.basic.model.CardPlayedModel
import com.cards.controller.basic.model.CardPlayedResponse
import com.cards.controller.hearts.model.GameStatusModelHearts
import com.cards.controller.hearts.model.ScoreModelHearts
import com.cards.game.card.CardColor
import com.cards.game.card.CardRank
import com.cards.tools.Log
import org.springframework.web.bind.annotation.*

const val REQUESTPATH_BASE_HEARTS = "/api/v1/hearts/"

@RestController
@RequestMapping(REQUESTPATH_BASE_HEARTS)
class ControllerHearts(private val gameService: ServiceHearts) {

    @PostMapping("/new-game")
    fun newGame(): GameStatusModelHearts {
        return gameService.newGame()
    }

    @GetMapping("/game-status")
    fun getGameStatus(): GameStatusModelHearts {
        return gameService.getGameStatus()
    }

    @GetMapping("/score-list/")
    fun getScoreListKlaverjassen(): ScoreModelHearts {
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

    private fun createCardPlayResponse(response: CardPlayedModel?): CardPlayedResponse {
        return CardPlayedResponse(response!= null, response)
    }

    @GetMapping("/log")
    fun getLog(): String {
        return Log.get()
    }

}


