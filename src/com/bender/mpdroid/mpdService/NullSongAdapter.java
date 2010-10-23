package com.bender.mpdroid.mpdService;

import android.util.Log;

/**
 * Null object song adapter
 */
class NullSongAdapter implements MpdSongAdapterIF
{
    private static final String TAG = NullSongAdapter.class.getSimpleName();

    public String getSongName()
    {
        Log.e(TAG, "getSongName() called on NULL object");
        return null;
    }

    public String getArtist()
    {
        Log.e(TAG, "getArtist() called on NULL object");
        return null;
    }

    public String getAlbumName()
    {
        Log.e(TAG, "getAlbumName() called on NULL object");
        return null;
    }

    public String getFile()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getDate()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Integer getSongLength()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
