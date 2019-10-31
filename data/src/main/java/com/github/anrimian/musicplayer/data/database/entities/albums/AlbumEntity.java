package com.github.anrimian.musicplayer.data.database.entities.albums;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;

import javax.annotation.Nullable;

@Entity(tableName = "albums",
        foreignKeys = {
                @ForeignKey(entity = ArtistEntity.class,
                parentColumns = {"id"},
                childColumns = {"artistId"})
        },
        indices = @Index("artistId")
)
public class AlbumEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long artistId;

    @Nullable
    private Long storageId;

    private String albumName;
    private String albumKey;

    private int firstYear;
    private int lastYear;

    public AlbumEntity(long artistId,
                       @Nullable Long storageId,
                       String albumName,
                       String albumKey,
                       int firstYear,
                       int lastYear) {
        this.artistId = artistId;
        this.storageId = storageId;
        this.albumName = albumName;
        this.albumKey = albumKey;
        this.firstYear = firstYear;
        this.lastYear = lastYear;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    @Nullable
    public Long getStorageId() {
        return storageId;
    }

    public void setStorageId(@Nullable Long storageId) {
        this.storageId = storageId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumKey() {
        return albumKey;
    }

    public void setAlbumKey(String albumKey) {
        this.albumKey = albumKey;
    }

    public int getFirstYear() {
        return firstYear;
    }

    public void setFirstYear(int firstYear) {
        this.firstYear = firstYear;
    }

    public int getLastYear() {
        return lastYear;
    }

    public void setLastYear(int lastYear) {
        this.lastYear = lastYear;
    }
}
