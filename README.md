# MultiLoader Gradle Plugin

This is a work in progress gradle plugin designed for easing the creation of MultiLoader mods. It currently supports Fabric & Forge.

# Usage

Add this to your root project `build.gradle.kts`

```
plugins {
    id("dev.imabad.mlp") version("1.0.0-SNAPSHOT")
    id("fabric-loom") version("1.3-SNAPSHOT") apply(false)
    id("net.minecraftforge.gradle") version("6.0.+") apply(false)
}

multiLoader.root() {
    minecraftVersion.set("1.20.1")
    modID.set("test-mod")
}

subprojects {
    repositories {
        maven {
            name = "Creeperhost"
            url = uri("https://maven.creeperhost.net/")
        }
    }
}
```

Add this to your common `build.gradle.kts`

```
plugins {
    id("dev.imabad.mlp")
}

multiLoader.common()
```

Add this to your fabric `build.gradle.kts`

```
plugins {
    id("dev.imabad.mlp")
}

multiLoader.fabric() {
    fabricLoaderVersion.set("0.14.21")
}
```

Add this to your forge `build.gradle.kts`

```
plugins {
    id("dev.imabad.mlp")
}
multiLoader.forge() {
    forgeVersion.set("47.1.43")
}
```

# Credits

Original API design by Modmuss50.