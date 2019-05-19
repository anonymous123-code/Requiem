/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.client.gui;

import com.google.common.collect.ImmutableList;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.annotation.Unlocalized;
import ladysnake.requiem.api.v1.dialogue.ChoiceResult;
import ladysnake.requiem.api.v1.dialogue.CutsceneDialogue;
import ladysnake.requiem.client.ZaWorldFx;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.menu.YesNoScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class CutsceneDialogueScreen extends Screen {
    public static final int MIN_RENDER_Y = 40;
    public static final int TITLE_GAP = 20;
    public static final int CHOICE_GAP = 5;
    private final CutsceneDialogue dialogue;
    private int selectedChoice;
    private boolean hoveringChoice;
    public static final int MAX_TEXT_WIDTH = 300;

    public CutsceneDialogueScreen(Component title, CutsceneDialogue dialogue) {
        super(title);
        this.dialogue = dialogue;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (hoveringChoice) {
            confirmChoice(selectedChoice);
        }
        return true;
    }

    private ChoiceResult confirmChoice(int selectedChoice) {
        ChoiceResult result = this.dialogue.choose(this.dialogue.getCurrentChoices().get(selectedChoice));
        if (result == ChoiceResult.END_DIALOGUE) {
            Objects.requireNonNull(this.minecraft).openScreen(null);
            ((RequiemPlayer)this.minecraft.player).getDialogueTracker().endDialogue();
            ((RequiemPlayer) this.minecraft.player).getDeathSuspender().setLifeTransient(false);
        } else if (result == ChoiceResult.ASK_CONFIRMATION) {
            ImmutableList<String> choices = this.dialogue.getCurrentChoices();
            this.minecraft.openScreen(new YesNoScreen(
                    this::onBigChoiceMade,
                    new TranslatableComponent(this.dialogue.getCurrentText()),
                    new TextComponent(""),
                    I18n.translate(choices.get(0)),
                    I18n.translate(choices.get(1))
            ));
        } else {
            this.selectedChoice = 0;
        }
        return result;
    }

    private void onBigChoiceMade(boolean yes) {
        if (this.confirmChoice(yes ? 0 : 1) == ChoiceResult.DEFAULT) {
            this.minecraft.openScreen(this);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int int_2, int int_3) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            confirmChoice(selectedChoice);
            return true;
        }
        return super.keyPressed(keyCode, int_2, int_3);
    }

    @Override
    public boolean changeFocus(boolean shiftPressed) {
        this.selectedChoice = Math.floorMod(this.selectedChoice + (shiftPressed ? 1 : -1), this.dialogue.getCurrentChoices().size());
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        this.selectedChoice = Math.floorMod((int) (this.selectedChoice - scrollAmount), this.dialogue.getCurrentChoices().size());
        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        ImmutableList<@Unlocalized String> choices = this.dialogue.getCurrentChoices();
        String title = I18n.translate(this.dialogue.getCurrentText());
        int y = MIN_RENDER_Y + this.font.getStringBoundedHeight(title, MAX_TEXT_WIDTH) + TITLE_GAP;
        for (int i = 0; i < choices.size(); i++) {
            String choice = I18n.translate(choices.get(i));
            int strHeight = this.font.getStringBoundedHeight(choice, width);
            int strWidth = strHeight == 9 ? this.font.getStringWidth(choice) : width;
            if (mouseX < strWidth && mouseY > y && mouseY < y + strHeight) {
                this.selectedChoice = i;
                this.hoveringChoice = true;
                return;
            }
            y += strHeight + CHOICE_GAP;
            this.hoveringChoice = false;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        if (!ZaWorldFx.INSTANCE.hasFinishedAnimation()) {
            return;
        }
        this.renderBackground();
        int y = MIN_RENDER_Y;
        String title = I18n.translate(this.dialogue.getCurrentText());
        this.font.drawStringBounded(title, 10, y, MAX_TEXT_WIDTH, 0xFFFFFF);
        y += this.font.getStringBoundedHeight(title, MAX_TEXT_WIDTH) + TITLE_GAP;
        ImmutableList<String> choices = this.dialogue.getCurrentChoices();
        for (int i = 0; i < choices.size(); i++) {
            String choice = I18n.translate(choices.get(i));
            int strHeight = this.font.getStringBoundedHeight(choice, MAX_TEXT_WIDTH);
            this.font.drawStringBounded(choice, 10, y, MAX_TEXT_WIDTH, i == this.selectedChoice ? 0xE0E044 : 0xA0A0A0);
            y += strHeight + CHOICE_GAP;
        }
        String tip = I18n.translate("requiem:dialogue.instructions");
        this.font.draw(tip, (this.width - font.getStringWidth(tip)) * 0.5f, this.height - 30, 0x808080);
        super.render(mouseX, mouseY, tickDelta);
    }
}
