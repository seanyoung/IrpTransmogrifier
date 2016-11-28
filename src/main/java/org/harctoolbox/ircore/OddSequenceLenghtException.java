/*
Copyright (C) 2016 Bengt Martensson.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see http://www.gnu.org/licenses/.
*/

package org.harctoolbox.ircore;

/**
 * Thrown when an IrSequence has a length that is not an even number.
 */
public class OddSequenceLenghtException extends InvalidArgumentException {

    public OddSequenceLenghtException(int length) {
        super("IrSequence has odd length = " + Integer.toString(length));
    }

    public OddSequenceLenghtException() {
        super("IrSequence has odd length");
    }

    public OddSequenceLenghtException(String s) {
        super(s);
    }
}
