package dev.imabad.mlp;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.flow.FlowProviders;
import org.gradle.api.flow.FlowScope;

import javax.inject.Inject;

public class MultiLoaderPlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.getExtensions().create("multiLoader", MultiLoaderExtension.class);
//        project.getTasks().register("himikey", HiMikeTask.class);
//        project.afterEvaluate((project1 -> {
//            if(project1.getPlugins().hasPlugin("fabric-loom")) {
//                final LoomGradleExtension extension = LoomGradleExtension.get(project1);
//                new Thread(() -> {
//                    try {
//                        boolean shouldKeepLooking = true;
//                        while(shouldKeepLooking) {
//                            try {
//                                extension.getNamedMinecraftProvider();
//                                shouldKeepLooking = false;
//                            } catch(NullPointerException e){}
//                            Thread.sleep(1000);
//                        }
//                        extension.getMinecraftJars(MappingsNamespace.NAMED).forEach(p -> {
//                            System.out.println(p.toString());
//                        });
//                    } catch(Exception e){}
//                }).start();
//            }
//        }));
    }
}
