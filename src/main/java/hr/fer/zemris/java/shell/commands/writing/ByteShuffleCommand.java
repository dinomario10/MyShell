package hr.fer.zemris.java.shell.commands.writing;

import hr.fer.zemris.java.shell.ShellStatus;
import hr.fer.zemris.java.shell.commands.AbstractCommand;
import hr.fer.zemris.java.shell.interfaces.Environment;
import hr.fer.zemris.java.shell.utility.Progress;
import hr.fer.zemris.java.shell.utility.StringUtility;
import hr.fer.zemris.java.shell.utility.Utility;
import hr.fer.zemris.java.shell.utility.exceptions.SyntaxException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A command that is used for shuffling bytes of a file. The user can specify
 * the range of byte shuffling. This command creates a new file with the same
 * name as the original file, but with an index at the end and keeps the
 * original file.
 *
 * @author Mario Bobic
 */
public class ByteShuffleCommand extends AbstractCommand {

    /**
     * Constructs a new command object of type {@code ByteShuffleCommand}.
     */
    public ByteShuffleCommand() {
        super("BYTESHUFFLE", createCommandDescription());
    }

    @Override
    public String getCommandSyntax() {
        return "<filename> (optional: <offset> <length>)";
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
        desc.add("Shuffles bytes of the specified file.");
        desc.add("Upon execution, a new file is created and the original file is kept.");
        desc.add("Optional offset and length may be included.");
        return desc;
    }

    @Override
    protected ShellStatus execute0(Environment env, String s) throws IOException {
        if (s == null) {
            throw new SyntaxException();
        }

        String[] args = StringUtility.extractArguments(s);

        Path file = Utility.resolveAbsolutePath(env, args[0]);
        Utility.requireFile(file);

        // TODO Flag this up!
        long offset;
        long length;
        try {
            offset = Utility.parseSize(args[1]);
            length = Utility.parseSize(args[2]);
        } catch (Exception e) {
            offset = 0;
            length = Files.size(file);
        }
        env.writeln("Offset: " + offset + ", length: " + length);

        long fileEndPoint = offset + length;
        if (fileEndPoint > Files.size(file)) {
            env.writeln("The given offset and length are too big for file " + file.getFileName());
            env.writeln("The given file has the length of " + Files.size(file) + " bytes.");
            return ShellStatus.CONTINUE;
        }

        Path tempFile = Files.createTempFile(file.getParent(), null, null);
        Utility.requireDiskSpace(Files.size(file), tempFile);

        Progress progress = new Progress(env, Files.size(file)+length, true);
        try (
            FileInputStream in = new FileInputStream(file.toFile());
            FileOutputStream out = new FileOutputStream(tempFile.toFile())
        ) {
            /* First copy entire file. */
            int len;
            byte[] bytes = new byte[1024];
            while ((len = in.read(bytes)) > 0) {
                out.write(bytes, 0, len);
                progress.add(len);
            }

            /* Rewind both streams. */
            in.getChannel().position(offset);
            out.getChannel().position(offset);

            /* Then read with the previously set offset. */
            len = (int) length;
            bytes = new byte[len];
            if (in.read(bytes, 0, len) == -1) {
                env.writeln("Could not read file with offset " + offset);
                Files.delete(tempFile);
                return ShellStatus.CONTINUE;
            }

            /* Shuffle the bytes and write to a new file with offset. */
            byte[] shuffledBytes = shuffle(bytes);
            out.write(shuffledBytes, 0, len);
            progress.add(shuffledBytes.length);
        }

        /* Rename the temp file. */
        Path newFile = Utility.firstAvailable(file);
        Files.move(tempFile, newFile);

        return ShellStatus.CONTINUE;
    }

    /**
     * Shuffles the given byte array randomly. Does not create new objects.
     *
     * @param bytes array of bytes to be shuffled
     * @return the specified byte array, shuffled
     */
    private static byte[] shuffle(byte[] bytes) {
        Random rnd = ThreadLocalRandom.current();

        for (int i = bytes.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);

            byte b = bytes[index];
            bytes[index] = bytes[i];
            bytes[i] = b;
        }

        return bytes;
    }

    /**
     * Shuffles the given byte array using the Java&trade; utility methods. The
     * byte array is loaded into a List, shuffled and returned as a new byte
     * array.
     *
     * @param bytes array of bytes to be shuffled
     * @return shuffled array of bytes
     * @deprecated Uses a lot of heap space. Use {@link #shuffle(byte[])}
     *             instead.
     */
    @Deprecated
    @SuppressWarnings("unused")
    private static byte[] shuffleBytes(byte[] bytes) {
        List<Byte> list = new ArrayList<>();
        for (Byte b : bytes) {
            list.add(b);
        }

        Collections.shuffle(list);
        byte[] shuffledBytes = new byte[bytes.length];

        for (int i = 0; i < shuffledBytes.length; i++) {
            shuffledBytes[i] = list.get(i);
        }
        return shuffledBytes;
    }

}
