/*
Copyright (C) 2014 Bengt Martensson.

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

package org.harctoolbox.irp;

/**
 *
 */
public class FloatNumber implements Floatable {

    private final double data;

    public FloatNumber(double x) {
        data = x;
    }

    public FloatNumber(IrpParser.Float_numberContext ctx) throws IrpSyntaxException {
        data = Double.parseDouble(ctx.getText());
    }

    public FloatNumber(String str) throws IrpSyntaxException {
        this(new ParserDriver((str)).getParser().float_number());
    }

    @Override
    public double toFloat(NameEngine nameEngine, GeneralSpec generalSpec) {
        return toFloat();
    }

    public double toFloat() {
        return data;
    }

    @Override
    public String toString() {
        return Double.toString(data);
    }

    public static double parse(String str) throws IrpSyntaxException {
        FloatNumber floatNumber = new FloatNumber(str);
        return floatNumber.toFloat();
    }

    public static double parse(IrpParser.Float_numberContext ctx) throws IrpSyntaxException {
        FloatNumber floatNumber = new FloatNumber(ctx);
        return floatNumber.toFloat();
    }
}
