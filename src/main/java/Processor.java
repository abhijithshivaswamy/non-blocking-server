import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Processor implements Runnable {
    private final SocketChannel channel;
    private final ByteBuffer buffer;
    public Processor(SocketChannel socketChannel) throws IOException {
        this.channel = socketChannel;
        this.channel.configureBlocking(true);
        buffer = ByteBuffer.allocate(64);
    }

    @Override
    public void run() {
        try{
            int read = -1;
            do{
                read = channel.read(buffer);
                buffer.flip();
                while (buffer.hasRemaining()){
                    System.out.print((char)buffer.get());
                }
                buffer.clear();
            } while(read != -1);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                channel.close();
                System.out.println("Processor Task Terminated!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}