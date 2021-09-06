package io.funct.helpers;

import io.funct.exceptions.RuntimeError;
import io.funct.internal.Token;

public class InterpreterHelper {
    /**
     * Check if the provided operand is a number.
     * <p>
     * Throws a RuntimeError if the operand is not a number.
     *
     * @param operator The token that is the operator
     * @param operand  The operand that should be a Double
     */
    public static void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    /**
     * Checks if the provided left and right operands are a number
     * <p>
     * Throws a RuntimeError if either operand is not a number.
     *
     * @param operator The token that is the operator
     * @param left     The left operand that should be a double
     * @param right    The right operand that should be a double
     */
    public static void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    /**
     * Returns the boolean representation of the given object
     *
     * @param object The object to test
     * @return True or false depending on the provided object
     */
    public static boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;

        return true;
    }

    /**
     * Checks if two provided objects are equal
     *
     * @param a The first object to test
     * @param b The second object to test
     * @return True if the objects are equal
     */
    public static boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    /**
     * Converts the specified object to string
     *
     * @param object The object to convert
     * @return The string representation of the object
     */
    public static String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();

            // If it is an integer, remove the decimal part
            if (text.endsWith(".0")) text = text.substring(0, text.length() - 2);

            return text;
        }

        return object.toString();
    }
}
