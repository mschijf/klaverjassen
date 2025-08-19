package com.cards.controller.model

data class ScoreModelKlaverjassen(val scoreList: List<RoundScoreKlaverjassen>)

data class RoundScoreKlaverjassen (
    val northSouthPoints: String,
    val eastWestPoints: String,
    val northSouthBonus: String,
    val eastWestBonus: String)