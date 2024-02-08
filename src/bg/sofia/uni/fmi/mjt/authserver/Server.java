package bg.sofia.uni.fmi.mjt.authserver;

import bg.sofia.uni.fmi.mjt.authserver.command.Command;
import bg.sofia.uni.fmi.mjt.authserver.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.authserver.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.authserver.logger.crash.CrashLogger;
import bg.sofia.uni.fmi.mjt.authserver.response.Response;
import bg.sofia.uni.fmi.mjt.authserver.response.StatusCode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class Server {
    private static final int BUFFER_SIZE = 1024;
    private static final String HOST = "localhost";

    private final int port;
    private boolean isServerWorking;

    private ByteBuffer buffer;
    private Selector selector;

    private final CommandExecutor commandExecutor;
    private final CrashLogger crashLogger;

    public Server(int port, CommandExecutor commandExecutor, CrashLogger crashLogger) {
        this.crashLogger = crashLogger;
        this.commandExecutor = commandExecutor;
        this.port = port;
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            selector = Selector.open();
            configServerSocketChannel(serverSocketChannel, selector);
            this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
            this.isServerWorking = true;

            System.out.println("Server started");

            while (isServerWorking) {
                try {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }

                    keyIterator();
                } catch (IOException e) {
                    crashLogger.log(e);
                }
            }
        } catch (IOException e) {
            crashLogger.log(e);
        }
    }

    public void shutdown() {
        this.isServerWorking = false;

        if (selector != null && selector.isOpen()) {
            try {
                selector.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void keyIterator() throws IOException {
        Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            if (key.isReadable()) {
                handleRead(key);
            } else if (key.isAcceptable()) {
                accept(selector, key);
            }

            keyIterator.remove();
        }
    }

    private void configServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST, port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        String clientInput = getClientInput(channel);

        if (clientInput == null || clientInput.isBlank()) {
            writeClientOutput(channel, new Response("Null input", StatusCode.NOT_FOUND.getCode()).toString());
        }

        try {
            Command command = CommandCreator.create(clientInput, channel.socket().getInetAddress().getHostAddress());
            Response response = commandExecutor.execute(command);
            writeClientOutput(channel, response.toString());
        } catch (IllegalArgumentException e) {
            writeClientOutput(channel, new Response(e.getMessage(), StatusCode.BAD_REQUEST.getCode()).toString());
        } catch (RuntimeException e) {
            crashLogger.log(e);
            writeClientOutput(channel, new Response("Something unexpected happened",
                            StatusCode.INTERNAL_SERVER_ERROR.getCode()).toString());
        }

    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        clientChannel.write(buffer);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        System.out.println("New connection: " + sockChannel.getLocalAddress());

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }
}
