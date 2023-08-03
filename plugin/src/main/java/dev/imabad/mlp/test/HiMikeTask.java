package dev.imabad.mlp.test;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;

public class HiMikeTask extends DefaultTask {

    @TaskAction
    public void run() throws IOException {
//        byte[] bytes = ReappearTest.remapAccessWidener(getProject(), Files.readAllBytes(getProject().file("test.aw").toPath()));
//        Files.write(getProject().file("test2.at").toPath(), bytes);
    }
}
