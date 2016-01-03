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


package test;


import java.util.*;
import java.io.*;
import java.nio.charset.*;

import net.java.frej.*;


/**
 * This class uses frej library in the way similar to "grep" tool:
 * you pass regexp pattern as a command line argument (you'd better use quotes)
 * and then enter test lines. For each of lines program attempts to find a
 * matching region according to pattern and if succeeds, return the string
 * with the region changed by "regex.replacement". Try the example presented
 * when you specify no patterns at all. 
 * 
 * @author Rodion Gorkovenko
 */
public class Main {
    

    private static boolean debug = System.getProperties().getProperty("DEBUG") != null;    
    private static List<String> argList;
    private static String charsetName;
    private static String punctuators;
    private static double threshold;
    

    private Main() {}
    
    
    /** it is the main (and only) method of a class */
    public static void main(String... args) {
        Scanner in = new Scanner(System.in);
        Regex regex;
        int matchMode = 0;
        String temp;
        
        argList = new LinkedList<String>(Arrays.asList(args));
        
        if (argList.size() < 1 || fetchParameter("help") != null) {
            printHelp();
            return;
        } // if
        
        if ((temp = fetchParameter("mode")) != null) {
            if (temp.equals("exact")) {
                matchMode = 0;
            } else if (temp.equals("start")) {
                matchMode = 1;
            } else if (temp.equals("substr")) {
                matchMode = 2;
            } else {
                System.err.println("Error in mode (" + temp +")");
                return;
            } // else
        } // if
        
        charsetName = fetchParameter("charset");
        if (charsetName == null) {
            charsetName = Charset.defaultCharset().name();
        } // if
        
        if ((temp = fetchParameter("threshold")) != null) {
            threshold = Double.parseDouble(temp);
        } else {
            threshold = -1; //use default value
        } // else
        
        punctuators = fetchParameter("punctuators");
        if (punctuators == null) {
            punctuators = "/-";
        } // if

        if ((temp = fetchParameter("pattern")) != null) {
            String pattern = loadPattern(temp);
            if (pattern == null) {
                System.err.println("Problem with loading pattern from file");
                System.exit(1);
            } // if
            regex = new Regex(pattern, threshold, punctuators);
        } else if ((temp = fetchParameter("autotest")) != null) {
            try {
                processAutoTest(temp);
            } catch (Exception e) {
                System.out.println("Exception while autotesting");
                e.printStackTrace();
            } // catch
            return;
        } else if (argList.size() < 1) {
            System.err.println("Pattern missing from the command-line");
            return;
        } else if (argList.size() > 1) {
            System.err.println("Extra arguments in the command-line");
            return;
        } else {
            regex = new Regex(argList.get(0), threshold, punctuators);
        } // else
        
        try {

            while (in.hasNext()) {
            	String line = in.nextLine();
            	boolean b = false;

                switch (matchMode) {
                case 0:
                    b = regex.match(line);
                    break;
                case 1:
                    b = regex.matchFromStart(line);
                    break;
                case 2:
                    b = regex.presentInSequence(line) >= 0;
                } // switch

                if (b) {
                    System.out.println(regex.getReplacement());
                } else {
                    if (debug) {
                        System.out.println("(match failed)");
                    } // if
                } // else

                if (debug) {
                    System.out.println(regex.getMatchResult());
                } // if
            } // while

        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {}

    } // main
    
    
    private static void printHelp() {
        System.err.println("USAGE: java -jar frej.jar [--arg=value, ... ] [\"pattern\"]");
        System.err.println("allowed arguments:");
        System.err.println("  --pattern=<filename.ext>        - load pattern from file");
        System.err.println("  --charset=<charset>             - charset to use for loading from file");
        System.err.println("                                    (like UTF-8, IBM866, windows-1251)");
        System.err.println("  --mode=<exact|start|substr>     - matching mode (whole string, from start");
        System.err.println("                                    or anywhere in the string)");
        System.err.println("  --threshold=<value>             - threshold (allowed quantity of mistakes");
        System.err.println("                                    (0.0 .. 1.0), default is 0.34)");
        System.err.println("  --punctuators=<marks>           - punctuation marks recognized as tokens");
        System.err.println("                                    (default are slash and dash, i.e. /-)");
        System.err.println();
        System.err.println("Pattern specification example:");
        System.err.println("    java -jar frej.jar \"(give,(#)~A,(^doll*,buck*,usd))|got_$A_dollars\"");
        System.err.println("        give 5 dollars");
        System.err.println("        giv 70 bucks");
        System.err.println("        gave 1000 usd");
        System.err.println();
        System.err.println("Copyright 2011 Rodion Gorkovenko (Saint-Petersburg, Russia)");
        System.err.println("  http://frej.sf.net");
        System.err.println("  FREJ is distributed under terms of GNU Lesser General Public License");
    } // printHelp
    
    
    private static String fetchParameter(String name) {
        
        name = "--" + name + "=";
        
        for (String s : argList) {
            if (s.startsWith(name)) {
                argList.remove(s);
                return s.substring(name.length());
            } // if
        } // for
        
        return null;
    } // fetchParameter
    
    
    private static String loadPattern(String fileName) {
        BufferedReader in;
        StringBuilder b;
        
        if (fileName == null) {
            return null;
        } // if
        
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), Charset.forName(charsetName)));
        } catch (FileNotFoundException e) {
            return null;
        } // catch
        
        b = new StringBuilder();
        
        while(true) {
            String s;
            
            try {
                s = in.readLine();
            } catch (IOException e) {
                return null;
            } // catch
            
            if (s == null) {
                if (b.length() > 0) {
                    b.deleteCharAt(b.length() - 1);
                } // if
                break;
            } // if
            
            b.append(s);
            b.append('\r');
        } // while
        
        return b.toString();
    } // loadPattern


    private static void processAutoTest(String fileName) throws FileNotFoundException, IOException {
        BufferedReader in;

        in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), Charset.forName(charsetName)));

        for (int i = 0, n = Integer.parseInt(in.readLine().replaceAll("\\s.*", "")); i < n; i++) {
            String e = testOnePattern(in);
            if (!e.isEmpty()) {
                System.out.println(i + ":" + e);
            } // if
        } // for

    } // processAutoTest

    
    private static String testOnePattern(BufferedReader in) throws IOException {
        String pattern = "";
        String errors = "";
        Regex r;

        for (int n = Integer.parseInt(in.readLine().replaceAll("\\s.*", "")); n > 0; n--) {
            pattern += in.readLine() + "\r";
        } // for

        r = new Regex(pattern, threshold, punctuators);

        for (int i = 0, n = Integer.parseInt(in.readLine().replaceAll("\\s.*", "")); i < n; i++) {
            String answer;
            String mode, test;
            boolean b = false;
            
            test = in.readLine();
            mode = test.replaceFirst("\\s.*", "");
            test = test.replaceFirst("\\S+\\s+", "");
            if (mode.equals("substr")) {
                b = r.presentInSequence(test) >= 0;
            } else if (mode.equals("start")) {
                b = r.matchFromStart(test);
            } else if (mode.equals("exact")) {
                b = r.match(test);
            } else if (mode.equals("fail")) {
                b = !r.match(test);
                if (!b) {
                    errors += " " + i;
                } // if
                continue;
            } else {
                System.err.println("unknown test directive (" + mode +")!");
            } // else

            answer = in.readLine();

            if (!b || !answer.equals(r.getReplacement())) {
                errors += " " + i;
            } // if
        } // for

        return errors;
    } // testOnePattern
    

} // class Main

