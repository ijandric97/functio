package us.walr.helpers;

import us.walr.Interpreter;
import us.walr.Walrus;
import us.walr.internal.WalrusArray;
import us.walr.internal.WalrusCallable;

import java.util.List;
import java.util.Scanner;

public abstract class StandardLibrary {
    /**
     * clock() -> Returns current time in seconds since 1970
     */
    public static class clock implements WalrusCallable {
        @Override
        public int numberOfArguments() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return (double) System.currentTimeMillis() / 1000.0;
        }

        @Override
        public String toString() {
            return "<StdLib.clock>";
        }
    }

    /**
     * exit() -> Safely exits the program
     */
    public static class exit implements WalrusCallable {
        @Override
        public int numberOfArguments() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Walrus.exit(0);
        }

        @Override
        public String toString() {
            return "<StdLib.exit>";
        }
    }

    /**
     * println("string") -> Wrapper around java println function
     */
    public static class println implements WalrusCallable {
        @Override
        public int numberOfArguments() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            System.out.println(arguments.get(0));
            return null;
        }

        @Override
        public String toString() {
            return "<StdLib.print>";
        }
    }

    /**
     * input() -> Wrapper around java system input function
     */
    public static class input implements WalrusCallable {
        @Override
        public int numberOfArguments() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Scanner scanner = new Scanner(System.in);
            return scanner.nextLine();
        }

        @Override
        public String toString() {
            return "<StdLib.print>";
        }
    }

    /**
     * rand() -> generates a random float between 0 and 1.
     */
    public static class rand implements WalrusCallable {
        @Override
        public int numberOfArguments() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Math.random();
        }

        @Override
        public String toString() {
            return "<StdLib.rand>";
        }
    }

    /**
     * abs(x) -> Calculate absolute value of the specified expression
     */
    public static class abs implements WalrusCallable {
        @Override
        public int numberOfArguments() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object arg = arguments.get(0);
            if (arg instanceof Double) return Math.abs((Double) arguments.get(0));
            return null;
        }

        @Override
        public String toString() {
            return "<StdLib.abs>";
        }
    }

    /**
     * pow(x,y) -> raises x to the power of y
     */
    public static class pow implements WalrusCallable {
        @Override
        public int numberOfArguments() {
            return 2;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object x = arguments.get(0);
            Object y = arguments.get(1);
            if (x instanceof Double && y instanceof Double) return Math.pow((Double) x, (Double) y);
            return null;
        }

        @Override
        public String toString() {
            return "<StdLib.pow>";
        }
    }

    /**
     * sqrt(x) -> Square root of the number
     */
    public static class sqrt implements WalrusCallable {
        @Override
        public int numberOfArguments() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object x = arguments.get(0);
            if (x instanceof Double) return Math.sqrt((Double) x);
            return null;
        }

        @Override
        public String toString() {
            return "<StdLib.sqrt>";
        }
    }

    /**
     * round(x) -> rounds a number
     */
    public static class round implements WalrusCallable {
        @Override
        public int numberOfArguments() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object x = arguments.get(0);
            if (x instanceof Double) return Math.round((Double) x);
            return x;
        }

        @Override
        public String toString() {
            return "<StdLib.round>";
        }
    }

    /**
     * string(x) -> converts an object into string
     */
    public static class string implements WalrusCallable {
        @Override
        public int numberOfArguments() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object arg = arguments.get(0);
            return InterpreterHelper.stringify(arg);
        }

        @Override
        public String toString() {
            return "<StdLib.string>";
        }
    }

    /**
     * number(x) -> converts a string into number
     */
    public static class number implements WalrusCallable {
        @Override
        public int numberOfArguments() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object x = arguments.get(0);
            if (x instanceof String) {
                try {
                    return Double.parseDouble((String) x);
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return "<StdLib.number>";
        }
    }

    /**
     * array(size) -> creates an array
     */
    public static class array implements WalrusCallable {
        @Override
        public int numberOfArguments() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            int size = (int) ((double) arguments.get(0));
            return new WalrusArray(size);
        }

        @Override
        public String toString() {
            return "<StdLib.array>";
        }
    }
}
