rootProject.name = "HMCWraps"
include("core", "api")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("depends") {
            library("spigot", "org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")
            library("placeholderapi", "me.clip:placeholderapi:2.11.6")
            library("nexo", "com.nexomc:nexo:1.6.0")
            library("oraxen", "com.github.oraxen:oraxen:-SNAPSHOT")
            library("itemsadder", "com.github.LoneDev6:API-ItemsAdder:3.6.1")
            library("mythicmobs", "io.lumine:Mythic-Dist:5.9.1")
            library("annotations", "org.jetbrains:annotations:26.0.1")
            library("executableitems", "com.github.Ssomar-Developement:SCore:5.25.3.9")
        }
        create("libs") {
            library("particles", "com.owen1212055:particlehelper:1.5.0-SNAPSHOT")
            library("configupdater", "com.github.BG-Software-LLC:CommentedConfiguration:-SNAPSHOT")
            library("bstats", "org.bstats:bstats-bukkit:3.0.2")
            library("gui", "dev.triumphteam:triumph-gui:3.2.0-SNAPSHOT")
            library("configurate", "org.spongepowered:configurate-yaml:4.2.0")
            library("mclogs", "gs.mclo:java:2.2.1")
            library("nbtapi", "de.tr7zw:item-nbt-api:2.15.0")
            library("folialib", "com.tcoded:FoliaLib:0.4.4")

            library("adventure-api", "net.kyori", "adventure-api").versionRef("adventure")
            library("minimessage", "net.kyori", "adventure-text-minimessage").versionRef("adventure")
            library("adventure-bukkit", "net.kyori:adventure-platform-bukkit:4.4.0")
            version("adventure", "4.23.0")
            bundle("adventure", listOf("adventure-api", "minimessage", "adventure-bukkit"))

            library("lamp-common", "com.github.Revxrsal.Lamp", "common").versionRef("lamp")
            library("lamp-bukkit", "com.github.Revxrsal.Lamp", "bukkit").versionRef("lamp")
            version("lamp", "3.3.4")
            bundle("lamp", listOf("lamp-common", "lamp-bukkit"))
        }
    }
}
