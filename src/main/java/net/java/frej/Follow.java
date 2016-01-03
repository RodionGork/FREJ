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


final class Follow extends Elem {
    
    
    Follow(Regex owner, Elem... elems) {
        super(owner);
        children = elems;
    } // FuzzyRegexFollow
    
    
    @Override
    double matchAt(int i) {
        
        class PartMatcher {
            public int len;
            public double res;
            public StringBuilder s = new StringBuilder();
            
            public PartMatcher() {
                len = 0;
                res = 1;
            } // PartMatcher
            
            public PartMatcher(PartMatcher copy) {
                len = copy.len;
                res = copy.res;
                s.append(copy.s);
            } // PartMatcher
            
            public double matchAtFrom(int i, int j) {
                
                
                while(j < children.length) {
                    double cur;
                    
                    if (children[j].optional) {
                        PartMatcher incl, excl;
                        Regex.GroupMap inclGroups, tempGroups;

                        tempGroups = new Regex.GroupMap(owner.groups);
                        cur = children[j].matchAt(i + len);
                        incl = new PartMatcher(this);
                        if (cur < 1) {
                            incl.res *= 1 - Math.min(cur, 1);
                            incl.len = len + children[j].getMatchLen();
                            incl.s.append(children[j].getReplacement());
                            incl.matchAtFrom(i, j + 1);
                        } else {
                            incl.res = 1;
                            incl.len = 0;
                        } // else
                        excl = new PartMatcher(this);
                        
                        inclGroups = owner.groups;
                        owner.groups = tempGroups;
                        excl.matchAtFrom(i, j + 1);
                        
                        if (incl.res < 1 && incl.res <= excl.res) {
                            res = incl.res;
                            len = incl.len;
                            s.replace(0, s.length(), incl.s.toString());
                            owner.groups = inclGroups;
                        } else {
                            res = excl.res;
                            len = excl.len;
                            s.replace(0, s.length(), excl.s.toString());
                        } // else
                        
                        return res;

                    } else {
                        cur = children[j].matchAt(i + len);
                    } // else

                    res *= 1 - Math.min(cur, 1);
                    if (res == 0) {
                        res = 1;
                        len = 0;
                        return res;
                    } // if
                    len += children[j].getMatchLen();
                    s.append(children[j].getReplacement());
                    j++;
                } // while
                
                return (res = 1 - Math.pow(res, 1.0 / len));
                
            } // matchAtFrom
            
        } // class PartMatcher
        
        PartMatcher pm = new PartMatcher();
        pm.matchAtFrom(i, 0);
        matchStart = i;
        matchLen = pm.len;
        matchReplacement = pm.s.toString();
        saveGroup();
        
        return pm.res;
    } // matchAt
    
    
    @Override
    public String toString() {
        return childrenString("(", ")") + super.toString();
    } // toString
    
    
} // class FuzzyRegexFollow
