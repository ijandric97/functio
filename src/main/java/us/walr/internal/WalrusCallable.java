package us.walr.internal;

import us.walr.Interpreter;

import java.util.List;

public interface WalrusCallable {
    /**
     * @return Number of arguments required
     */
    int numberOfArguments();


    /**
     * Function body
     *
     * @param interpreter Instance of the interpreter
     * @param arguments   List of arguments
     * @return Value function produces (can be null if we want nothing)
     */
    Object call(Interpreter interpreter, List<Object> arguments);
}
