/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package dev.nanite.mlp;

import dev.nanite.mlp.aw2at.AccessWidenerToTransformerTask;
import dev.nanite.mlp.test.AccessRemappper;
import net.fabricmc.accesswidener.AccessWidenerFormatException;
import net.fabricmc.mappingio.format.ProGuardReader;
import net.fabricmc.mappingio.format.TsrgReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

public class AccessRemappTests {

    private static AccessRemappper remappper;
    @BeforeClass
    public static void loadRemapper() throws IOException, URISyntaxException {

        Path cacheDir = Path.of("temp");

        if (!Files.exists(cacheDir)) {
            Files.createDirectory(cacheDir);
        }

        byte[] mojangMappings = AccessWidenerToTransformerTask.getMojangMappings(cacheDir, "1.20.1");
        byte[] srgMappings = AccessWidenerToTransformerTask.getSrgMappings(cacheDir, "1.20.1");

        MemoryMappingTree tree = new MemoryMappingTree();
        ProGuardReader.read(new InputStreamReader(new ByteArrayInputStream(mojangMappings)), "named", "obf", tree);
        TsrgReader.read(new InputStreamReader(new ByteArrayInputStream(srgMappings)), tree);

        remappper = new AccessRemappper(List.of(Path.of("test_data", "minecraft-mapped.jar")), tree, "named", "srg");
    }
    @Test
    public void accessibleField() {
        String aw = createAW("accessible field net/minecraft/world/entity/vehicle/Boat waterLevel D");
        String at = createAT("public net.minecraft.world.entity.vehicle.Boat f_38277_");
        assertEquals(at, remappper.remap(aw));
    }

    @Test
    public void accessibleMethod() {
        String aw = createAW("accessible method net/minecraft/world/entity/vehicle/Boat getWaterLevelAbove ()F");
        String at = createAT("public net.minecraft.world.entity.vehicle.Boat m_38371_()F");
        assertEquals(at, remappper.remap(aw));
    }

    @Test
    public void accessibleClass() {
        String aw = createAW("accessible class net/minecraft/world/entity/vehicle/Boat");
        String at = createAT("public net.minecraft.world.entity.vehicle.Boat");
        assertEquals(at, remappper.remap(aw));
    }


    @Test
    public void extendableField() {
        String aw = createAW("extendable field net/minecraft/world/entity/vehicle/Boat waterLevel D");
        assertThrows(AccessWidenerFormatException.class, () -> remappper.remap(aw));
    }

    @Test
    public void extendableMethod() {
        String aw = createAW("extendable method net/minecraft/world/entity/vehicle/Boat getWaterLevelAbove ()F");
        String at = createAT("protected-f net.minecraft.world.entity.vehicle.Boat m_38371_()F");
        assertEquals(at, remappper.remap(aw));
    }

    @Test
    public void extendableClass() {
        String aw = createAW("extendable class net/minecraft/world/entity/vehicle/Boat");
        String at = createAT("public-f net.minecraft.world.entity.vehicle.Boat");
        assertEquals(at, remappper.remap(aw));
    }


    @Test
    public void mutableField() {
        String aw = createAW("mutable field net/minecraft/world/entity/vehicle/Boat waterLevel D");
        String at = createAT("public-f net.minecraft.world.entity.vehicle.Boat f_38277_");
        assertEquals(at, remappper.remap(aw));
    }

    @Test
    public void mutableMethod() {
        String aw = createAW("mutable method net/minecraft/world/entity/vehicle/Boat getWaterLevelAbove ()F");
        assertThrows(AccessWidenerFormatException.class, () -> remappper.remap(aw));
    }

    @Test
    public void mutableClass() {
        String aw = createAW("mutable class net/minecraft/world/entity/vehicle/Boat");
        assertThrows(AccessWidenerFormatException.class, () -> remappper.remap(aw));
    }

    @Test
    public void allValid() {
        String aw = createAW("""
                accessible field net/minecraft/world/entity/vehicle/Boat waterLevel D
                accessible method net/minecraft/world/entity/vehicle/Boat getWaterLevelAbove ()F
                accessible class net/minecraft/world/entity/vehicle/Boat
                extendable method net/minecraft/world/entity/vehicle/Boat getWaterLevelAbove ()F
                extendable class net/minecraft/world/entity/vehicle/Boat
                mutable field net/minecraft/world/entity/vehicle/Boat waterLevel D""");
        String at = createAT("""
                public net.minecraft.world.entity.vehicle.Boat f_38277_
                public net.minecraft.world.entity.vehicle.Boat m_38371_()F
                public net.minecraft.world.entity.vehicle.Boat
                protected-f net.minecraft.world.entity.vehicle.Boat m_38371_()F
                public-f net.minecraft.world.entity.vehicle.Boat
                public-f net.minecraft.world.entity.vehicle.Boat f_38277_""");

        assertEquals(at, remappper.remap(aw));

    }


    private static String createAW(String line) {
        return "accessWidener v1 named" + "\n" + line;
    }

    private static String createAT(String line) {
        //Writer adds this
        return line + "\n";
    }
}