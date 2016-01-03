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


abstract class Elem {

    Regex owner;
    int matchStart, matchLen;
    Elem[] children;
    String replacement, matchReplacement;
    String group;
    boolean optional;
    
    
    Elem(Regex owner) {
        this.owner = owner;
    } // FuzzyRegexElem
    
    
    abstract double matchAt(int i);
    
    
    int getMatchLen() {
        return matchLen;
    } // getMatchLen
    
    
    String childrenString(String prefix, String suffix) {
        StringBuilder s = new StringBuilder(prefix);
        
        for (int i = 0; i < children.length; i++) {
            s.append(children[i].toString());
            s.append(',');
        } // for
        
        s.deleteCharAt(s.length() - 1);
        s.append(suffix);
        
        return s.toString();
    } // childrenString
    
    
    void setReplacement(String repl) {
        replacement = repl;
    } // setReplacement
    
    
    String getReplacement() {
        StringBuilder s = new StringBuilder();
        
        if (replacement == null) {
            
            return getMatchReplacement();
            
        } else {
        
            for (int i = 0; i < replacement.length(); i++) {
                char c = replacement.charAt(i);
                
                if (c == '$' && i < replacement.length() - 1) {
                    c = replacement.charAt(++i);

                    switch (c) {
                    case '$':
                        s.append(getMatchReplacement());
                        break;
                    case '<':
                        s.append(owner.prefix());
                        break;
                    case '>':
                        s.append(owner.suffix());
                        break;
                    default:
                        if (i < 2 || replacement.charAt(i - 2) != '(') {
                            s.append(owner.getGroup(replacement.substring(i, i + 1)));
                        } else {
                            String name = replacement.substring(i).replaceFirst("\\).*", "");
                            s.deleteCharAt(s.length() - 1);
                            s.append(owner.getGroup(name));
                            i += name.length();
                        } // else
                        break;
                    } // switch
                    continue;
                } // if
                
                s.append(c);
            } // for
            
        } // else

        return s.toString();
    } //getReplacement
    
    
    String getMatchReplacement() {
        StringBuilder s = new StringBuilder();
        
        if (matchReplacement != null) {
            return matchReplacement;
        } // if
        
        for (int i = 0; i < matchLen; i++) {
            s.append(owner.tokens[matchStart + i]);
            s.append(' ');
        } // for
        if (s.length() > 0) {
            s.deleteCharAt(s.length() - 1);
        } // if
        
        return s.toString();
    } // getMatchReplacement
    
    
    void saveGroup() {
        
        if (group == null || group.isEmpty()) {
            return;
        } // if
        
        owner.setGroup(group, getMatchReplacement());
    } // saveGroup
    
    
    void setGroup(String name) {
        group = name;
    } // setGroup
    
    
    @Override
    public String toString() {
        return (group != "" && group != null ? "~" + group : "") + (replacement != null ? "|" + replacement : "");
    } // toString
    
    
} // class FuzzyRegexElem
