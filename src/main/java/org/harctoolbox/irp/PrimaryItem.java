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

import org.antlr.v4.runtime.tree.ParseTree;

/**
 *
 */
public abstract class PrimaryItem implements Numerical,XmlExport {

    public static PrimaryItem newPrimaryItem(IrpParser.Primary_itemContext ctx) throws IrpSyntaxException {
        ParseTree child = ctx.getChild(0);
        return (child instanceof IrpParser.NameContext)
                ? new Name((IrpParser.NameContext) child)
                : (child instanceof IrpParser.NumberContext)
                ? new Number((IrpParser.NumberContext) child)
                : (child instanceof IrpParser.Para_expressionContext)
                ? new Expression((IrpParser.Para_expressionContext) child)
                : new Name(child.getText());
    }

    protected PrimaryItem() {
    }

    public static PrimaryItem newPrimaryItem(long n) {
        return new Number(n);
    }

    public static PrimaryItem newPrimaryItem(String name) throws IrpSyntaxException {
        return new Name(name);
    }
}
