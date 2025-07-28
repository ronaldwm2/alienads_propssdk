package com.example.calculator;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView display;
    private TextView expressionDisplay;
    private CalculatorEngine engine;
    private StringBuilder currentExpression;
    private boolean lastWasEquals = false;
    private boolean isInScientificMode = false;
    private GestureDetector gestureDetector;
    private SharedPreferences preferences;
    
    // Animation and UI state
    private boolean isDarkTheme = false;
    private List<String> calculationHistory;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeComponents();
        setupGestures();
        loadPreferences();
        setupButtons();
        updateTheme();
    }
    
    private void initializeComponents() {
        display = findViewById(R.id.display);
        expressionDisplay = findViewById(R.id.expressionDisplay);
        engine = new CalculatorEngine();
        currentExpression = new StringBuilder();
        calculationHistory = new ArrayList<>();
        preferences = getSharedPreferences("calculator_prefs", MODE_PRIVATE);
    }
    
    private void setupGestures() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                toggleTheme();
                return true;
            }
            
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(velocityX) > Math.abs(velocityY)) {
                    if (velocityX > 0) {
                        // Swipe right - show history
                        showHistory();
                    } else {
                        // Swipe left - clear entry
                        clearEntry();
                    }
                    return true;
                }
                return false;
            }
        });
        
        display.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }
    
    private void loadPreferences() {
        isDarkTheme = preferences.getBoolean("dark_theme", false);
        isInScientificMode = preferences.getBoolean("scientific_mode", false);
    }
    
    private void savePreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("dark_theme", isDarkTheme);
        editor.putBoolean("scientific_mode", isInScientificMode);
        editor.apply();
    }

    private void setupButtons() {
        // Number buttons with improved handling
        findViewById(R.id.btn0).setOnClickListener(v -> onNumberClick("0"));
        findViewById(R.id.btn1).setOnClickListener(v -> onNumberClick("1"));
        findViewById(R.id.btn2).setOnClickListener(v -> onNumberClick("2"));
        findViewById(R.id.btn3).setOnClickListener(v -> onNumberClick("3"));
        findViewById(R.id.btn4).setOnClickListener(v -> onNumberClick("4"));
        findViewById(R.id.btn5).setOnClickListener(v -> onNumberClick("5"));
        findViewById(R.id.btn6).setOnClickListener(v -> onNumberClick("6"));
        findViewById(R.id.btn7).setOnClickListener(v -> onNumberClick("7"));
        findViewById(R.id.btn8).setOnClickListener(v -> onNumberClick("8"));
        findViewById(R.id.btn9).setOnClickListener(v -> onNumberClick("9"));
        
        // Operation buttons
        findViewById(R.id.btnAdd).setOnClickListener(v -> onOperatorClick("+"));
        findViewById(R.id.btnSubtract).setOnClickListener(v -> onOperatorClick("-"));
        findViewById(R.id.btnMultiply).setOnClickListener(v -> onOperatorClick("*"));
        findViewById(R.id.btnDivide).setOnClickListener(v -> onOperatorClick("/"));
        
        // Special buttons
        findViewById(R.id.btnEquals).setOnClickListener(v -> onEqualsClick());
        findViewById(R.id.btnClear).setOnClickListener(v -> onClearClick());
        findViewById(R.id.btnDecimal).setOnClickListener(v -> onDecimalClick());
        
        // Long press for additional functionality
        findViewById(R.id.btnClear).setOnLongClickListener(v -> {
            clearAll();
            return true;
        });
        
        // Add button animations
        addButtonAnimations();
    }
    
    private void addButtonAnimations() {
        int[] buttonIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                          R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                          R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply, R.id.btnDivide,
                          R.id.btnEquals, R.id.btnClear, R.id.btnDecimal};
        
        for (int id : buttonIds) {
            Button button = findViewById(id);
            if (button != null) {
                button.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            animateButtonPress(v, true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            animateButtonPress(v, false);
                            break;
                    }
                    return false;
                });
            }
        }
    }
    
    private void animateButtonPress(View view, boolean pressed) {
        float scale = pressed ? 0.95f : 1.0f;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", scale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", scale);
        scaleX.setDuration(100);
        scaleY.setDuration(100);
        scaleX.setInterpolator(new DecelerateInterpolator());
        scaleY.setInterpolator(new DecelerateInterpolator());
        scaleX.start();
        scaleY.start();
    }

    // Fixed: Proper handling of number input with leading zero prevention
    private void onNumberClick(String number) {
        if (lastWasEquals) {
            currentExpression.setLength(0);
            lastWasEquals = false;
        }
        
        // Prevent leading zeros except for decimal numbers
        String currentExpr = currentExpression.toString();
        if (number.equals("0") && 
            (currentExpr.isEmpty() || 
             currentExpr.matches(".*[+\\-*/^(]0*$") ||
             currentExpr.equals("0"))) {
            if (!currentExpr.endsWith(".")) {
                // Don't add leading zero unless it's after a decimal point
                if (currentExpr.isEmpty() || !currentExpr.matches(".*\\.$")) {
                    return;
                }
            }
        }
        
        // Remove leading zeros before adding new digit
        if (!number.equals("0") && currentExpr.matches(".*[+\\-*/^(]0+$")) {
            // Remove trailing zeros after operators
            while (currentExpr.endsWith("0") && 
                   currentExpr.length() > 1 && 
                   "+-*/^(".contains(String.valueOf(currentExpr.charAt(currentExpr.length() - 2)))) {
                currentExpression.deleteCharAt(currentExpression.length() - 1);
                currentExpr = currentExpression.toString();
            }
        }
        
        currentExpression.append(number);
        updateDisplay();
        animateDisplayUpdate();
    }

    // Fixed: Proper operator handling with validation
    private void onOperatorClick(String operator) {
        if (currentExpression.length() == 0) {
            if (operator.equals("-")) {
                // Allow negative numbers at the start
                currentExpression.append(operator);
                updateDisplay();
            }
            return;
        }
        
        String currentExpr = currentExpression.toString();
        char lastChar = currentExpr.charAt(currentExpr.length() - 1);
        
        // Replace last operator if user presses another operator
        if ("+-*/^".contains(String.valueOf(lastChar))) {
            currentExpression.setCharAt(currentExpression.length() - 1, operator.charAt(0));
        } else {
            currentExpression.append(operator);
        }
        
        lastWasEquals = false;
        updateDisplay();
    }

    // Fixed: Comprehensive calculation with error handling
    private void onEqualsClick() {
        if (currentExpression.length() == 0) {
            return;
        }
        
        String expression = currentExpression.toString();
        
        // Remove trailing operators
        while (expression.length() > 0 && "+-*/^".contains(String.valueOf(expression.charAt(expression.length() - 1)))) {
            expression = expression.substring(0, expression.length() - 1);
        }
        
        if (expression.isEmpty()) {
            return;
        }
        
        try {
            CalculatorEngine.CalculationResult result = engine.evaluate(expression);
            
            if (result.isError()) {
                displayError(result.getResult());
                vibrate();
            } else {
                String resultStr = result.getResult();
                display.setText(resultStr);
                
                // Add to history with timestamp
                String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                calculationHistory.add(timestamp + " | " + expression + " = " + resultStr);
                
                // Set up for next calculation
                currentExpression.setLength(0);
                currentExpression.append(resultStr);
                lastWasEquals = true;
                
                animateResult();
            }
            
            updateExpressionDisplay("");
            
        } catch (Exception e) {
            displayError("Calculation Error");
            vibrate();
        }
    }

    private void onClearClick() {
        if (currentExpression.length() > 0) {
            currentExpression.deleteCharAt(currentExpression.length() - 1);
        }
        
        if (currentExpression.length() == 0) {
            display.setText("0");
            updateExpressionDisplay("");
        } else {
            updateDisplay();
        }
        
        lastWasEquals = false;
    }
    
    private void clearAll() {
        currentExpression.setLength(0);
        display.setText("0");
        updateExpressionDisplay("");
        lastWasEquals = false;
        
        // Clear animation
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(display, "alpha", 0f);
        fadeOut.setDuration(200);
        fadeOut.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(display, "alpha", 1f);
                fadeIn.setDuration(200);
                fadeIn.start();
            }
        });
        fadeOut.start();
    }
    
    private void clearEntry() {
        // Clear current number being entered
        String expr = currentExpression.toString();
        int lastOperatorIndex = -1;
        
        for (int i = expr.length() - 1; i >= 0; i--) {
            if ("+-*/^".contains(String.valueOf(expr.charAt(i)))) {
                lastOperatorIndex = i;
                break;
            }
        }
        
        if (lastOperatorIndex >= 0) {
            currentExpression.setLength(lastOperatorIndex + 1);
        } else {
            currentExpression.setLength(0);
        }
        
        updateDisplay();
    }

    // Fixed: Proper decimal point handling
    private void onDecimalClick() {
        String currentExpr = currentExpression.toString();
        
        // Find the last number in the expression
        int lastOperatorIndex = -1;
        for (int i = currentExpr.length() - 1; i >= 0; i--) {
            if ("+-*/^(".contains(String.valueOf(currentExpr.charAt(i)))) {
                lastOperatorIndex = i;
                break;
            }
        }
        
        String lastNumber = lastOperatorIndex >= 0 ? 
            currentExpr.substring(lastOperatorIndex + 1) : currentExpr;
        
        // Only add decimal if current number doesn't already have one
        if (!lastNumber.contains(".")) {
            if (lastNumber.isEmpty() || "+-*/^(".contains(String.valueOf(currentExpr.charAt(currentExpr.length() - 1)))) {
                currentExpression.append("0.");
            } else {
                currentExpression.append(".");
            }
            updateDisplay();
        }
        
        lastWasEquals = false;
    }
    
    private void updateDisplay() {
        String expression = currentExpression.toString();
        if (expression.isEmpty()) {
            display.setText("0");
            updateExpressionDisplay("");
        } else {
            // Show last number being entered in main display
            String[] parts = expression.split("[+\\-*/^]");
            String lastPart = parts.length > 0 ? parts[parts.length - 1] : expression;
            
            if (lastPart.isEmpty()) {
                display.setText("0");
            } else {
                display.setText(lastPart);
            }
            
            updateExpressionDisplay(expression);
        }
    }
    
    private void updateExpressionDisplay(String expression) {
        if (expressionDisplay != null) {
            if (expression.isEmpty()) {
                expressionDisplay.setText("");
                expressionDisplay.setVisibility(View.GONE);
            } else {
                // Highlight operators in different color
                SpannableString spannableString = new SpannableString(expression);
                for (int i = 0; i < expression.length(); i++) {
                    char c = expression.charAt(i);
                    if ("+-*/^()".contains(String.valueOf(c))) {
                        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), i, i + 1, 0);
                    }
                }
                expressionDisplay.setText(spannableString);
                expressionDisplay.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void displayError(String message) {
        display.setText(message);
        display.setTextColor(Color.RED);
        
        // Reset color after 2 seconds
        display.postDelayed(() -> {
            display.setTextColor(isDarkTheme ? Color.WHITE : Color.BLACK);
        }, 2000);
    }
    
    private void animateDisplayUpdate() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(display, "scaleX", 1.1f, 1.0f);
        animator.setDuration(150);
        animator.start();
    }
    
    private void animateResult() {
        ObjectAnimator pulseX = ObjectAnimator.ofFloat(display, "scaleX", 1.0f, 1.15f, 1.0f);
        ObjectAnimator pulseY = ObjectAnimator.ofFloat(display, "scaleY", 1.0f, 1.15f, 1.0f);
        pulseX.setDuration(300);
        pulseY.setDuration(300);
        pulseX.start();
        pulseY.start();
    }
    
    private void vibrate() {
        // Simple vibration feedback for errors
        try {
            android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(100);
            }
        } catch (Exception e) {
            // Vibration not available
        }
    }
    
    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        updateTheme();
        savePreferences();
        Toast.makeText(this, "Theme changed", Toast.LENGTH_SHORT).show();
    }
    
    private void updateTheme() {
        int backgroundColor = isDarkTheme ? Color.parseColor("#222222") : Color.WHITE;
        int textColor = isDarkTheme ? Color.WHITE : Color.BLACK;
        
        findViewById(android.R.id.content).setBackgroundColor(backgroundColor);
        display.setTextColor(textColor);
        if (expressionDisplay != null) {
            expressionDisplay.setTextColor(textColor);
        }
    }
    
    private void showHistory() {
        if (calculationHistory.isEmpty()) {
            Toast.makeText(this, "No calculation history", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Calculation History");
        
        ListView listView = new ListView(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, calculationHistory);
        listView.setAdapter(adapter);
        
        builder.setView(listView);
        builder.setPositiveButton("Clear History", (dialog, which) -> {
            calculationHistory.clear();
            engine.clearHistory();
            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Close", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calculator_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_history:
                showHistory();
                return true;
            case R.id.action_theme:
                toggleTheme();
                return true;
            case R.id.action_scientific:
                toggleScientificMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void toggleScientificMode() {
        isInScientificMode = !isInScientificMode;
        savePreferences();
        Toast.makeText(this, 
            "Scientific mode " + (isInScientificMode ? "enabled" : "disabled"), 
            Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        savePreferences();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }
}