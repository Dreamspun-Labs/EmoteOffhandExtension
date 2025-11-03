package org.geysermc.extension.emoteoffhand;

import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.event.bedrock.ClientEmoteEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class EmoteOffhand implements Extension {
    private static final Yaml YAML = new Yaml();

    private boolean passthroughEmotes = false;

    @Subscribe
    public void onPostInitialize(GeyserPostInitializeEvent event) {
        this.logger().info("Loading EmoteOffhand...");

        try {
            Path configPath = this.dataFolder().resolve("config.yml");
            if (Files.notExists(configPath)) createConfig();
            Map<String, Object> config = YAML.load(new FileReader(configPath.toFile()));
            if (!config.containsKey("passthrough-emotes") || !(config.get("passthrough-emotes") instanceof Boolean)) {
                this.logger().warning("Invalid config found! Config will be rewritten.");
                createConfig();
            } else {
                this.passthroughEmotes = (boolean) config.get("passthrough-emotes");
            }
        } catch (IOException e) {
            this.logger().error("Unable to load config! Will use the default config.", e);
        }

        this.logger().info("Loaded EmoteOffhand!");
    }

    @Subscribe
    public void onEmote(ClientEmoteEvent event) {
        if (!this.passthroughEmotes) event.setCancelled(true);

        event.connection().entities().switchHands();
    }

    private void createConfig() throws IOException {
        if (Files.notExists(this.dataFolder())) Files.createDirectories(this.dataFolder());
        Path configPath = this.dataFolder().resolve("config.yml");

        InputStream internalConfig = EmoteOffhand.class.getResourceAsStream("/extension-config.yml");
        if (internalConfig == null) {
            throw new IOException("`extension-config.yml` not found within the JAR file. The extension JAR file may be corrupt.");
        }

        Files.write(configPath, internalConfig.readAllBytes());

        internalConfig.close();
    }
}
