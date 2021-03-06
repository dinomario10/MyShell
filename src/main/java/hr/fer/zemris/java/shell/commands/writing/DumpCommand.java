package hr.fer.zemris.java.shell.commands.writing;

import hr.fer.zemris.java.shell.ShellStatus;
import hr.fer.zemris.java.shell.commands.AbstractCommand;
import hr.fer.zemris.java.shell.interfaces.Environment;
import hr.fer.zemris.java.shell.utility.Progress;
import hr.fer.zemris.java.shell.utility.StringUtility;
import hr.fer.zemris.java.shell.utility.Utility;
import hr.fer.zemris.java.shell.utility.exceptions.SyntaxException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static hr.fer.zemris.java.shell.utility.CommandUtility.promptConfirm;

/**
 * Dumps empty bytes (zeroes) to the given file name. Number of dumped bytes is
 * user-defined. The user input can be given in any unit, with either standard
 * or digital prefixes.
 * <p>
 * <strong>Standard</strong> prefix means the unit 1000 is raised to the power
 * of the prefix, while <strong>digital</strong> prefix means the unit 1024 is
 * raised to the specified power. If the input is given without a unit, it is
 * considered as bytes.
 *
 * @author Mario Bobic
 */
public class DumpCommand extends AbstractCommand {

    /** Standard size for the loading byte buffer array. */
    public static final int STD_LOADER_SIZE = 16*1024;

    /**
     * Constructs a new command object of type {@code DumpCommand}.
     */
    public DumpCommand() {
        super("DUMP", createCommandDescription());
    }

    @Override
    public String getCommandSyntax() {
        return "<size> <filename>";
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
        desc.add("Creates a new dump file with the given size and file name.");
        desc.add("Size can be given in any unit, with either standard (kB) or digital (kiB) prefixes.");
        return desc;
    }

    @Override
    protected ShellStatus execute0(Environment env, String s) throws IOException {
        String[] args = StringUtility.extractArguments(s);

        /* Consider size having a space. */
        String sizeUnit;
        String pathname;
        if (args.length == 2) {
            sizeUnit = args[0];
            pathname = args[1];
        } else if (args.length == 3) {
            sizeUnit = args[0] + args[1];
            pathname = args[2];
        } else {
            throw new SyntaxException();
        }

        long size;
        try {
            size = Utility.parseSize(sizeUnit);
        } catch (IllegalArgumentException e) {
            throw new SyntaxException();
        }

        Path path = Utility.resolveAbsolutePath(env, pathname);
        Utility.requireDiskSpace(size, path);
        if (Files.isDirectory(path)) {
            env.writeln("A directory named " + path.getFileName() + " already exists.");
            return ShellStatus.CONTINUE;
        }
        if (Files.exists(path)) {
            if (!promptConfirm(env, "File " + path + " already exists. Overwrite?")) {
                env.writeln("Cancelled.");
                return ShellStatus.CONTINUE;
            }
        }

        dumpBytes(env, path, size);
        env.writeln("Dumped " + Utility.humanReadableByteCount(size) + " in file " + path.getFileName());

        return ShellStatus.CONTINUE;
    }

    /**
     * Dumps binary zeros into the given <tt>file</tt> with the given
     * <tt>size</tt>.
     *
     * @param env a environment
     * @param file file to be created
     * @param size number of bytes to be generated
     * @throws IOException if an I/O error occurs
     */
    private static void dumpBytes(Environment env, Path file, long size) throws IOException {
        Progress progress = new Progress(env, size, true);
        try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(file))) {
            int len = STD_LOADER_SIZE;
            byte[] bytes = new byte[STD_LOADER_SIZE];

            long writtenBytes = 0;
            while (writtenBytes < size) {
                if (size - writtenBytes < len) {
                    len = (int) (size - writtenBytes);
                }
                out.write(bytes, 0, len);
                writtenBytes += len;
                progress.add(len);
            }
        } finally {
            progress.stop();
        }
    }

}
