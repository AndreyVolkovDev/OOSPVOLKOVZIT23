import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Controller {
    private Model model;
    private View view;

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;

        // Добавляем слушателя к кнопке в View.
        // При нажатии будет выполняться код внутри actionPerformed.
        this.view.addCalculateListener(new CalculateListener());
    }

    class CalculateListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String expression = view.getExpression().trim().replaceAll("\\s+", "");

            // 1. Валидация по требованиям
            if (expression.isEmpty()) {
                view.setResult("Ошибка: поле ввода не может быть пустым.");
                return;
            }
            if (!Character.isDigit(expression.charAt(expression.length() - 1)) && expression.charAt(expression.length() - 1) != ')') {
                view.setResult("Ошибка: уравнение должно заканчиваться числом или скобкой.");
                return;
            }
            // Упрощенная проверка на начало (допускаем унарный минус)
            if (!Character.isDigit(expression.charAt(0)) && expression.charAt(0) != '-' && expression.charAt(0) != '(') {
                view.setResult("Ошибка: уравнение должно начинаться с числа, унарного минуса или скобки.");
                return;
            }

            // 2. Выполнение расчета через Model
            try {
                double result = model.calculate(expression);
                view.setResult(String.valueOf(result));
            } catch (Exception ex) {
                // 3. Обработка ошибок от Model
                view.setResult("Ошибка: " + ex.getMessage());
            }
        }
    }
}