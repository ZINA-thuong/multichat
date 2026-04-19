package chatcloud;


import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatController {
    private WebEngine webEngine;
    private WebSocketService wsClient;
    private String userName;

    public ChatController(WebEngine webEngine) {
        this.webEngine = webEngine;
    }

    /**
     * Hàm này được gọi từ JavaScript (index.html) khi người dùng nhấn "Tham gia"
     */
    public void setUserAndJoin(String name) {
        this.userName = name;
        try {
            // Khởi tạo kết nối WebSocket tới Server (Thay localhost bằng IP Cloud nếu cần)
            // Ví dụ: "ws://123.456.78.9:8888"
            wsClient = new WebSocketService("ws://localhost:8888", this::handleIncomingMessage);
            
            // Lệnh kết nối không đồng bộ
            wsClient.connect();

            // Tư duy thực chiến: Kết nối mạng cần thời gian (Handshake).
            // Chúng ta đợi kết nối mở thành công rồi mới gửi tin báo danh (JOIN).
            new Thread(() -> {
                try {
                    int retry = 0;
                    while (!wsClient.isOpen() && retry < 10) {
                        Thread.sleep(500); // Đợi 0.5s mỗi lần kiểm tra
                        retry++;
                    }
                    if (wsClient.isOpen()) {
                        wsClient.sendMessage("JOIN:" + userName);
                    } else {
                        Platform.runLater(() -> 
                            webEngine.executeScript("addSystemMessage('Lỗi: Không thể kết nối tới Server!')")
                        );
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Xử lý dữ liệu nhận được từ WebSocketService
     */
    private void handleIncomingMessage(String rawData) {
        // Luôn phải dùng Platform.runLater khi muốn cập nhật giao diện từ luồng mạng
        Platform.runLater(() -> {
            if (rawData.startsWith("JOIN:")) {
                String name = rawData.substring(5);
                webEngine.executeScript(String.format("addSystemMessage('%s đã tham gia phòng chat')", name));
            } 
            else if (rawData.startsWith("LEAVE:")) {
                String name = rawData.substring(6);
                webEngine.executeScript(String.format("addSystemMessage('%s đã rời phòng chat')", name));
            } 
            else {
                // Xử lý tin nhắn chat thông thường (Định dạng: Tên:Nội dung)
                String[] parts = rawData.split(":", 2);
                if (parts.length == 2) {
                    String sender = parts[0];
                    String content = parts[1];
                    String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                    boolean isMe = sender.equals(userName);

                    webEngine.executeScript(String.format(
                        "addMessageToHTML('%s', '%s', '%s', %b)", 
                        sender, content, time, isMe
                    ));
                }
            }
        });
    }

    /**
     * Hàm gọi từ JavaScript khi người dùng nhấn "Gửi"
     */
    public void sendFromHTML(String text) {
        if (wsClient != null && wsClient.isOpen()) {
            wsClient.sendMessage(userName + ":" + text);
        } else {
            webEngine.executeScript("addSystemMessage('Mất kết nối, không thể gửi tin nhắn!')");
        }
    }

    /**
     * Gọi khi đóng ứng dụng (từ Main.java)
     */
    public void sendLeaveMessage() {
        if (wsClient != null && wsClient.isOpen() && userName != null) {
            wsClient.sendMessage("LEAVE:" + userName);
            wsClient.close();
        }
    }
}
