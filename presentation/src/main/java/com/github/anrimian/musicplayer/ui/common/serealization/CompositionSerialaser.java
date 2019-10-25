package com.github.anrimian.musicplayer.ui.common.serealization;

import android.os.Bundle;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;

import java.util.Date;
import java.util.Objects;

public interface CompositionSerialaser {

    String ARTIST = "artist";
    String TITLE = "title";
    String ALBUM = "album";
    String FILE_PATH = "file_path";
    String DURATION = "duration";
    String SIZE = "size";
    String ID = "id";
    String STORAGRE_ID = "storage_id";
    String DATE_ADDED = "date_added";
    String DATE_MODIFIED = "date_modified";
    String CORRUPTION_TYPE = "corruption_type";

    static Bundle serialize(Composition composition) {
        Bundle bundle = new Bundle();
        bundle.putString(ARTIST, composition.getArtist());
        bundle.putString(TITLE, composition.getTitle());
        bundle.putString(ALBUM, composition.getAlbum());
        bundle.putString(FILE_PATH, composition.getFilePath());
        bundle.putLong(SIZE, composition.getSize());
        bundle.putLong(ID, composition.getId());
        Long storageId = composition.getStorageId();
        bundle.putLong(STORAGRE_ID, storageId == null? -1: storageId);
        bundle.putLong(DATE_ADDED, composition.getDateAdded().getTime());
        bundle.putLong(DATE_MODIFIED, composition.getDateModified().getTime());
        bundle.putSerializable(CORRUPTION_TYPE, composition.getCorruptionType());
        return bundle;
    }

    static Composition deserialize(Bundle bundle) {
        long storageId = bundle.getLong(STORAGRE_ID);
        return new Composition(
                bundle.getString(ARTIST),
                bundle.getString(TITLE),
                bundle.getString(ALBUM),
                Objects.requireNonNull(bundle.getString(FILE_PATH)),
                bundle.getLong(DURATION),
                bundle.getLong(SIZE),
                bundle.getLong(ID),
                storageId == -1? null: storageId,
                new Date(bundle.getLong(DATE_ADDED)),
                new Date(bundle.getLong(DATE_MODIFIED)),
                (CorruptionType) bundle.getSerializable(CORRUPTION_TYPE)
        );
    }
}
