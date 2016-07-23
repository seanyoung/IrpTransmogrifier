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

package org.harctoolbox.irp;

import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class BitspecIrstream extends IrStreamItem {
    private BitSpec bitSpec;
    private IrStream irStream;

    public BitspecIrstream(IrpParser.ProtocolContext ctx) throws IrpSyntaxException, InvalidRepeatException {
        this(ctx.bitspec_irstream());
    }

    public BitspecIrstream(IrpParser.Bitspec_irstreamContext ctx) throws IrpSyntaxException, InvalidRepeatException {
        bitSpec = new BitSpec(ctx.bitspec());
        irStream = new IrStream(ctx.irstream());
    }

    public BitspecIrstream(String str) throws IrpSyntaxException, InvalidRepeatException {
        this((new ParserDriver(str)).getParser().bitspec_irstream());
    }

    @Override
    public String toIrpString() {
        return bitSpec.toIrpString() + irStream.toIrpString();
    }

    @Override
    public Element toElement(Document document) throws IrpSyntaxException {
        Element root = document.createElement("bitspec_irstream");
        root.setAttribute("interleavingOk", Boolean.toString(interleavingOk()));
        root.appendChild(bitSpec.toElement(document));
        root.appendChild(irStream.toElement(document));
        return root;
    }

    @Override
    public boolean isEmpty(NameEngine nameEngine) throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec,
            double elapsed)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        return irStream.evaluate(state, pass, nameEngine, generalSpec, bitSpec, elapsed);
    }

    @Override
    EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec,
            BitSpec bitSpec, double elapsed)
            throws IncompatibleArgumentException, ArithmeticException, UnassignedException, IrpSyntaxException {
        return evaluate(state, pass, nameEngine, generalSpec, elapsed);
    }

    @Override
    int numberOfBitSpecs() {
        return irStream.numberOfBitSpecs() + 1;
    }

    @Override
    boolean interleavingOk() {
        return bitSpec.isStandardPWM(new NameEngine(), new GeneralSpec()) && irStream.interleavingOk();
    }

    @Override
    int numberOfBits() {
        return irStream.numberOfBits();
    }

    @Override
    int numberOfBareDurations() {
        return irStream.numberOfBareDurations();
    }

    @Override
    public int numberOfInfiniteRepeats() {
        return bitSpec.numberOfInfiniteRepeats() + irStream.numberOfInfiniteRepeats();
    }
}
