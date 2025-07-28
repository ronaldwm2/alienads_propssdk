# Advanced Calculator App

A sophisticated Android calculator application with enhanced mathematical capabilities, advanced UI features, and comprehensive error handling.

## 🚀 Enhanced Features

### Mathematical Engine
- **Scientific Operations**: Support for sin, cos, tan, log, ln, sqrt, power (^), and absolute value
- **Advanced Expression Parsing**: Uses Shunting Yard algorithm for proper operator precedence
- **High Precision Arithmetic**: BigDecimal implementation with 15-digit precision
- **Mathematical Constants**: Built-in support for π (pi) and e (Euler's number)
- **Implicit Multiplication**: Automatically handles expressions like "2π" or "3(4+5)"

### User Interface Enhancements
- **Dual Display System**: Expression display shows full calculation, main display shows current input
- **Animated Interactions**: Button press animations, result pulse effects, and smooth transitions
- **Gesture Controls**:
  - Double-tap display to toggle dark/light theme
  - Swipe right to view calculation history
  - Swipe left to clear current entry
  - Long-press Clear button to reset everything
- **Color-coded Interface**: Different colors for numbers, operators, and functions
- **Responsive Design**: Optimized layout with proper spacing and visual hierarchy

### Advanced Functionality
- **Calculation History**: Timestamped history of all calculations with ability to clear
- **Theme System**: Dark and light theme with persistent preferences
- **Error Handling**: Comprehensive error messages with visual and haptic feedback
- **Input Validation**: Smart input processing prevents invalid expressions
- **Memory Management**: Auto-cleanup of history and optimized performance

## 🐛 Bug Fixes Applied

### Original Bugs Identified and Fixed:

1. **Leading Zero Handling** ❌➡️✅
   - **Before**: Allowed invalid numbers like "00123"
   - **After**: Prevents leading zeros, properly handles decimal numbers like "0.5"

2. **Operator Validation** ❌➡️✅
   - **Before**: No validation for consecutive operators
   - **After**: Smart operator replacement and validation

3. **Division by Zero** ❌➡️✅
   - **Before**: App would crash or show infinity
   - **After**: Proper error handling with user-friendly messages

4. **Number Formatting** ❌➡️✅
   - **Before**: Always showed decimals even for whole numbers
   - **After**: Smart formatting removes unnecessary decimal places

5. **State Management** ❌➡️✅
   - **Before**: Poor state reset after calculations
   - **After**: Proper state management for continuous calculations

6. **Decimal Point Logic** ❌➡️✅
   - **Before**: Could add multiple decimal points to same number
   - **After**: Prevents multiple decimals, handles edge cases

## 🔧 Technical Architecture

### Core Components

#### `CalculatorEngine.java`
- Mathematical expression parser and evaluator
- Implements Shunting Yard algorithm for infix-to-postfix conversion
- Handles scientific functions and error cases
- Manages calculation history

#### `MainActivity.java` 
- Enhanced UI controller with gesture handling
- Animation system for smooth user interactions
- Theme management and preference persistence
- Complex input validation and state management

### Key Algorithms

1. **Expression Parsing**: Converts human-readable math expressions to computer-evaluable form
2. **Operator Precedence**: Ensures correct mathematical order of operations
3. **Error Recovery**: Graceful handling of mathematical errors and edge cases
4. **Input Sanitization**: Prevents invalid input patterns and malformed expressions

## 🎨 UI/UX Improvements

### Visual Design
- Modern Material Design principles
- Consistent color scheme with semantic meaning
- High contrast for accessibility
- Smooth animations and transitions

### User Experience
- Intuitive gesture controls
- Immediate visual feedback
- Clear error messages
- Progressive disclosure of features

### Accessibility
- Large touch targets (80dp minimum)
- High contrast colors
- Semantic button labeling
- Screen reader compatibility

## 🧪 Advanced Use Cases

### Scientific Calculations
```
sin(30) → 0.5
2π → 6.283185307179586
log(100) → 2
sqrt(16) → 4
2^3 → 8
```

### Complex Expressions
```
2 + 3 * 4 → 14 (proper precedence)
(2 + 3) * 4 → 20 (parentheses support)
sin(30) + cos(60) → 1 (trigonometric functions)
2π * 3 → 18.849555921538757 (constants and multiplication)
```

### Error Handling Examples
```
5 / 0 → "Math Error: Division by zero"
log(-5) → "Math Error: Logarithm of non-positive number"
sqrt(-4) → "Math Error: Square root of negative number"
2 + + 3 → "Invalid operator sequence"
```

## 📱 User Interactions

### Basic Operations
- Tap numbers and operators to build expressions
- Tap equals (=) to calculate result
- Tap Clear (C) to delete last character
- Long-press Clear to reset everything

### Advanced Gestures
- **Double-tap display**: Toggle between light and dark themes
- **Swipe right on display**: Open calculation history
- **Swipe left on display**: Clear current number entry
- **Menu button**: Access additional features

### Menu Options
- **History**: View and manage calculation history
- **Toggle Theme**: Switch between light and dark modes
- **Scientific Mode**: Enable/disable advanced mathematical functions

## 🔐 Data Persistence

### Preferences Stored
- Theme selection (light/dark)
- Scientific mode toggle
- Last calculation state

### History Management
- Automatic timestamp for each calculation
- Maximum 50 entries (auto-cleanup)
- Export/clear functionality

## 🛠️ Build Configuration

### Dependencies
```gradle
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.11.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
```

### Permissions
```xml
<uses-permission android:name="android.permission.VIBRATE" />
```

### Target Configuration
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

## 🚀 Performance Optimizations

### Memory Management
- Efficient string building with StringBuilder
- Limited history size to prevent memory leaks
- Proper cleanup in lifecycle methods

### UI Performance
- Hardware-accelerated animations
- Efficient layout with GridLayout
- Optimized redraw cycles

### Calculation Performance
- BigDecimal for precision without performance loss
- Optimized parsing algorithms
- Lazy evaluation where possible

## 🧪 Testing Scenarios

### Edge Cases Handled
1. Very large numbers (scientific notation)
2. Very small numbers (precision handling)
3. Division by zero and mathematical errors
4. Invalid input sequences
5. Rapid user input and gestures
6. Theme switching during calculations
7. Device rotation (portrait-only design)

### User Flow Testing
1. Basic arithmetic operations
2. Scientific function usage
3. History management
4. Theme and preference persistence
5. Error recovery and continuation
6. Gesture interactions
7. Menu functionality

This calculator represents a significant advancement over basic calculator apps, providing professional-grade mathematical capabilities while maintaining an intuitive and beautiful user interface.