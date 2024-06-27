import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.text.DecimalFormat;
import java.util.Stack;

public class CalculatorController {

    @FXML
    private Button minBtn, maxBtn, closeBtn;
    @FXML
    private TextField resultField;
    @FXML
    private TextField resultStatement;
    @FXML
    private ToggleButton themeSwitchButton;
    @FXML
    private Button subBtn;

    @FXML
    public void initialize() {
        resultField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                evaluateExpression();
            }
        });
        Platform.runLater(() -> resultField.requestFocus());

        resultField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!isAllowedInput(event.getCharacter())) {
                event.consume(); 
            }
        });

        // Debugging to check if subBtn is correctly initialized
        assert subBtn != null : "fx:id=\"subBtn\" was not injected: check your FXML file 'Calculator.fxml'.";
    }

    private boolean isAllowedInput(String str) {
        return str.matches("[0-9+\\-*/%^.]"); 
    }

    // Window control methods
    public void handleMinButtonClick(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true); // Minimize the window
    }

    public void handleMaxButtonClick(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setMaximized(!stage.isMaximized()); 
    }

    public void handleCloseButtonClick(ActionEvent event) {
        Node source = (Node) event.getSource();
        ((Stage) source.getScene().getWindow()).close();
    }

    // Button click handling methods

    public void handleButtonClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonText = clickedButton.getText();
        String currentText = resultField.getText();

        if (resultField.getText().equals("Error")) {
            resultField.setText(""); 
        }

        switch (buttonText) {
            case "<":
                backspaceHandle();
                break;
            case "=":
                evaluateExpression();
                break;
            case "%":
                if (!currentText.isEmpty()) {
                    double currentValue = Double.parseDouble(currentText);
                    resultField.setText(String.valueOf(currentValue / 100));
                }
                break;
            default:
                if (buttonText.matches("[0-9.]")) { 
                    resultField.setText(currentText + buttonText);
                } else if (buttonText.equals("-")) {
               
                    if (currentText.isEmpty() || currentText.endsWith("+") || currentText.endsWith("-")
                            || currentText.endsWith("*") || currentText.endsWith("/") || currentText.endsWith("%")
                            || currentText.endsWith("^")
                            || Character.isDigit(currentText.charAt(currentText.length() - 1))
                            || currentText.endsWith("(")) {
                        resultField.setText(currentText + buttonText);
                    }
                } else if (buttonText.matches("[+*/^]")) {
                 
                    if (!currentText.isEmpty() && !currentText.endsWith("+") && !currentText.endsWith("-")
                            && !currentText.endsWith("*") && !currentText.endsWith("/") && !currentText.endsWith("%")
                            && !currentText.endsWith("^")) {
                        resultField.setText(currentText + buttonText);
                    }
                }
                break;
        }
    }

    public void handleClearClick(ActionEvent event) {
        resultField.setText("");
        resultStatement.setText("");
        resultField.setEditable(true);
        resultField.positionCaret(0);
    }

    private void evaluateExpression() {
        try {
            String expression = resultField.getText();
            double result = eval(expression);
            resultStatement.setText(expression);
            if (result == (long) result) {
                resultField.setText(String.valueOf((long) result));
            } else {
                resultField.setText(formatDouble(result));
            }
            resultField.positionCaret(resultField.getText().length());
        } catch (Exception e) {
            resultField.setText("Error");
        }
    }

    private void backspaceHandle() {
        String currentText = resultField.getText();
        if (!currentText.isEmpty()) {
            resultField.setText(currentText.substring(0, currentText.length() - 1));
        }
    }

    private String formatDouble(double value) {
        DecimalFormat df = new DecimalFormat("#.####");
        return df.format(value);
    }

    private double eval(String expression) {
        char[] tokens = expression.toCharArray();

        Stack<Double> values = new Stack<>();

        Stack<Character> ops = new Stack<>();

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] == ' ')
                continue;

            if ((tokens[i] >= '0' && tokens[i] <= '9') || tokens[i] == '.') {
                StringBuilder sbuf = new StringBuilder();
                while (i < tokens.length && ((tokens[i] >= '0' && tokens[i] <= '9') || tokens[i] == '.'))
                    sbuf.append(tokens[i++]);
                values.push(Double.parseDouble(sbuf.toString()));
                i--;
            } else if (tokens[i] == '(') {
                ops.push(tokens[i]);
            } else if (tokens[i] == ')') {
                while (ops.peek() != '(')
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                ops.pop();
            } else if (tokens[i] == '-'
                    && (i == 0 || (i > 0 && !Character.isDigit(tokens[i - 1]) && tokens[i - 1] != ')'))) {

                StringBuilder sbuf = new StringBuilder();
                sbuf.append(tokens[i++]);
                while (i < tokens.length && (tokens[i] >= '0' && tokens[i] <= '9' || tokens[i] == '.'))
                    sbuf.append(tokens[i++]);
                values.push(Double.parseDouble(sbuf.toString()));
                i--;
            } else if (tokens[i] == '+' || tokens[i] == '-' || tokens[i] == '*' || tokens[i] == '/' || tokens[i] == '^'
                    || tokens[i] == '%') {

                while (!ops.empty() && hasPrecedence(tokens[i], ops.peek()))
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                ops.push(tokens[i]);
            }
        }
        while (!ops.empty())
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));

        return values.pop();
    }

    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')')
            return false;
        if ((op1 == '*' || op1 == '/' || op1 == '%' || op1 == '^') && (op2 == '+' || op2 == '-'))
            return false;
        if (op1 == '^' && (op2 == '*' || op2 == '/' || op2 == '%'))
            return false;
        return true;
    }
    private double applyOp(char op, double b, double a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0)
                    throw new UnsupportedOperationException("Cannot divide by zero");
                return a / b;
            case '^':
                return Math.pow(a, b);
        }
        return 0;
    }
}
