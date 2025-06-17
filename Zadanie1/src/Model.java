import java.util.*;

public class Model {

    /**
     * Главный публичный метод для вычисления выражения.
     * @param expressionString Входная строка с уравнением.
     * @return Результат вычисления.
     * @throws Exception В случае ошибки в выражении (синтаксис, деление на ноль).
     */
    public double calculate(String expressionString) throws Exception {
        // Заменяем оператор "//" на один символ, чтобы упростить токенизацию
        String preparedExpression = expressionString.replace("//", "#");
        List<String> tokens = tokenize(preparedExpression);
        List<String> rpn = convertToRPN(tokens);
        return evaluateRPN(rpn);
    }

    /**
     * Разбивает строку на токены (числа, операторы, скобки).
     */
    private List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentNumber = new StringBuilder();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                currentNumber.append(c);
            } else {
                if (currentNumber.length() > 0) {
                    tokens.add(currentNumber.toString());
                    currentNumber.setLength(0);
                }
                // Обработка унарного минуса
                if (c == '-' && (tokens.isEmpty() || isOperator(tokens.get(tokens.size() - 1)) || tokens.get(tokens.size() - 1).equals("("))) {
                    currentNumber.append(c);
                } else {
                    tokens.add(String.valueOf(c));
                }
            }
        }

        if (currentNumber.length() > 0) {
            tokens.add(currentNumber.toString());
        }

        return tokens;
    }

    /**
     * Конвертирует список токенов из инфиксной нотации в Обратную Польскую Нотацию (RPN).
     * Алгоритм "Сортировочная станция".
     */
    private List<String> convertToRPN(List<String> tokens) {
        List<String> outputQueue = new ArrayList<>();
        Stack<String> operatorStack = new Stack<>();

        for (String token : tokens) {
            if (isNumber(token)) {
                outputQueue.add(token);
            } else if (isOperator(token)) {
                while (!operatorStack.isEmpty() && isOperator(operatorStack.peek()) &&
                        getPrecedence(operatorStack.peek()) >= getPrecedence(token)) {
                    outputQueue.add(operatorStack.pop());
                }
                operatorStack.push(token);
            } else if (token.equals("(")) {
                operatorStack.push(token);
            } else if (token.equals(")")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    outputQueue.add(operatorStack.pop());
                }
                if (!operatorStack.isEmpty()) {
                    operatorStack.pop(); // Выкидываем '('
                }
            }
        }

        while (!operatorStack.isEmpty()) {
            outputQueue.add(operatorStack.pop());
        }

        return outputQueue;
    }

    /**
     * Вычисляет результат из списка токенов в RPN.
     */
    private double evaluateRPN(List<String> rpnTokens) throws ArithmeticException {
        Stack<Double> valueStack = new Stack<>();

        for (String token : rpnTokens) {
            if (isNumber(token)) {
                valueStack.push(Double.parseDouble(token));
            } else if (isOperator(token)) {
                if (valueStack.size() < 2) {
                    throw new IllegalArgumentException("Invalid expression: missing operands for operator " + token);
                }
                double rightOperand = valueStack.pop();
                double leftOperand = valueStack.pop();
                double result = 0;

                switch (token) {
                    case "+":
                        result = leftOperand + rightOperand;
                        break;
                    case "-":
                        result = leftOperand - rightOperand;
                        break;
                    case "*":
                        result = leftOperand * rightOperand;
                        break;
                    case "/":
                        if (rightOperand == 0) throw new ArithmeticException("Division by zero.");
                        result = leftOperand / rightOperand;
                        break;
                    case "^":
                        result = Math.pow(leftOperand, rightOperand);
                        break;
                    case "#": // Наш оператор для целочисленного деления
                        if (rightOperand == 0) throw new ArithmeticException("Division by zero.");
                        result = Math.floor(leftOperand / rightOperand);
                        break;
                }
                valueStack.push(result);
            }
        }
        if (valueStack.size() != 1) {
            throw new IllegalArgumentException("Invalid expression: too many operands.");
        }
        return valueStack.pop();
    }

    // Вспомогательные методы
    private boolean isNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isOperator(String token) {
        return "+-*/^#".contains(token);
    }

    private int getPrecedence(String operator) {
        switch (operator) {
            case "^":
                return 3;
            case "*":
            case "/":
            case "#": // целочисленное деление
                return 2;
            case "+":
            case "-":
                return 1;
            default:
                return 0;
        }
    }
}