#FREJ - Fuzzy Regular Expressions for Java

Project purpose:

Library and command-line tool for text processing, using
fuzzy matching of fragments joined by expressions of special form.

Was created for conversion of postal addresses infested with misspellings and variative names.

For example an expression:

    [^
        (barack, (?h*), obama) |44-th, // Current guy
        (=george, washington) |1-st,   // First man
        ({^abraham,abe},lincoln)|16-th // Best of all times
    ]~A |$A\_president\r\n\/\/of_U\_S\_A

Converts the name of the person (possibly misspelt) to `N-th president of USA` phrase.

[Complete documentation on patterns](http://frej.sourceforge.net/rules.html)

More detailed older project page and demo applet could be found at [frej.sf.net](http://frej.sourceforge.net/)
