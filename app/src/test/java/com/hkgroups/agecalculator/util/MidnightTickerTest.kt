package com.hkgroups.agecalculator.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MidnightTickerTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `first emission is the current date`() = runTest {
        val today = LocalDate.now()
        val first = MidnightTicker.flow().first()
        assertEquals(today, first)
    }
}
