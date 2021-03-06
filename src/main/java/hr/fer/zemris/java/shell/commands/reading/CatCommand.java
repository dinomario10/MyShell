package hr.fer.zemris.java.shell.commands.reading;

import hr.fer.zemris.java.shell.ShellStatus;
import hr.fer.zemris.java.shell.commands.AbstractCommand;
import hr.fer.zemris.java.shell.interfaces.Environment;
import hr.fer.zemris.java.shell.utility.FlagDescription;
import hr.fer.zemris.java.shell.utility.Utility;
import hr.fer.zemris.java.shell.utility.exceptions.InvalidFlagException;
import hr.fer.zemris.java.shell.utility.exceptions.SyntaxException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A command that is used for writing out the contents of a file. This command
 * requires an argument. If the given argument is a directory, an error message
 * is written. This command can write out contents of all kinds of files, but
 * the content is not guaranteed to make any sense for non-text files. A charset
 * may also be provided to this command.
 *
 * @author Mario Bobic
 */
public class CatCommand extends AbstractCommand {

    /* Flags */
    /** Charset for decoding files. */
    private Charset charset;

    /**
     * Constructs a new command object of type {@code CatCommand}.
     */
    public CatCommand() {
        super("CAT", createCommandDescription(), createFlagDescriptions());
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
        desc.add("Displays the contents of a file.");
        desc.add("Argument must be a path to a file, ");
        desc.add("If a charset is not provided, default platform charset is used.");
        return desc;
    }

    /**
     * Creates a list of {@code FlagDescription} objects where each entry
     * describes the available flags of this command. This method is generates
     * description exclusively for the command that this class represents.
     *
     * @return a list of strings that represents description
     */
    private static List<FlagDescription> createFlagDescriptions() {
        List<FlagDescription> desc = new ArrayList<>();
        desc.add(new FlagDescription("c", "charset", "charset", "Specify the charset to be used."));
        return desc;
    }

    @Override
    protected String compileFlags(Environment env, String s) {
        /* Initialize default values. */
        charset = Charset.defaultCharset();

        /* Compile! */
        s = commandArguments.compile(s);

        /* Replace default values with flag values, if any. */
        if (commandArguments.containsFlag("c", "charset")) {
            String arg = commandArguments.getFlag("c", "charset").getArgument();
            charset = Utility.resolveCharset(arg);
            if (charset == null) {
                throw new InvalidFlagException("Invalid charset: " + arg);
            }
        }

        return super.compileFlags(env, s);
    }

    @Override
    protected ShellStatus execute0(Environment env, String s) {
        if (s == null) {
            throw new SyntaxException();
        }

        Path file = Utility.resolveAbsolutePath(env, s);
        Utility.requireFile(file);

        /* Passed all checks, start working. */
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                    new BufferedInputStream(
                        Files.newInputStream(file)), charset))
        ) {

            int len;
            char[] cbuf = new char[1024];
            while ((len = br.read(cbuf)) > 0) {
                env.write(cbuf, 0, len);
            }

        } catch (IOException e) {
            /* This could happen if the file is protected. */
            env.writeln("Access is denied: " + e.getMessage());
        }

        return ShellStatus.CONTINUE;
    }

}
