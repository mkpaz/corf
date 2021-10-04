package telekit.base.net;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetOptionHandler;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static telekit.base.util.CommonUtils.hush;

/**
 * Default terminal type is VT100. Inherit or use {@link TelnetOptionHandler}
 * to modify the behavior.
 */
public class ApacheTelnetClient implements Runnable {

    protected static final Logger LOG = Logger.getLogger(ApacheTelnetClient.class.getName());

    protected static final int SLEEP_TIMEOUT = 100;
    protected static final int DEFAULT_CONNECT_TIMEOUT = 5_000;
    protected static final int DEFAULT_INACTIVITY_TIMEOUT = 10_000;

    protected final TelnetClient telnetClient;
    protected final String host;
    protected final int port;
    protected final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

    protected Thread thread;
    protected long inactivityTimeout = DEFAULT_INACTIVITY_TIMEOUT;
    protected Charset charset = US_ASCII;

    public ApacheTelnetClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.telnetClient = new TelnetClient();
    }

    @Override
    public void run() {
        InputStream inputStream = telnetClient.getInputStream();
        try {
            byte[] readBytes = new byte[1024];
            int readLength;

            do {
                readLength = inputStream.read(readBytes);
                if (readLength > 0) {
                    byteBuffer.write(readBytes, 0, readLength);
                }
            } while (readLength >= 0);
        } catch (IOException e) {
            LOG.fine("Error while reading from socket");
            LOG.fine(ExceptionUtils.getStackTrace(e));
        }

        try {
            disconnect();
        } catch (IOException e) {
            LOG.fine("Error while closing connection");
            LOG.fine(ExceptionUtils.getStackTrace(e));
        }
    }

    public void connect() throws IOException {
        connect(DEFAULT_CONNECT_TIMEOUT);
    }

    public void connect(int timeout) throws IOException {
        // connect
        telnetClient.setConnectTimeout(timeout);
        telnetClient.connect(host, port);

        // start itself in a new thread
        thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    public void addOptionHandler(TelnetOptionHandler optionHandler)
            throws InvalidTelnetOptionException, IOException {
        // Examples:
        // new TerminalTypeOptionHandler("VT100", false, false, true, false)
        // new EchoOptionHandler(true, false, true, false)
        // new SuppressGAOptionHandler(true, true, true, true)
        telnetClient.addOptionHandler(optionHandler);
    }

    public boolean addOptionHandlerSilently(TelnetOptionHandler optionHandler) {
        try {
            addOptionHandler(optionHandler);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void disconnect() throws IOException {
        try {
            if (telnetClient.isConnected()) {
                telnetClient.disconnect();
            }
        } finally {
            hush(() -> thread.interrupt());
        }
    }

    public boolean isConnected() {
        return telnetClient.isConnected();
    }

    public @Nullable String readUntil(String... patterns) {
        if (patterns == null || patterns.length == 0) {
            throw new IllegalArgumentException("At least one pattern must be specified");
        }

        int lastBufferSize = byteBuffer.size();
        long remainingInactivityCycles = inactivityTimeout / SLEEP_TIMEOUT;

        while (remainingInactivityCycles != 0) {
            // exit the loop when no data was read during inactivity timeout
            // otherwise we could wait missing patterns for eternity
            if (lastBufferSize == byteBuffer.size()) {
                remainingInactivityCycles--;
            } else {
                lastBufferSize = byteBuffer.size();
                remainingInactivityCycles = inactivityTimeout / SLEEP_TIMEOUT;
            }

            try {
                Thread.sleep(SLEEP_TIMEOUT);
            } catch (InterruptedException e) {
                LOG.warning("Thread interrupted");
            }

            String s = byteBuffer.toString(charset);
            for (String pattern : patterns) {
                if (s.contains(pattern)) { return s; }
            }
        }

        return null;
    }

    public void sendString(String s) throws IOException {
        // There's only one byte buffer here and it's used to search for
        // user defined patterns in waitFor(). So, it should be flushed
        // before sending any command to the remote side. If you want to
        // keep full session log just concat all subsequent waitFor()
        // results.
        flush();

        OutputStream out = telnetClient.getOutputStream();
        out.write(s.getBytes());
        out.flush();
    }

    public void sendLine(String s) throws IOException {
        sendString(s + "\r\n");
    }

    public void flush() {
        byteBuffer.reset();
    }

    public long getInactivityTimeout() {
        return inactivityTimeout;
    }

    public void setInactivityTimeout(long inactivityTimeout) {
        this.inactivityTimeout = inactivityTimeout;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = defaultIfNull(charset, US_ASCII);
    }
}