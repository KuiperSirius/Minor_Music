package com.nulldreams.media.manager.ruler;

import com.nulldreams.media.model.Song;

import java.util.List;

/**
 * Created by Yihao Guo on 2018/05/30.
 */
public interface Rule {
    Song previous (Song song, List<Song> songList, boolean isUserAction);
    Song next(Song song, List<Song> songList, boolean isUserAction);
    void clear ();
}
