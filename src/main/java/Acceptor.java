import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

public class Acceptor implements Runnable {
    private final Selector selector = Selector.open();
    private final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
    private final BlockingQueue<SocketChannel> queue;

    public Acceptor(InetAddress address, BlockingQueue<SocketChannel> queue) throws IOException {
        this.queue = queue;
        this.serverSocketChannel.configureBlocking(false);
        this.serverSocketChannel.bind(new InetSocketAddress(address,8085));
    }

    @Override
    public void run() {
        try {
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while(!Thread.currentThread().isInterrupted()){
                int readyChannels = selector.select(500);
                if(readyChannels > 0){
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while(iter.hasNext()){
                        SelectionKey key = iter.next();
                        if(key.isAcceptable()){
                            SocketChannel connectionChannel = ((ServerSocketChannel)key.channel()).accept();
                            queue.offer(connectionChannel);
                        }
                        iter.remove();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                selector.close();
                serverSocketChannel.close();
                System.out.println("Acceptor Task Terminated!");
            }catch (Exception ex) {}
        }
    }
}