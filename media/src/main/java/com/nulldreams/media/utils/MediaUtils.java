package com.nulldreams.media.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;

import com.nulldreams.media.model.Album;
import com.nulldreams.media.model.Song;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yihao Guo on 2018/05/31.
 * MediaStore中定义了一系列的数据表格，通过ContentResolver提供的查询接口，我们可以得到各种需要的信息。
 */
public class MediaUtils {

    private static final String TAG = MediaUtils.class.getSimpleName();

    public static final DecimalFormat FORMAT = new DecimalFormat("00");
    /*歌曲信息属性说明如下
     * //获取歌曲在系统中的id MediaStore.Audio.Media._ID

     //获取歌曲的歌名 MediaStore.Audio.Media.TITLE;

     //获取歌曲所在专辑的id MediaStore.Audio.Media.ALBUM_ID;

     //获取专辑的歌手名 MediaStore.Audio.Media.ARTIST

     //获取歌曲的时长 MediaStore.Audio.Media.DURATION

     //获取歌曲的大小 MediaStore.Audio.Media.SIZE

     //获取专辑名 MediaStore.Audio.Media.ALBUM

     //获取歌曲路径，如xx/xx/xx.mp3 MediaStore.Audio.Media.DATA
     */

    public static final String[] AUDIO_KEYS = new String[]{
            MediaStore.Audio.Media._ID, //歌曲ID
            MediaStore.Audio.Media.TITLE, //歌曲的名称
            MediaStore.Audio.Media.TITLE_KEY, //歌曲的专辑名
            MediaStore.Audio.Media.ARTIST, //歌曲的歌手名
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ARTIST_KEY,
            MediaStore.Audio.Media.COMPOSER,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM_KEY,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION, //歌曲的总播放时长
            MediaStore.Audio.Media.SIZE, //歌曲文件的大小
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.IS_RINGTONE,
            MediaStore.Audio.Media.IS_PODCAST,
            MediaStore.Audio.Media.IS_ALARM,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.IS_NOTIFICATION,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATA,
    };
    /*
 专辑信息
 //获取专辑id
 MediaStore.Audio.Albums._ID
 //获取专辑名
 MediaStore.Audio.Albums.ALBUM
 //获取专辑歌手
 MediaStore.Audio.Albums.ARTIST
 //获取专辑歌曲数
 MediaStore.Audio.Albums.NUMBER_OF_SONGS
      */
    public static final String[] ALBUM_COLUMNS = new String[] {
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ALBUM_KEY,
            MediaStore.Audio.Albums.ALBUM_ART,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            MediaStore.Audio.Albums.FIRST_YEAR,
            MediaStore.Audio.Albums.LAST_YEAR,
    };
/*
歌手信息
//歌手id
MediaStore.Audio.Artists._ID
//歌手名
MediaStore.Audio.Artists.ARTIST
//歌手歌曲数
MediaStore.Audio.Artists.NUMBER_OF_TRACKS
 */
    public static final String[] ARTIST_COLUMNS = new String[] {
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.ARTIST_KEY,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
    };

    /*public static List<Song> getArtistList (Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                ARTIST_COLUMNS,
                null,
                null,
                null);
        return getAudioList(cursor);
    }*/

    public static List<Song> getAlbumSongList (Context context, int album_id) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                AUDIO_KEYS,
                MediaStore.Audio.Media.ALBUM_ID + " = " + album_id,
                null,
                null);
        return getAudioList(cursor);
    }

    public static List<Album> getAlbumList (Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                ALBUM_COLUMNS,
                null,
                null,
                null);
        int count = cursor.getCount();
        List<Album> albumList = null;
        if (count > 0) {
            albumList = new ArrayList<>();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                int id, minYear, maxYear, numSongs;
                String album, albumKey, artist, albumArt;
                id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums._ID));
                minYear = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums.FIRST_YEAR));
                maxYear = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums.LAST_YEAR));
                numSongs = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS));
                album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
                albumKey = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_KEY));
                artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST));
                albumArt = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));

                Album albumObj = new Album(id, minYear, maxYear, numSongs,
                        album, albumKey, artist, albumArt);
                albumList.add(albumObj);
            }

        }
        cursor.close();
        return albumList;
    }

    public static List<Song> getAudioList(Context context) {


        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                AUDIO_KEYS,
                MediaStore.Audio.Media.IS_MUSIC + "=" + 1,
                null,
                null);
        return getAudioList(cursor);
    }

    private static List<Song> getAudioList (Cursor cursor) {
        List<Song> audioList = null;
        if (cursor.getCount() > 0) {
            audioList = new ArrayList<Song>();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                Bundle bundle = new Bundle ();
                for (int i = 0; i < AUDIO_KEYS.length; i++) {
                    final String key = AUDIO_KEYS[i];
                    final int columnIndex = cursor.getColumnIndex(key);
                    final int type = cursor.getType(columnIndex);
                    switch (type) {
                        case Cursor.FIELD_TYPE_BLOB:
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            float floatValue = cursor.getFloat(columnIndex);
                            bundle.putFloat(key, floatValue);
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            int intValue = cursor.getInt(columnIndex);
                            bundle.putInt(key, intValue);
                            break;
                        case Cursor.FIELD_TYPE_NULL:
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            String strValue = cursor.getString(columnIndex);
                            bundle.putString(key, strValue);
                            break;
                    }
                }
                Song audio = new Song(bundle);
                audioList.add(audio);
            }
        }

        cursor.close();
        return audioList;
    }



    public static String formatTime (int durationInMilliseconds) {
        int seconds = durationInMilliseconds /  1000;
        int minutes = seconds / 60;
        int secondsRemain = seconds % 60;
        return FORMAT.format(minutes) + ":" + FORMAT.format(secondsRemain);
    }
}
