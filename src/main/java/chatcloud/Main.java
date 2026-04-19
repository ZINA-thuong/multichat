package chatcloud;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class Main extends Application {
    private ChatController controller;

    @Override
    public void start(Stage stage) {
        // 1. Khởi tạo WebView (Thành phần hiển thị HTML)
        WebView webView = new WebView();
        var webEngine = webView.getEngine();

        // 2. Khởi tạo Controller (Điều khiển logic kết nối WebSocket)
        controller = new ChatController(webEngine);

        // 3. Thiết lập Bridge giữa Java và JavaScript
        webEngine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == javafx.concurrent.Worker.State.SUCCEEDED) {
                // Lấy đối tượng window của trình duyệt HTML
                JSObject window = (JSObject) webEngine.executeScript("window");
                // Đặt tên "javaApp" để trong HTML có thể gọi: window.javaApp.setUserAndJoin(...)
                window.setMember("javaApp", controller);
            }
        });

        // 4. Load file giao diện HTML từ thư mục resources
        try {
            String url = getClass().getResource("/index.html").toExternalForm();
            webEngine.load(url);
        } catch (Exception e) {
            System.err.println("Không tìm thấy file index.html trong resources!");
            e.printStackTrace();
        }

        // 5. Xử lý sự kiện khi người dùng đóng cửa sổ ứng dụng
        stage.setOnCloseRequest(e -> {
            System.out.println("Đang đóng ứng dụng...");
            if (controller != null) {
                controller.sendLeaveMessage(); // Thông báo cho Server máy này thoát
            }
            Platform.exit();
            System.exit(0);
        });

        // 6. Thiết lập kích thước cửa sổ và hiển thị
        Scene scene = new Scene(webView, 420, 650);
        stage.setScene(scene);
        stage.setTitle("Sky Chat - WebSocket Edition");
        
        // Đảm bảo cửa sổ hiện lên trên cùng khi khởi chạy
        stage.show();
    }

    public static void main(String[] args) {
        // Kích hoạt ứng dụng JavaFX
        launch(args);
    }
}