package hr.fer.zemris.java.shell;

/**
 * An enumeration that names the procedure that should be followed after a
 * certain command is executed. Although most commands return {@link #CONTINUE}
 * on successful executing, the {@link #TERMINATE} command status should be
 * considered upon encountering a critical error in executing.
 * 
 * @author Mario Bobic
 */
public enum ShellStatus {

    /**
     * Continue running the Shell and accepting new commands.
     */
    CONTINUE,

    /**
     * Terminate the Shell upon executing the last command.
     */
    TERMINATE
}
