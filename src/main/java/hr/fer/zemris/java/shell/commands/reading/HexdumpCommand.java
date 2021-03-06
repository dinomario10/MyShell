package hr.fer.zemris.java.shell.commands.reading;

import hr.fer.zemris.java.shell.ShellStatus;
import hr.fer.zemris.java.shell.commands.AbstractCommand;
import hr.fer.zemris.java.shell.interfaces.Environment;
import hr.fer.zemris.java.shell.utility.Utility;
import hr.fer.zemris.java.shell.utility.exceptions.SyntaxException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * This command is used for producing a hex-output of the specified file. The
 * dump is printed in a way that on the left side, hexadecimal byte values are
 * shown while on the right side only a standard subset of characters is shown.
 * All characters whose byte value is less than 32 or greater than 127 are
 * replaced with '.'
 *
 * @author Mario Bobic
 */
public class HexdumpCommand extends AbstractCommand {

    /**
     * Constructs a new command object of type {@code HexdumpCommand}.
     */
    public HexdumpCommand() {
        super("HEXDUMP", createCommandDescription());
    }

    @Override
    public String getCommandSyntax() {
        return "<filename>";
    }

    /**
     * Creates a list of strings where each string represents a new line of this
     * command's description. This method is generates description exclusively
     * for the command that this class represents.
     *
     * @return a list of strings that represents description
     */
    private static List<String> createCommandDescription() {
        List<String> desc = new ArrayList<>();
        desc.add("Produces a hex-output of the specified file.");
        desc.add("On the left side, hexadecimal byte values are shown, "
                + "while on the right side only a standard subset of characters is shown.");
        desc.add("All characters whose byte value is less than 32 or greater than 127 are replaced with '.'");
        return desc;
    }

    @Override
    protected ShellStatus execute0(Environment env, String s) throws IOException {
        if (s == null) {
            throw new SyntaxException();
        }

        Path file = Utility.resolveAbsolutePath(env, s);
        Utility.requireFile(file);

        /* Passed all checks, start working. */
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file))) {

            int len;
            long total = 0;
            byte[] bytes = new byte[16];
            while ((len = in.read(bytes)) != -1) {
                env.writeln(bytesToString(bytes, len, total));
                total += len;
            }

        }

        return ShellStatus.CONTINUE;
    }

    /**
     * Returns a string representation of the specified array of <tt>bytes</tt>
     * starting from 0 and considering the specified <tt>len</tt> in such a way
     * that the <tt>total</tt> is the <i>zeroth</i> segment of the string,
     * written in hexadecimal, followed by the <i>first</i> segment that is the
     * first eight bytes, then the <i>second</i> segment that is the next eight
     * bytes and finally a string recreated from those bytes, considering only
     * the values that are not less than 32 or greater than 127. These values
     * are replaced by the '.' symbol.
     *
     * @param bytes array of bytes
     * @param len length of the array true contents
     * @param total total length processed so far
     * @return a string representation of the bytes
     */
    private static String bytesToString(byte[] bytes, int len, long total) {
        StringBuilder sb = new StringBuilder();

        /* Segment 0 */
        sb.append(String.format("%08X: ", total));

        StringJoiner sj = new StringJoiner(" ");

        /* Segment 1 */
        int seg1 = Math.min(8, len);
        for (int i = 0; i < seg1; i++) {
            sj.add(String.format("%02X", (bytes[i])));
        }

        sb.append(String.format("%-23s" , sj.toString()));
        sb.append("|");
        sj = new StringJoiner(" ");

        /* Segment 2 */
        for (int i = 8; i < len; i++) {
            sj.add(String.format("%02X", (bytes[i])));
        }
        sb.append(String.format("%-23s" , sj.toString()));

        sb.append(" | ");

        /* Segment 3 */
        for (int i = 0; i < len; i++) {
            byte b = bytes[i];
            sb.append(String.format("%c", (b < 32 || b > 127) ? '.' : b));
        }

        return sb.toString();
    }

}
