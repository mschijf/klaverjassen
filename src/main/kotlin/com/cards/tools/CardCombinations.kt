package com.cards.tools

import com.cards.game.card.Card

class CardCombinations {
    fun getPosssibleCardCombinations(countPlayer1: Int, countPlayer2: Int, countPlayer3: Int,
                                     possiblePLayer1:Set<Card>, possiblePLayer2:Set<Card>, possiblePLayer3:Set<Card> ): List<List<Card>> {
        val listAll = (possiblePLayer1 + possiblePLayer2 + possiblePLayer3).toList()
        val possible1 = possiblePLayer1.map{listAll.indexOf(it)}.toSet()
        val possible2 = possiblePLayer1.map{listAll.indexOf(it)}.toSet()
        val possible3 = possiblePLayer1.map{listAll.indexOf(it)}.toSet()
        val indexResult = getPossibleCombinations(countPlayer1, countPlayer2, countPlayer3, possible1, possible2, possible3)
        return indexResult.map { indexCombi -> indexCombi.map { index -> listAll[index] } }
    }

    fun getPossibleCombinations(countPlayer1: Int, countPlayer2: Int, countPlayer3: Int, possible1: Set<Int>, possible2: Set<Int>, possible3: Set<Int>): List<List<Int>> {
        return combinationsChatGPT(countPlayer1, countPlayer2, countPlayer3)
            .filter { combi -> combi.subList(0, countPlayer1).all{it in possible1} &&
                        combi.subList(countPlayer1, countPlayer1+countPlayer2).all{it in possible2} &&
                        combi.subList(countPlayer1+countPlayer2, countPlayer1+countPlayer2+countPlayer3).all{it in possible3}
            }

    }

    private fun combinationsChatGPT(a: Int, b: Int, c: Int): List<List<Int>> {
        val n = a + b + c

        val result = mutableListOf<List<Int>>()
        val elements = (0 until n).toList()

        // helper: generate all k-combinations from a list
        fun <T> choose(list: List<T>, k: Int, start: Int = 0, prefix: List<T> = emptyList(), acc: MutableList<List<T>> = mutableListOf()): List<List<T>> {
            if (prefix.size == k) {
                acc.add(prefix)
                return acc
            }
            for (i in start until list.size) {
                choose(list, k, i + 1, prefix + list[i], acc)
            }
            return acc
        }

        for (groupA in choose(elements, a)) {
            val remainingAfterA = elements - groupA.toSet()
            for (groupB in choose(remainingAfterA, b)) {
                val groupC = (remainingAfterA - groupB.toSet())
                result.add(groupA + groupB + groupC)
            }
        }

        return result
    }
}