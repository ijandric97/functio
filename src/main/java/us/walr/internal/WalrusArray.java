package us.walr.internal;

import us.walr.Interpreter;
import us.walr.exceptions.RuntimeError;

import java.util.List;

public class WalrusArray extends WalrusInstance {
    private final Object[] elements;

    public WalrusArray(int size) {
        super(null);
        elements = new Object[size];
    }

    ;

    @Override
    public Object get(Token name) {
        switch (name.lexeme()) {
            case "get" -> {
                return new GetArrayElement();
            }
            case "set" -> {
                return new SetArrayElement();
            }
            case "length" -> {
                return (double) elements.length;
            }
        }

        throw new RuntimeError(name, "Undefined property '" + name.lexeme() + "'.");
    }

    @Override
    public void set(Token name, Object value) {
        throw new RuntimeError(name, "Cannot add attributes to arrays.");
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[");
        for (int i = 0; i < elements.length; i++) {
            if (i != 0) buffer.append(", ");
            buffer.append(elements[i]);
        }
        buffer.append("]");
        return buffer.toString();
    }

    private class GetArrayElement implements WalrusCallable {
        @Override
        public int numberOfArguments() {
            return 1;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            try {
                int index = (int) (double) arguments.get(0);
                return elements[index];

            } catch (ArrayIndexOutOfBoundsException e) {
                throw new RuntimeError((Token) arguments.get(2), "Index out of bounds");
            }
        }
    }

    private class SetArrayElement implements WalrusCallable {
        @Override
        public int numberOfArguments() {
            return 2;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            try {
                int index = (int) (double) arguments.get(0);
                Object value = arguments.get(1);
                return elements[index] = value;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new RuntimeError((Token) arguments.get(2), "Index out of bounds");
            }
        }
    }
}
