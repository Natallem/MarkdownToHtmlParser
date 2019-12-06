package md2html;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MdSourceFile {

    private BufferedReader reader;
    public static char END = '\0';
    protected char curChar = '\n';
    protected char prevChar;
    protected int positionInLine = 0;
    protected int numberOfLine = 1;

    public MdSourceFile(String fileName) throws MdException {
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8));
        } catch (final IOException e) {
            throw error("Error opening input file '%s': %s", fileName, e.getMessage());
        }
    }

    private char read() throws MdException {
        try {
            final int read = reader.read();
            return read == -1 ? END : (char) read;
        } catch (final IOException e) {
            throw error("Source read error", e.getMessage());
        }
    }

    public char getCurrentChar() {
        return curChar;
    }

    public char readNextChar() throws MdException {
        if (curChar == '\n') {
            numberOfLine++;
            positionInLine = 0;
        }
        prevChar = curChar;
        curChar = read();
        positionInLine++;
        return curChar;
    }

    public MdException error(final String format, final Object... args) throws MdException {
        return new MdException(numberOfLine, positionInLine, String.format("%d:%d: %s", numberOfLine, positionInLine, String.format(format, args)));
    }

    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            System.out.println("cannot close file input " + e.getMessage());
        }
    }
}
