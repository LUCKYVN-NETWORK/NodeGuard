package me.stella;

import me.stella.discord.WebhookService;
import me.stella.objects.AutoCloseJavaScheduler;
import me.stella.objects.profilers.impl.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NodeGuard extends JavaPlugin {

    public static final Logger console = Logger.getLogger("Minecraft");

    public static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");

    public static final NumberFormat numFormatter = NumberFormat.getInstance();

    private static final AutoCloseJavaScheduler scheduler = new AutoCloseJavaScheduler(Executors.newScheduledThreadPool(64));

    public static boolean DEBUG;

    public static AutoCloseJavaScheduler getScheduler() {
        return scheduler;
    }

    private static NodeGuard main;


    public static NodeGuard getMain() {
        return main;
    }

    @Override
    public void onEnable() {
        main = this;
        saveDefaultConfig();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.gc();
                System.runFinalization();
            } catch(Throwable t) {}
        }, 10L, 60L, TimeUnit.SECONDS);
        FileConfiguration config = getConfig();
        DEBUG = config.getBoolean("node.debug", false);
        (new PingMonitor(config.getConfigurationSection("node.ping-monitor"))).activateProfiler();
        (new RAMMonitor(config.getConfigurationSection("node.ram-monitor"))).activateProfiler();
        (new ThreadMonitor(config.getConfigurationSection("node.thread-monitor"))).activateProfiler();
        (new TPSMonitor(config.getConfigurationSection("node.tps-monitor"))).activateProfiler();
        final String server = config.getString("node.server-name");
        final String webhook = config.getString("discord.webhook");
        final List<String> mention = config.getStringList("discord.ping-role");
        (new Thread(() -> {
            try {
                scheduler.submit(() -> {
                    JSONObject baseWebhookObj = new JSONObject();
                    StringBuilder contentBuilder = new StringBuilder();
                    mention.forEach(id -> contentBuilder.append("<@&{id}> ".replace("{id}", id)));
                    contentBuilder.append("Máy chủ **" + server + "** đã bắt đầu mở chế độ theo dõi!");
                    baseWebhookObj.put("content", contentBuilder.toString());
                    baseWebhookObj.put("tts", false);
                    JSONArray embeds = new JSONArray();
                    baseWebhookObj.put("embeds", embeds);
                    WebhookService.sendWebhook(webhook, baseWebhookObj);
                }).get();
            } catch(Throwable t) { t.printStackTrace(); }
        })).start();
    }

    @Override
    public void onDisable() {
        final String server = getConfig().getString("node.server-name");
        final String webhook = getConfig().getString("discord.webhook");
        final List<String> mention = getConfig().getStringList("discord.ping-role");
        (new Thread(() -> {
            try {
                scheduler.clearTasks();
                Thread.sleep(1000L);
                scheduler.submit(() -> {
                    JSONObject baseWebhookObj = new JSONObject();
                    StringBuilder contentBuilder = new StringBuilder();
                    mention.forEach(id -> contentBuilder.append("<@&{id}> ".replace("{id}", id)));
                    contentBuilder.append("Máy chủ **" + server + "** đã tắt theo dõi!");
                    baseWebhookObj.put("content", contentBuilder.toString());
                    baseWebhookObj.put("tts", false);
                    JSONArray embeds = new JSONArray();
                    baseWebhookObj.put("embeds", embeds);
                    WebhookService.sendWebhook(webhook, baseWebhookObj);
                }).get();
                Thread.sleep(2500L);
                console.log(Level.INFO, "Thread cleanup complete! Scheduler now shutting down...");
                scheduler.shutdown();
            } catch(Throwable t) { t.printStackTrace(); }
        })).start();
        getServer().getScheduler().cancelTasks(this);
    }

    public static int getServerProtocol() {
        String version = getMain().getServer().getClass().getName().split("\\.")[3];
        String[] patchSegments = version.split("_"); int protocolValue = 0;
        protocolValue += Integer.parseInt(patchSegments[0].substring(1)) * 1000;
        protocolValue += Integer.parseInt(patchSegments[1]) * 10;
        protocolValue += Integer.parseInt(patchSegments[2].substring(1));
        return protocolValue;
    }
}
