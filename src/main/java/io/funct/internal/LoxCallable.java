package io.funct.internal;

import io.funct.Interpreter;

import java.util.List;

public interface LoxCallable {
    /**
     * @return Number of arguments required
     */
    int arity();


    /**
     * Function body
     *
     * @param interpreter Instance of the interpreter
     * @param arguments   List of arguments
     * @return Value function produces (can be null if we want nothing)
     */
    Object call(Interpreter interpreter, List<Object> arguments);
}
