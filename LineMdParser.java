package md2html;

import java.util.*;

public class LineMdParser {
    final char END = '\0';

    String line;

    int pointer = -1;
    char curChar;
    char prevChar;
    char nextChar;

    Map<String, Integer> tokensCount;
    static Map<String, String> tokensMark;

    private void setMap() {
        tokensCount = new HashMap<>();
        tokensCount.put("**", 0);
        tokensCount.put("--", 0);
        tokensCount.put("*", 0);
        tokensCount.put("__", 0);
        tokensCount.put("_", 0);
        tokensCount.put("`", 0);
        tokensCount.put("~", 0);
        tokensCount.put("++", 0);
    }

    static {
        tokensMark = new HashMap<>();
        tokensMark.put("**", "strong");
        tokensMark.put("--", "s");
        tokensMark.put("*", "em");
        tokensMark.put("__", "strong");
        tokensMark.put("_", "em");
        tokensMark.put("`", "code");
        tokensMark.put("~", "mark");
        tokensMark.put("++", "u");
    }

    public LineMdParser(String originalLine) {
        System.out.println(originalLine);
        this.line = originalLine + '\0';
        curChar = END;
        prevChar = END;
        nextChar = line.charAt(0);
        setMap();
    }

    private char getNextChar() {
        if (pointer >= line.length()) return END;
        else return line.charAt(pointer + 1);
    }

    private void setNextChar() {
        prevChar = curChar;
        curChar = nextChar;
        if (nextChar != END) {
            pointer++;
            nextChar = getNextChar();
        }
    }

    public String parseLine() {
        StringBuilder result = new StringBuilder();
        parseUntil(result);
        return result.toString();
    }

    private String parseUntil(StringBuilder builder) {
        setNextChar();
        while (curChar != END) {
            String token;
            StringBuilder resultString = new StringBuilder();
            switch (curChar) {
                case '\\':
                    if (nextChar == '*' || nextChar == '_') {
                    } else {
                        builder.append(curChar);
                    }
                    break;
                case '&':
                    builder.append("&amp;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                case '<':
                    builder.append("&lt;");
                    break;
                case '~':
                    token = "~";
                    if (analiseAfterToken(token, builder, false, resultString)) {
                        return resultString.toString();
                    }
                    break;
                case '-':
                    if (nextChar == '-') {
                        setNextChar();
                        token = "--";
                        if (analiseAfterToken(token, builder, false, resultString)) {
                            return resultString.toString();
                        }
                    } else {
                        builder.append("-");
                    }
                    break;
                case '+':
                    if (nextChar == '+') {
                        setNextChar();
                        token = "++";
                        if (analiseAfterToken(token, builder, false, resultString)) {
                            return resultString.toString();
                        }
                    } else {
                        builder.append("+");
                    }
                    break;
                case '*':
                    if (nextChar == '*') {
                        setNextChar();
                        token = "**";
                        if (analiseAfterToken(token, builder, false, resultString)) {
                            return resultString.toString();
                        }
                    } else {
                        token = "*";
                        if (analiseAfterToken(token, builder, true, resultString)) {
                            return resultString.toString();
                        }
                    }
                    break;
                case '_':
                    if (nextChar == '_') {
                        setNextChar();
                        token = "__";
                        if (analiseAfterToken(token, builder, false, resultString)) {
                            return resultString.toString();
                        }
                    } else {
                        token = "_";
                        if (analiseAfterToken(token, builder, true, resultString)) {
                            return resultString.toString();
                        }
                    }
                    break;
                case '`':
                    token = "`";
                    if (analiseAfterToken(token, builder, false, resultString)) {
                        return resultString.toString();
                    }
                    break;
                case '[':
                    StringBuilder builderForLink = new StringBuilder();
                    if (createImageOfLink(builderForLink,false)){
                        builder.append(builderForLink);
                    } else {
                        builder.append("["+ builderForLink);
                    }
                    break;
                case '!':
                    StringBuilder builderForImage = new StringBuilder();
                    if (createImageOfLink(builderForImage,true)){
                        builder.append(builderForImage);
                    } else {
                        builder.append("!"+ builderForImage);
                    }
                    break;
                default:
                    builder.append(curChar);
                    break;
            }
            setNextChar();
        }
        return "";
    }
    //return true if appeared close bracket

    private boolean analiseAfterToken(String token, StringBuilder builder, boolean needToCheckSpace, StringBuilder result) {
        if (tokensCount.get(token) > 0) {
            result.append(token);
            return true;
        } else {
            if (needToCheckSpace && nextChar == ' ') {
                builder.append(curChar);
            } else {
                tokensCount.replace(token, 1);
                StringBuilder stringBetweenTokens = new StringBuilder();
                String closeToken = parseUntil(stringBetweenTokens);
                if (closeToken.equals(token)) {
                    tokensCount.replace(token, 0);
                    builder.append(String.format("<%s>%s</%s>", tokensMark.get(token), stringBetweenTokens, tokensMark.get(token)));
                } else {
                    tokensCount.replace(token, 0);
                    builder.append(token + stringBetweenTokens);
                    result.append(closeToken);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean createImageOfLink(StringBuilder result, boolean imageMode){
        setNextChar();
        if (imageMode){
            if (curChar!='['){
                result.append("[");
                return false;
            } else {
                setNextChar();
            }
        }
        StringBuilder name = new StringBuilder();
        while (curChar != ']' && curChar != END) {
            name.append(curChar);
            setNextChar();
        }
        if (curChar==']') {
            setNextChar();
            if (curChar == '(') {
                StringBuilder link = new StringBuilder();
                setNextChar();
                while (curChar != ')' && curChar != END) {
                    link.append(curChar);
                    setNextChar();
                }
                if (curChar == ')') {
                    String parsedName;
                    if (!imageMode) {
                        LineMdParser parserForNameOfLink = new LineMdParser(name.toString());
                         parsedName = parserForNameOfLink.parseLine();
                    } else parsedName = name.toString();
                    if (imageMode) result.append("<img alt='"+parsedName+"' src='"+ link+"'>");
                    else result.append("<a href='"+link+ "'>"+parsedName+"</a>");
                    return true;
                } else {
                    if (imageMode) result.append("[");
                    result.append(name+"]"+link);
                    return false;
                }
            } else {
                if (imageMode) result.append("[");
                result.append(name+"]");
                return false;
            }
        } else {
            if (imageMode) result.append("[");
            result.append(name);
            return false;
        }
    }
}
