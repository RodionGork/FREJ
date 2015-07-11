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

package net.java.frej.fuzzy;


import java.util.*;


/**
 * Class providing fuzzy string comparison.
 * 
 * It is used in fuzzy regexp matching, but could also be used
 * alone, for fuzzy string matching or fuzzy substring search.
 * 
 * Based on Demerau-Levenshtein distance evaluation, i.e. there are four
 * types of "mistakes" each counting as 1 point (char deletion, char adding,
 * char replacement, swap of two adjacent chars).
 * 
 * @author Rodion Gorkovenko
 */
public final class Fuzzy {
    
    /** keeps starting position of matched region after substring search*/
    public int resultStart;
    /** keeps ending position of matched region after substring search*/
    public int resultEnd;
    /** keeps index of best match after matching against a list of strings*/
    public int resultIndex;
    /** keeps best matched string after matching against a list of strings*/
    public String matchedPattern;
    /** "distance" of last match (roughly mistakes count divided by length*/
    public double result;
    /** if result of match is higher than threshold, boolean methods return "false" */
    public double threshold = 0.34;

    protected final int MAX_PATTERN = 64;
    protected final int MAX_SOURCE = 256;
    protected final int BIG_VALUE = 1000000;
    protected int[][] e = new int[MAX_PATTERN + 1][MAX_SOURCE + 1];
    protected WayType[][] w = new WayType[MAX_PATTERN + 1][MAX_SOURCE + 1];
    
    
    private static enum WayType {
        TRANSIT, INSERT, DELETE, SUBST, SWAP
    } // enum WayType

    
    /**
     * Tries to find substring "pattern" in the "source" and if successful,
     * returns the position of the beginning of the match.
     * @return position of found substring (0 .. source.length() - 1) or (-1) if
     * substring was not found (with given threshold).
     */
    public int substrStart(CharSequence source, CharSequence pattern) {
        
        if (containability(source, pattern) < threshold)
            return resultStart;
        
        return -1;
    } // find
    

    /**
     * Tries to find substring "pattern" in the "source" and if successful,
     * returns the position of the end of the match.
     * @return position of found substring end (0 .. source.length() - 1) or (-1) if
     * substring was not found (with given threshold).
     */
    public int substrEnd(CharSequence source, CharSequence pattern) {
        
        if (containability(source, pattern) < threshold)
            return resultEnd;
        
        return -1;
    } // find
    

    /**
     * Tests whether "source" matches "pattern".
     * @return true or false depending on match quality.
     */
    public boolean equals(CharSequence source, CharSequence pattern) {
        return similarity(source, pattern) < threshold;
    } // compare
    
    
    /**
     * Tests whether any of "patterns" is presented in "source" as substring.
     * Stops on first good match.
     * @return true or false depending on whether any of pattern search succeeds.
     */
    public boolean containsOneOf(CharSequence source, CharSequence... patterns) {
        
        for (CharSequence p : patterns) {
            if (containability(source, p) < threshold) {
                return true;
            } // if
        } // for
        
        return false;
    } // containsOneOf
    
    
    /**
     * Core method for searching substring. Finds the region for which
     * Demerau-Levenshtein distance is minimal.
     * @return normalized best distance (i.e. distance / pattern.length())
     */
    public double containability(CharSequence source, CharSequence pattern) {
        int m = pattern.length() + 1;
        int n = source.length() + 1;
        int best, start;
        char p, s, p1, s1;

        for (int x = 0; x < n; x++) {
            e[0][x] = 0;
        } // for

        p = 0;
        
        for (int y = 1; y < m; y++) {
            e[y][0] = y;
            w[y][0] = WayType.DELETE; 

            p1 = p;
            p = Character.toUpperCase(pattern.charAt(y - 1));
            s = 0;
            
            for (int x = 1; x < n; x++) {
                int cost;
                int val, temp;

                s1 = s;
                s = Character.toUpperCase(source.charAt(x - 1));
                
                cost =  (p == s) ? 0 : 1;
                
                val = e[y - 1][x - 1] + cost;
                w[y][x] = WayType.SUBST;

                temp = e[y - 1][x] + 1;
                if (val > temp) {
                    val = temp;
                    w[y][x] = WayType.DELETE;
                } // if
                
                temp = e[y][x - 1] + 1;
                if (val > temp) {
                    val = temp;
                    w[y][x] = WayType.INSERT;
                } // if

                if (p1 == s && p == s1) {
                    temp = e[y - 2][x - 2] + cost;
                    if (val > temp) {
                        val = temp;
                        w[y][x] = WayType.SWAP;
                    } // if
                } // if
                
                e[y][x] = val;
            } // for

        } // for

        best = n - 1;
        for (int x = 0; x < n; x++) {
            if (e[m - 1][x] < e[m - 1][best]) {
                best = x;
            } // if
        } // for

        start = best;
        for (int y = m - 1; y > 0;) {
            switch (w[y][start]) {
            case INSERT:
                start--;
                break;
            case DELETE:
                y--;
                break;
            case SWAP:
                y-=2;
                start-=2;
                break;
            default:
                start--;
                y--;
                break;
            } // switch
        } // for

        resultStart = start + 1;
        resultEnd = best;

        return (result = e[m - 1][best] / (double) pattern.length());
    } // containability

    
    /**
     * Given list or array of strings, searches for one which is best matched
     * with whole original string (equality=true) or with some its substring
     * (equality=false).
     * @return best match result (normalized distance). 
     */
    public double bestEqual(String string, Object patterns, boolean equality) {
        String[] array;
        double value = Double.POSITIVE_INFINITY;
        
        if (patterns instanceof String[]) {
            array = (String[]) patterns;
        } else if (patterns instanceof Collection) {
            Collection<?> c = (Collection<?>) patterns; 
            array = c.toArray(new String[c.size()]);
        } else {
            throw new IllegalArgumentException();
        } // else
        
        resultIndex = -1;
        
        for (int i = 0; i < array.length; i++) {
            double cur = equality ? similarity(string, array[i]) : containability(string, array[i]);
            if (cur < value) {
                value = cur;
                resultIndex = i;
                matchedPattern = array[i];
            } // if
        } // for
        
        return value;
    } // bestEqual
    
    
    /**
     * Core method for measuring Demerau-Levenshtein distance between two strings.
     * @return normalized distance (distance / average(source.length(), pattern.length()))
     */
    public double similarity(CharSequence source, CharSequence pattern) {
        int m;
        int n;
        char s, p, s1, p1;
        
        m = pattern.length() + 1;
        n = source.length() + 1;
        for (int x = 0; x < n; x++) {
            e[0][x] = x; 
        } // for
        
        p = 0;
        
        for (int y = 1; y < m; y++) {
            e[y][0] = y;
            
            p1 = p;
            p = Character.toUpperCase(pattern.charAt(y - 1));
            s = 0;

            for (int x = 1; x < n; x++) {
                int val = BIG_VALUE;
                int cost;
                
                s1 = s;
                s = Character.toUpperCase(source.charAt(x - 1));
                
                cost = (s == p) ? 0 : 1;

                val = e[y - 1][x - 1] + cost;
                val = Math.min(e[y][x - 1] + 1, val);
                val = Math.min(e[y - 1][x] + 1, val);
                
                if (s == p1 && p == s1) {
                    val = Math.min(e[y - 2][x - 2] + cost, val);
                } // if

                e[y][x] = val;
                
            } // for
        } // for
        
        return (result = 2 * e[m - 1][n - 1] / (m + n - 2.0));
    } // similarity


} // Fuzzy
