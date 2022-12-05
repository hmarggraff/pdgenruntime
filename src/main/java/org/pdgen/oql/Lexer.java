// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.Named;
import org.pdgen.data.SortedNamedVector;
import org.pdgen.env.Res;

import java.util.Hashtable;

public class Lexer {
    public enum Token {
        EOFComment,
        Error,
        EOF,
        None,
        Nil,
        True,
        False,
        Mod,
        Abs,
        Like,
        Not,
        And,
        Or,
        First,
        Last,
        ForAll,
        Exists,
        Unique,
        Instanceof,
        In,
        Some,
        Any,
        All,
        Count,
        Sum,
        Min,
        Max,
        Avg,
        Intersect,
        Union,
        Except,
        Element,
        LBrace,
        RBrace,
        LBracket,
        RBracket,
        Dot,
        Minus,
        Plus,
        Star,
        Slash,
        Less,
        Greater,
        LessOrEqual,
        GreaterOrEqual,
        Equal,
        Unequal,
        Elipses,
        Colon,
        Comma,
        Question,
        Name,
        String,
        Int,
        Char,
        Float,
    }

    /*
    public static final int tokEOFComment = -3;
    public static final int tokError = -2;
    public static final int tokEOF = -1;
    public static final int tokNone = 0;
    @SuppressWarnings({"PointlessArithmeticExpression"})
    public static final int tokNil = tokNone + 1;//1
    public static final int tokTrue = tokNil + 1;
    public static final int tokFalse = tokTrue + 1;
    public static final int tokMod = tokFalse + 1;
    public static final int tokAbs = tokMod + 1;
    public static final int tokLike = tokAbs + 1;
    public static final int tokNot = tokLike + 1;
    public static final int tokAnd = tokNot + 1;
    public static final int tokOr = tokAnd + 1;
    public static final int tokFirst = tokOr + 1; // 10
    public static final int tokLast = tokFirst + 1;
    public static final int tokForAll = tokLast + 1;
    public static final int tokExists = tokForAll + 1;
    public static final int tokUnique = tokExists + 1;
    public static final int tokInstanceof = tokUnique + 1;
    public static final int tokIn = tokInstanceof + 1;
    public static final int tokSome = tokIn + 1;
    public static final int tokAny = tokSome + 1;
    public static final int tokAll = tokAny + 1;
    public static final int tokCount = tokAll + 1; //20
    public static final int tokSum = tokCount + 1;//21
    public static final int tokMin = tokSum + 1;
    public static final int tokMax = tokMin + 1;
    public static final int tokAvg = tokMax + 1;
    public static final int tokIntersect = tokAvg + 1;
    public static final int tokUnion = tokIntersect + 1;
    public static final int tokExcept = tokUnion + 1;
    public static final int tokElement = tokExcept + 1;
    public static final int tokLBrace = tokElement + 1;
    public static final int tokRBrace = tokLBrace + 1;//30
    public static final int tokLBracket = tokRBrace + 1;//31
    public static final int tokRBracket = tokLBracket + 1;
    public static final int tokDot = tokRBracket + 1;
    public static final int tokMinus = tokDot + 1;//34
    public static final int tokPlus = tokMinus + 1;
    public static final int tokStar = tokPlus + 1; //36
    public static final int tokSlash = tokStar + 1;
    public static final int tokLess = tokSlash + 1;
    public static final int tokGreater = tokLess + 1;
    public static final int tokLessOrEqual = tokGreater + 1; //40
    public static final int tokGreaterOrEqual = tokLessOrEqual + 1;//40
    public static final int tokEqual = tokGreaterOrEqual + 1;
    public static final int tokUnequal = tokEqual + 1;
    public static final int tokElipses = tokUnequal + 1;
    public static final int tokColon = tokElipses + 1;
    public static final int tokComma = tokColon + 1;
    public static final int tokQuestion = tokComma + 1;
    public static final int tokName = tokQuestion + 1;
    public static final int tokString = tokName + 1;
    public static final int tokInt = tokString + 1;
    public static final int tokChar = tokInt + 1;
    public static final int tokFloat = tokChar + 1;
    */
    public String errMsg;
    public Hashtable<String, Token> keywords;
    public Token currToken;
    protected int pos;
    protected String src;
    public long intVal;
    public double floatVal;
    public String stringVal;
    public Character charVal;
    public String nameVal;
    protected boolean isPushBack;

    public Lexer(String expression) {
        keywords = new Hashtable<String, Token>();
        src = expression;
        keywords.put("nil", Token.Nil);
        keywords.put("null", Token.Nil);
        keywords.put("true", Token.True);
        keywords.put("false", Token.False);
        keywords.put("mod", Token.Mod);
        keywords.put("like", Token.Like);
        keywords.put("not", Token.Not);
        keywords.put("and", Token.And);
        keywords.put("or", Token.Or);
        keywords.put("for", Token.ForAll);
        keywords.put("all", Token.All);
        keywords.put("in", Token.In);
        keywords.put("exists", Token.Exists);
        keywords.put("some", Token.Some);
        keywords.put("any", Token.Any);
        keywords.put("instanceof", Token.Instanceof);
        keywords.put("NIL", Token.Nil);
        keywords.put("NULL", Token.Nil);
        keywords.put("TRUE", Token.True);
        keywords.put("FALSE", Token.False);
        keywords.put("MOD", Token.Mod);
        keywords.put("LIKE", Token.Like);
        keywords.put("NOT", Token.Not);
        keywords.put("AND", Token.And);
        keywords.put("OR", Token.Or);
        keywords.put("FOR", Token.ForAll);
        keywords.put("ALL", Token.All);
        keywords.put("IN", Token.In);
        keywords.put("EXISTS", Token.Exists);
        keywords.put("SOME", Token.Some);
        keywords.put("ANY", Token.Any);
        keywords.put("INSTANCEOF", Token.Instanceof);
    }

    public int getPos() {
        return pos;
    }

    public String getText() {
        return src;
    }

    protected char nextChar() throws OQLParseException {
        if (pos >= src.length())
            throw new OQLParseException(Res.str("UnexpectedEof"), src, pos);
        else
            return src.charAt(pos++);
    }

    public Token nextToken() throws OQLParseException {
        if (isPushBack)
            isPushBack = false;
        else
            currToken = nextToken1();
        return currToken;
    }

    protected Token nextToken1() throws OQLParseException {
        while (pos < src.length()) {
            char c = nextChar();
            switch (c) {
                case '?': {
                    return Token.Question;
                }
                case '(': {
                    return Token.LBrace;
                }
                case ')': {
                    return Token.RBrace;
                }
                case '[': {
                    return Token.LBracket;
                }
                case ']': {
                    return Token.RBracket;
                }
                case '*': {
                    return Token.Star;
                }
                case '/': {
                    char cc = nextChar();
                    if (cc == '*') {
                        boolean foundStar = false;
                        for (; ; ) {
                            cc = nextChar();
                            if (cc == '*') {
                                foundStar = true;
                            } else if (cc == '/') {
                                if (foundStar)
                                    return nextToken1();
                                else
                                    foundStar = false;
                            }
                        }
                    } else if (cc == '/') {
                        while (pos < src.length()) {
                            cc = nextChar();
                            if (cc == '\n' || cc == '\r')
                                return nextToken1();
                        }
                        return Token.EOFComment;
                    } else {
                        pos--;
                        return Token.Slash;
                    }
                }
                case '+': {
                    return Token.Plus;
                }
                case ':': {
                    return Token.Colon;
                }
                case ',': {
                    return Token.Comma;
                }
                case '=': {
                    return Token.Equal;
                }
                case '.': {
                    char cc = nextChar();
                    if (cc == '.')
                        return Token.Elipses;
                    else {
                        pos--;
                        return Token.Dot;
                    }
                }
                case '-': {
                    char cc = nextChar();
                    if (cc == '>')
                        return Token.Dot;
                    else {
                        pos--;
                        return Token.Minus;
                    }
                }
                case '|': {
                    char cc = nextChar();
                    if (cc == '|')
                        return Token.Plus;
                    else {
                        pos--;
                        throw new OQLParseException("not a token: |", src, pos);
                    }
                }
                case '<': {
                    char cc = nextChar();
                    if (cc == '=')
                        return Token.LessOrEqual;
                    else {
                        pos--;
                        return Token.Less;
                    }
                }
                case '>': {
                    char cc = nextChar();
                    if (cc == '=')
                        return Token.GreaterOrEqual;
                    else {
                        pos--;
                        return Token.Greater;
                    }
                }
                case '!': {
                    char cc = nextChar();
                    if (cc == '=')
                        return Token.Unequal;
                    else {
                        pos--;
                        throw new OQLParseException("not a token: !. Maybe you meant not", src, pos);
                    }
                }
                case '"': {
                    return scanString();
                }
                default: {
                    if (Character.isDigit(c)) {
                        return scanNumber(c);
                    } else if (Character.isJavaIdentifierStart(c)) {
                        return scanName(c);
                    }
                }
            }
        }
        return Token.EOF;
    }

    /**
     * ----------------------------------------------------------------------- pushBack
     */
    public void pushBack() {
        isPushBack = true;
    }

    protected Token scanName(char p0) throws OQLParseException {
        StringBuffer b = new StringBuffer();
        b.append(p0);
        while (pos < src.length()) {
            char c = nextChar();
            if (Character.isJavaIdentifierPart(c)) {
                b.append(c);
            } else {
                pos--;
                break;
            }
        }
        String tName = b.toString();
        Token ktok = keywords.get(tName);
        if (ktok != null) {
            return ktok;
        } else {
            nameVal = tName;
            return Token.Name;
        }
    }

    protected Token scanNumber(char p0) throws OQLParseException {
        boolean isFloat = false;
        boolean isDot = false;
        StringBuffer b = new StringBuffer();
        b.append(p0);
        while (pos < src.length()) {
            char c = nextChar();
            if (!Character.isDigit(c) && c != 'e' && c != 'E' && c != '.') {
                pos--;
                break;
            }
            if (c == '.') {
                if (isDot) {
                    isFloat = false;
                    pos -= 2;
                    b.setLength(b.length() - 1);
                    break;
                }
                isFloat = true;
                isDot = true;
            } else if (c == 'e' || c == 'E')
                isFloat = true;
            b.append(c);
        }
        if (isFloat) {
            floatVal = Double.parseDouble(b.toString());
            return Token.Float;
        } else {
            intVal = Long.parseLong(b.toString());
            return Token.Int;
        }
    }

    protected Token scanString() throws OQLParseException {
        StringBuffer b = new StringBuffer();
        while (pos < src.length()) {
            char c = nextChar();
            if (c == '"') {
                stringVal = b.toString();
                return Token.String;
            }
            if (c == '\\') {
                c = nextChar();
                if (c == 'n')
                    c = '\n';
                else if (c == 't')
                    c = '\t';
            }
            b.append(c);
        }
        throw new OQLParseException("Unexpected EOF in String", src, pos);
    }

    static SortedNamedVector<Operator> opList;

    public static class Operator implements Named {
        String myName;
        String myLongName;

        Operator(String name, String description) {
            myName = name;
            myLongName = name + ": " + description;
        }

        public String getName() {
            return myName;
        }

        public String toString() {
            return myLongName;
        }
    }

    public static SortedNamedVector<Operator> getOperatorList() {
        if (opList == null) {
            opList = new SortedNamedVector<Operator>();
            opList.add(new Operator("null", "not any object"));
            opList.add(new Operator("true", "boolean true"));
            opList.add(new Operator("false", "boolean false"));
            opList.add(new Operator("mod", "modulus operator"));
            opList.add(new Operator("like", "wildcard comparison operator"));
            opList.add(new Operator("not", "negation operator"));
            opList.add(new Operator("and", "logical and operator"));
            opList.add(new Operator("or", "logical or operator"));
            opList.add(new Operator("for all x in _ : _", "subquery which must be true for all elements"));
            opList.add(new Operator("exists x in _ : _", "subquery which must be true for some elements"));
            opList.add(new Operator("instance", "test is obejct is instance of class"));
            opList.add(new Operator("_ ? _ : _", "if operator"));
            opList.add(new Operator("[_]", "index operator"));
            opList.add(new Operator("*", "multiply operator"));
            opList.add(new Operator("/", "divide operator"));
            opList.add(new Operator("+", "addition or string concationation operator"));
            opList.add(new Operator("=", "equal operator"));
            opList.add(new Operator("!=", "not equal operator"));
            opList.add(new Operator("-", "subtraction operator"));
            opList.add(new Operator("<", "less operator"));
            opList.add(new Operator("<=", "less or equal operator"));
            opList.add(new Operator(">", "greater operator"));
            opList.add(new Operator(">=", "greater or equal operator"));
        }
        return opList;
    }
}
