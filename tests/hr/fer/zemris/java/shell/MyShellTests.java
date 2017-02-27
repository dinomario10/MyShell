package hr.fer.zemris.java.shell;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import hr.fer.zemris.java.shell.MyShell.EnvironmentImpl;

/**
 * Tests the functionality of {@link MyShell} class.
 *
 * @author Mario Bobic
 */
@SuppressWarnings("javadoc")
public class MyShellTests {
	
	/** Environment used by some tests. */
	private EnvironmentImpl environment = new EnvironmentImpl();

	@Test
	public void test() {
		assertNotNull(environment);
	}

	@Test(expected=IllegalArgumentException.class)
	public void test2() {
		// must throw!
		throw new IllegalArgumentException();
	}

}
