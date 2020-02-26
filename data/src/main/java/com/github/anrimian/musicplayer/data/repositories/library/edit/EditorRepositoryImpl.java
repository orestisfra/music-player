package com.github.anrimian.musicplayer.data.repositories.library.edit;

import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.AlbumAlreadyExistsException;
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.ArtistAlreadyExistsException;
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.DuplicateFolderNamesException;
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.FileExistsException;
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.GenreAlreadyExistsException;
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.MoveFolderToItselfException;
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.MoveInTheSameFolderException;
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.artist.StorageArtistsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenresProvider;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSourceTags;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.StateRepository;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

import static com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource.renameFile;
import static com.github.anrimian.musicplayer.domain.Constants.TRIGGER;

public class EditorRepositoryImpl implements EditorRepository {

    private final CompositionSourceEditor sourceEditor = new CompositionSourceEditor();

    private final StorageMusicDataSource storageMusicDataSource;
    private final StorageFilesDataSource filesDataSource;
    private final CompositionsDaoWrapper compositionsDao;
    private final AlbumsDaoWrapper albumsDao;
    private final ArtistsDaoWrapper artistsDao;
    private final GenresDaoWrapper genresDao;
    private final FoldersDaoWrapper foldersDao;
    private final StorageMusicProvider storageMusicProvider;
    private final StorageGenresProvider storageGenresProvider;
    private final StorageArtistsProvider storageArtistsProvider;
    private final StorageAlbumsProvider storageAlbumsProvider;
    private final StateRepository stateRepository;
    private final Scheduler scheduler;

    public EditorRepositoryImpl(StorageMusicDataSource storageMusicDataSource,
                                StorageFilesDataSource filesDataSource,
                                CompositionsDaoWrapper compositionsDao,
                                AlbumsDaoWrapper albumsDao,
                                ArtistsDaoWrapper artistsDao,
                                GenresDaoWrapper genresDao,
                                FoldersDaoWrapper foldersDao,
                                StorageMusicProvider storageMusicProvider,
                                StorageGenresProvider storageGenresProvider,
                                StorageArtistsProvider storageArtistsProvider,
                                StorageAlbumsProvider storageAlbumsProvider,
                                StateRepository stateRepository,
                                Scheduler scheduler) {
        this.storageMusicDataSource = storageMusicDataSource;
        this.filesDataSource = filesDataSource;
        this.compositionsDao = compositionsDao;
        this.albumsDao = albumsDao;
        this.artistsDao = artistsDao;
        this.genresDao = genresDao;
        this.foldersDao = foldersDao;
        this.storageMusicProvider = storageMusicProvider;
        this.storageGenresProvider = storageGenresProvider;
        this.storageArtistsProvider = storageArtistsProvider;
        this.storageAlbumsProvider = storageAlbumsProvider;
        this.stateRepository = stateRepository;
        this.scheduler = scheduler;
    }

    /*
    rename genre - working
    add genre - working( no:( )
    change genre
    remove genre
    update\change album artist
    update genre name - not sure

    ******
    Seems, jaudiotagger names 'album-artist' and 'genre' differently than android media scanner

    We can add genre, but can't delete?

    Scan genres once?
     */

    @Override
    public Completable changeCompositionGenre(FullComposition composition,
                                              ShortGenre oldGenre,
                                              String newGenre) {
        return sourceEditor.changeCompositionGenre(composition.getFilePath(), oldGenre.getName(), newGenre)
                .doOnComplete(() -> {
                    genresDao.changeCompositionGenre(composition.getId(), oldGenre.getId(), newGenre);
                    storageMusicProvider.scanMedia(composition.getFilePath());
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable addCompositionGenre(FullComposition composition,
                                           String newGenre) {
        return sourceEditor.addCompositionGenre(composition.getFilePath(), newGenre)
                .doOnComplete(() -> {
                    genresDao.addCompositionToGenre(composition.getId(), newGenre);
                    storageMusicProvider.scanMedia(composition.getFilePath());
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable removeCompositionGenre(FullComposition composition, ShortGenre genre) {
        return sourceEditor.removeCompositionGenre(composition.getFilePath(), genre.getName())
                .doOnComplete(() -> {
                    genresDao.removeCompositionFromGenre(composition.getId(), genre.getId());
                    storageMusicProvider.scanMedia(composition.getFilePath());
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionAuthor(FullComposition composition, String newAuthor) {
        return sourceEditor.setCompositionAuthor(composition.getFilePath(), newAuthor)
                .doOnComplete(() -> {
                    compositionsDao.updateArtist(composition.getId(), newAuthor);
                    storageMusicProvider.scanMedia(composition.getFilePath());
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionAlbumArtist(FullComposition composition, String newAuthor) {
        return sourceEditor.setCompositionAlbumArtist(composition.getFilePath(), newAuthor)
                .doOnComplete(() -> {
                    compositionsDao.updateAlbumArtist(composition.getId(), newAuthor);
                    storageMusicProvider.scanMedia(composition.getFilePath());
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionAlbum(FullComposition composition, String newAlbum) {
        return sourceEditor.setCompositionAlbum(composition.getFilePath(), newAlbum)
                .doOnComplete(() -> {
                    compositionsDao.updateAlbum(composition.getId(), newAlbum);
                    storageMusicProvider.scanMedia(composition.getFilePath());
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionTitle(FullComposition composition, String title) {
        return sourceEditor.setCompositionTitle(composition.getFilePath(), title)
                .andThen(storageMusicDataSource.updateCompositionTitle(composition, title))
                .doOnComplete(() -> {
                    compositionsDao.updateTitle(composition.getId(), title);
                    storageMusicProvider.scanMedia(composition.getFilePath());
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionFileName(FullComposition composition, String fileName) {
        return Single.fromCallable(() -> FileUtils.getChangedFilePath(composition.getFilePath(), fileName))
                .flatMap(newPath -> runRenameFile(composition.getFilePath(), newPath))
                .flatMapCompletable(newPath -> storageMusicDataSource.updateCompositionFilePath(composition, newPath))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeCompositionsFilePath(List<Composition> compositions) {
        return storageMusicDataSource.updateCompositionsFilePath(compositions)
                .subscribeOn(scheduler);
    }

    @Override
    public Completable changeFolderName(long folderId, String newName) {
        return getFullFolderPath(folderId)
                .map(fullPath -> {
                    List<Composition> compositions = compositionsDao.getAllCompositionsInFolder(folderId);

                    String newPath = filesDataSource.renameCompositionsFolder(compositions, fullPath, newName);

                    compositionsDao.updateFilesPath(compositions, fullPath, newPath);
                    return FileUtils.getFileName(fullPath);
                })
                .doOnSuccess(name -> foldersDao.changeFolderName(folderId, name))
                .ignoreElement()
                .subscribeOn(scheduler);
    }

    @Override
    public Single<String> moveFile(String filePath, String oldPath, String newPath) {
        if (Objects.equals(oldPath, newPath)) {
            return Single.error(new MoveInTheSameFolderException("move in the same folder"));
        }
        return Single.fromCallable(() -> FileUtils.getChangedFilePath(filePath, oldPath, newPath))
                .flatMap(path -> runRenameFile(filePath, path))
                .subscribeOn(scheduler);
    }

    //move in directory with duplicate name
    @Override
    public Completable moveFiles(Collection<FileSource2> files,
                                 @Nullable Long fromFolderId,
                                 @Nullable Long toFolderId) {
        return verifyFolderMove(fromFolderId, toFolderId, files)
                .andThen(Single.zip(getFullFolderPath(fromFolderId),
                        getFullFolderPath(toFolderId),
                        compositionsDao.extractAllCompositionsFromFiles(files),
                        (fromPath, toPath, compositions) -> {
                            filesDataSource.moveCompositionsToFolder(compositions, fromPath, toPath);

                            compositionsDao.updateFilesPath(compositions, fromPath, toPath);
                            return TRIGGER;
                        }))
                .ignoreElement()
                .doOnComplete(() -> foldersDao.updateFolderId(files, toFolderId))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable moveFilesToNewDirectory(Collection<FileSource2> files,
                                               @Nullable Long fromFolderId,
                                               String directoryName) {
        return null;
    }

    @Override
    public Completable createDirectory(String path) {
        return Completable.fromAction(() -> {
            File file = new File(path);
            if (file.exists()) {
                throw new FileExistsException();
            }
            if (!file.mkdir()) {
                throw new Exception("file not created, path: " + path);
            }
        }).subscribeOn(scheduler);
    }

    @Override
    public Completable updateAlbumName(String name, long albumId) {
        return checkAlbumExists(name)
                .andThen(Single.fromCallable(() -> albumsDao.getCompositionsInAlbum(albumId)))
                .flatMap(compositions -> Observable.fromIterable(compositions)
                        .flatMapCompletable(composition -> sourceEditor.setCompositionAlbum(composition.getFilePath(), name))
                        .toSingleDefault(compositions))
                .doOnSuccess(compositions -> {
                    albumsDao.updateAlbumName(name, albumId);
                    for (Composition composition: compositions) {
                        storageMusicProvider.scanMedia(composition.getFilePath());
                    }
                })
                .ignoreElement()
                .subscribeOn(scheduler);
    }

    @Override
    public Completable updateAlbumArtist(String newArtistName, long albumId) {
        return Single.fromCallable(() -> albumsDao.getCompositionsInAlbum(albumId))
                .flatMap(compositions -> Observable.fromIterable(compositions)
                        .flatMapCompletable(composition -> sourceEditor.setCompositionAlbumArtist(composition.getFilePath(), newArtistName))// not working, we can't edit album artist?
                        .toSingleDefault(compositions))
                .doOnSuccess(compositions -> {
                    albumsDao.updateAlbumArtist(albumId, newArtistName);
                    for (Composition composition: compositions) {
                        storageMusicProvider.scanMedia(composition.getFilePath());
                    }
                })
                .ignoreElement()
                .subscribeOn(scheduler);
    }

    @Override
    public Completable updateArtistName(String name, long artistId) {
        Set<Composition> compositionsToScan = new LinkedHashSet<>();
        return checkArtistExists(name)

                .andThen(Single.fromCallable(() -> artistsDao.getCompositionsByArtist(artistId)))
                .doOnSuccess(compositionsToScan::addAll)
                .flatMapObservable(Observable::fromIterable)
                .flatMapCompletable(composition -> sourceEditor.setCompositionAuthor(composition.getFilePath(), name))

                .andThen(Single.fromCallable(() -> albumsDao.getAllAlbumsForArtist(artistId)))
                .flatMapObservable(Observable::fromIterable)
                .flatMapCompletable(album -> Single.fromCallable(() -> albumsDao.getCompositionsInAlbum(album.getId()))
                        .doOnSuccess(compositionsToScan::addAll)
                        .flatMapObservable(Observable::fromIterable)
                        .flatMapCompletable(composition -> sourceEditor.setCompositionAlbumArtist(composition.getFilePath(), name))
                )
                .doOnComplete(() -> {
                    artistsDao.updateArtistName(name, artistId);

                    for (Composition composition: compositionsToScan) {
                        storageMusicProvider.scanMedia(composition.getFilePath());
                    }
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Completable updateGenreName(String name, long genreId) {
        return checkGenreExists(name)
                .andThen(Single.fromCallable(() -> genresDao.getGenreName(genreId)))
                .flatMapCompletable(oldName -> Single.fromCallable(() -> genresDao.getCompositionsInGenre(genreId))
                        .flatMapObservable(Observable::fromIterable)
                        .flatMapCompletable(composition -> sourceEditor.changeCompositionGenre(composition.getFilePath(), oldName, name))
                        .doOnComplete(() -> {
                            genresDao.updateGenreName(name, genreId);
                            storageGenresProvider.updateGenreName(oldName, name);
                        })
                )
                .subscribeOn(scheduler);
    }

    @Override
    public Maybe<CompositionSourceTags> getCompositionFileTags(FullComposition composition) {
        return sourceEditor.getFullTags(composition.getFilePath())
                .subscribeOn(scheduler);
    }

    @Override
    public Single<String[]> getCompositionFileGenres(FullComposition composition) {
        return sourceEditor.getCompositionGenres(composition.getFilePath())
                .subscribeOn(scheduler);
    }

    private Completable checkAlbumExists(String name) {
        return Completable.fromAction(() -> {
            if (albumsDao.isAlbumExists(name)) {
                throw new AlbumAlreadyExistsException();
            }
        });
    }

    private Completable checkArtistExists(String name) {
        return Completable.fromAction(() -> {
            if (artistsDao.isArtistExists(name)) {
                throw new ArtistAlreadyExistsException();
            }
        });
    }

    private Completable checkGenreExists(String name) {
        return Completable.fromAction(() -> {
            if (genresDao.isGenreExists(name)) {
                throw new GenreAlreadyExistsException();
            }
        });
    }

    private Single<String> runRenameFile(String oldPath, String newPath) {
        return Single.fromCallable(() -> {
            renameFile(oldPath, newPath);
            return newPath;
        });
    }

    private Single<String> getFullFolderPath(@Nullable Long folderId) {
        return Single.fromCallable(() -> {
            StringBuilder sbPath = new StringBuilder();
            String rootFolderPath = stateRepository.getRootFolderPath();
            if (rootFolderPath != null) {
                sbPath.append(rootFolderPath);
            }
            if (folderId != null) {
                if (sbPath.length() != 0) {
                    sbPath.append('/');
                }
                sbPath.append(foldersDao.getFullFolderPath(folderId));
            }
            return sbPath.toString();
        });
    }

    private Completable verifyFolderMove(@Nullable Long fromFolderId,
                                         @Nullable Long toFolderId,
                                         Collection<FileSource2> files) {
        return Completable.fromAction(() -> {
            if (Objects.equals(fromFolderId, toFolderId)) {
                throw new MoveInTheSameFolderException("move in the same folder");
            }
            for (FileSource2 fileSource: files) {
                if (fileSource instanceof FolderFileSource2) {
                    FolderFileSource2 folder = (FolderFileSource2) fileSource;
                    long folderId = folder.getId();

                    List<Long> childFoldersId = foldersDao.getAllChildFoldersId(folderId);
                    if (Objects.equals(toFolderId, folderId) || childFoldersId.contains(toFolderId)) {
                        throw new MoveFolderToItselfException("moving and destination folders matches");
                    }
                    String name = foldersDao.getFolderName(folderId);
                    if (foldersDao.getChildFoldersNames(toFolderId).contains(name)) {
                        throw new DuplicateFolderNamesException();
                    }
                }
            }

        });
    }
}
