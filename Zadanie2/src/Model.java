import java.util.*;

public class Model {

    private static final int MAX_TERMS = 15;

    /**
     * Главный публичный метод для вычисления выражения.
     * @param expressionString Входная строка с уравнением.
     * @return Результат вычисления.
     * @throws Exception В случае ошибки в выражении.
     */
    public double calculate(String expressionString) throws Exception {
        // Предобработка: заменяем операторы для унификации
        String preparedExpression = expressionString.replace("**", "^").replace("//", "#");

        List<String> tokens = tokenize(preparedExpression);

        // Проверка на количество слагаемых (требование 3)
        long termCount = tokens.stream().filter(this::isNumber).count();
        if (termCount > MAX_TERMS) {
            throw new IllegalArgumentException("Количество слагаемых не должно превышать " + MAX_TERMS + " (найдено: " + termCount + ").");
        }

        List<String> rpn = convertToRPN(tokens);
        return evaluateRPN(rpn);
    }

    /**
     * Разбивает строку на токены (числа, операторы, функции, скобки).
     */
    private List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                buffer.append(c);
            } else if (Character.isLetter(c)) {
                buffer.append(c);
            } else {
                if (buffer.length() > 0) {
                    tokens.add(buffer.toString());
                    buffer.setLength(0);
                }
                // Обработка унарного минуса
                if (c == '-' && (tokens.isEmpty() || isOperator(tokens.get(tokens.size() - 1)) || tokens.get(tokens.size() - 1).equals("("))) {
                    buffer.append(c); // Начинаем собирать отрицательное число
                } else {
                    tokens.add(String.valueOf(c));
                }
            }
        }

        if (buffer.length() > 0) {
            tokens.add(buffer.toString());
        }

        return tokens;
    }

    /**
     * Конвертирует список токенов из инфиксной нотации в Обратную Польскую Нотацию (RPN).
     */
    private List<String> convertToRPN(List<String> tokens) {
        List<String> outputQueue = new ArrayList<>();
        Stack<String> operatorStack = new Stack<>();

        for (String token : tokens) {
            if (isNumber(token)) {
                outputQueue.add(token);
            } else if (isFunction(token)) {
                operatorStack.push(token);
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
                if (operatorStack.isEmpty()) throw new IllegalArgumentException("Ошибка в расстановке скобок (нет открывающей).");
                operatorStack.pop(); // Выкидываем '('

                // Если после скобки на стеке оказалась функция, выталкиваем и ее
                if (!operatorStack.isEmpty() && isFunction(operatorStack.peek())) {
                    outputQueue.add(operatorStack.pop());
                }
            }
        }

        while (!operatorStack.isEmpty()) {
            String op = operatorStack.pop();
            if (op.equals("(")) throw new IllegalArgumentException("Ошибка в расстановке скобок (нет закрывающей).");
            outputQueue.add(op);
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
            } else if (isFunction(token) || isOperator(token)) {
                double result;
                // Унарные операторы/функции
                if (token.equals("!") || isFunction(token)) {
                    if (valueStack.isEmpty()) throw new IllegalArgumentException("Недостаточно операндов для функции " + token);
                    double operand = valueStack.pop();
                    switch (token) {
                        case "!":
                            result = factorial(operand);
                            break;
                        case "log": // Логарифм по основанию 2
                            if (operand <= 0) throw new ArithmeticException("Аргумент логарифма должен быть > 0.");
                            result = Math.log(operand) / Math.log(2);
                            break;
                        case "exp":
                            result = Math.exp(operand);
                            break;
                        default:
                            throw new IllegalArgumentException("Неизвестная функция: " + token);
                    }
                } else { // Бинарные операторы
                    if (valueStack.size() < 2) throw new IllegalArgumentException("Недостаточно операндов для оператора " + token);
                    double rightOperand = valueStack.pop();
                    double leftOperand = valueStack.pop();
                    switch (token) {
                        case "+": result = leftOperand + rightOperand; break;
                        case "-": result = leftOperand - rightOperand; break;
                        case "*": result = leftOperand * rightOperand; break;
                        case "/":
                            if (rightOperand == 0) throw new ArithmeticException("Деление на ноль.");
                            result = leftOperand / rightOperand;
                            break;
                        case "^": result = Math.pow(leftOperand, rightOperand); break;
                        case "#":
                            if (rightOperand == 0) throw new ArithmeticException("Деление на ноль.");
                            result = Math.floor(leftOperand / rightOperand);
                            break;
                        default: throw new IllegalArgumentException("Неизвестный оператор: " + token);
                    }
                }
                valueStack.push(result);
            }
        }
        if (valueStack.size() != 1) {
            throw new IllegalArgumentException("Выражение составлено некорректно.");
        }
        return valueStack.pop();
    }

    // --- Вспомогательные методы ---

    private double factorial(double n) {
        if (n < 0 || n != Math.floor(n)) {
            throw new ArithmeticException("Факториал определен только для неотрицательных целых чисел.");
        }
        if (n > 20) { // Избегаем переполнения long
            throw new ArithmeticException("Слишком большое число для вычисления факториала (> 20).");
        }
        double result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    private boolean isNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isFunction(String token) {
        return token.equalsIgnoreCase("log") || token.equalsIgnoreCase("exp");
    }

    private boolean isOperator(String token) {
        return "+-*/^#!".contains(token);
    }

    private int getPrecedence(String operator) {
        switch (operator) {
            case "!":
                return 4; // У факториала высший приоритет
            case "^":
                return 3;
            case "*":
            case "/":
            case "#":
                return 2;
            case "+":
            case "-":
                return 1;
            default:
                return 0;
        }
    }
}