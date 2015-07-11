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

import net.java.frej.fuzzy.Fuzzy;


final class Token extends Elem {

    
    private String token;
    private boolean partial;
    
    private Fuzzy fuzzy;
    
    
    Token(Regex owner, String token) {
        super(owner);
        fuzzy = owner.getFuzzy();
        changePattern(token);
    } // FuzzyRegexToken

    
    @Override
    double matchAt(int i) {
        
        matchStart = i;
        matchLen = 0;

        if (i >= owner.tokens.length) {
            return Double.POSITIVE_INFINITY;
        } // if
        
        if (partial && owner.tokens[i].length() > token.length()) {
            fuzzy.similarity(owner.tokens[i].substring(0, token.length()), token);
        } else {
            fuzzy.similarity(owner.tokens[i], token);
        } // else
        
        matchLen = 1;

        saveGroup();
        
        return fuzzy.result;
    } // matchAt
    
    
    @Override
    String getReplacement() {
        
        if (replacement == null) {
            
            return partial ? getMatchReplacement() : token;
            
        } // if
        
        return super.getReplacement();
    } //getReplacement
    
    
    void saveGroup() {
        
        if (group == null || group.isEmpty()) {
            return;
        } // if
        
        owner.setGroup(group, getReplacement());
    } // saveGroup
    
    
    @Override
    public String toString() {
        return token + super.toString();
    } // toString


    void changePattern(String pattern) {
        if (pattern == null || pattern.charAt(pattern.length() - 1) != '*') {
            token = pattern;
            partial = false;
        } else {
            this.token = pattern.substring(0, pattern.length() - 1);
            partial = true;
        } // else
    } // changePattern
    
    
} // class FuzzyRegexToken
