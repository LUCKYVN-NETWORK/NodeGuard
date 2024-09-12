package me.stella.objects.profilers;

import me.stella.NodeGuard;
import me.stella.discord.WebhookService;
import me.stella.discord.elements.EmbedWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractProfiler {

    private final double threshold;
    private final long cooldown;
    private final long interval;

    private long cd = -1;

    public AbstractProfiler(ConfigurationSection section) {
        this(section.getDouble("threshold"), section.getLong("cooldown"), section.getLong("interval"));
    }

    private AbstractProfiler(double threshold, long cooldown, long interval) {
        this.threshold = threshold;
        this.cooldown = cooldown;
        this.interval = interval;
    }

    public final double getThreshold() {
        return this.threshold;
    }

    public final long getCooldown() {
        return this.cooldown;
    }

    public final long getInterval() {
        return this.interval;
    }

    public void setCD(long timeout) {
        cd = timeout;
    }

    public long getCD() {
        return cd;
    }

    public abstract boolean isExceedingThreshold();

    public abstract EmbedWrapper buildWarningEmbed();

    public abstract void debug();

    public void activateProfiler() {
        final FileConfiguration config = NodeGuard.getMain().getConfig();
        NodeGuard.getScheduler().scheduleAtFixedRate(() -> {
            try {
                if(NodeGuard.DEBUG)
                    debug();
                if(isExceedingThreshold() && System.currentTimeMillis() >= getCD()) {
                    String webhook = config.getString("discord.webhook", "");
                    JSONObject baseWebhookObj = new JSONObject();
                    List<String> mention = config.getStringList("discord.ping-role");
                    StringBuilder contentBuilder = new StringBuilder();
                    mention.forEach(id -> contentBuilder.append("<@&{id}> ".replace("{id}", id)));
                    baseWebhookObj.put("content", contentBuilder.toString());
                    baseWebhookObj.put("tts", false);
                    JSONArray embeds = new JSONArray();
                    embeds.add(buildWarningEmbed());
                    baseWebhookObj.put("embeds", embeds);
                    WebhookService.sendWebhook(webhook, baseWebhookObj);
                    setCD(System.currentTimeMillis() + getCooldown());
                }
            } catch(Throwable t) { t.printStackTrace(); }
        }, 2000L, getInterval(), TimeUnit.MILLISECONDS);
    }

}
