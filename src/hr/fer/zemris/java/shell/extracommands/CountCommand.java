package hr.fer.zemris.java.shell.extracommands;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.java.shell.CommandStatus;
import hr.fer.zemris.java.shell.Helper;
import hr.fer.zemris.java.shell.commands.AbstractCommand;
import hr.fer.zemris.java.shell.interfaces.Environment;

/**
 * A command that is used for counting the amount of files and directories in a
 * directory tree. This counting begins in the current working directory or the
 * directory specified and is going through all of the subdirectories.
 *
 * @author Mario Bobic
 */
public class CountCommand extends AbstractCommand {

	/** Defines the proper syntax for using this command. */
	private static final String SYNTAX = "count (<path>)";
	
	/**
	 * Constructs a new command object of type {@code CountCommand}.
	 */
	public CountCommand() {
		super("COUNT", createCommandDescription());
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
		desc.add("Counts the number of files and folders.");
		desc.add("This counting begins in the current working directory or the "
				+ "directory specified and is going through all of the subdirectories.");
		desc.add("Syntax: " + SYNTAX);
		return desc;
	}

	@Override
	protected CommandStatus execute0(Environment env, String s) throws IOException {
		Path path = s == null ? env.getCurrentPath() : Helper.resolveAbsolutePath(env, s);
		if (!Files.isDirectory(path)) {
			writeln(env, "The system cannot find the directory specified.");
			return CommandStatus.CONTINUE;
		}
		
		CountFileVisitor countVisitor = new CountFileVisitor();
		
		Files.walkFileTree(path, countVisitor);
		int files = countVisitor.getFileCount();
		int folders = countVisitor.getFolderCount();
		int fails = countVisitor.getFails();
		writeln(env, "Files: " + files);
		writeln(env, "Folders: " + folders);
		if (fails != 0) {
			writeln(env, "Failed to access " + fails + " folders.");
		}

		return CommandStatus.CONTINUE;
	}
	
	/**
	 * A {@linkplain SimpleFileVisitor} extended and used to serve the
	 * {@linkplain CountCommand}.
	 *
	 * @author Mario Bobic
	 */
	private static class CountFileVisitor extends SimpleFileVisitor<Path> {

		/** Number of files this visitor has encountered. */
		private int fileCount = 0;
		/** Number of folders this visitor has encountered. */
		private int folderCount = 0;

		/** Number of folders that failed to be accessed. */
		private int fails = 0;
		
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			folderCount++;
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			fileCount++;
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			if (Files.isDirectory(file)) {
				fails++;
			} else {
				fileCount++;
			}
			return FileVisitResult.CONTINUE;
		}

		/**
		 * Returns the number of files this visitor has encountered.
		 * 
		 * @return the number of files this visitor has encountered
		 */
		public int getFileCount() {
			return fileCount;
		}
		
		/**
		 * Returns the number of folders this visitor has encountered, not
		 * counting the folder it started from.
		 * 
		 * @return the number of files this visitor has encountered
		 */
		public int getFolderCount() {
			return folderCount - 1;
		}
		
		/**
		 * Returns the number of folders that failed to be accessed.
		 * 
		 * @return the number of folders that failed to be accessed
		 */
		public int getFails() {
			return fails;
		}
		
	}

}