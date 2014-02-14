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


final class Numeric extends Elem {
    
    
    private int min, max;
    
    
    Numeric(Regex owner, String param) {
        super(owner);
        
        String[] parts = param.split(":");
        
        if (parts.length == 0 || parts[0].isEmpty()) {
            min = Integer.MIN_VALUE;
            max = Integer.MAX_VALUE;
        } else if (parts.length == 1) {
            min = 1;
            max = Integer.parseInt(parts[0]);
        } else {
            min = Integer.parseInt(parts[0]);
            max = Integer.parseInt(parts[1]);
        } // else
        
    } // FuzzyRegexNumeric
    
    
    @Override
    double matchAt(int i) {
        int val;
        
        matchStart = i;
        matchLen = 0;

        if (i >= owner.tokens.length) {
            return Double.POSITIVE_INFINITY;
        } // if
        
        try {
            val = Integer.parseInt(owner.tokens[i]);
        } catch (NumberFormatException e) {
            return Double.POSITIVE_INFINITY;
        } // catch
        
        if (val < min || val > max) {
            return Double.POSITIVE_INFINITY;
        } // if
        
        matchLen = 1;

        saveGroup();
        
        return 0;
    } // matchAt
    
    
    @Override
    public String toString() {
        if (min == Integer.MIN_VALUE && max == Integer.MAX_VALUE) {
            return "(#)";
        } else if (min == 1) {
            return "(#" + max + ")";
        } // else if
        return "(#" + min + ":" + max + ")" + super.toString(); 
    } // toString
    
    
} // class FuzzyRegexNumeric
