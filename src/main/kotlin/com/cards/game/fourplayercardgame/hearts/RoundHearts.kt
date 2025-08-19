package com.cards.game.fourplayercardgame.hearts

import com.cards.game.fourplayercardgame.basic.Round

class RoundHearts() : Round() {

    //score
    fun getScore(): ScoreHearts {
        var score = ScoreHearts.ZERO
        if (isComplete()) {
            getTrickList().forEach { trick ->
                score = score.plus((trick as TrickHearts).getScore())
            }
        }
        return score
    }
}