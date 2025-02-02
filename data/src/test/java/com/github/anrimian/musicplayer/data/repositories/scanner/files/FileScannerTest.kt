package com.github.anrimian.musicplayer.data.repositories.scanner.files

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSourceTags
import com.github.anrimian.musicplayer.domain.models.scanner.Idle
import com.github.anrimian.musicplayer.domain.models.scanner.Running
import com.github.anrimian.musicplayer.domain.repositories.StateRepository
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.TestScheduler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.*
import java.util.concurrent.TimeUnit

class FileScannerTest {

    private val compositionsDao: CompositionsDaoWrapper = mock()
    private val compositionSourceEditor: CompositionSourceEditor = mock()
    private val stateRepository: StateRepository = mock()
    private val analytics: Analytics = mock()
    private val scheduler = Schedulers.trampoline()

    private val fileScanner = FileScanner(
            compositionsDao,
            compositionSourceEditor,
            stateRepository,
            analytics,
            scheduler
    )

    private val testStateObserver = fileScanner.getStateObservable().test()

    @BeforeEach
    fun setUp() {
        whenever(stateRepository.currentFileScannerVersion).thenReturn(1)
        whenever(stateRepository.lastFileScannerVersion).thenReturn(1)

        val source: CompositionSourceTags = mock()
        whenever(compositionSourceEditor.getFullTags(any()))
            .thenReturn(Single.just(source))
    }

    @Test
    fun `run successful scan`() {
        val composition: FullComposition = mock()

        whenever(compositionsDao.selectNextCompositionToScan(eq(0)))
                .thenReturn(Maybe.just(composition))
                .thenReturn(Maybe.empty())

        fileScanner.scheduleFileScanner()

        verify(compositionsDao).setCompositionLastFileScanTime(any(), any())
        verify(stateRepository).lastFileScannerVersion = eq(1)
        verify(stateRepository).lastCompleteScanTime = any()

        testStateObserver.assertValues(
                Idle,
                Running(composition),
                Idle
        )
    }

    @Test
    fun `error with getting composition from db - do not run next loop`() {
        val exception: Exception = mock()
        whenever(compositionsDao.selectNextCompositionToScan(eq(0)))
                .thenReturn(Maybe.error(exception))
                .thenReturn(Maybe.just(mock<FullComposition>()))

        fileScanner.scheduleFileScanner()

        verify(compositionsDao, never()).setCompositionLastFileScanTime(any(), any())
        verify(stateRepository, never()).lastFileScannerVersion = any()
        verify(stateRepository, never()).lastCompleteScanTime = any()
        verify(analytics).processNonFatalError(exception)

        testStateObserver.assertValues(
                Idle
        )
    }

    @Test
    fun `error with scan - set scan time and run next loop`() {
        val composition1: FullComposition = mock()
        val composition2: FullComposition = mock()

        whenever(compositionsDao.selectNextCompositionToScan(eq(0)))
                .thenReturn(Maybe.just(composition1))
                .thenReturn(Maybe.just(composition2))
                .thenReturn(Maybe.empty())

        val exception = RuntimeException()
        Mockito.doThrow(exception)
            .doThrow(exception)
            .doThrow(exception)
            .doNothing()
            .whenever(compositionsDao).updateCompositionBySourceTags(any(), any())

        fileScanner.scheduleFileScanner()

        verify(compositionsDao, times(2)).setCompositionLastFileScanTime(any(), any())
        verify(analytics).processNonFatalError(exception)
        verify(stateRepository).lastFileScannerVersion = eq(1)
        verify(stateRepository).lastCompleteScanTime = any()

        testStateObserver.assertValues(
                Idle,
                Running(composition1),
                Running(composition2),
                Idle
        )
    }

    @Test
    fun `test file scanner version update`() {
        whenever(stateRepository.currentFileScannerVersion).thenReturn(2)
        val lastScanTime = 1000L
        whenever(stateRepository.lastCompleteScanTime).thenReturn(lastScanTime)

        val composition: FullComposition = mock()

        whenever(compositionsDao.selectNextCompositionToScan(any()))
            .thenReturn(Maybe.just(composition))
            .thenReturn(Maybe.empty())

        fileScanner.scheduleFileScanner()

        verify(compositionsDao, times(2)).selectNextCompositionToScan(eq(lastScanTime))
        verify(compositionsDao).setCompositionLastFileScanTime(eq(composition), any())
        verify(stateRepository).lastFileScannerVersion = eq(2)
        verify(stateRepository).lastCompleteScanTime = any()

        testStateObserver.assertValues(
            Idle,
            Running(composition),
            Idle
        )
    }

    @Test
    fun `test file read timeout`() {
        val testScheduler = TestScheduler()
        val fileScanner = FileScanner(
            compositionsDao,
            compositionSourceEditor,
            stateRepository,
            analytics,
            testScheduler
        )
        val testStateObserver = fileScanner.getStateObservable().test()

        val composition: FullComposition = mock()
        val source: CompositionSourceTags = mock()

        whenever(compositionsDao.selectNextCompositionToScan(eq(0)))
            .thenReturn(Maybe.just(composition))
            .thenReturn(Maybe.empty())
        whenever(compositionSourceEditor.getFullTags(any()))
            .thenReturn(Single.just(source).delay(3, TimeUnit.SECONDS, testScheduler))
            .thenReturn(Single.just(source).delay(3, TimeUnit.SECONDS, testScheduler))
            .thenReturn(Single.just(source))

        fileScanner.scheduleFileScanner()
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS)

        verify(compositionsDao).setCompositionLastFileScanTime(eq(composition), any())
        verify(compositionsDao).updateCompositionBySourceTags(eq(composition), eq(source))
        verify(stateRepository).lastFileScannerVersion = eq(1)
        verify(stateRepository).lastCompleteScanTime = any()

        testStateObserver.assertValues(
            Idle,
            Running(composition),
            Idle
        )

    }
}