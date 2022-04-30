package com.skylex_chess_clock.chessclock.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.skylex_chess_clock.chessclock.util.TimeMapper
import com.skylex_chess_clock.chessclock.util.TopLevelFiles.Companion.ClockMode
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class UserPreferencesRepoTest {

    companion object {
        private const val TEST_DATASTORE_NAME = "test_datastore"
    }

    private val testContext: Context = ApplicationProvider.getApplicationContext()
    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testCoroutineDispatcher + Job())

    private val testDataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = testCoroutineScope,
            produceFile = { testContext.preferencesDataStoreFile(TEST_DATASTORE_NAME) }
        )

    private val sut = UserPreferencesRepo(testDataStore)

    // MockData
    private val defaultPlayerTime = TimeMapper(5, TimeUnit.MINUTES)
    private val defaultTimeIncrement = TimeMapper(2, TimeUnit.SECONDS)
    private val defaultClockMode = ClockMode.SUDDEN_DEATH.name

    private val expectedInitialPreferences = UserPreferences(
        defaultPlayerTime,
        defaultTimeIncrement,
        ClockMode.valueOf(defaultClockMode)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testCoroutineDispatcher)
    }

    @Test
    fun verify_initial_fetch() = runTest {
        assertEquals(
            expectedInitialPreferences,
            sut.fetchInitialPreferences()
        )
    }

    @Test
    fun verify_updateUserSettings() = runTest {
        sut.updateUserSettings(
            clockMode = expectedInitialPreferences.clockMode,
            time = expectedInitialPreferences.time,
            increment = expectedInitialPreferences.increment
        )
        assertEquals(
            expectedInitialPreferences,
            sut.userPreferencesFlow.first()
        )
    }

    @Test
    fun verify_updateClockMode() = runTest {
        val expectedClockMode = ClockMode.HOURGLASS
        sut.updateClockMode(expectedClockMode)
        assertEquals(
            expectedClockMode,
            sut.userPreferencesFlow.first().clockMode
        )
    }

    @Test
    fun verify_updateClockTime() = runTest {
        val expectedClockTime = TimeMapper(2, TimeUnit.DAYS)
        sut.updateClockTime(expectedClockTime)
        assertEquals(
            expectedClockTime,
            sut.userPreferencesFlow.first().time
        )
    }

    @Test
    fun verify_updateTimeIncrement() = runTest {
        val expectedTimeIncrement = TimeMapper(2, TimeUnit.DAYS)
        sut.updateTimeIncrement(expectedTimeIncrement)
        assertEquals(
            expectedTimeIncrement,
            sut.userPreferencesFlow.first().increment
        )
    }

    @After
    fun cleanUp() {
        Dispatchers.resetMain()
        testCoroutineDispatcher.cleanupTestCoroutines()
        testCoroutineScope.runBlockingTest {
            testDataStore.edit { it.clear() }
        }
        testCoroutineScope.cancel()
    }

}