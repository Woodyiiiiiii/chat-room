package client;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ChatClient {

    private static final String LOCALHOST = "localhost";
    private static final int DEFAULT_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;

    private String host;
    private int port;
    private AsynchronousSocketChannel clientChannel;
    private Charset charset = Charset.forName("UTF-8");

    public ChatClient() {
        this(LOCALHOST, DEFAULT_PORT);
    }

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    private void close(Closeable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void start() {
        try {
            // 创建channel
            clientChannel = AsynchronousSocketChannel.open();
            // 使用Future对象获得返回connect结果，future.get()确保连接成功
            Future<Void> future = clientChannel.connect(new InetSocketAddress(host, port));
            future.get();

            // 处理用户的输入，写入服务器
            new Thread(new UserInputHandler(this)).start();

            // 接下来是读取服务器传来的消息，即其他用户发送的消息
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER);
            while (true) {
                Future<Integer> readResult = clientChannel.read(buffer);
                int result = readResult.get();
                if (result <= 0) {
                    // 服务器异常
                    System.out.println("服务器断开");
                    close(clientChannel);
                    System.exit(1);
                } else {
                    buffer.flip();
                    String msg = String.valueOf(charset.decode(buffer));
                    buffer.clear();
                    System.out.println(msg);
                }
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void send(String msg) {
        if (msg.isEmpty()) {
            return;
        }

        ByteBuffer buffer = charset.encode(msg);
        // Integer类型的Future对象获得未来返回的结果
        Future<Integer> writeResult = clientChannel.write(buffer);
        try {
            writeResult.get();
        } catch (InterruptedException| ExecutionException e) {
            System.out.println("发送消息失败");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("127.0.0.1", 7777);
        client.start();
    }

}
