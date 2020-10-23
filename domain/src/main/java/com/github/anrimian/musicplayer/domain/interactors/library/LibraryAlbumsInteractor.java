package com.github.anrimian.musicplayer.domain.interactors.library;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class LibraryAlbumsInteractor {

    private final LibraryRepository musicProviderRepository;
    private final EditorRepository editorRepository;
    private final SettingsRepository settingsRepository;
    private final UiStateRepository uiStateRepository;

    public LibraryAlbumsInteractor(LibraryRepository musicProviderRepository,
                                   EditorRepository editorRepository,
                                   SettingsRepository settingsRepository,
                                   UiStateRepository uiStateRepository) {
        this.musicProviderRepository = musicProviderRepository;
        this.editorRepository = editorRepository;
        this.settingsRepository = settingsRepository;
        this.uiStateRepository = uiStateRepository;
    }

    public Observable<List<Album>> getAlbumsObservable(@Nullable String searchText) {
        return musicProviderRepository.getAlbumsObservable(searchText);
    }

    public Observable<List<Composition>> getAlbumItemsObservable(long albumId) {
        return musicProviderRepository.getAlbumItemsObservable(albumId);
    }

    public Observable<Album> getAlbumObservable(long albumId) {
        return musicProviderRepository.getAlbumObservable(albumId);
    }

    public Completable updateAlbumName(String name, long albumId) {
        return editorRepository.updateAlbumName(name, albumId);
    }

    public void setOrder(Order order) {
        settingsRepository.setAlbumsOrder(order);
    }

    public Order getOrder() {
        return settingsRepository.getAlbumsOrder();
    }

    public void saveListPosition(ListPosition listPosition) {
        uiStateRepository.saveAlbumsListPosition(listPosition);
    }

    public ListPosition getSavedListPosition() {
        return uiStateRepository.getSavedAlbumsListPosition();
    }
}
