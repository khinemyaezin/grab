package com.grab.framework.workflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowAuthoringTypesTest {

    @TempDir
    Path output;

    @Test
    void javacRejectsIncompatibleConnections() throws IOException {
        String setup = """
                WorkflowFlow<String, String> start = flow(String.class);
                WorkflowStep<String, Integer> size = map("size", String.class, Integer.class, String::length);
                WorkflowStep<String, String> text = map("text", String.class, String.class, value -> value);
                WorkflowFlow<String, Integer> sized = start.then(size);
                WorkflowOperation<Integer, String> write = WorkflowOperation.mutation("write", Integer.class, String.class);
                """;
        assertEquals(0, compile(setup + "sized.then(map(\"next\", Integer.class, String.class, Object::toString));"));
        assertTrue(compile(setup + "sized.then(text);") != 0);
        assertTrue(compile(setup + "when(\"optional\", value -> true, size);") != 0);
        assertTrue(compile(setup + "choose(\"choice\", String.class, String.class, Map.of(\"a\", text, \"b\", size), \"a\", value -> \"a\");") != 0);
        assertTrue(compile(setup + "join(\"joined\", size, text, Integer.class, (length, value) -> value);") != 0);
        assertTrue(compile(setup + "bind(\"bound\", String.class, String.class, write, value -> value, (input, result) -> result);") != 0);
    }

    private int compile(String body) throws IOException {
        String source = """
                import com.grab.framework.workflow.*;
                import java.util.Map;
                import static com.grab.framework.workflow.WorkflowDsl.*;

                class AuthoringSample {
                    static void define() {
                %s
                    }
                }
                """.formatted(body);
        Path file = output.resolve("AuthoringSample.java");
        Files.writeString(file, source);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String classpath = System.getProperty("java.class.path");
        String destination = output.toString();
        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        return compiler.run(null, diagnostics, diagnostics, "-proc:none", "-classpath", classpath, "-d", destination,
                file.toString());
    }
}
