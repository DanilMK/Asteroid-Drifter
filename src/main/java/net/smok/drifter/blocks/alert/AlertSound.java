package net.smok.drifter.blocks.alert;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.smok.drifter.network.ServerBoundPackets;
import net.smok.drifter.registries.DrifterSounds;
import net.smok.drifter.utils.SavedDataSlot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AlertSound {

    public static final AlertSound[] SOUND_PRESETS = new AlertSound[] {
            new AlertSound(DrifterSounds.SHIP_ALARM.get(), 20, 1),
            new AlertSound(DrifterSounds.HARD_ALARM.get(), 20, 1),
            new AlertSound(DrifterSounds.BIOHAZARD_ALARM.get(), 20, 1),
            new AlertSound(DrifterSounds.FIRE_ALARM.get(), 20, 1),

            new AlertSound(DrifterSounds.MARS_ALARM.get(), 20, 1),
            new AlertSound(DrifterSounds.EARTH_ALARM.get(), 20, 1),
            new AlertSound(DrifterSounds.VENUS_ALARM.get(), 20, 1),
            new AlertSound(DrifterSounds.SIREN_ALARM.get(), 20, 1),

            new AlertSound(DrifterSounds.GALAXY_POLICE_ALARM.get(), 20, 1),
            new AlertSound(SoundEvents.BELL_BLOCK, 20, 1),
            new AlertSound(SoundEvents.NOTE_BLOCK_HARP.value(), 20, 1),
            new AlertSound(SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), 20, 1)
    };


    private ResourceLocation id;
    private int period; // 1t - 5s (100t)
    private float pitch; // 0-2
    private SoundEvent soundEvent;

    @Contract(pure = true)
    public AlertSound(@NotNull AlertSound defaultSound) {
        this.id = defaultSound.id;
        this.period = defaultSound.period;
        this.pitch = defaultSound.pitch;
        this.soundEvent = defaultSound.soundEvent;
    }

    public AlertSound(@NotNull SoundEvent soundEvent, int period, float pitch) {
        id = BuiltInRegistries.SOUND_EVENT.getKey(soundEvent);
        this.period = period;
        this.pitch = pitch;
        this.soundEvent = soundEvent;
    }

    public AlertSound(ResourceLocation id, int period, float pitch) {
        this.period = period;
        this.pitch = pitch;
        setId(id);
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getIdAsString() {
        return id.toString();
    }

    public void setId(String id) {
        setId(new ResourceLocation(id));
    }

    public void setId(ResourceLocation id) {
        BuiltInRegistries.SOUND_EVENT.getOptional(id).ifPresent(soundEvent1 -> {
            this.soundEvent = soundEvent1;
            this.id = id;
        });
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = Mth.clamp(period, 2, 100);
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = Mth.clamp(pitch, 0, 2);
    }

    public SoundEvent getSoundEvent() {
        return soundEvent;
    }

    public static ServerBoundPackets.ByteCodec<AlertSound> BYTE_CODEC = ServerBoundPackets.codec((byteBuf, sound) -> {
        byteBuf.writeResourceLocation(sound.id);
        byteBuf.writeInt(sound.period);
        byteBuf.writeFloat(sound.pitch);
    }, byteBuf -> new AlertSound(byteBuf.readResourceLocation(), byteBuf.readInt(), byteBuf.readFloat()));


    public static SavedDataSlot<AlertSound> savedDataSlot(AlertSound defaultSound, String name) {
        return new SavedDataSlot<>(new AlertSound(defaultSound)) {
            @Override
            public void load(CompoundTag compoundTag) {
                if (compoundTag.contains(name, Tag.TAG_COMPOUND)) {
                    CompoundTag tag = compoundTag.getCompound(name);

                    if (tag.contains("id", Tag.TAG_STRING))
                        getValue().setId(tag.getString("id"));

                    if (tag.contains("period", Tag.TAG_INT))
                        getValue().setPeriod(tag.getInt("period"));

                    if (tag.contains("pitch", Tag.TAG_FLOAT))
                        getValue().setPitch(tag.getFloat("pitch"));
                }

            }

            @Override
            public void save(CompoundTag compoundTag) {
                CompoundTag tag = new CompoundTag();
                tag.putString("id", getValue().getIdAsString());
                tag.putInt("period", getValue().getPeriod());
                tag.putFloat("pitch", getValue().getPitch());
                compoundTag.put(name, tag);
            }
        };
    }

    public Component getPitchText() {
        return Component.translatable("tooltip.asteroid_drifter.detector_sound_pitch", (int) (pitch * 100));
    }

    public Component getPeriodText() {
        return Component.translatable("tooltip.asteroid_drifter.detector_sound_period", String.format("%.1f", period / 20f));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        AlertSound that = (AlertSound) object;
        return period == that.period && Float.compare(pitch, that.pitch) == 0 && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, period, pitch);
    }

    @Override
    public String toString() {
        return "AlertSound{" +
                "id=" + id +
                ", period=" + period +
                ", pitch=" + pitch +
                '}';
    }
}
