package de.skyslycer.hmcwraps.converter;

import de.skyslycer.hmcwraps.HMCWraps;
import de.skyslycer.hmcwraps.serialization.CollectionFile;
import de.skyslycer.hmcwraps.serialization.PhysicalWrap;
import de.skyslycer.hmcwraps.serialization.Wrap;
import de.skyslycer.hmcwraps.serialization.WrapFile;
import de.skyslycer.hmcwraps.serialization.WrappableItem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public class FileConverter {

    private final HMCWraps plugin;

    public FileConverter(HMCWraps plugin) {
        this.plugin = plugin;
    }

    public boolean convertAll() {
        var success = true;
        var collections = new HashMap<String, List<String>>();
        var wrapFiles = new HashMap<String, WrapFile>();
        try (Stream<Path> paths = Files.find(HMCWraps.CONVERT_PATH, 1, ((filterFile, attributes) -> attributes.isDirectory() && !filterFile.equals(HMCWraps.CONVERT_PATH)))) {
            for (Path path : paths.toList()) {
                plugin.getLogger().info("Converting folder: " + path.toString());
                try {
                    var construct = loadFolder(path, collections);
                    if (construct == null) continue;
                    collections.putAll(construct.collections);
                    wrapFiles.put(path.getFileName().toString(), construct.wrapFile);
                } catch (RuntimeException exception) {
                    plugin.getLogger().warning("An error occurred while converting the folder " + path.getFileName() + " to a wrap file!");
                    exception.printStackTrace();
                    success = false;
                }
            }
        } catch (Exception exception) {
            plugin.getLogger().warning("An error occurred whilst trying to load all folders to convert! Please report this.");
            exception.printStackTrace();
            success = false;
        }
        var generatedPath = HMCWraps.WRAP_FILES_PATH.resolve("generated");
        for (Map.Entry<String, WrapFile> entry : wrapFiles.entrySet()) {
            try {
                Files.createDirectories(generatedPath);
                var filePath = getUnusedFile(generatedPath.resolve(entry.getKey() + ".yml"));
                Files.createFile(filePath);
                YamlConfigurationLoader.builder()
                        .defaultOptions(ConfigurationOptions.defaults().implicitInitialization(false))
                        .path(filePath).indent(2)
                        .build().save(BasicConfigurationNode.factory().createNode().set(entry.getValue()));
            } catch (Exception exception) {
                plugin.getLogger().warning("Could not save generated wrap file! Please report this.\n"
                        + "Your conversion may not have worked correctly. You are advised to delete the generated folders and try again.");
                exception.printStackTrace();
                success = false;
            }
        }
        if (!collections.isEmpty()) {
            var generatedCollectionPath = HMCWraps.COLLECTION_FILES_PATH.resolve("generated");
            try {
                Files.createDirectories(generatedCollectionPath);
                var filePath = getUnusedFile(generatedCollectionPath.resolve("collection.yml"));
                Files.createFile(filePath);
                YamlConfigurationLoader.builder()
                        .defaultOptions(ConfigurationOptions.defaults().implicitInitialization(false))
                        .path(filePath).indent(2)
                        .build().save(BasicConfigurationNode.factory().createNode().set(new CollectionFile(collections, true)));
            } catch (Exception exception) {
                plugin.getLogger().warning("Could not save generated collection file! Please report this.\n"
                        + "Your conversion may not have worked correctly. You are advised to delete the generated folders and try again.");
                exception.printStackTrace();
                success = false;
            }
        }
        return success;
    }

    public FolderConstruct loadFolder(Path path, Map<String, List<String>> collections) throws Exception {
        var file = path.toFile();
        if (!file.exists() || !file.isDirectory()) {
            return null;
        }
        var map = new HashMap<String, WrappableItem>();
        var newCollections = new HashMap<>(collections);
        try (Stream<Path> paths = Files.find(path, 1,
                ((filterFile, attributes) -> attributes.isRegularFile() && (filterFile.toString().endsWith(".yml") || filterFile.toString().endsWith(".yaml"))))) {
            for (Path itemSkinsFile : paths.toList()) {
                plugin.getLogger().info("> Converting file: " + itemSkinsFile.toString());
                var construct = convert(itemSkinsFile);
                if (construct == null) continue;
                var material = "MATERIAL_ERROR";
                if (construct.materials.size() == 1) {
                    material = construct.materials.get(0);
                } else {
                    var matchingCollections = plugin.getCollections().entrySet().stream()
                            .filter(entry -> new HashSet<>(entry.getValue()).containsAll(construct.materials)).toList();
                    var matchingGeneratedCollections = newCollections.entrySet().stream()
                            .filter(entry -> new HashSet<>(entry.getValue()).containsAll(construct.materials)).toList();
                    if (matchingCollections.size() >= 1) {
                        material = matchingCollections.stream().findFirst().get().getKey();
                    } else if (matchingGeneratedCollections.size() >= 1) {
                        material = matchingGeneratedCollections.stream().findFirst().get().getKey();
                    } else {
                        var i = 1;
                        var generatedCollection = "GENERATED_COLLECTION_1";
                        var allCollections = new HashMap<>(plugin.getCollections());
                        allCollections.putAll(newCollections);
                        while (allCollections.containsKey("GENERATED_COLLECTION_" + i)) {
                            generatedCollection = "GENERATED_COLLECTION_" + ++i;
                        }
                        newCollections.put(generatedCollection, construct.materials);
                        material = generatedCollection;
                    }
                }
                WrappableItem item;
                if (map.containsKey(material)) {
                    item = map.get(material);
                    item.getWraps().put(item.getWraps().size() + 1 + "", construct.wrap);
                } else {
                    item = new WrappableItem(new HashMap<>(Map.of("1", construct.wrap)));
                }
                map.put(material, item);
            }
            return new FolderConstruct(new WrapFile(map, true), newCollections);
        }
    }

    public ConvertConstruct convert(Path path) throws ConfigurateException {
        var file = path.toFile();
        if (!file.exists() || file.isDirectory()) {
            return null;
        }
        var itemSkinsFile = YamlConfigurationLoader.builder()
                .defaultOptions(ConfigurationOptions.defaults().implicitInitialization(false))
                .path(path)
                .build().load().get(ItemSkinsFile.class);
        if (itemSkinsFile == null) {
            return null;
        }
        var itemSkinsItem = itemSkinsFile.getAvailableItem();
        var itemSkinsPhysical = itemSkinsFile.getPhysicalItem();
        PhysicalWrap physical = null;
        if (itemSkinsPhysical != null) {
            physical = new PhysicalWrap(
                    itemSkinsPhysical.getMaterial(), itemSkinsPhysical.getDisplayName(), itemSkinsPhysical.getGlowing(), itemSkinsPhysical.getLore(),
                    null, itemSkinsPhysical.getCustomModelData(), null, null, true);
        }
        List<String> lockedLore = null;
        if (itemSkinsFile.getUnavailableItem() != null) {
            lockedLore = itemSkinsFile.getUnavailableItem().getLore();
        }
        String lockedName = null;
        if (itemSkinsFile.getUnavailableItem() != null) {
            lockedName = itemSkinsFile.getUnavailableItem().getDisplayName();
        }
        var wrap = new Wrap(
                itemSkinsItem.getMaterial(), itemSkinsItem.getDisplayName(), itemSkinsItem.getGlowing(), itemSkinsItem.getLore(), null,
                itemSkinsFile.getCustomModelData(), null, null, true, file.getName().replace(".yml", ""),
                null, physical, itemSkinsFile.getPermission(), lockedName, lockedLore, null);
        return new ConvertConstruct(wrap, itemSkinsFile.getMaterial().stream().map(String::toUpperCase).toList());
    }

    private Path getUnusedFile(Path path) {
        var file = path;
        if (!Files.exists(file)) {
            return path;
        }
        var i = 1;
        while (Files.exists(file)) {
            var fileName = path.getFileName().toString();
            var extension = fileName.substring(fileName.lastIndexOf("."));
            var name = fileName.substring(0, fileName.lastIndexOf("."));
            file = path.resolveSibling(name + "_" + i + extension);
            i++;
        }
        return file;
    }

    static private class ConvertConstruct {

        private final Wrap wrap;
        private final List<String> materials;

        private ConvertConstruct(Wrap wrap, List<String> materials) {
            this.wrap = wrap;
            this.materials = materials;
        }

    }

    static private class FolderConstruct {

        private final WrapFile wrapFile;
        private final Map<String, List<String>> collections;

        private FolderConstruct(WrapFile wrapFile, Map<String, List<String>> collections) {
            this.wrapFile = wrapFile;
            this.collections = collections;
        }

    }

}