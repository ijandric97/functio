/*
 * Copyright (c) 1996, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */

package us.walr.helpers;

import java.text.CharacterIterator;

/**
 * {@code LexerIterator} implements the
 * {@code CharacterIterator} protocol for a {@code String}.
 * The {@code LexerIterator} class iterates over the
 * entire {@code String}.
 *
 * @see CharacterIterator
 * @since 1.1
 */

public final class LexerIterator implements CharacterIterator {
    private String text;
    private int begin;
    private int end;
    // invariant: begin <= pos <= end
    private int pos;

    /**
     * Constructs an iterator with an initial index of 0.
     *
     * @param text the {@code String} to be iterated over
     */
    public LexerIterator(String text) {
        this(text, 0);
    }

    /**
     * Constructs an iterator with the specified initial index.
     *
     * @param text The String to be iterated over
     * @param pos  Initial iterator position
     */
    public LexerIterator(String text, int pos) {
        this(text, 0, text.length(), pos);
    }

    /**
     * Constructs an iterator over the given range of the given string, with the
     * index set at the specified position.
     *
     * @param text  The String to be iterated over
     * @param begin Index of the first character
     * @param end   Index of the character following the last character
     * @param pos   Initial iterator position
     */
    public LexerIterator(String text, int begin, int end, int pos) {
        if (text == null)
            throw new NullPointerException();
        this.text = text;

        if (begin < 0 || begin > end || end > text.length())
            throw new IllegalArgumentException("Invalid substring range");

        if (pos < begin || pos > end)
            throw new IllegalArgumentException("Invalid position");

        this.begin = begin;
        this.end = end;
        this.pos = pos;
    }

    /**
     * Reset this iterator to point to a new string.  This package-visible
     * method is used by other java.text classes that want to avoid allocating
     * new LexerIterator objects every time their setText method
     * is called.
     *
     * @param text The String to be iterated over
     * @since 1.2
     */
    public void setText(String text) {
        if (text == null)
            throw new NullPointerException();
        this.text = text;
        this.begin = 0;
        this.end = text.length();
        this.pos = 0;
    }

    /**
     * Implements CharacterIterator.first() for String.
     *
     * @see CharacterIterator#first
     */
    public char first() {
        pos = begin;
        return current();
    }

    /**
     * Implements CharacterIterator.last() for String.
     *
     * @see CharacterIterator#last
     */
    public char last() {
        if (end != begin) {
            pos = end - 1;
        } else {
            pos = end;
        }
        return current();
    }

    /**
     * Implements CharacterIterator.setIndex() for String.
     *
     * @see CharacterIterator#setIndex
     */
    public char setIndex(int p) {
        if (p < begin || p > end)
            throw new IllegalArgumentException("Invalid index");
        pos = p;
        return current();
    }

    /**
     * Implements CharacterIterator.current() for String.
     *
     * @see CharacterIterator#current
     */
    public char current() {
        if (pos >= begin && pos < end) {
            return text.charAt(pos);
        } else {
            return DONE;
        }
    }

    /**
     * Implements CharacterIterator.next() for String.
     *
     * @see CharacterIterator#next
     */
    public char next() {
        if (pos < end - 1) {
            pos++;
            return text.charAt(pos);
        } else {
            pos = end;
            return DONE;
        }
    }

    /**
     * Implements CharacterIterator.previous() for String.
     *
     * @see CharacterIterator#previous
     */
    public char previous() {
        if (pos > begin) {
            pos--;
            return text.charAt(pos);
        } else {
            return DONE;
        }
    }

    /**
     * Implements CharacterIterator.getBeginIndex() for String.
     *
     * @see CharacterIterator#getBeginIndex
     */
    public int getBeginIndex() {
        return begin;
    }

    /**
     * Implements CharacterIterator.getEndIndex() for String.
     *
     * @see CharacterIterator#getEndIndex
     */
    public int getEndIndex() {
        return end;
    }

    /**
     * Implements CharacterIterator.getIndex() for String.
     *
     * @see CharacterIterator#getIndex
     */
    public int getIndex() {
        return pos;
    }

    /**
     * Compares the equality of two LexerIterator objects.
     *
     * @param obj the LexerIterator object to be compared with.
     * @return true if the given obj is the same as this
     * LexerIterator object; false otherwise.
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof LexerIterator that))
            return false;

        if (hashCode() != that.hashCode())
            return false;
        if (!text.equals(that.text))
            return false;
        return pos == that.pos && begin == that.begin && end == that.end;
    }

    /**
     * Computes a hashcode for this iterator.
     *
     * @return A hash code
     */
    public int hashCode() {
        return text.hashCode() ^ pos ^ begin ^ end;
    }

    /**
     * Creates a copy of this iterator.
     *
     * @return A copy of this
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    // CUSTOM FUNCTIONS //

    /**
     * Repeats next() command the specified number of times
     *
     * @param times Number of places to go next
     * @return The character at the new position
     */
    public char next(int times) {
        char ret = DONE;
        for (int i = 0; i < times; i++) {
            ret = this.next();
        }

        return ret;
    }

    /**
     * Returns a character at a specified index without setting the current position to it.
     *
     * @param p Index which we want to peek
     * @return Character at a specified index, or DONE if out of bounds.
     */
    public char peek(int p) {
        if (p >= begin && p < end) {
            return text.charAt(p);
        } else {
            return DONE;
        }
    }

    /**
     * Returns the next character without moving the iterator to it.
     *
     * @return the character after the current position, or DONE if out of bounds
     */
    public char peekNext() {
        return peek(pos + 1);
    }


    /**
     * Returns the previous character without moving the iterator to it.
     *
     * @return the character before the current position, or DONE if out of bounds
     */
    public char peekPrevious() {
        return peek(pos - 1);
    }

    /**
     * Checks if the current position contains the expected character
     *
     * @param c Character we expect at the current position
     * @return True if the current position contains the expected character
     */
    public boolean match(char c) {
        return match(pos, c);
    }

    /**
     * Checks if the given position contains the expected character
     *
     * @param p The position (index) to check
     * @param c Character we expect at the given position
     * @return True if the given position contains the expected character
     */
    public boolean match(int p, int c) {
        return this.peek(p) == c;
    }

    /**
     * Checks if the next position contains the expected character
     *
     * @param c Character we expect at the next position
     * @return True if the next position contains the expected character
     */
    public boolean matchNext(int c) {
        return this.match(pos + 1, c);
    }

    /**
     * Advances the Iterator if the next character matched the expected literal.
     *
     * @param expected The literal of the next character in the input string
     * @return True if the next character matches the expected one
     */
    public boolean matchNextAndAdvance(char expected) {
        if (this.matchNext(expected)) {
            // Everything is good, we will keep this iteration and return true for this comparison
            this.next();
            return true;
        }

        return false;
    }


    /**
     * Checks if the previous position contains the expected character
     *
     * @param c Character we expect at the previous position
     * @return True if the previous position contains the expected character
     */
    public boolean matchPrevious(int c) {
        return this.match(pos - 1, c);
    }

    /**
     * Checks if the character at the current position is letter from the English alphabet (a-Z)
     *
     * @return True if character at current position is part of the Alphabet
     */
    public boolean isAlpha() {
        return this.isAlpha(pos);
    }

    /**
     * Checks if the character at the specified position is letter from the English alphabet (a-Z)
     *
     * @param p The position (index) to check
     * @return True if character at specified position is part of the Alphabet
     */
    public boolean isAlpha(int p) {
        char c = this.peek(p);
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    /**
     * Checks if the character is letter from the English alphabet (a-Z).
     *
     * @param c The character to check
     * @return True if the character is part of the Alphabet
     */
    public boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    /**
     * Checks if the current character is an ASCII number character.
     * Default Java isDigit also supports non-ASCII number version which we do not want.
     *
     * @return True if current character is valid ASCII number.
     */
    public boolean isDigit() {
        return this.isDigit(pos);
    }

    /**
     * Checks if the character at the specified position is an ASCII number character.
     * Default Java isDigit also supports non-ASCII number version which we do not want.
     *
     * @param p The position (index) to check
     * @return True if the specified character is valid ASCII number.
     */
    public boolean isDigit(int p) {
        char c = this.peek(p);
        return c >= '0' && c <= '9';
    }

    /**
     * Checks if the character is an ASCII number character.
     * Default Java isDigit also supports non-ASCII number version which we do not want.
     *
     * @param c The character to check
     * @return True if the specified character is valid ASCII number.
     */
    public boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Checks if the current character is an English Alphabet or ASCII number character.
     *
     * @return True if current character is either an Alphabet character or ASCII number.
     */
    public boolean isAlphaNumeric() {
        return this.isAlphaNumeric(pos);
    }

    /**
     * Checks if the character at the specified position is an English Alphabet or ASCII number character.
     *
     * @param p The position (index) to check
     * @return True if the specified character is either an Alphabet character or ASCII number.
     */
    public boolean isAlphaNumeric(int p) {
        return this.isAlpha(p) || this.isDigit(p);
    }

    /**
     * Checks if the character is an English Alphabet or ASCII number character.
     *
     * @param c The character to check
     * @return True if the specified character is either an Alphabet character or ASCII number.
     */
    public boolean isAlphaNumeric(char c) {
        return this.isAlpha(c) || this.isDigit(c);
    }

    /**
     * Checks if End Of File (EOF) has been reached.
     *
     * @return Boolean literal that is true if EOF was reached.
     */
    public boolean isEOF() {
        return isEOF(pos);
    }

    /**
     * Checks if End of File (EOF) has been reached at a given position (index).
     *
     * @param p The position (index) to check
     * @return True if the specified position is out of bounds (EOF)
     */
    public boolean isEOF(int p) {
        return this.peek(p) == CharacterIterator.DONE;
    }

    /**
     * Checks if the current position contains a new line character.
     *
     * @return True if the current position is a new line character.
     */
    public boolean isNewLine() {
        return isNewLine(pos);
    }

    /**
     * Checks if the character at a given position (index) is a new line character.
     *
     * @param p The position (index) to check.
     * @return True if the specified position is a new line character.
     */
    public boolean isNewLine(int p) {
        return this.peek(p) == '\n';
    }
}
