package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

//fade out when timer is finishing(?)
//handle 'play last song' option
//states: enable/disable/paused/disable_wait_for_finish
public class SleepTimerInteractor {

    public static final long NO_TIMER = 1;

    private final LibraryPlayerInteractor libraryPlayerInteractor;
    private final SettingsRepository settingsRepository;
    private final UiStateRepository uiStateRepository;

    private final BehaviorSubject<Long> timerCountDownSubject = BehaviorSubject.createDefault(NO_TIMER);
    private final BehaviorSubject<SleepTimerState> sleepTimerState = BehaviorSubject.createDefault(SleepTimerState.DISABLED);

    private Disposable timerDisposable;

    public SleepTimerInteractor(LibraryPlayerInteractor libraryPlayerInteractor,
                                SettingsRepository settingsRepository,
                                UiStateRepository uiStateRepository) {
        this.libraryPlayerInteractor = libraryPlayerInteractor;
        this.settingsRepository = settingsRepository;
        this.uiStateRepository = uiStateRepository;
    }

    public void start() {
        startSleepTimer(settingsRepository.getSleepTimerTime());
        sleepTimerState.onNext(SleepTimerState.ENABLED);
    }

    public void stop() {
        pause();
        uiStateRepository.setSleepTimerRemainingMillis(0L);
        timerCountDownSubject.onNext(NO_TIMER);
        sleepTimerState.onNext(SleepTimerState.DISABLED);
    }

    public void pause() {
        if (timerDisposable != null) {
            timerDisposable.dispose();
        }
        sleepTimerState.onNext(SleepTimerState.PAUSED);
    }

    public void resume() {
        startSleepTimer(uiStateRepository.getSleepTimerRemainingMillis());
        sleepTimerState.onNext(SleepTimerState.ENABLED);
    }

    public Observable<Long> getSleepTimerCountDownObservable() {
        return timerCountDownSubject;
    }

    public Observable<SleepTimerState> getSleepTimerStateObservable() {
        return sleepTimerState;
    }

    public void setPlayLastSong(boolean playLastSong) {
        settingsRepository.setSleepTimerPlayLastSong(playLastSong);
    }

    public void setSleepTimerTime(long millis) {
        settingsRepository.setSleepTimerTime(millis);
    }

    private void startSleepTimer(long timeMillis) {
        long remainingSeconds = timeMillis / 1000L;
        timerDisposable = Observable.interval( 1, TimeUnit.SECONDS)
                .map(seconds -> remainingSeconds - seconds)
                .doOnNext(timerCountDownSubject::onNext)
                .takeUntil(seconds -> seconds <= 0)
                .doOnDispose(() -> uiStateRepository.setSleepTimerRemainingMillis(remainingSeconds))
                .doOnComplete(this::onTimerFinished)
                .subscribe();
    }

    private void onTimerFinished() {
        libraryPlayerInteractor.pause();
        timerCountDownSubject.onNext(NO_TIMER);
        sleepTimerState.onNext(SleepTimerState.DISABLED);
    }

    public enum SleepTimerState {
        ENABLED,
        DISABLED,
        PAUSED
    }
}
