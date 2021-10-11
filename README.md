# Walrus programming language

Walrus is an implementation and extension of the Lox language as featured in the Crafting Interpreters book. It is coded with IntelliJ IDEA and built using Gradle. The version of Java used is 16 (latest at the time), but newer versions *might work.

It features a custom handwritten lexer based on a customized StringCharacterIterator class. The parser is a modified Lox parser that is based on Recursive Descent parsing. Modifications include using Java collections for iterating through the list of tokens and additional functionalities such as unary operators. The interpreter is also based on a modified Lox interpreter with added arrays and a small standard library (and small additions such as string concatenation).

Additionally, most of the code should be documented with comments in Javadoc format.

## Standard library

Standard library consists of the following functions:
* clock() -> Returns current time in seconds since 1970
* exit() -> Safely exits the program
* println("string") -> Wrapper around java println function
* input() -> Wrapper around java system input function
* rand() -> Generates a random float between 0 and 1.
* abs(x) -> Calculate absolute value of the specified expression
* pow(x,y) -> Raises x to the power of y
* sqrt(x) -> Square root of the number
* round(x) -> Rounds a number
* string(x) -> Converts an object into string
* number(x) -> Converts a string into number
* array(size) -> Creates an array