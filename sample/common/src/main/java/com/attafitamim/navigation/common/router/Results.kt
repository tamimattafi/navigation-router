package com.attafitamim.navigation.common.router

import com.attafitamim.navigation.router.core.result.ResultKey

object Results {
    object OnResultKey : ResultKey<String>

    data class ComplexResult(
        val p1: String,
        val p2: String
    )

    object OnComplexResultKey : ResultKey<ComplexResult>
}