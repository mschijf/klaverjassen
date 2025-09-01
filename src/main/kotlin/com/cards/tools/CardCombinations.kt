package com.cards.tools

import com.cards.game.card.Card

class CardCombinations {

    fun getPossibleCardCombinations(countPlayer1: Int, countPlayer2: Int, countPlayer3: Int,
                                    canHavePlayer1:Set<Card>, canHavePlayer2:Set<Card>, canHavePlayer3:Set<Card>,
                                    sureHasPlayer1:Set<Card>, sureHasPlayer2:Set<Card>, sureHasPlayer3:Set<Card>,): List<Triple<List<Card>, List<Card>, List<Card>>> {

        val listAll = (canHavePlayer1 + canHavePlayer2 + canHavePlayer3 + sureHasPlayer1 + sureHasPlayer2 + sureHasPlayer3).toList()
        val possible1 = canHavePlayer1.map{listAll.indexOf(it)}.toSet()
        val possible2 = canHavePlayer2.map{listAll.indexOf(it)}.toSet()
        val possible3 = canHavePlayer3.map{listAll.indexOf(it)}.toSet()
        val sure1 = sureHasPlayer1.map{listAll.indexOf(it)}.toSet()
        val sure2 = sureHasPlayer2.map{listAll.indexOf(it)}.toSet()
        val sure3 = sureHasPlayer3.map{listAll.indexOf(it)}.toSet()
        val indexResult = getPossibleCombinations(countPlayer1, countPlayer2, countPlayer3, possible1, possible2, possible3, sure1, sure2, sure3)
        return indexResult.map { indexCombi ->
            Triple(
            indexCombi.first.map { index -> listAll[index] },
            indexCombi.second.map { index -> listAll[index] },
            indexCombi.third.map { index -> listAll[index] }
            )
        }

    }

    fun getPossibleCombinations(countPlayer1: Int, countPlayer2: Int, countPlayer3: Int,
                                possible1: Set<Int>, possible2: Set<Int>, possible3: Set<Int>,
                                sure1: Set<Int>, sure2: Set<Int>, sure3: Set<Int>): List<Triple<List<Int>, List<Int>, List<Int>>> {

        return combinationsChatGPT(countPlayer1, countPlayer2, countPlayer3)
            .filter { combi ->
                combi.first.all{it in possible1+sure1} && sure1.all{it in combi.first} &&
                        combi.second.all{it in possible2+sure2} && sure2.all{it in combi.second} &&
                        combi.third.all{it in possible3+sure3} && sure3.all{it in combi.third}
            }

    }

    fun combinationsChatGPT(a: Int, b: Int, c: Int): List<Triple<List<Int>, List<Int>, List<Int>>> {
        if (cache.containsKey(Triple(a,b,c)))
            return cache[Triple(a,b,c)]!!

        val n = a + b + c
        val result = mutableListOf<Triple<List<Int>, List<Int>, List<Int>>>()
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
                val groupC = (remainingAfterA - groupB.toSet()).sorted()
                result.add(Triple(groupA.sorted(), groupB.sorted(), groupC))
            }
        }

        cache[Triple(a,b,c)] = result
        return result
    }

    companion object {
        private val cache = mutableMapOf<Triple<Int, Int, Int>, List<Triple<List<Int>, List<Int>, List<Int>>>>()
    }
}