package me.stella.objects.profilers.impl;

import me.stella.NodeGuard;
import me.stella.discord.elements.EmbedWrapper;
import me.stella.discord.elements.FieldWrapper;
import me.stella.objects.profilers.AbstractProfiler;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

public class TPSMonitor extends AbstractProfiler {

    public TPSMonitor(ConfigurationSection section) {
        super(section);
    }

    @Override
    public boolean isExceedingThreshold() {
        return getTPS() <= getThreshold();
    }

    public double getTPS() {
        Server server = Bukkit.getServer();
        try {
            return ((double[]) server.getClass().getMethod("getTPS").invoke(server))[0];
        } catch(Throwable t) {}
        return 20.0D;
    }

    @Override
    public void debug() {
        NodeGuard.console.log(Level.INFO, "TPS Mod { tps = " + NodeGuard.numFormatter.format(getTPS()) + " ; players = " + NodeGuard.numFormatter.format(Bukkit.getOnlinePlayers().size()) + " }");
    }

    @Override
    public EmbedWrapper buildWarningEmbed() {
        List<FieldWrapper> fields = new ArrayList<>();
        fields.add(new FieldWrapper("Cụm máy chủ", NodeGuard.getMain().getConfig().getString("node.server-name"), false));
        fields.add(new FieldWrapper("Thời điểm quá tải", NodeGuard.timeFormatter.format(Calendar.getInstance().getTime()), false));
        fields.add(new FieldWrapper("TPS hiện tại", NodeGuard.numFormatter.format(getTPS()), true));
        fields.add(new FieldWrapper("Số người chơi", NodeGuard.numFormatter.format(Bukkit.getOnlinePlayers().size()), true));
        return new EmbedWrapper("**Máy chủ đang quá tải!**", "Hệ thống phát hiện TPS của máy chủ quá thấp!", 0xff69b4, fields);
    }
}