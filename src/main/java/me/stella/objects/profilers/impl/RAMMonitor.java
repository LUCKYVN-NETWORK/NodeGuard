package me.stella.objects.profilers.impl;

import me.stella.NodeGuard;
import me.stella.discord.elements.EmbedWrapper;
import me.stella.discord.elements.FieldWrapper;
import me.stella.objects.profilers.AbstractProfiler;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;

public class RAMMonitor extends AbstractProfiler {

    public RAMMonitor(ConfigurationSection section) {
        super(section);
    }

    @Override
    public boolean isExceedingThreshold() {
        return getRAMUsage() >= getThreshold();
    }

    private double getRAMUsage() {
        return 100.0D - ((getFreeRAM() / getRAMAllocated()) * 100.0D);
    }

    private double getRAMAllocated() {
        return ((double) Runtime.getRuntime().totalMemory()) / 1048576.0;
    }

    private double getFreeRAM() {
        return (double) Runtime.getRuntime().freeMemory()  / 1048576.0;
    }

    @Override
    public void debug() {
        NodeGuard.console.log(Level.INFO, "RAM Mod { usage = " + NodeGuard.numFormatter.format(getRAMUsage()) + "% ; free = " + NodeGuard.numFormatter.format(getFreeRAM()) + " MB }");
    }

    @Override
    public EmbedWrapper buildWarningEmbed() {
        List<FieldWrapper> fields = new ArrayList<>();
        fields.add(new FieldWrapper("Cụm máy chủ", NodeGuard.getMain().getConfig().getString("node.server-name"), false));
        fields.add(new FieldWrapper("Thời điểm quá tải", NodeGuard.timeFormatter.format(Calendar.getInstance().getTime()), false));
        fields.add(new FieldWrapper("RAM đang dùng", NodeGuard.numFormatter.format(getRAMUsage()) + "%", true));
        fields.add(new FieldWrapper("RAM còn trống", NodeGuard.numFormatter.format(getFreeRAM()) + "MB", true));
        return new EmbedWrapper("**Máy chủ đang quá tải!**", "Hệ thống phát hiện RAM đang quá cao!", 0xff8c00, fields);
    }
}
