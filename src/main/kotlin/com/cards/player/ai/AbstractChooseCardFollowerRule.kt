package com.cards.player.ai

import com.cards.player.Player

abstract class AbstractChooseCardFollowerRule(player: Player, brainDump: BrainDump): AbstractChooseCardRule(player, brainDump) {

    protected val leadColor = currentTrick.getLeadColor()!!
    protected val winningCard = currentTrick.getWinningCard()!!
    protected val winningSide = currentTrick.getWinningSide()!!

}

