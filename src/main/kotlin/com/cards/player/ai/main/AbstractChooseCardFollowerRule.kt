package com.cards.player.ai.main

import com.cards.player.Player

abstract class AbstractChooseCardFollowerRule(player: Player): AbstractChooseCardRule(player) {

    protected val leadColor = currentTrick.getLeadColor()!!
    protected val winningCard = currentTrick.getWinningCard()!!
    protected val winningSide = currentTrick.getWinningSide()!!

}

