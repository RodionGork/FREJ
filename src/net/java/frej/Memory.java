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


final class Memory extends Elem {

    
    private String groupName;
    private Token token;
    
    Memory(Regex owner, String name) {
        super(owner);
        token = new Token(owner, null);
        groupName = name;
    } // Memory
    
    
    @Override
    double matchAt(int i) {
        double retVal;
        token.changePattern(owner.getGroup(groupName));
        retVal = token.matchAt(i);
        matchLen = token.getMatchLen();
        return retVal;
    } // matchAt
    
    
    @Override
    String getReplacement() {
        return token.getReplacement();
    } // getReplacement
    
    @Override
    String getMatchReplacement() {
        return token.getMatchReplacement();
    } // getMatchReplacement

    @Override
    public String toString() {
        return "($" + groupName + ")" + super.toString();
    } // toString
    
    
    @Override
    void setGroup(String name) {
        token.setGroup(name);
    } // setGroup
    
    
} // Memory
