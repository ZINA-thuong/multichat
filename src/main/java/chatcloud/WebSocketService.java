package chatcloud;



import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.function.Consumer;

public class WebSocketService extends WebSocketClient {
    private Consumer<String> onMessageReceived;

    public WebSocketService(String serverUri, Consumer<String> callback) throws Exception {
        super(new URI(serverUri));
        this.onMessageReceived = callback;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Đã kết nối tới Cloud Server!");
    }

    @Override
    public void onMessage(String message) {
        // Đẩy tin nhắn nhận được từ Server về Controller để hiển thị lên HTML
        onMessageReceived.accept(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Mất kết nối Server.");
    }

    @Override
    public void onError(Exception ex) { ex.printStackTrace(); }
    
    // Ghi đè phương thức send của thư viện
    public void sendMessage(String text) {
        if (this.isOpen()) {
            this.send(text);
        }
    }
}