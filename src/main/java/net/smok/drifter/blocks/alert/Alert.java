package net.smok.drifter.blocks.alert;

import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.smok.drifter.utils.SavedDataSlot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Alert {

    private final Detector detector;

    private String name;
    private boolean active;
    private boolean tested;
    private final SavedDataSlot<Icon> icon;
    private final SavedDataSlot<AlertSound> sound;

    public Alert(Detector detector, String defaultName) {
        this(detector, Icon.ICON_PRESETS[0], defaultName);
    }

    public Alert(Detector detector, Icon defaultIcon, String defaultName) {
        this.detector = detector;
        this.name = Language.getInstance().getOrDefault(defaultName);
        icon = Icon.savedDataSlot(defaultIcon);
        sound = AlertSound.savedDataSlot(AlertSound.SOUND_PRESETS[0], "sound");
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull Component text() {
        return Component.literal(name);
    }

    public @NotNull Component subText() {
        return Component.empty();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (this.name.equals(name)) return;
        this.name = name;
        update();
    }

    public int getColor() {
        return icon.getValue().getColorIndex();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (this.active == active) return;
        this.active = active;
        update();
    }

    public boolean isTested() {
        return tested;
    }

    public void setTested(boolean tested) {
        if (this.tested == tested) return;
        this.tested = tested;
        update();
    }

    public void setIcon(Icon icon) {
        if (this.icon.getValue().equals(icon)) return;
        this.icon.setValue(icon);
        update();
    }

    public Icon getIcon() {
        return icon.getValue();
    }

    public AlertSound getSound() {
        return sound.getValue();
    }

    public void setSound(AlertSound sound) {
        boolean equals = this.sound.getValue().equals(sound);
        if (equals) return;
        this.sound.setValue(sound);
        update();
    }

    public boolean isActiveOrTested() {
        return active || tested;
    }

    private void update() {
        detector.setChanged();
    }

    public boolean canEditName() {
        return true;
    }

    public boolean canEditIcon() {
        return true;
    }

    public boolean canEditSound() {
        return true;
    }

    public boolean canBeTested() {
        return true;
    }

    @Contract("_, _, _, _ -> new")
    public static @NotNull SavedDataSlot<Alert> savedDataSlot(Detector detector, String defaultName, Icon defaultIcon, String dataName) {
        return new SavedDataSlot<>(new Alert(detector, defaultIcon, defaultName)) {
            @Override
            public void load(CompoundTag compoundTag) {
                if (compoundTag.contains(dataName, Tag.TAG_COMPOUND)) {
                    CompoundTag tag = compoundTag.getCompound(dataName);

                    if (tag.contains("name", Tag.TAG_STRING))
                        getValue().name = tag.getString("name");

                    if (tag.contains("active"))
                        getValue().active = tag.getBoolean("active");

                    if (tag.contains("tested"))
                        getValue().tested = tag.getBoolean("tested");

                    getValue().icon.load(tag);
                    getValue().sound.load(tag);
                }
            }

            @Override
            public void save(CompoundTag compoundTag) {
                CompoundTag tag = new CompoundTag();
                tag.putString("name", getValue().name);
                tag.putBoolean("active", getValue().active);
                tag.putBoolean("tested", getValue().tested);
                getValue().sound.save(tag);
                getValue().icon.save(tag);
                compoundTag.put(dataName, tag);
            }
        };
    }
}
