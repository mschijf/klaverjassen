package com.cards.game.klaverjassen.basic

data class GameStatus (val gameFinished: Boolean, val roundFinished: Boolean, val trickFinished: Boolean) {
    init {
        assert(if (gameFinished) roundFinished && trickFinished else true)
        assert(if (roundFinished) trickFinished else true)
    }
}