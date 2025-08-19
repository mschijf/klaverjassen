package com.cards.controller.klaverjassen.model

data class ScoreModelKlaverjassen(val scoreList: List<RoundScoreKlaverjassen>)

data class RoundScoreKlaverjassen (
    val northSouthPoints: String,
    val eastWestPoints: String,
    val northSouthBonus: String,
    val eastWestBonus: String)