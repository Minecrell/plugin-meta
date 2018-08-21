/*
 * This file is part of plugin-meta, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.plugin.meta.velocity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.spongepowered.plugin.meta.PluginDependency;
import org.spongepowered.plugin.meta.PluginMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class VelocityPluginMetadataTest {

    @Test
    public void parseVelocityPluginMetadata() throws IOException {
        PluginMetadata testPlugin;
        try (InputStream in = getClass().getResourceAsStream("velocity-plugin1.json")) {
            testPlugin = VelocityPluginMetadata.DEFAULT.read(in);
        }

        assertEquals("testplugin", testPlugin.getId());
        assertEquals("1.0-SNAPSHOT", testPlugin.getVersion());
        assertEquals(Collections.singletonList("Minecrell"), testPlugin.getAuthors());

        Collection<PluginDependency> dependencies = new HashSet<>(testPlugin.getDependencies());
        assertEquals(2, dependencies.size());

        assertEquals(new HashSet<>(Arrays.asList(
                new PluginDependency(PluginDependency.LoadOrder.AFTER, "optionaldependency", null, true),
                new PluginDependency(PluginDependency.LoadOrder.AFTER, "requireddependency", null, false)
        )), dependencies);
    }

}
