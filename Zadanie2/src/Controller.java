import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Controller {
    private Model model;
    private View view;

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
        this.view.addCalculateListener(new CalculateListener());
    }

    /**
     * Проверяет, что количество открывающих и закрывающих скобок совпадает.
     * @param expression Строка с уравнением.
     * @return true, если скобки сбалансированы, иначе false.
     */
    private boolean areBracketsBalanced(String expression) {
        int balance = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') {
                balance++;
            } else if (c == ')') {
                balance--;
            }
            // Если в какой-то момент закрывающих скобок стало больше, выражение неверно
            if (balance < 0) {
                return false;
            }
        }
        // В конце баланс должен быть равен нулю
        return balance == 0;
    }

    class CalculateListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Удаляем все пробелы для упрощения обработки
            String expression = view.getExpression().trim().replaceAll("\\s+", "");

            if (expression.isEmpty()) {
                view.setResult("Ошибка: поле ввода не может быть пустым.");
                return;
            }

            // 1. Новая проверка: правильное количество скобок (требование 2.g)
            if (!areBracketsBalanced(expression)) {
                view.setResult("Ошибка: количество открывающих и закрывающих скобок не совпадает.");
                return;
            }

            // 2. Выполнение расчета через Model
            try {
                double result = model.calculate(expression);
                // Форматируем результат для красивого вывода
                if (result == (long) result) {
                    view.setResult(String.format("%d", (long)result));
                } else {
                    view.setResult(String.format("%.4f", result));
                }
            } catch (Exception ex) {
                // 3. Обработка любых ошибок от Model (включая проверку на число слагаемых)
                view.setResult("Ошибка: " + ex.getMessage());
            }
        }
    }
}