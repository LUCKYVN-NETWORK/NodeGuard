package me.stella.objects.profilers.impl;

import com.google.common.util.concurrent.AtomicDouble;
import me.stella.NodeGuard;
import me.stella.discord.elements.EmbedWrapper;
import me.stella.discord.elements.FieldWrapper;
import me.stella.objects.profilers.AbstractProfiler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

public class PingMonitor extends AbstractProfiler {

    public PingMonitor(ConfigurationSection section) {
        super(section);
    }

    @Override
    public boolean isExceedingThreshold() {
        return getAveragePing()[0]  >= getThreshold();
    }

    private int getPlayerPing(Player player) {
        try {
            if(NodeGuard.getServerProtocol() >= 1170)
                return (int) player.getClass().getMethod("getPing").invoke(player);
            else {
                Object playerHandle = player.getClass().getMethod("getHandle").invoke(player);
                return (int) playerHandle.getClass().getField("ping").get(playerHandle);
            }
        } catch(Throwable t) { t.printStackTrace(); }
        return 0;
    }

    private double[] getAveragePing() {
        AtomicDouble maxPing = new AtomicDouble(-1.0D);
        AtomicDouble total = new AtomicDouble(0.0D);
        Bukkit.getOnlinePlayers().forEach(player -> {
            int playerPing = getPlayerPing(player);
            maxPing.set(Math.max(maxPing.get(), playerPing));
            total.addAndGet(playerPing);
        });
        return new double[] { (total.get() / (double)Bukkit.getOnlinePlayers().size()), maxPing.get()};
    }

    @Override
    public void debug() {
        double[] pingStats = getAveragePing();
        NodeGuard.console.log(Level.INFO, "Ping Mod { average = " + NodeGuard.numFormatter.format(pingStats[0]) + "ms ; max = " + NodeGuard.numFormatter.format(pingStats[1]) + "ms }");
    }

    @Override
    public EmbedWrapper buildWarningEmbed() {
        List<FieldWrapper> fields = new ArrayList<>();
        double[] pingStats = getAveragePing();
        fields.add(new FieldWrapper("Cụm máy chủ", NodeGuard.getMain().getConfig().getString("node.server-name"), false));
        fields.add(new FieldWrapper("Thời điểm quá tải", NodeGuard.timeFormatter.format(Calendar.getInstance().getTime()), false));
        fields.add(new FieldWrapper("Ping trung bình", NodeGuard.numFormatter.format(pingStats[0]) + "ms", true));
        fields.add(new FieldWrapper("Ping cao nhất", NodeGuard.numFormatter.format(pingStats[1]) + "ms", true));
        return new EmbedWrapper("**Máy chủ đang quá tải!**", "Hệ thống phát hiện ping của người chơi đang quá cao!", 0x00b7eb, fields);
    }
}
