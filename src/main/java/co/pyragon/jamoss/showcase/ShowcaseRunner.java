package co.pyragon.jamoss.showcase;

/**
 * Entry point for the dev-only media capture ({@code ./gradlew runShowcase}), enabled by
 * {@code -Dcreateoscillation.showcase=true}. See {@link WorldShowcaseRunner}.
 */
public final class ShowcaseRunner {

	public static final String PROPERTY = "createoscillation.showcase";

	private ShowcaseRunner() {}

	public static boolean enabled() {
		return Boolean.getBoolean(PROPERTY);
	}

	public static void install() {
		if (enabled())
			WorldShowcaseRunner.install();
	}
}
