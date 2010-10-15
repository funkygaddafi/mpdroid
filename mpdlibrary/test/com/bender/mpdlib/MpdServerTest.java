package com.bender.mpdlib;

import com.bender.mpdlib.commands.MpdCommands;
import com.bender.mpdlib.commands.Response;
import junit.framework.TestCase;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * todo: replace with documentation
 */
public class MpdServerTest extends TestCase
{
    private MpdServer mpdServer;
    public static final String HOSTNAME = "hostname";
    private static final int PORT = 6601;
    public static final String VERSION = "MPD 0.15.0";
    private CommandStreamProvider commandStreamProvider;
    private CallbackStreamProvider callbackStreamProvider;

    @Override
    public void setUp() throws Exception
    {
        commandStreamProvider = new CommandStreamProvider();
        callbackStreamProvider = new CallbackStreamProvider();
        mpdServer = new MpdServer(commandStreamProvider, callbackStreamProvider);
        commandStreamProvider.appendServerResult(Response.OK + " " + VERSION);
        StringBuilder stringBuilder = new StringBuilder();
        setStatus(stringBuilder);
    }

    public void testConnectWithHostname() throws Exception
    {
        mpdServer.connect(HOSTNAME);
        assertEquals(true, mpdServer.isConnected());
    }

    public void testConnectWithHostnameAndPort() throws Exception
    {
        mpdServer.connect(HOSTNAME, PORT);
        assertEquals(true, mpdServer.isConnected());
    }

    public void testConnectWithAuthenticationUnsupported() throws Exception
    {
        boolean unsupported = false;
        try
        {
            mpdServer.connect(HOSTNAME, PORT, "password");
        }
        catch (IllegalArgumentException e)
        {
            unsupported = true;
        }
        assertEquals(true, unsupported);
    }

    public void testVersion() throws Exception
    {
        mpdServer.connect(HOSTNAME);
        String version = mpdServer.getServerVersion();
        assertEquals(VERSION, version);
    }

    public void testPlay() throws Exception
    {
        mpdServer.connect(HOSTNAME);

        commandStreamProvider.appendServerResult(MpdServer.OK_RESPONSE);
        mpdServer.play();

        List<String> commandQueue = (List<String>) commandStreamProvider.commandQueue;
        assertEquals(true, commandQueue.contains(MpdCommands.play.toString()));
    }

    public void testDisconnect() throws Exception
    {
        mpdServer.connect(HOSTNAME);

        commandStreamProvider.appendServerResult(MpdServer.OK_RESPONSE);
        mpdServer.disconnect();

        List<String> commandQueue = (List<String>) commandStreamProvider.commandQueue;
        assertEquals(MpdCommands.close.toString(), commandQueue.get(commandQueue.size() - 1));
    }

    public void testStatus() throws Exception
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(MpdStatus.state).append(": ");
        stringBuilder.append(PlayStatus.Paused.serverString);
        commandStreamProvider.removeLastCommand();
        setStatus(stringBuilder);

        mpdServer.connect(HOSTNAME);

        PlayStatus playStatus = mpdServer.getPlayStatus();

        assertEquals(PlayStatus.Paused, playStatus);
    }

    private void setStatus(StringBuilder stringBuilder)
    {
        String s = stringBuilder.toString();
        if (!s.equals(""))
        {
            commandStreamProvider.appendServerResult(s);
        }
        commandStreamProvider.appendServerResult(Response.OK.toString());
    }

    public class CommandStreamProvider implements SocketStreamProviderIF
    {
        private BufferedWriter bufferedWriter;
        private BufferedReader bufferedReader;
        private Queue<String> commandQueue;
        private Queue<String> responseQueue;

        private boolean connected;

        public CommandStreamProvider() throws IOException
        {
            bufferedReader = mock(BufferedReader.class);
            bufferedWriter = mock(BufferedWriter.class);
            commandQueue = new LinkedList<String>();
            responseQueue = new LinkedList<String>();
            when(bufferedReader.readLine()).thenAnswer(new Answer<String>()
            {
                public String answer(InvocationOnMock invocationOnMock) throws Throwable
                {
                    return responseQueue.remove();
                }
            });
            doAnswer(new Answer()
            {
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable
                {
                    commandQueue.offer((String) invocationOnMock.getArguments()[0]);
                    return null;
                }
            }).when(bufferedWriter).write(anyString());
        }

        public void connect(SocketAddress socketAddress) throws IOException
        {
            connected = true;
        }

        public BufferedReader getBufferedReader() throws IOException
        {
            return bufferedReader;
        }

        public BufferedWriter getBufferedWriter() throws IOException
        {
            return bufferedWriter;
        }

        public void disconnect() throws IOException
        {
            connected = false;
        }

        public boolean isConnected()
        {
            return connected;
        }

        public void appendServerResult(String s)
        {
            responseQueue.offer(s);
        }

        public void removeLastCommand()
        {
            List<String> list = (List<String>) responseQueue;
            list.remove(list.size() - 1);
        }
    }

    private class CallbackStreamProvider implements SocketStreamProviderIF
    {
        private boolean connected;

        public void connect(SocketAddress socketAddress) throws IOException
        {
            connected = true;
        }

        public BufferedReader getBufferedReader() throws IOException
        {
            BufferedReader mock = mock(BufferedReader.class);
            return mock;
        }

        public BufferedWriter getBufferedWriter() throws IOException
        {
            BufferedWriter mock = mock(BufferedWriter.class);
            doAnswer(new Answer()
            {
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable
                {
                    synchronized (this)
                    {
                        wait();
                    }
                    return null;
                }
            }).when(mock).write(anyString());
            return mock;
        }

        public void disconnect() throws IOException
        {
            connected = false;
        }

        public boolean isConnected()
        {
            return connected;
        }
    }
}