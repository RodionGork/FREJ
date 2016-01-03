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


import java.util.*;

import net.java.frej.fuzzy.Fuzzy;


/**
 * Class represents fuzzy regular expression at whole.
 * 
 * Pattern of fuzzy regexp is passed as string to constructor.
 * 
 * Then any string could be checked against this regexp with the
 * help of match, matchFromStart or presentInSequence methods.
 * 
 * After matching it is possible to receive replacement for matched
 * region via getReplacement method.
 * 
 * Few more auxiliary methods provided for handling parts of original
 * string and result.
 * 
 * @author Rodion Gorkovenko
 */
public final class Regex {
    
    
    private enum CharType {
        SEPARATOR, DIGIT, LETTER, ALLOWED_PUNCT
    } // CharType
    
    private Fuzzy fuzzy = new Fuzzy();
    private Elem root;
    private Special terminator = new Special(this, null);
    String[] tokens;
    private int[] tokenPos;
    private double matchResult;
    private String original, replaceResult;
    private int firstMatched, lastMatched;
    GroupMap groups = new GroupMap();
    private String allowedPunct = "/-";
    private double threshold = fuzzy.threshold;
    Map<String, Elem> subs = new HashMap<String, Elem>();
    
    
    @SuppressWarnings("serial")
    static class GroupMap extends HashMap<String, String> {
        
        public GroupMap() {
            super();
        } // GroupMap
        
        public GroupMap(Regex.GroupMap map) {
            super(map);
        } // GroupMap
        
    } // GroupMap
    
    
    /**
     * Creates new regular expression (builds it as a tree of elements) from
     * presented pattern. Behavior is undefined if pattern is incorrect.
     */
    public Regex(String pattern) {
        String ssubs[];
        pattern = fixPattern(pattern);
        ssubs = pattern.split("::");
        for (int i = ssubs.length - 1; i >= 0; i--) {
            int p;
            if (i > 0) {
                for (p = 0; Character.isLetterOrDigit(ssubs[i].charAt(p)); p++);
            } else {
                p = 0;
            } // else
            subs.put(ssubs[i].substring(0, p), parse(ssubs[i].substring(p).replaceAll("\\s+", "")));
        } // for
        root = subs.get("");
    } // Regex
    
    
    /**
     * Creates new regular expression from presented pattern, specifying
     * also settings of threshold value and allowed punctuation marks.
     */
    public Regex(String pattern, double threshold, String punctuators) {
        this(pattern);
        if (threshold >= 0) {
            setThreshold(threshold);
        } // if
        if (punctuators != null) {
            setAllowedPunctuationMarks(punctuators);
        } // if
    } // punctuators
    
    
    private String fixPattern(String pattern) {
        StringBuilder b = new StringBuilder();
        Stack<Character> brackets = new Stack<Character>();
        int slashes = 0;
        boolean comment = false;
        char prevChar = 0;
        int lineCount = 1, posCount = 0;
        
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            posCount++;
            
            if (comment) {
                if (c == '\r' || c == '\n') {
                    comment = false;
                } else {
                    continue;
                } // else
            } else {
                if (prevChar == '/' && c == '/') {
                    b.deleteCharAt(b.length() - 1);
                    comment = true;
                    continue;
                } // if
            } // else
            
            prevChar = c;
            b.append(c);
            
            if (c == '\\') {
                slashes++;
                continue;
            } // if
            
            if ((slashes & 1) == 0) {
                if (Character.isWhitespace(c)) {
                    if (c == '\r' || c == '\n') {
                        lineCount++;
                        posCount = 0;
                    } // if
                } else if ("({[".indexOf(c) >= 0) {
                    brackets.push(Character.valueOf(c));
                    b.replace(b.length() - 1, b.length(), "(");
                } else if (")}]".indexOf(c) >= 0) {
                    if (brackets.empty() || brackets.peek() != "({[".charAt(")}]".indexOf(c))) {
                        throw new IllegalArgumentException("Mismatched bracket at " + lineCount + ":" + posCount);
                    } // if
                    brackets.pop();
                    b.replace(b.length() - 1, b.length(), ")");
                } // if
            } else {
                // opening and closing round brackets should be presented as \o and \c
                switch (c) {
                    case '(':
                        b.replace(b.length() - 1, b.length(), "o");
                        break;
                    case ')':
                        b.replace(b.length() - 1, b.length(), "c");
                        break;
                } // switch
            } // else
            
            slashes = 0;
        } // for
        
        return b.toString();
    } // fixPattern
    
    
    private String eliminateEscapes(String s) {
        StringBuilder b = new StringBuilder();
        boolean prevSlash = false;
        
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            
            if (!prevSlash) {
                if (c == '\\') {
                    prevSlash = true;
                } else if (c == '_') {
                    b.append(' ');
                } else {
                    b.append(c);
                } // else
            } else {
                prevSlash = false;
                if (c == 'r') {
                    b.append('\r');
                } else if (c == 'n') {
                    b.append('\n');
                } else if (c == 'o') {
                    b.append('(');
                } else if (c == 'c') {
                    b.append(')');
                } else {
                    b.append(c);
                } // else
            } // else
        } // for
        
        return b.toString();
    } // eliminateEscapes
    
    
    private Elem parse(String pattern) {
        Elem retVal;
        String repl = null;
        String g;
        
        replSearch:
        for (int i = pattern.length() - 1, brackets = 0; i >= 0; i--) {
            char c = pattern.charAt(i);
            switch (c) {
            case ')': brackets++; break;
            case '(': brackets--; break;
            case '|':
                if (brackets == 0) {
                    repl = pattern.substring(i + 1);
                    pattern = pattern.substring(0, i);
                    break replSearch;
                } // if
                break;
            } // switch
        } // for
        
        g = extractGroupName(pattern);
        if (g.length() > 0) {
            pattern = pattern.substring(0, pattern.length() - g.length() - 1);
        } // if
        
        if (pattern.charAt(0) == '(') {
            String expr;
            int p = 1;
            boolean optional;

            if (pattern.charAt(p) == '?') {
                optional = true;
                p++;
            } else {
                optional = false;
            } // else
            
            if (pattern.length() <= p || pattern.charAt(pattern.length() - 1) != ')') {
                throw new RuntimeException("Unclosed closure!"); 
            } // if
            
            expr = pattern.toString().substring(p + 1, pattern.length() - 1);
            
            switch (pattern.charAt(p)) {
            case '=':
                retVal = new Both(this, parseList(expr));
                break;
                
            case '^':
                retVal = new Any(this, parseList(expr));
                break;
                
            case '!':
                retVal = new Regular(this, eliminateEscapes(expr));
                break;
                
            case '#':
                retVal = new Numeric(this, expr);
                break;
            
            case '@':
                if (!subs.containsKey(expr)) {
                    throw new RuntimeException("Undefined sub '" + expr + "'!");
                } // if
                retVal = new Subexpr(this, expr);
                break;
                
            case '$':
                retVal = new Memory(this, expr);
                break;
            
            case '.':
                retVal = new Special(this, expr);
                break;
                
            default:
                retVal = new Follow(this, parseList(pattern.toString().substring(p, pattern.length() - 1)));
                break;
                
            } // switch

            retVal.optional = optional;
            
        } else {
            retVal = new Token(this, eliminateEscapes(pattern)); 
        } // else
        
        if (repl != null) {
            retVal.setReplacement(eliminateEscapes(repl));
        } // if
        
        retVal.setGroup(g);
        
        return retVal;
    } // parse
    
    
    private String extractGroupName(String pattern) {
        
        for (int i = pattern.length() - 1; i >= 0; i--) {
            char c = pattern.charAt(i);
            if (c == '~') {
                return pattern.substring(i + 1);
            } // if
            if (!Character.isLetterOrDigit(c)) {
                return "";
            } // if
        } // for
        
        return "";
    } // pattern
    
    
    private Elem[] parseList(String pattern) {
        List<Elem> list = new LinkedList<Elem>();
        StringBuilder s = new StringBuilder(pattern);
        int brackets = 0;
        
        s.append(',');
        
        for (int i = 0; s.length() > 0; i++) {
            char c = s.charAt(i);
            
            if (c == '(') {
                brackets++;
            } else if (c == ')') {
                brackets--;
            } else if (c == ',' && brackets == 0) {
                list.add(parse(s.substring(0, i)));
                s.delete(0, i + 1);
                i = -1;
                continue;
            } // else if
        } // for
        
        return list.toArray(new Elem[list.size()]);
    } // parseList
    
    
    /**
     * Checks whether this regexp matches to any subsequence in presented string.
     * @return number of token from which best match starts or (-1) if all matches
     * are bad enough.
     */
    public int presentInSequence(String seq) {
        double bestResult = Double.POSITIVE_INFINITY;
        int bestPos = -1;
        int bestLen = -1;
        GroupMap tempGroups = null;
        
        groups.clear();
        splitTokens(seq);
        
        for (int i = 0; i < tokens.length; i++) {
            double cur = root.matchAt(i);
            if (cur < bestResult) {
                bestResult = cur;
                bestPos = i;
                bestLen = root.getMatchLen();
                firstMatched = bestPos;
                lastMatched = bestPos + bestLen - 1; 
                replaceResult = root.getReplacement();
                tempGroups = groups;
                groups = new GroupMap();
            } // if
        } // for
        
        if (bestResult > threshold) {
            return -1;
        } // if
        
        groups = tempGroups;
        matchResult = bestResult;
        
        return bestPos;
    } // presentInSequence
    
    
    /**
     * Check whether presented string matches with this regexp with all tokens.
     * @return true or false depending on quality of best matching variant.
     */
    public boolean match(String seq) {
        groups.clear();
        splitTokens(seq);
        matchResult = root.matchAt(0);
        if (matchResult > threshold || root.getMatchLen() != tokens.length) {
            return false;
        } // if
        firstMatched = 0;
        lastMatched = tokens.length - 1;
        replaceResult = root.getReplacement();
        return true;
    } // match
    
    
    /**
     * Checks whether this regexp matches to beginning of presented sequence.
     * @return true or false depending on quality of best match.
     */
    public boolean matchFromStart(String seq) {
        groups.clear();
        splitTokens(seq);
        matchResult = root.matchAt(0);
        if (matchResult > threshold) {
            return false;
        } // if
        firstMatched = 0;
        lastMatched = root.getMatchLen() - 1;
        replaceResult = root.getReplacement();
        return true;
    } // match
    
    
    /**
     * Returns result of the last match. Result is strongly linked to "distance"
     * between strings being fuzzy matched, i.e. it is roughly count of
     * dissimilarities divided by length of matched region.
     * 
     * For example "Free" and "Frej" match result is 0.25 while "Bold" and "Frej"
     * gives 1.0.
     * @return measure of dissimilarity, 0 means exact match.
     */
    public double getMatchResult() {
        return matchResult;
    } // getMatchResult
    
    
    /**
     * Gives replacement string which is generated after successful match
     * according to rules specified in regexp pattern.
     * @return replacement as a string.
     */
    public String getReplacement() {
        return replaceResult;
    } // getReplacement
    
    
    /**
     * Tells the character position (of string which have been matched) from
     * which the match starts.
     * @return position, as integer from range 0 .. seq.length() - 1
     */
    public int getMatchStart() {
        
        if (firstMatched < 0) {
            return 0;
        } else if (firstMatched >= tokens.length) {
            return original.length();
        } // else if
        
        return tokenPos[firstMatched];
    } // getMatchStart
    
    
    /**
     * Tells the character position (of string which have been matched) where
     * last match ends (i.e. position strictly following last character of matched region).
     * @return position, as integer from range 0 .. seq.length() - 1
     */
    public int getMatchEnd() {
        // returns position immediately following 
        
        if (lastMatched < 0) {
            return 0;
        } else if (lastMatched >= tokens.length) {
            return original.length();
        } // else if
        
        return tokenPos[lastMatched] + tokens[lastMatched].length();
    } // getMatchEnd
    
    
    /**
     * Reconstructs pattern which was used for creation of this regexp.
     * @return string representation of pattern.
     */
    public String pattern() {
        return root.toString();
    } // pattern
    
    
    /**
     * Tells number of tokens in matched region (mostly important when pattern
     * contains optional elements).
     * @return token count.
     */
    public int matchedTokenCount() {
        return root.getMatchLen();
    } // matchedTokenCount
    
    
    /**
     * Returns the part of matched string, which precedes matching region.
     * String is trimmed of spaces since spaces are token delimiters.
     * @return beginning of the seq used in presentInSequence etc.
     */
    public String prefix() {
        return original.substring(0, getMatchStart()).trim();
    } // prefix
    
    
    /**
     * Returns the part of matched string, which follows matching region.
     * String is trimmed of spaces since spaces are token delimiters.
     * @return ending of the seq used in presentInSequence etc.
     */
    public String suffix() {
        return original.substring(getMatchEnd()).trim();
    } // prefix
    
    
    /**
     * Allows to set up which punctuation marks are allowed in the tokens
     * By default only slash and dash i.e. punct = "/-"
     */
    public String setAllowedPunctuationMarks(String punct) {
        return allowedPunct = punct;
    } // setAllowedPunctuationMarks

    
    /**
     * Returns value of threshold used in matching methods to decide whether matching result
     * signifies match or mismatch. By default equals to frej.Fuzzy.threshold.
     */
    public double getThreshold() {
        return threshold;
    } // getThreshold
    
    
    /**
     * Sets value of threshold used in matching methods to decide whether matching result
     * signifies match or mismatch. By default equals to frej.Fuzzy.threshold.
     */
    public void setThreshold(double t) {
        threshold = t;
    } // setThreshold
    
    
    private void splitTokens(String expr) {
        List<String> tokenList = new LinkedList<String>();
        List<Integer> posList = new LinkedList<Integer>();
        StringBuilder s = new StringBuilder();
        CharType prevCharClass, charClass;
        
        original = expr;
        
        prevCharClass = CharType.SEPARATOR;
        for (int i = 0; i <= expr.length(); i++) {
            char c;
            
            try {
                c = expr.charAt(i);
            } catch (IndexOutOfBoundsException e) {
                c = 0;
                charClass = CharType.SEPARATOR;
            } // catch

            charClass = Character.isLetter(c) ? CharType.LETTER :
                    Character.isDigit(c) ? CharType.DIGIT : 
                    allowedPunct.indexOf(c) >= 0 ? CharType.ALLOWED_PUNCT : CharType.SEPARATOR;

            if ((prevCharClass != charClass || charClass == CharType.ALLOWED_PUNCT) && s.length() > 0) {
                tokenList.add(s.toString());
                posList.add(i - s.length());
                s.setLength(0);
            } // if

            prevCharClass = charClass;
            
            if (charClass != CharType.SEPARATOR) {
                s.append(c);
            } // if
            
        } // for
        
        tokens = tokenList.toArray(new String[tokenList.size()]);
        tokenPos = new int[posList.size()];
        for (int i = 0; i < tokenPos.length; i++) {
            tokenPos[i] = posList.remove(0);
        } // for
    } // splitTokens


    String getGroup(String name) {
        String s = groups.get(name);
        if (s == null) {
            return "";
        } // if
        return s;
    } // getGroup


    void setGroup(String name, String s) {
        groups.put(name, s);
    } // setGroup
    
    Fuzzy getFuzzy() {
        return fuzzy;
    }

} // class FuzzyRegex
