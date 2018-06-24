package com.github.anrimian.simplemusicplayer.ui.player_screens.player_screen;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created on 02.11.2017.
 */

@InjectViewState
public class PlayerPresenter extends MvpPresenter<PlayerView> {

    private final MusicPlayerInteractor musicPlayerInteractor;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private Disposable trackStateDisposable;
    private Disposable currentCompositionDisposable;
    private Disposable playQueueChangeDisposable;

    private final List<Composition> playQueue = new ArrayList<>();
    private Composition composition;

    public PlayerPresenter(MusicPlayerInteractor musicPlayerInteractor,
                           Scheduler uiScheduler) {
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().bindPlayList(playQueue);
        getViewState().showInfinitePlayingButton(musicPlayerInteractor.isInfinitePlayingEnabled());
        getViewState().showRandomPlayingButton(musicPlayerInteractor.isRandomPlayingEnabled());
    }

    void onStart() {
        subscribeOnPlayerStateChanges();
        subscribeOnCurrentPlaylistChanging();
    }

    void onStop() {
        presenterDisposable.clear();
        if (trackStateDisposable != null) {
            trackStateDisposable.dispose();
        }
    }

    void onPlayButtonClicked() {
        musicPlayerInteractor.play();
    }

    void onStopButtonClicked() {
        musicPlayerInteractor.pause();
    }

    void onSkipToPreviousButtonClicked() {
        musicPlayerInteractor.skipToPrevious();
    }

    void onSkipToNextButtonClicked() {
        musicPlayerInteractor.skipToNext();
    }

    void onEnableInfinitePlayingButtonClicked() {
        musicPlayerInteractor.setInfinitePlayingEnabled(true);
        getViewState().showInfinitePlayingButton(true);
    }

    void onDisableInfinitePlayingButtonClicked() {
        musicPlayerInteractor.setInfinitePlayingEnabled(false);
        getViewState().showInfinitePlayingButton(false);
    }

    void onEnableRandomPlayingButtonClicked() {
        musicPlayerInteractor.setRandomPlayingEnabled(true);
        getViewState().showRandomPlayingButton(true);
    }

    void onDisableRandomPlayingButtonClicked() {
        musicPlayerInteractor.setRandomPlayingEnabled(false);
        getViewState().showRandomPlayingButton(false);
    }

    void onShareCompositionButtonClicked() {
        getViewState().showShareMusicDialog(composition.getFilePath());
    }

    void onCompositionItemClicked(Composition composition) {
        musicPlayerInteractor.skipToPosition(playQueue.indexOf(composition));//TODO optimise later
    }

    void onTrackRewoundTo(int progress) {
        long position = composition.getDuration() * progress / 100;
        musicPlayerInteractor.seekTo(position);
    }

    void onDeleteCompositionButtonClicked() {
        musicPlayerInteractor.deleteComposition(composition)
                .observeOn(uiScheduler)
                .subscribe();//TODO displayError
    }

    public void onSeekStart() {
        musicPlayerInteractor.onSeekStarted();
    }

    public void onSeekStop(int progress) {
        long position = composition.getDuration() * progress / 100;
        musicPlayerInteractor.onSeekFinished(position);
    }

    private void subscribeOnCurrentCompositionChanging() {
        currentCompositionDisposable = musicPlayerInteractor.getCurrentCompositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onCurrentCompositionChanged);
        presenterDisposable.add(currentCompositionDisposable);
    }

    private void onCurrentCompositionChanged(CurrentComposition currentComposition) {
        composition = currentComposition.getComposition();
        if (trackStateDisposable != null) {
            trackStateDisposable.dispose();
            trackStateDisposable = null;
        }

        getViewState().showCurrentComposition(composition, currentComposition.getQueuePosition());
        getViewState().showTrackState(currentComposition.getPlayPosition(), composition.getDuration());
        subscribeOnTrackPositionChanging();
    }

    private void subscribeOnPlayerStateChanges() {
        presenterDisposable.add(musicPlayerInteractor.getPlayerStateObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayerStateChanged));
    }

    private void onPlayerStateChanged(PlayerState playerState) {
        switch (playerState) {
            case PLAY: {
                getViewState().showPlayState();
                return;
            }
            default: {
                getViewState().showStopState();
            }
        }
    }

    private void subscribeOnCurrentPlaylistChanging() {
        presenterDisposable.add(musicPlayerInteractor.getPlayQueueObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayListChanged));
    }

    private void onPlayListChanged(List<Composition> newPlayQueue) {
        if (currentCompositionDisposable != null) {
            currentCompositionDisposable.dispose();
            currentCompositionDisposable = null;
            composition = null;
        }
        if (trackStateDisposable != null) {
            trackStateDisposable.dispose();
            trackStateDisposable = null;
        }

        List<Composition> oldPlayList = new ArrayList<>(playQueue);
        playQueue.clear();
        playQueue.addAll(newPlayQueue);

        getViewState().updatePlayQueue(oldPlayList, playQueue);
        getViewState().showMusicControls(!playQueue.isEmpty());

        subscribeOnCurrentCompositionChanging();
        subscribeOnPlayQueueChanged();
    }

    private void subscribeOnPlayQueueChanged() {
        if (playQueueChangeDisposable != null) {
            playQueueChangeDisposable.dispose();//TODO add to presenter disposable later
        }
        playQueueChangeDisposable = musicPlayerInteractor.getPlayQueueChangeObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayQueueChanged);
    }

    private void onPlayQueueChanged(Change<List<Composition>> change) {
        List<Composition> compositions = change.getData();
        switch (change.getChangeType()) {
            case DELETED: {
                for (Composition composition: compositions) {
                    int index = playQueue.indexOf(composition);
                    if (index != -1) {
                        playQueue.remove(index);
                        getViewState().notifyPlayQueueItemRemoved(index);

                        if (playQueue.isEmpty()) {
                            getViewState().showMusicControls(false);
                        }
                    }
                }
            }
        }
    }

    private void subscribeOnTrackPositionChanging() {
        trackStateDisposable = musicPlayerInteractor.getTrackPositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onTrackPositionChanged);
    }

    private void onTrackPositionChanged(Long currentPosition) {
        long duration = composition.getDuration();
        getViewState().showTrackState(currentPosition, duration);
    }
}