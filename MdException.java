package md2html;

public class MdException extends Throwable{
    private final int pos;
    private final int line;

    public MdException(final int line, final int pos, final String message) {
        super(message);
        this.line = line;
        this.pos = pos;
    }

}
