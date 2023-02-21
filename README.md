
### Using MythicLib as dependency
Register the PhoenixDev repo
```
<repository>
    <id>phoenix</id>
    <url>https://nexus.phoenixdevt.fr/repository/maven-public/</url>
</repository>
```
Then add MythicLib-dist as dependency
```
<dependency>
    <groupId>io.lumine</groupId>
    <artifactId>MythicLib-dist</artifactId>
    <version>1.5.1-SNAPSHOT</version>
    <scope>provided</scope>
    <optional>true</optional>
</dependency>
```

### Compiling MythicLib
MythicLib centralizes all the version-dependent code for MMOItems and MMOCore, meaning it uses NMS instead of the regular spigot API. Since Minecraft server builds now use obfuscated mappings ([since 1.17](https://www.spigotmc.org/threads/spigot-bungeecord-1-17-1-17-1.510208/)) you will first need to setup your mojang-remapped spigot jars for all versions above 1.17.

Here are the instructions you can use to generate the four required mojang-remapped jars (using [BuildTools](https://www.spigotmc.org/wiki/buildtools/)). Since spigot 1.17 runs on Java 16 and my default Java installation is version 17, I had to redownload a Java 16 JDK and have BuildTools run on that one instead in order to build a remapped spigot 1.17.
```
"C:\Program Files\Java\jdk-16.0.1\bin\java" -jar BuildTools.jar --rev 1.17   --remapped
java                                        -jar BuildTools.jar --rev 1.18   --remapped
java                                        -jar BuildTools.jar --rev 1.18.2 --remapped
java                                        -jar BuildTools.jar --rev 1.19   --remapped
java                                        -jar BuildTools.jar --rev 1.19.3 --remapped
```

Version wrappers for 1.17+ have separate Maven modules. Another option is to build your remapped spigot jar for one specific version and remove all the other modules.

Since MythicLib 1.3.4, it is no longer required to first compile MythicLib before compiling MMOItems and MMOCore since the PhoenixDevt repo now includes MythicLib, MMOItems and MMOCore development artifacts.