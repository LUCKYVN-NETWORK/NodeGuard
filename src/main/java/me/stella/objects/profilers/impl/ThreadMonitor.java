package me.stella.objects.profilers.impl;

import me.stella.NodeGuard;
import me.stella.discord.elements.EmbedWrapper;
import me.stella.discord.elements.FieldWrapper;
import me.stella.objects.profilers.AbstractProfiler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class ThreadMonitor extends AbstractProfiler {

    private final AtomicLong lastPing;

    public ThreadMonitor(ConfigurationSection section) {
        super(section);
        lastPing = new AtomicLong(System.nanoTime());
        (new BukkitRunnable() {
            @Override
            public void run() {
                lastPing.set(System.nanoTime());
            }
        }).runTaskTimer(NodeGuard.getMain(), 1L, 1L);
    }

    @Override
    public boolean isExceedingThreshold() {
        return getLastResponseMilli() >= getThreshold();
    }

    private double getLastResponse() {
        return ((double) (System.nanoTime() - lastPing.get()));
    }

    private double getLastResponseSeconds() {
        return getLastResponse() / 1000000000.0D;
    }

    private double getLastResponseMilli() {
        return getLastResponse() / 1000000.0D;
    }

    @Override
    public void debug() {
        NodeGuard.console.log(Level.INFO, "Thread Mod { lastResponse = " + NodeGuard.numFormatter.format(getLastResponseMilli()) + "ms ; threads = " + NodeGuard.numFormatter.format(Thread.activeCount()) + " }");
    }

    @Override
    public EmbedWrapper buildWarningEmbed() {
        List<FieldWrapper> fields = new ArrayList<>();
        fields.add(new FieldWrapper("Cụm máy chủ", NodeGuard.getMain().getConfig().getString("node.server-name"), false));
        fields.add(new FieldWrapper("Thời điểm quá tải", NodeGuard.timeFormatter.format(Calendar.getInstance().getTime()), false));
        fields.add(new FieldWrapper("Đã ngừng phản hồi", NodeGuard.numFormatter.format(getLastResponseSeconds()) + "s", true));
        fields.add(new FieldWrapper("Số thread đang chạy", NodeGuard.numFormatter.format(Thread.activeCount()), true));
        return new EmbedWrapper("**Máy chủ đang quá tải!**", "Hệ thống phát hiện máy chủ đang đơ!", 0xdc143c, fields);
    }

}
