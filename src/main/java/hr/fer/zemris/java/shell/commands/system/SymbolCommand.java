package hr.fer.zemris.java.shell.commands.system;

import hr.fer.zemris.java.shell.ShellStatus;
import hr.fer.zemris.java.shell.commands.AbstractCommand;
import hr.fer.zemris.java.shell.interfaces.Environment;
import hr.fer.zemris.java.shell.utility.StringUtility;
import hr.fer.zemris.java.shell.utility.exceptions.SyntaxException;

import java.util.ArrayList;
import java.util.List;

import static hr.fer.zemris.java.shell.utility.CommandUtility.formatln;

/**
 * This command is used for changing or simply writing out the current symbols
 * of this Shell. These symbols are the prompt symbol, morelines symbol and the
 * multiline symbol.
 *
 * @author Mario Bobic
 */
public class SymbolCommand extends AbstractCommand {

    /** The prompt symbol type used by MyShell. */
    private static final String PROMPT = "PROMPT";
    /** The morelines symbol type used by MyShell. */
    private static final String MORELINES = "MORELINES";
    /** The multiline symbol type used by MyShell. */
    private static final String MULTILINE = "MULTILINE";

    /**
     * Constructs a new command object of type {@code SymbolCommand}.
     */
    public SymbolCommand() {
        super("SYMBOL", createCommandDescription());
    }

    @Override
    public String getCommandSyntax() {
        return "<type> (<newsymbol>)";
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
        desc.add("Changes or simply writes out the current symbol provided in the argument.");
        desc.add("To print out a prompt symbol simply input \"symbol PROMPT\"");
        desc.add("To change a prompt symbol input \"symbol PROMPT <newsymbol>\", "
                + "where the <newsymbol> is a desired new symbol.");
        desc.add("To print out a morelines symbol simply input \"symbol MORELINES\"");
        desc.add("To change a morelines symbol input \"symbol MORELINES <newsymbol>\", "
                + "where the <newsymbol> is a desired new symbol.");
        desc.add("To print out a multiline symbol simply input \"symbol MULTILINE\"");
        desc.add("To change a multiline symbol input \"symbol MULTILINE <newsymbol>\", "
                + "where the <newsymbol> is a desired new symbol.");
        return desc;
    }

    @Override
    protected ShellStatus execute0(Environment env, String s) {
        if (s == null) {
            throw new SyntaxException();
        }

        String[] args = StringUtility.extractArguments(s);
        if (args.length == 1) {
            printSymbol(env, args[0]);
        } else if (args.length == 2) {
            setSymbol(env, args[0], args[1]);
        } else {
            throw new SyntaxException();
        }

        return ShellStatus.CONTINUE;
    }

    /**
     * Prints out the symbol for the specified symbol <tt>type</tt>. A symbol
     * type may be a prompt symbol, a morelines symbol or a multiline symbol. If
     * the specified symbol <tt>type</tt> is not supported, an error message is
     * printed out onto the specified environment <tt>env</tt>.
     *
     * @param env an environment
     * @param type the symbol type for which the symbol is to be printed out
     */
    private static void printSymbol(Environment env, String type) {
        switch (type) {
        case PROMPT:
            env.writeln("Symbol for " + PROMPT + " is '" + env.getPromptSymbol() + "'");
            break;
        case MORELINES:
            env.writeln("Symbol for " + MORELINES + " is '" + env.getMorelinesSymbol() + "'");
            break;
        case MULTILINE:
            env.writeln("Symbol for " + MULTILINE + " is '" + env.getMultilineSymbol() + "'");
            break;
        default:
            env.writeln(type + " symbol type not supported.");
        }
    }

    /**
     * Sets the symbol for the specified symbol <tt>type</tt> and prints out the
     * changes. A symbol type may be a prompt symbol, a morelines symbol or a
     * multiline symbol. If the specified <tt>newSymbol</tt> is not exactly 1
     * characters long, an error message is printed out onto the specified
     * environment <tt>env</tt>. If the specified symbol <tt>type</tt> is not
     * supported, an error message is printed out onto the specified environment
     * <tt>env</tt>.
     *
     * @param env an environment
     * @param type the symbol type for which the new symbol is specified
     * @param newSymbol a new symbol which will replace the old one
     */
    private static void setSymbol(Environment env, String type, String newSymbol) {
        if (newSymbol.length() != 1) {
            env.writeln("The new symbol must be a character of length 1.");
            return;
        }
        Character symbol = newSymbol.charAt(0);

        switch (type) {
        case PROMPT:
            formatln(env, "Symbol for %s changed from '%c' to '%c'",
                    PROMPT, env.getPromptSymbol(), symbol);
            env.setPromptSymbol(symbol);
            break;
        case MORELINES:
            formatln(env, "Symbol for %s changed from '%c' to '%c'",
                    PROMPT, env.getMorelinesSymbol(), symbol);
            env.setMorelinesSymbol(symbol);
            break;
        case MULTILINE:
            formatln(env, "Symbol for %s changed from '%c' to '%c'",
                    PROMPT, env.getMultilineSymbol(), symbol);
            env.setMultilineSymbol(symbol);
            break;
        default:
            env.writeln(type + " symbol type not supported.");
        }
    }

}
