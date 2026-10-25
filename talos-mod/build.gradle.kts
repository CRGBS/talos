import org.gradle.api.artifacts.component.ModuleComponentIdentifier

plugins {
    id("fabric-loom")
}

java { toolchain.languageVersion = JavaLanguageVersion.of(21) }

loom {
    accessWidenerPath = file("src/main/resources/talos.accesswidener")
}

// Resolve the embedded GraalPy runtime separately so its complete runtime graph can
// be added to Loom's non-transitive include configuration below. Using a separate
// configuration keeps Minecraft, Fabric API and unrelated mod dependencies out of
// the nested jars.
val graalpyRuntimeClasspath = configurations.create("graalpyRuntimeClasspath") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    implementation(project(":talos-graalpy-runtime"))
    include(project(":talos-graalpy-runtime"))
    add(graalpyRuntimeClasspath.name, project(":talos-graalpy-runtime"))

    implementation("org.java-websocket:Java-WebSocket:1.5.7")
    include("org.java-websocket:Java-WebSocket:1.5.7")

    testImplementation("net.fabricmc:fabric-loader-junit:${property("fabric_loader_version")}")
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Loom's include configuration is deliberately non-transitive. Including the
// talos-graalpy-runtime project alone therefore does not package its external
// dependencies (including org.graalvm.polyglot.Context). Include each resolved JAR
// from that project's runtime graph as a direct nested dependency instead. Resolve
// only actual JAR artifacts; this avoids trying to package aggregator POMs.
val graalpyRuntimeJars = graalpyRuntimeClasspath.incoming.artifacts.artifacts
    .mapNotNull { artifact ->
        val component = artifact.id.componentIdentifier as? ModuleComponentIdentifier
        if (component != null && artifact.file.extension.equals("jar", ignoreCase = true)) {
            "${component.group}:${component.module}:${component.version}"
        } else {
            null
        }
    }
    .distinct()
    .sorted()

graalpyRuntimeJars.forEach { dependency ->
    dependencies.add("include", dependency)
}

tasks.test { useJUnitPlatform() }

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") { expand(mapOf("version" to project.version)) }
}
