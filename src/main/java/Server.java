import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.*;
import java.util.concurrent.*;

/**
 * A Simple Non Blocking Server Implementation
 * Acceptor - is a task which accepts new Connections, non-blocking
 * Processor - is a task which reads stream of data from each accepted connection, blocking. One Processor for each connection.
 * Threading model:
 *  A ExecutorService is used to schedule both acceptor and processor.
 *  Both Acceptor and Processor respond to interruption, by stopping their activity.
 *  A lambda task, receive connections from Acceptor via Queue and spins a new Processor for each connection.
 */
public class Server {
    public static void main(String[] args) throws Exception{
        final ExecutorService executor = Executors.newCachedThreadPool();
        final BlockingQueue<SocketChannel> queue = new LinkedBlockingQueue<>();
        executor.execute(new Acceptor(InetAddress.getLoopbackAddress(),queue)); // Start acceptor task
        executor.execute(() -> {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    SocketChannel chan = queue.take();
                    executor.execute(new Processor(chan));
                }
                catch (InterruptedException | IOException ex){
                    ex.printStackTrace();
                    if( ex instanceof InterruptedException) Thread.currentThread().interrupt();
                }
            }
            System.out.println("Consumer Task Terminated!");
        });
        System.in.read();
        System.out.println("Starting Shutdown!");
        executor.shutdownNow();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        System.out.println("Program Exiting!");
    }
}