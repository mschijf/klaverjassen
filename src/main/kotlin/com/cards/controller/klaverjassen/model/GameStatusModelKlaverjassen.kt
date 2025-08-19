package com.cards.controller.klaverjassen.model

import com.cards.controller.basic.model.GameStatusModel

data class GameStatusModelKlaverjassen(
    val generic: GameStatusModel,
    val trumpChoice: TrumpChoiceModel,
)
