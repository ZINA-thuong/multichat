package chatcloud;


import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleServer extends WebSocketServer {
    // Lưu danh sách các máy đang kết nối
    private static Set<WebSocket> conns = Collections.newSetFromMap(new ConcurrentHashMap<WebSocket, Boolean>());

    public SimpleServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conns.add(conn);
        System.out.println("Có kết nối mới: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        conns.remove(conn);
        System.out.println("Một máy đã ngắt kết nối.");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // Khi nhận tin từ 1 máy, gửi lại cho TẤT CẢ các máy khác
        for (WebSocket sock : conns) {
            sock.send(message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) { ex.printStackTrace(); }

    @Override
    public void onStart() { System.out.println("Server đã sẵn sàng tại cổng: " + getPort()); }

    public static void main(String[] args) {
        // Thay vì fix cứng 8888, hãy đọc từ hệ thống
        String portEnv = System.getenv("PORT");
        int port = (portEnv != null) ? Integer.parseInt(portEnv) : 8888;
        SimpleServer server = new SimpleServer(port);
        server.start();
    }
}