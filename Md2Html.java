package md2html;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Md2Html {
    MdSourceFile source;
    PrintWriter writer;

    public static void main(String[] args){
        try {
            Md2Html md2Html = new Md2Html(args[0], args[1]);
            md2Html.parse();
        } catch (MdException e) {
            System.out.println(e.getMessage());
        }
    }

    public void closeAll() {
        writer.close();
        source.close();
    }

    public Md2Html(String inputFileNameOrString, String outputFileName) throws MdException {
        source = new MdSourceFile(inputFileNameOrString);
        try {
            writer = new PrintWriter(outputFileName, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new MdException(0, 0, "Cannot open output file: " + e.getMessage());
        }
    }

    void parse() throws MdException {
        try {
            source.readNextChar();
            while (source.getCurrentChar() != source.END) {
                String paragraph = getParagraph();
                int type = getTypeOfParagraph(paragraph);
                String typeString;
                if (type != 0) paragraph = cleanHeader(type, paragraph);
                if (type != 0) typeString = "h" + type;
                else typeString = "p";
                LineMdParser lineMdParser = new LineMdParser(paragraph);
                paragraph = lineMdParser.parseLine();
                writer.print(String.format("<%s>%s</%s>\n", typeString, paragraph, typeString));
            }
        } finally {
            this.closeAll();
        }
    }

    private String cleanHeader(int typeOfHeader, String paragraph) {
        return paragraph.substring(typeOfHeader + 1);
    }

    private String getParagraph() throws MdException {
        skipEmptyLines();
        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            while (source.getCurrentChar() != '\r') {
                stringBuilder.append(source.getCurrentChar());
                source.readNextChar();
            }
            source.readNextChar();//\r->\n
            source.readNextChar();//\n->?\r
            if (source.getCurrentChar() == '\r' || source.getCurrentChar() == '\0') break;
            else {
                stringBuilder.append("\r\n");
            }
        }
        skipEmptyLines();
        return stringBuilder.toString();
    }

    private int getTypeOfParagraph(String paragraph) {
        int pointer = 0;
        while ((pointer < paragraph.length()) && (paragraph.charAt(pointer) == '#') && (pointer < 8))
            pointer++;
        if (pointer == 0 || pointer > 6) return 0;
        if (paragraph.charAt(pointer) == ' ') return pointer;
        return 0;
    }

    private void skipEmptyLines() throws MdException {
        while (source.getCurrentChar() == '\r' && source.getCurrentChar() != source.END) {
            source.readNextChar();
            source.readNextChar();
        }
    }
}
