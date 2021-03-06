package hr.fer.zemris.java.shell.commands.writing;

import hr.fer.zemris.java.shell.ShellStatus;
import hr.fer.zemris.java.shell.commands.VisitorCommand;
import hr.fer.zemris.java.shell.interfaces.Environment;
import hr.fer.zemris.java.shell.utility.Progress;
import hr.fer.zemris.java.shell.utility.StringUtility;
import hr.fer.zemris.java.shell.utility.Utility;
import hr.fer.zemris.java.shell.utility.exceptions.SyntaxException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static hr.fer.zemris.java.shell.utility.CommandUtility.promptConfirm;

/**
 * Many sources of information contain redundant data or data that adds little
 * to the stored information. This results in tremendous amounts of data being
 * transferred between client and server applications or computers in general.
 * <p>
 * This command is used for compressing files and directories to a ZIP file
 * format.
 *
 * @author Mario Bobic
 */
public class ZipCommand extends VisitorCommand {

    /**
     * Constructs a new command object of type {@code ZipCommand}.
     */
    public ZipCommand() {
        super("ZIP", createCommandDescription());
    }

    @Override
    public String getCommandSyntax() {
        return "<source_path> (<target_path>)";
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
        desc.add("Compresses files and directories to a ZIP file format.");
        desc.add("If the target path is not specified, the zip file will "
                + "be named same as source file name with .zip extension.");
        desc.add("If the target path is a directory, the zip file will be "
                + "stored inside of that directory.");
        return desc;
    }

    @Override
    protected ShellStatus execute0(Environment env, String s) throws IOException {
        if (s == null) {
            throw new SyntaxException();
        }

        String[] args = StringUtility.extractArguments(s);
        if (args.length > 2) {
            throw new SyntaxException();
        }

        Path source = Utility.resolveAbsolutePath(env, args[0]);
        Path target;
        try {
            target = Utility.resolveAbsolutePath(env, args[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            target = source.resolveSibling(Utility.getFileName(source) + Utility.ZIP_FILE_EXT);
        }

        Utility.requireExists(source);

        if (source.equals(target)) {
            env.writeln("Zip file name must be different from original file name.");
            return ShellStatus.CONTINUE;
        }

        if (Files.isDirectory(target)) {
            target = target.resolve(source.getFileName() + Utility.ZIP_FILE_EXT);
        }

        if (Files.exists(target)) {
            if (!promptConfirm(env, "File " + target + " already exists. Overwrite?")) {
                env.writeln("Cancelled.");
                return ShellStatus.CONTINUE;
            }
        }

        /* Passed all checks, start working. */
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(target))) {
            ZipFileVisitor zipVisitor = new ZipFileVisitor(env, source, zos);
            walkFileTree(source, zipVisitor);
        }

        return ShellStatus.CONTINUE;
    }

    /**
     * Writes all bytes of the specified <tt>file</tt> to the given output
     * stream. The bytes are read little by little to avoid large memory
     * consumption and are read by a buffered input stream to reduce the number
     * of I/O accesses.
     *
     * @param env an environment
     * @param stream stream where the file will be written
     * @param file file to be written to the output stream
     * @throws IOException if an I/O error occurs
     */
    private static void write(Environment env, OutputStream stream, Path file) throws IOException {
        Progress progress = new Progress(env, Files.size(file), true);
        try (
            BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file))
        ) {
            int len;
            byte[] buff = new byte[1024];
            while ((len = in.read(buff)) > 0) {
                stream.write(buff, 0, len);
                progress.add(len);
            }
        } finally {
            progress.stop();
        }
    }

    /**
     * A {@linkplain SimpleFileVisitor} extended and used to serve the
     * {@linkplain ZipCommand}.
     *
     * @author Mario Bobic
     */
    private class ZipFileVisitor extends SimpleFileVisitor<Path> {

        /** An environment. */
        private final Environment environment;
        /** Starting path. */
        private final Path start;
        /** Zip output stream. */
        private final ZipOutputStream zos;

        /**
         * Constructs an instance of {@code ZipFileVisitor} with the specified
         * arguments.
         *
         * @param environment an environment
         * @param start starting file of the tree walker
         * @param zos zip output stream
         */
        public ZipFileVisitor(Environment environment, Path start, ZipOutputStream zos) {
            this.environment = environment;
            this.start = Utility.getParent(start);
            this.zos = zos;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path relative = start.relativize(file);
            ZipEntry zipEntry = new ZipEntry(relative.toString());

            zos.putNextEntry(zipEntry);
            write(environment, zos, file);
            zos.closeEntry();

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            environment.writeln("Failed to access " + file);
            return FileVisitResult.CONTINUE;
        }
    }

}
