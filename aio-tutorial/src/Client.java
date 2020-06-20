import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Client {

    final String LOCALHOST = "localhost";
    final int DEFAULT_PORT = 8888;
    AsynchronousSocketChannel clientChannel;

    private void close(Closeable closable) {
        if (closable != null) {
            try {
                closable.close();
                System.out.println("关闭" + closable);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        try {
            // 创建channel
            clientChannel = AsynchronousSocketChannel.open();
            // 返回的Future对象保存了调用成功的信息
            Future<Void> future = clientChannel.connect(new InetSocketAddress(LOCALHOST, DEFAULT_PORT));
            // 阻塞式调用，等待服务器端和客户端连接
            future.get();

            // 等待用户的输入
            BufferedReader consoleReader =
                    new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String input = consoleReader.readLine();

                // 把用户发送的字符串消息转换成字节存进buffer中
                byte[] inputBytes = input.getBytes();
                ByteBuffer buffer = ByteBuffer.wrap(inputBytes);
                // 写入服务器端
                Future<Integer> writeResult = clientChannel.write(buffer);

                writeResult.get();         // 保证写入成功
                buffer.flip();
                Future<Integer> readResult = clientChannel.read(buffer);    // 读取数据

                readResult.get();
                String echo = new String(buffer.array());       // 字节数组转换成字符串
                buffer.clear();

                System.out.println(echo);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            close(clientChannel);
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }


}
