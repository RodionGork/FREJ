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


final class Any extends Elem {

    
    Any(Regex owner, Elem... elems) {
        super(owner);
        children = elems;
    } // FuzzyRegexAny
    
    
    @Override
    double matchAt(int i) {
        double bestResult = Double.POSITIVE_INFINITY;
        Regex.GroupMap tempGroups = null;
        Regex.GroupMap oldGroups = new Regex.GroupMap(owner.groups);
        int bestNum = -1;
        
        matchStart = i;
        matchLen = 0;
        
        for (int j = 0; j < children.length; j++) {
            double cur;
            owner.groups = new Regex.GroupMap(oldGroups);
            cur = children[j].matchAt(i); 
            if (cur < bestResult || cur == bestResult && children[j].getMatchLen() > matchLen) {
                bestNum = j;
                bestResult = cur;
                matchLen = children[j].getMatchLen();
                tempGroups = owner.groups;
            } // if
        } // for

        if (tempGroups != null) {
            owner.groups = tempGroups;
        } // if
        
        matchReplacement = (bestNum >= 0) ? children[bestNum].getReplacement() : null;
        saveGroup();
        
        return bestResult;
    } // matchAt
    
    
    @Override
    public String toString() {
        return childrenString("(^", ")") + super.toString();
    } // toString
    
    
} // class FuzzyRegexAny
