package io.github.grakkit;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Grakkit {
    // The engine which all JavaScript contexts should use
    public static final Engine engine = Engine.newBuilder()
        .option("engine.WarnInterpreterOnly", "false")
        .build();

    public static String dataDir;
    public static String platform;

    public static void initialiseCore() {
        Context context = Context.newBuilder("js")
            // Show the warning at least once, so the user is aware that the
            // performance isn ot as good as it could be
            .engine(Engine.create())
            .allowAllAccess(true)
            .allowExperimentalOptions(true)
            .option("js.nashorn-compat", "true")
            .option("js.commonjs-require", "true")
            .option("js.ecmascript-version", "2022")
            .option("js.commonjs-require-cwd", dataDir)
            .build();

        String core;
        try {
            Path file = new File(Grakkit.class.getResource("index.js").toURI()).toPath();
            core = Files.readString(file);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException("Unable to load the JS core!", e);
        }

        context.eval("js", core);
    }
}
