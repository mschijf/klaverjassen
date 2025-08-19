package com.cards.controller.hearts.model

data class ScoreModelHearts(val scoreList: List<RoundScoreHearts>)

data class RoundScoreHearts (val south: Int, val west: Int, val east: Int, val north: Int)