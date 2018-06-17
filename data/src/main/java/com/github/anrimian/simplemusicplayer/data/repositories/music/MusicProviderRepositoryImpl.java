package com.github.anrimian.simplemusicplayer.data.repositories.music;

import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;
import com.google.android.exoplayer2.upstream.FileDataSource;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

/**
 * Created on 24.10.2017.
 */

public class MusicProviderRepositoryImpl implements MusicProviderRepository {

    private StorageMusicDataSource storageMusicDataSource;
    private Scheduler scheduler;

    public MusicProviderRepositoryImpl(StorageMusicDataSource storageMusicDataSource,
                                       Scheduler scheduler) {
        this.storageMusicDataSource = storageMusicDataSource;
        this.scheduler = scheduler;
    }

    @Override
    public Single<List<Composition>> getAllCompositions() {
        return storageMusicDataSource.getCompositions()
                .map(compositions -> (List<Composition>) new ArrayList(compositions.values()))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable processErrorWithComposition(Throwable throwable, Composition composition) {
        return processCompositionError(throwable, composition)//TODO write error about composition. Handle deleted compositions
                .subscribeOn(scheduler);
    }

    @Override
    public Completable deleteComposition(Composition composition) {
        return storageMusicDataSource.deleteComposition(composition);
    }

    private Completable processCompositionError(Throwable throwable, Composition composition) {
        if (throwable instanceof FileDataSource.FileDataSourceException) {
            throwable = throwable.getCause();
            if (throwable instanceof FileNotFoundException) {
                return storageMusicDataSource.deleteComposition(composition);
            }
        }
        return Completable.error(new IllegalStateException("unexpected error with composition: " + throwable));
    }
}
