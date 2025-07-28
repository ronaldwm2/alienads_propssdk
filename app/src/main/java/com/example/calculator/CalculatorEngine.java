package com.example.calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CalculatorEngine {
    private static final int PRECISION = 15;
    private static final MathContext MATH_CONTEXT = new MathContext(PRECISION, RoundingMode.HALF_UP);
    
    private List<String> history;
    private boolean lastWasError;
    
    public CalculatorEngine() {
        this.history = new ArrayList<>();
        this.lastWasError = false;
    }
    
    /**
     * Evaluates a mathematical expression with support for:
     * - Basic operations (+, -, *, /)
     * - Parentheses
     * - Scientific operations (sin, cos, tan, log, ln, sqrt, ^)
     * - Constants (pi, e)
     */
    public CalculationResult evaluate(String expression) {
        try {
            lastWasError = false;
            
            // Preprocessing: handle constants and functions
            expression = preprocessExpression(expression);
            
            // Validate expression
            String validationError = validateExpression(expression);
            if (validationError != null) {
                lastWasError = true;
                return new CalculationResult(validationError, true);
            }
            
            // Convert to postfix notation and evaluate
            BigDecimal result = evaluateExpression(expression);
            
            // Format result
            String formattedResult = formatResult(result);
            
            // Add to history
            addToHistory(expression + " = " + formattedResult);
            
            return new CalculationResult(formattedResult, false);
            
        } catch (ArithmeticException e) {
            lastWasError = true;
            return new CalculationResult("Math Error: " + e.getMessage(), true);
        } catch (Exception e) {
            lastWasError = true;
            return new CalculationResult("Error: Invalid expression", true);
        }
    }
    
    private String preprocessExpression(String expression) {
        // Replace mathematical constants
        expression = expression.replace("π", String.valueOf(Math.PI));
        expression = expression.replace("pi", String.valueOf(Math.PI));
        expression = expression.replace("e", String.valueOf(Math.E));
        
        // Handle implicit multiplication (e.g., "2π" becomes "2*π")
        expression = addImplicitMultiplication(expression);
        
        return expression;
    }
    
    private String addImplicitMultiplication(String expression) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            char current = expression.charAt(i);
            result.append(current);
            
            if (i < expression.length() - 1) {
                char next = expression.charAt(i + 1);
                
                // Add * between number and letter, letter and number, ) and (, etc.
                if ((Character.isDigit(current) && Character.isLetter(next)) ||
                    (Character.isLetter(current) && Character.isDigit(next)) ||
                    (current == ')' && (Character.isDigit(next) || Character.isLetter(next) || next == '(')) ||
                    ((Character.isDigit(current) || Character.isLetter(current)) && next == '(')) {
                    result.append('*');
                }
            }
        }
        return result.toString();
    }
    
    private String validateExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return "Empty expression";
        }
        
        // Check for balanced parentheses
        int parenthesesCount = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') parenthesesCount++;
            else if (c == ')') parenthesesCount--;
            if (parenthesesCount < 0) return "Mismatched parentheses";
        }
        if (parenthesesCount != 0) return "Mismatched parentheses";
        
        // Check for consecutive operators
        if (expression.matches(".*[+\\-*/^]{2,}.*")) {
            return "Invalid operator sequence";
        }
        
        return null; // No errors
    }
    
    private BigDecimal evaluateExpression(String expression) {
        // Convert infix to postfix notation using Shunting Yard algorithm
        List<String> postfix = infixToPostfix(expression);
        
        // Evaluate postfix expression
        return evaluatePostfix(postfix);
    }
    
    private List<String> infixToPostfix(String expression) {
        List<String> output = new ArrayList<>();
        Stack<String> operatorStack = new Stack<>();
        
        int i = 0;
        while (i < expression.length()) {
            char c = expression.charAt(i);
            
            if (Character.isDigit(c) || c == '.') {
                // Parse number
                StringBuilder number = new StringBuilder();
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    number.append(expression.charAt(i));
                    i++;
                }
                output.add(number.toString());
                continue;
            } else if (Character.isLetter(c)) {
                // Parse function name
                StringBuilder function = new StringBuilder();
                while (i < expression.length() && Character.isLetter(expression.charAt(i))) {
                    function.append(expression.charAt(i));
                    i++;
                }
                operatorStack.push(function.toString());
            } else if (c == '(') {
                operatorStack.push(String.valueOf(c));
            } else if (c == ')') {
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    output.add(operatorStack.pop());
                }
                if (!operatorStack.isEmpty()) operatorStack.pop(); // Remove '('
                
                // If there's a function on top of stack, add it to output
                if (!operatorStack.isEmpty() && isFunction(operatorStack.peek())) {
                    output.add(operatorStack.pop());
                }
            } else if (isOperator(String.valueOf(c))) {
                while (!operatorStack.isEmpty() && 
                       !operatorStack.peek().equals("(") &&
                       getPrecedence(operatorStack.peek()) >= getPrecedence(String.valueOf(c))) {
                    output.add(operatorStack.pop());
                }
                operatorStack.push(String.valueOf(c));
            }
            i++;
        }
        
        while (!operatorStack.isEmpty()) {
            output.add(operatorStack.pop());
        }
        
        return output;
    }
    
    private BigDecimal evaluatePostfix(List<String> postfix) {
        Stack<BigDecimal> stack = new Stack<>();
        
        for (String token : postfix) {
            if (isNumber(token)) {
                stack.push(new BigDecimal(token));
            } else if (isOperator(token)) {
                if (stack.size() < 2) throw new IllegalArgumentException("Invalid expression");
                BigDecimal b = stack.pop();
                BigDecimal a = stack.pop();
                stack.push(performOperation(a, b, token));
            } else if (isFunction(token)) {
                if (stack.isEmpty()) throw new IllegalArgumentException("Invalid expression");
                BigDecimal operand = stack.pop();
                stack.push(performFunction(operand, token));
            }
        }
        
        if (stack.size() != 1) throw new IllegalArgumentException("Invalid expression");
        return stack.pop();
    }
    
    private BigDecimal performOperation(BigDecimal a, BigDecimal b, String operator) {
        switch (operator) {
            case "+":
                return a.add(b, MATH_CONTEXT);
            case "-":
                return a.subtract(b, MATH_CONTEXT);
            case "*":
                return a.multiply(b, MATH_CONTEXT);
            case "/":
                if (b.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a.divide(b, MATH_CONTEXT);
            case "^":
                return BigDecimal.valueOf(Math.pow(a.doubleValue(), b.doubleValue()));
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }
    
    private BigDecimal performFunction(BigDecimal operand, String function) {
        double value = operand.doubleValue();
        
        switch (function.toLowerCase()) {
            case "sin":
                return BigDecimal.valueOf(Math.sin(Math.toRadians(value)));
            case "cos":
                return BigDecimal.valueOf(Math.cos(Math.toRadians(value)));
            case "tan":
                return BigDecimal.valueOf(Math.tan(Math.toRadians(value)));
            case "log":
                if (value <= 0) throw new ArithmeticException("Logarithm of non-positive number");
                return BigDecimal.valueOf(Math.log10(value));
            case "ln":
                if (value <= 0) throw new ArithmeticException("Natural logarithm of non-positive number");
                return BigDecimal.valueOf(Math.log(value));
            case "sqrt":
                if (value < 0) throw new ArithmeticException("Square root of negative number");
                return BigDecimal.valueOf(Math.sqrt(value));
            case "abs":
                return operand.abs();
            default:
                throw new IllegalArgumentException("Unknown function: " + function);
        }
    }
    
    private boolean isNumber(String token) {
        try {
            new BigDecimal(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean isOperator(String token) {
        return token.matches("[+\\-*/^]");
    }
    
    private boolean isFunction(String token) {
        return token.matches("(?i)(sin|cos|tan|log|ln|sqrt|abs)");
    }
    
    private int getPrecedence(String operator) {
        switch (operator) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            case "^":
                return 3;
            default:
                return 0;
        }
    }
    
    private String formatResult(BigDecimal result) {
        // Remove trailing zeros and decimal point if not needed
        String resultStr = result.stripTrailingZeros().toPlainString();
        
        // Handle very large or very small numbers with scientific notation
        if (resultStr.length() > 12 || (result.abs().compareTo(new BigDecimal("0.0001")) < 0 && 
                                       result.compareTo(BigDecimal.ZERO) != 0)) {
            return String.format("%.6E", result.doubleValue());
        }
        
        return resultStr;
    }
    
    private void addToHistory(String calculation) {
        history.add(calculation);
        if (history.size() > 50) { // Keep only last 50 calculations
            history.remove(0);
        }
    }
    
    public List<String> getHistory() {
        return new ArrayList<>(history);
    }
    
    public void clearHistory() {
        history.clear();
    }
    
    public boolean wasLastError() {
        return lastWasError;
    }
    
    public static class CalculationResult {
        private final String result;
        private final boolean isError;
        
        public CalculationResult(String result, boolean isError) {
            this.result = result;
            this.isError = isError;
        }
        
        public String getResult() {
            return result;
        }
        
        public boolean isError() {
            return isError;
        }
    }
}