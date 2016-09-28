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

package org.harctoolbox.analyze;

import java.util.ArrayList;
import java.util.List;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.harctoolbox.irp.IrStreamItem;

public class BiphaseDecoder extends AbstractDecoder {

    private final int half;
    private final int full;
    private ParameterData data;

    public BiphaseDecoder(Analyzer analyzer, Analyzer.AnalyzerParams params, int half, int full)  {
        super(analyzer, params);
        setBitSpec(analyzer.getTimebase());
        this.half = half;
        this.full = full;
    }

    public BiphaseDecoder(Analyzer analyzer, Analyzer.AnalyzerParams params)  {
        this(analyzer, params, analyzer.getTiming(0), analyzer.getTiming(1));
    }

    @Override
    protected List<IrStreamItem> process(int beg, int length) throws DecodeException {
        List<IrStreamItem> items = new ArrayList<>(2*length);
        data = new ParameterData();
        BiphaseState state = BiphaseState.start;
        for (int i = beg; i < beg + length; i++) {
            int noBitsLimit = params.getNoBitsLimit(noPayload);
            boolean isFlash = i % 2 == 0;
            boolean useExtent = params.isUseExtents() && (i == beg + length - 1);
            int time = analyzer.getCleanedTime(i);
            boolean isShort = time == half;
            boolean isLong = time == full;

            switch (state) {
                case start:
                    if (!isFlash)
                        throw new ThisCannotHappenException();

                    if (isShort)
                        if (params.isInvert()) {
                            data.update(1);
                            state = BiphaseState.zero;
                        } else {
                            state = BiphaseState.pendingFlash;
                        }
                    else {
                        saveParameter(data, items, params.getBitDirection());
                        data = new ParameterData();
                        items.add(newFlash(time));
                        state = BiphaseState.zero;
                    }
                    break;

                case pendingGap:
                    if (!isFlash)
                        throw new ThisCannotHappenException();

                    if (isShort) {
                        data.update(params.isInvert());
                        state = BiphaseState.zero;
                    } else if (isLong) {
                        data.update(params.isInvert());
                        state = BiphaseState.pendingFlash;
                    } else {
                        data.update(params.isInvert());
                        saveParameter(data, items, params.getBitDirection());
                        data = new ParameterData();
                        //items.add(newGap(half));
                        items.add(newFlash(time-half));
                        state = BiphaseState.zero;
                    }
                    break;

                case pendingFlash:
                    if (isFlash)
                        throw new ThisCannotHappenException();

                    if (isShort) {
                        data.update(!params.isInvert());
                        state = BiphaseState.zero;
                    } else if (isLong) {
                        data.update(!params.isInvert());
                        state = BiphaseState.pendingGap;
                    } else {
                        data.update(!params.isInvert());
                        saveParameter(data, items, params.getBitDirection());
                        data = new ParameterData();
                        //items.add(newGap(half));
                        items.add(useExtent ? newExtent(analyzer.getTotalDuration(beg, length-1) + time-half) : newGap(time-half));
                        state = BiphaseState.zero;
                    }
                    break;

                case zero:
                    if (isShort) {
                        state = isFlash ? BiphaseState.pendingFlash : BiphaseState.pendingGap;
                    } else {
                        saveParameter(data, items, params.getBitDirection());
                        data = new ParameterData();
                        items.add(isFlash ? newFlash(time)
                                : useExtent ? newExtent(analyzer.getTotalDuration(beg, length-1) + time)
                                        : newGap(time));
                        state = BiphaseState.zero; // redundant...
                    }
                    break;

                default:
                    throw new ThisCannotHappenException();
            }
            if (data.getNoBits() >= noBitsLimit) {
                saveParameter(data, items, params.getBitDirection());
                data = new ParameterData();
            }
        }
        return items;
    }

    private enum BiphaseState {
        start,
        pendingGap,
        pendingFlash,
        zero;
    }
}
