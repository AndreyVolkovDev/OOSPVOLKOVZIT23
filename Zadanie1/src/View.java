import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class View extends JFrame {
    private JTextField expressionField;
    private JButton calculateButton;
    private JLabel resultLabel;

    public View() {
        // Настройка основного окна
        setTitle("MVC Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 200);
        setLocationRelativeTo(null);

        // Создание компонентов
        expressionField = new JTextField(30);
        calculateButton = new JButton("Рассчитать");
        JLabel inputLabel = new JLabel("Введите уравнение:");
        resultLabel = new JLabel("Результат: ");

        // Настройка панели
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Добавление компонентов на панель
        panel.add(inputLabel);
        panel.add(expressionField);
        panel.add(calculateButton);
        panel.add(resultLabel);

        // Добавление панели в окно
        this.add(panel);
    }

    // Геттер для получения введенного выражения
    public String getExpression() {
        return expressionField.getText();
    }

    // Сеттер для отображения результата
    public void setResult(String result) {
        resultLabel.setText("Результат: " + result);
    }

    // Метод для добавления "слушателя" на кнопку (связь с Controller)
    public void addCalculateListener(ActionListener listener) {
        calculateButton.addActionListener(listener);
    }
}