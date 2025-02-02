package com.github.anrimian.musicplayer.data.controllers.music.players;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.utils.functions.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.PublishSubject;
import utils.TestDataProvider;

public class CompositeMediaPlayerTest {

    private final AppMediaPlayer player1 = mock(AppMediaPlayer.class);
    private final AppMediaPlayer player2 = mock(AppMediaPlayer.class);

    private CompositeMediaPlayer compositeMediaPlayer;

    private final InOrder inOrder = Mockito.inOrder(player1, player2);

    private final PublishSubject<PlayerEvent> player1EventSubject = PublishSubject.create();
    private final PublishSubject<PlayerEvent> player2EventSubject = PublishSubject.create();

    private final PublishSubject<Long> player1PositionSubject = PublishSubject.create();
    private final PublishSubject<Long> player2PositionSubject = PublishSubject.create();

    @BeforeEach
    public void setUp() {
        when(player1.getTrackPositionObservable()).thenReturn(player1PositionSubject);
        when(player1.getEventsObservable()).thenReturn(player1EventSubject);
        when(player1.getSpeedChangeAvailableObservable()).thenReturn(Observable.just(true));


        when(player2.getTrackPositionObservable()).thenReturn(player2PositionSubject);
        when(player2.getEventsObservable()).thenReturn(player2EventSubject);
        when(player2.getSpeedChangeAvailableObservable()).thenReturn(Observable.just(true));

        ArrayList<Function<AppMediaPlayer>> players = new ArrayList<>();
        players.add(() -> player1);
        players.add(() -> player2);
        compositeMediaPlayer = new CompositeMediaPlayer(players);
    }

    @Test
    public void testPlayersSwitch() {
        CompositionSource composition = TestDataProvider.fakeCompositionSource(0);

        compositeMediaPlayer.prepareToPlay(composition, 0L, null);
        inOrder.verify(player1).prepareToPlay(eq(composition), eq(0L),  eq(null));

        player1EventSubject.onNext(new ErrorEvent(ErrorType.UNSUPPORTED, composition));
        inOrder.verify(player1).release();
        inOrder.verify(player2).prepareToPlay(eq(composition), eq(0L),  eq(ErrorType.UNSUPPORTED));
    }

    @Test
    public void testAllPlayersNotWorking() {
        CompositionSource composition = TestDataProvider.fakeCompositionSource(0);

        TestObserver<PlayerEvent> eventsObserver = compositeMediaPlayer.getEventsObservable().test();

        compositeMediaPlayer.prepareToPlay(composition, 0L, null);
        inOrder.verify(player1).prepareToPlay(eq(composition), eq(0L),  eq(null));

        player1EventSubject.onNext(new ErrorEvent(ErrorType.UNSUPPORTED, composition));
        inOrder.verify(player1).release();
        inOrder.verify(player2).prepareToPlay(eq(composition), eq(0L),  eq(ErrorType.UNSUPPORTED));

        player2EventSubject.onNext(new ErrorEvent(ErrorType.UNSUPPORTED, composition));

        eventsObserver.assertValue(new ErrorEvent(ErrorType.UNSUPPORTED, composition));

        CompositionSource composition2 = TestDataProvider.fakeCompositionSource(2);

        compositeMediaPlayer.prepareToPlay(composition2, 0L, null);
        inOrder.verify(player2).release();
        inOrder.verify(player1).prepareToPlay(eq(composition2), eq(0L),  eq(null));
    }

    @Test
    public void testPlayersSwitchWithPosition() {
        CompositionSource composition = TestDataProvider.fakeCompositionSource(0);

        compositeMediaPlayer.prepareToPlay(composition, 0L, null);
        inOrder.verify(player1).prepareToPlay(eq(composition), eq(0L), eq(null));

        player1PositionSubject.onNext(100L);

        player1EventSubject.onNext(new ErrorEvent(ErrorType.UNSUPPORTED, composition));
        inOrder.verify(player1).release();
        inOrder.verify(player2).prepareToPlay(eq(composition), eq(100L), eq(ErrorType.UNSUPPORTED));
    }
}