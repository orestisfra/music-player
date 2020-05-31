package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;

import java.util.HashMap;

import io.reactivex.Observable;

public class PlayerCoordinatorInteractor {

    private final PlayerInteractor playerInteractor;

    private PlayerType activePlayerType = PlayerType.LIBRARY;
    private HashMap<PlayerType, CompositionSource> preparedSourcesMap = new HashMap<>();

    //set and unset active state - ok
    //filter player state - ok
    //filter seekbar state - ok

    //play from inactive player - ok, but position lost if we changed it after prepare

    //"skip to from" inactive player - ok, but position blinking
    //"seek to" inactive player - seek and save, but do not affect current composition
    //filter player events
    public PlayerCoordinatorInteractor(PlayerInteractor playerInteractor) {
        this.playerInteractor = playerInteractor;
    }

    public void startPlaying(CompositionSource compositionSource, PlayerType playerType) {
        if (activePlayerType != playerType) {
            playerInteractor.pause();
        }
        activePlayerType = playerType;
        playerInteractor.startPlaying(compositionSource);
    }

    public void play(PlayerType playerType) {
        if (activePlayerType != playerType) {
            playerInteractor.pause();
            CompositionSource source = preparedSourcesMap.get(playerType);
            if (source != null) {
                playerInteractor.prepareToPlay(source);
            }
        }
        activePlayerType = playerType;
        playerInteractor.play();
    }

    public void playOrPause(PlayerType playerType) {
        if (activePlayerType != playerType) {
            playerInteractor.pause();
            CompositionSource source = preparedSourcesMap.get(playerType);
            if (source != null) {
                playerInteractor.prepareToPlay(source);
            }
        }
        activePlayerType = playerType;
        playerInteractor.playOrPause();
    }

    public void stop(PlayerType playerType) {
        if (playerType == activePlayerType) {
            playerInteractor.stop();
        }
    }

    public void pause(PlayerType playerType) {
        if (playerType == activePlayerType) {
            playerInteractor.pause();
        }
    }

    //blink sound from previous composition
    public void prepareToPlay(CompositionSource compositionSource, PlayerType playerType) {
        preparedSourcesMap.put(playerType, compositionSource);
        if (playerType == activePlayerType) {
            playerInteractor.prepareToPlay(compositionSource);
        }
    }

    public Observable<Long> getTrackPositionObservable(PlayerType playerType) {
        return playerInteractor.getTrackPositionObservable()
                .filter(o -> isPlayerTypeActive(playerType));
    }

    public Observable<PlayerState> getPlayerStateObservable(PlayerType playerType) {
        return playerInteractor.getPlayerStateObservable()
                .map(state -> isPlayerTypeActive(playerType)? state : PlayerState.PAUSE);
    }

    public boolean isPlayerTypeActive(PlayerType playerType) {
        return activePlayerType == playerType;
    }

    public PlayerState getPlayerState(PlayerType playerType) {
        return isPlayerTypeActive(playerType)? playerInteractor.getPlayerState() : PlayerState.PAUSE;
    }
}
