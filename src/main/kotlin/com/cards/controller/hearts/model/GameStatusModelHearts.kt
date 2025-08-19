package com.cards.controller.hearts.model

import com.cards.controller.basic.model.GameStatusModel

data class GameStatusModelHearts(
    val generic: GameStatusModel,
    val goingUp: Boolean,
)
