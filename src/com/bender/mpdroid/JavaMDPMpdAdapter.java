package com.bender.mpdroid;

import android.util.Log;
import org.bff.javampd.MPD;
import org.bff.javampd.MPDPlayer;
import org.bff.javampd.events.PlayerChangeEvent;
import org.bff.javampd.events.PlayerChangeListener;
import org.bff.javampd.exception.MPDConnectionException;
import org.bff.javampd.exception.MPDException;
import org.bff.javampd.exception.MPDPlayerException;
import org.bff.javampd.exception.MPDResponseException;

import java.net.UnknownHostException;

/**
 * This is an mpd adapter that uses the JavaMPD library to talk to the mpd server.
 *
 * @see org.bff.javampd.MPD
 */
class JavaMDPMpdAdapter implements MpdAdapterIF
{
    private MPD mpdService;
    private static final String TAG = JavaMDPMpdAdapter.class.getSimpleName();
    private boolean muted;

    public JavaMDPMpdAdapter()
    {
    }


    public PlayStatus getPlayStatus()
    {
        PlayStatus status = PlayStatus.Stopped;
        try
        {
            if (mpdService != null)
            {
                MPDPlayer mpdPlayer = mpdService.getMPDPlayer();
                status = JavaMDPPlayStatus.convertFromJavaMDPStatus(mpdPlayer.getStatus());
            }
        }
        catch (MPDException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        return status;
    }

    public void next()
    {
        try
        {
            if (mpdService != null)
            {
                mpdService.getMPDPlayer().playNext();
            }
        }
        catch (MPDConnectionException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        catch (MPDPlayerException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
    }

    public void prev()
    {
        try
        {
            if (mpdService != null)
            {
                mpdService.getMPDPlayer().playPrev();
            }
        }
        catch (MPDConnectionException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        catch (MPDPlayerException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
    }

    public Integer setVolume(Integer volume)
    {
        Integer currentVolume = 0;
        try
        {
            if (mpdService != null)
            {
                MPDPlayer mpdPlayer = mpdService.getMPDPlayer();
                mpdPlayer.setVolume(volume);
                currentVolume = mpdPlayer.getVolume();
            }
        }
        catch (MPDConnectionException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        catch (MPDPlayerException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        return currentVolume;
    }

    public Integer getVolume()
    {
        Integer volume = 0;
        try
        {
            if (mpdService != null)
            {
                volume = mpdService.getMPDPlayer().getVolume();
            }
        }
        catch (MPDConnectionException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        catch (MPDPlayerException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        return volume;
    }

    public Boolean toggleMute()
    {
        try
        {
            MPDPlayer mpdPlayer = mpdService.getMPDPlayer();
            if (muted)
            {
                mpdPlayer.unMute();
            }
            else
            {
                mpdPlayer.mute();
            }
        }
        catch (MPDConnectionException e)
        {
            e.printStackTrace();
        }
        catch (MPDPlayerException e)
        {
            e.printStackTrace();
        }
        return muted;
    }

    public void connect(String server, int port, String password)
    {
        try
        {
            if (mpdService != null)
            {
                mpdService = new MPD(server, port, password);
                initializeListeners();
            }
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        catch (MPDConnectionException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
    }

    private void initializeListeners()
    {
        mpdService.getMPDPlayer().addPlayerChangeListener(new JavaMPDPlayerChangeListener());
    }

    public void connect(String server, int port)
    {
        try
        {
            mpdService = new MPD(server, port);
            initializeListeners();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        catch (MPDConnectionException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
    }

    public void connect(String server)
    {
        try
        {
            mpdService = new MPD(server);
            initializeListeners();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        catch (MPDConnectionException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
    }

    public void disconnect()
    {
        try
        {
            if (mpdService != null)
            {
                mpdService.close();
            }
        }
        catch (MPDConnectionException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        catch (MPDResponseException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        mpdService = null;
    }

    public boolean isConnected()
    {
        if (mpdService != null)
        {
            return mpdService.isConnected();
        }
        return false;
    }

    public String getServerVersion()
    {
        String version = null;
        if (mpdService != null)
        {
            version = mpdService.getVersion();
        }
        return version;
    }

    public PlayStatus playOrPause()
    {
        PlayStatus playStatus = PlayStatus.Stopped;
        try
        {
            if (mpdService != null)
            {
                MPDPlayer mpdPlayer = mpdService.getMPDPlayer();
                MPDPlayer.PlayerStatus status = mpdPlayer.getStatus();
                if (status.equals(MPDPlayer.PlayerStatus.STATUS_PLAYING))
                {
                    mpdPlayer.pause();
                }
                else
                {
                    mpdPlayer.play();
                }
                playStatus = JavaMDPPlayStatus.convertFromJavaMDPStatus(mpdPlayer.getStatus());
            }
        }
        catch (MPDConnectionException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        catch (MPDPlayerException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        catch (MPDException e)
        {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        return playStatus;
    }

    private static enum JavaMDPPlayStatus
    {
        Playing(MPDPlayer.PlayerStatus.STATUS_PLAYING, PlayStatus.Playing),
        Paused(MPDPlayer.PlayerStatus.STATUS_PAUSED, PlayStatus.Paused),
        Stopped(MPDPlayer.PlayerStatus.STATUS_STOPPED, PlayStatus.Stopped);

        private final MPDPlayer.PlayerStatus javaMPDStatus;
        private final PlayStatus playStatus;

        private JavaMDPPlayStatus(MPDPlayer.PlayerStatus statusPlaying, PlayStatus playing)
        {
            javaMPDStatus = statusPlaying;
            playStatus = playing;
        }

        private static PlayStatus convertFromJavaMDPStatus(MPDPlayer.PlayerStatus status)
        {
            PlayStatus ret = PlayStatus.Stopped;
            for (JavaMDPPlayStatus javaMDPPlayStatus : values())
            {
                if (javaMDPPlayStatus.javaMPDStatus.equals(status))
                {
                    ret = javaMDPPlayStatus.playStatus;
                }
            }
            return ret;
        }
    }

    private class JavaMPDPlayerChangeListener implements PlayerChangeListener
    {
        public void playerChanged(PlayerChangeEvent event)
        {
            switch (event.getId())
            {
                case PlayerChangeEvent.PLAYER_MUTED:
                    muted = true;
                    break;
                case PlayerChangeEvent.PLAYER_UNMUTED:
                    muted = false;
                    break;
            }
        }
    }
}
