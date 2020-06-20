import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

public class Server {

    final String LOCALHOST = "localhost";
    final int DEFAULT_PORT = 8888;
    AsynchronousServerSocketChannel serverChannel;


    /**
     * 关闭函数
     * @param closable 一个接口，代表某个数据源或目的
     */
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

    /**
     * 启动函数，管理方法逻辑
     */
    public void start() {
        try {
            // 绑定监听端口, 跟NIO一样
            // 使用默认的AsynchronousChannelGroup，实际上从线程池中拿取了新线程
            serverChannel = AsynchronousServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(LOCALHOST, DEFAULT_PORT));
            System.out.println("启动服务器，监听端口:" + DEFAULT_PORT);

            while (true) {
                // 第一个参数是Object类，作用是一些辅助信息，NIO的select也可以用
                // 第二个参数才是真正的实现，传入CompletionHandler接口实现的类
                // 因为执行accept函数后就立刻返回，服务器就结束了，所以需要放入一个while循环接收客户端
                // 但while循环不停地执行accept函数，过于频繁，无意义且浪费资源
                // 调用System.in.read()，阻塞式的，等待输入
                serverChannel.accept(null, new AcceptHandler());
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(serverChannel);
        }
    }

    /**
     * 实现CompletionHandler接口实现异步调用
     */
    private class AcceptHandler implements
            CompletionHandler<AsynchronousSocketChannel, Object> {
        /**
         * 返回时调用completed
         * @param result 建立连接的AsynchronousSocketChannel
         * @param attachment 辅助信息
         */
        @Override
        public void completed(AsynchronousSocketChannel result, Object attachment) {
            // 如果serverChannel仍是打开的，让它继续接收连接
            // 不用担心StackOverFlow，accept底层有保障
            if (serverChannel.isOpen()) {
                serverChannel.accept(null, this);
            }

            AsynchronousSocketChannel clientChannel = result;
            if (clientChannel != null && clientChannel.isOpen()) {
                // 客户端处理进程——负责处理读写操作
                ClientHandler handler = new ClientHandler(clientChannel);

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                // 试验attachment，Map负责读操作
                Map<String, Object> info = new HashMap<>();
                info.put("type", "read");
                info.put("buffer", buffer);

                // clientChannel从buffer中读取数据
                // 第二个参数是attachment
                clientChannel.read(buffer, info, handler);
            }
        }

        /**
         * 异步调用失败后执行
         * @param exc
         * @param attachment
         */
        @Override
        public void failed(Throwable exc, Object attachment) {
            // 处理错误
        }
    }

    /**
     * 内部类 ClientHandler
     * 客户端处理进程类
     */
    private class ClientHandler implements
            CompletionHandler<Integer, Object> {
        private AsynchronousSocketChannel clientChannel;

        public ClientHandler(AsynchronousSocketChannel channel) {
            this.clientChannel = channel;
        }

        @Override
        public void completed(Integer result, Object attachment) {
            Map<String, Object> info = (Map<String, Object>) attachment;
            String type = (String) info.get("type");

            // Map中只有两个键值对，不断地被覆写
            // 这是一个简易echo服务器，所以目的是将客户端发来的数据传回去
            // 只需要改变buffer的状态就可以了(读换成写)
            if ("read".equals(type)) {
                // 服务器端从客户端中读取数据，完成后改为写入状态
                ByteBuffer buffer = (ByteBuffer) info.get("buffer");
                buffer.flip();
                info.put("type", "write");
                clientChannel.write(buffer, info, this);
                buffer.clear();
            } else if ("write".equals(type)) {
                // 服务器要求客户端中写入数据，所以创建新的buffer，重新调用read
                ByteBuffer buffer = ByteBuffer.allocate(1024);

                info.put("type", "read");
                info.put("buffer", buffer);

                clientChannel.read(buffer, info, this);
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            // 处理错误
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
