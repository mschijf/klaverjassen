package com.cards.tools

import kotlin.math.max
import kotlin.math.min

//calculate  (  n  )
//           (  k  )
fun combinations(n: Int, k: Int): Long {
    if (n < k) return -1
    var result = (max(k, n-k) + 1L .. n).reduceOrNull { acc, i ->  acc * i} ?:1
    (min(k, n-k) downTo 1).forEach {
        result /= it
    }
    return result
}

private val combinationCache: List<List<Long>> =
    (0..24).map{ up ->
        (0..24).map{ down ->
            if (up >= down) combinations(up, down) else -1L
        }
    }

fun cardCombinations(n: Int, k: Int): Long {
    return combinationCache[n][k]
}

