package hr.fer.zemris.java.shell.commands.system;

import hr.fer.zemris.java.shell.ShellStatus;
import hr.fer.zemris.java.shell.commands.AbstractCommand;
import hr.fer.zemris.java.shell.interfaces.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * A command that is used for printing the current working directory.
 *
 * @author Mario Bobic
 */
public class PwdCommand extends AbstractCommand {

    /**
     * Constructs a new command object of type {@code PwdCommand}.
     */
    public PwdCommand() {
        super("PWD", createCommandDescription());
    }

    @Override
    public String getCommandSyntax() {
        return "";
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
        desc.add("Prints out the working directory (absolute directory path).");
        return desc;
    }

    @Override
    protected ShellStatus execute0(Environment env, String s) {
        env.writeln(env.getCurrentPath());
        return ShellStatus.CONTINUE;
    }

}
