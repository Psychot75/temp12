/**
 * Launcher — plain entry point that does NOT extend Application.
 *
 * WHY THIS CLASS EXISTS:
 * When a fat JAR's manifest Main-Class is a class that extends
 * javafx.application.Application, the JVM launcher inspects the class
 * *before* calling main() and throws:
 *   "Error: JavaFX runtime components are missing"
 * even when the JavaFX jars are fully on the classpath.
 *
 * By making this plain class the manifest entry point, the check is
 * bypassed. It simply delegates to Main.main(), which then calls
 * Application.launch() normally.
 *
 * For 'mvn javafx:run', the javafx-maven-plugin targets Main directly
 * (see <mainClass> in pom.xml), so this class is not involved there.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}