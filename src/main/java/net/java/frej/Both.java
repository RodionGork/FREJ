/*
Copyright 2011 Rodion Gorkovenko

This file is a part of FREJ
(project FREJ - Fuzzy Regular Expressions for Java - http://frej.sf.net)

FREJ is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

FREJ is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with FREJ.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.java.frej;


final class Both extends Elem {
    
    private Elem expr;
    
    Both(Regex owner, Elem... elems) {
        super(owner);
        
        if (elems.length != 2) {
            throw new RuntimeException("Incorrect subexpressions number for BOTH element");
        } // if
        
        expr = new Any(owner, new Follow(owner, elems[0], elems[1]), new Follow(owner, elems[1], elems[0]));
        
        children = elems;
    } // FuzzyRegexBoth
    
    
    @Override
    double matchAt(int i) {
        double res;
        
        matchStart = i;
        res = expr.matchAt(i);
        matchLen = expr.getMatchLen();
        matchReplacement = expr.getReplacement();
        
        saveGroup();
        
        return res;
    } // matchAt


    @Override
    public String toString() {
        return childrenString("(=", ")") + super.toString();
    } // toString
    
    
} // class FuzzyRegexBoth
