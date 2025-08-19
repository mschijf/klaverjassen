package com.cards.game.card

object CARDDECK {

    val baseDeckCardsSevenAndHigher =
        CardColor.values()
            .flatMap{color ->
                CardRank.values()
                    .filter{cardRank -> cardRank.rankNumber >= 7}
                    .map{rank -> Card(color, rank)}
        }

    val baseDeckCardsSixAndLower =
        CardColor.values()
            .flatMap{color ->
                CardRank.values()
                    .filter{cardRank -> cardRank.rankNumber <= 6}
                    .map{rank -> Card(color, rank)}
        }

    val baseDeckCardsAll =
        CardColor.values()
            .flatMap{color ->
                CardRank.values().map{rank -> Card(color, rank)}
            }
}