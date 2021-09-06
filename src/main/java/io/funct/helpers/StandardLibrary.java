package io.funct.helpers;

import io.funct.Interpreter;
import io.funct.Lox;
import io.funct.internal.LoxCallable;

import java.util.List;

public abstract class StandardLibrary {
    /**
     * clock() -> Returns current time in seconds since 1970
     */
    public static class clock implements LoxCallable {
        @Override
        public int arity() {
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
    public static class exit implements LoxCallable {
        @Override
        public int arity() {
            return 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return Lox.exit(0);
        }

        @Override
        public String toString() {
            return "<StdLib.exit>";
        }
    }

    /**
     * println("string") -> Wrapper around java println function
     */
    public static class println implements LoxCallable {
        @Override
        public int arity() {
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
     * abs(x) -> Calculate absolute value of the specified expression
     */
    public static class abs implements LoxCallable {
        @Override
        public int arity() {
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
     * rand() -> generates a random float between 0 and 1.
     */
    public static class rand implements LoxCallable {
        @Override
        public int arity() {
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
     * pow(x,y) -> raises x to the power of y
     */
    public static class pow implements LoxCallable {
        @Override
        public int arity() {
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
}
