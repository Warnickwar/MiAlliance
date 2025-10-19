package com.mialliance.components.implementations;

import com.mialliance.components.Component;
import com.mialliance.components.ComponentObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class CooldownComponent extends Component<ComponentObject> {

    private final HashMap<String, Integer> cooldowns;

    public CooldownComponent() {
        this.cooldowns = new HashMap<>();
    }

    @Override
    protected void serverTick() {
        cooldowns.forEach((str, num) -> {
            if (num <= 0) {
                cooldowns.remove(str);
            } else {
                cooldowns.put(str, num-1);
            }
        });
    }

    public void setCooldown(@NotNull String key, int time) {
        cooldowns.put(key, time);
    }

    public boolean isCooldownPresent(@NotNull String key) {
        return cooldowns.containsKey(key);
    }

    public void endCooldown(@NotNull String key) {
        if (this.isCooldownPresent(key)) {
            cooldowns.remove(key);
        }
    }

    @Override
    public void save(CompoundTag tag) {
        ListTag cds = new ListTag();
        cooldowns.forEach((str, num) -> {
            CompoundTag temp = new CompoundTag();
            temp.putString("name", str);
            temp.putInt("timeToLive", num);
            cds.add(temp);
        });
        tag.put("cooldowns", cds);
    }

    @Override
    public void load(CompoundTag tag) {
        cooldowns.clear();
        ListTag cds = tag.getList("cooldowns", ListTag.TAG_COMPOUND);
        cds.forEach(uTag -> {
            if (!(uTag instanceof CompoundTag cTag)) return;
            cooldowns.put(cTag.getString("name"), cTag.getInt("timeToLive"));
        });
    }

}
