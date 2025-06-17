import javax.swing.*;

public class App {
    public static void main(String[] args) {
        // Запуск GUI в потоке диспетчеризации событий (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            // Создание компонентов MVC
            Model model = new Model();
            View view = new View();
            Controller controller = new Controller(model, view);

            // Делаем View видимым
            view.setVisible(true);
        });
    }
}