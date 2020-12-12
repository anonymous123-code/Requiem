/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.api.v1.remnant;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * @since 1.2.0
 */
public interface RemnantComponent extends AutoSyncedComponent {
    ComponentKey<RemnantComponent> KEY = ComponentRegistry.getOrCreate(new Identifier("requiem", "remnant"), RemnantComponent.class);

    static boolean isIncorporeal(Entity entity) {
        RemnantComponent r = KEY.getNullable(entity);
        return r != null && r.isIncorporeal();
    }

    static boolean isSoul(Entity entity) {
        RemnantComponent r = KEY.getNullable(entity);
        return r != null && r.isSoul();
    }

    /**
     * Return a player's {@link RemnantState}. The remnant state is live, and
     * every modification made to it is reflected on the player.
     *
     * @return the player's remnant state
     * @since 1.2.0
     */
    @Contract(pure = true)
    static RemnantComponent get(PlayerEntity player) {
        return KEY.get(player);
    }

    /**
     * Make this player become the given {@link RemnantType type of remnant}.
     * <p>
     * If the given remnant type is the same as the current one, this method
     * does not have any visible effect. Otherwise, it will reset the current state,
     * replace it with a new one of the given type, and notify players of the change.
     * <p>
     * After this method has been called, the {@code RemnantType} returned by {@link #getRemnantType()}
     * will be {@code type}.
     *
     * @param type the remnant type to become
     * @see #getRemnantType()
     * @since 1.2.0
     */
    void become(RemnantType type);

    RemnantType getRemnantType();

    /**
     * Return whether this player is currently incorporeal.
     * A player is considered incorporeal if its current corporeality
     * is not tangible and they have no surrogate body.
     * @return true if the player is currently incorporeal, {@code false} otherwise
     */
    boolean isIncorporeal();

    boolean isSoul();

    void setSoul(boolean incorporeal);

    /**
     * Called when this remnant state's player is cloned
     *
     * @param original the player's clone
     * @param lossless false if the original player is dead, true otherwise
     */
    void prepareRespawn(ServerPlayerEntity original, boolean lossless);

    void setDefaultRemnantType(@Nullable RemnantType defaultType);

    @Nullable RemnantType getDefaultRemnantType();
}
