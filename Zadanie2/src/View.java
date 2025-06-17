import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class View extends JFrame {
    private JTextField expressionField;
    private JButton calculateButton;
    private JLabel resultLabel;

    public View() {
        setTitle("MVC Advanced Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 200);
        setLocationRelativeTo(null);

        expressionField = new JTextField(30);
        calculateButton = new JButton("Рассчитать");
        JLabel inputLabel = new JLabel("Введите уравнение:");
        resultLabel = new JLabel("Результат: ");

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        panel.add(inputLabel);
        panel.add(expressionField);
        panel.add(calculateButton);
        panel.add(resultLabel);

        this.add(panel);
    }

    public String getExpression() {
        return expressionField.getText();
    }

    public void setResult(String result) {
        resultLabel.setText("Результат: " + result);
    }

    public void addCalculateListener(ActionListener listener) {
        calculateButton.addActionListener(listener);
    }
}