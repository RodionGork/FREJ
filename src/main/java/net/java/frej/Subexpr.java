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


final class Subexpr extends Elem {

    
    private String subName;
    private Elem elem;
    
    Subexpr(Regex owner, String name) {
        super(owner);
        elem = owner.subs.get(subName = name);
    } // Subexpr
    
    
    @Override
    double matchAt(int i) {
        double retVal;
        retVal = elem.matchAt(i);
        matchLen = elem.getMatchLen();
        saveGroup();
        return retVal;
    } // matchAt
    
    
    @Override
    String getReplacement() {
        
        if (replacement == null) {
            
            return elem.getReplacement();
            
        } // if
        
        return super.getReplacement();
    } // getReplacement
    
    
    @Override
    String getMatchReplacement() {
        return elem.getReplacement();
    } // getMatchReplacement


    @Override
    public String toString() {
        return "(@" + subName + ")" + super.toString();
    } // toString
    
    
} // Supexpr
