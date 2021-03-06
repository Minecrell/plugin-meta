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
package org.spongepowered.plugin.meta;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class PluginMetadataTest {
    @Test
    void testIdPattern() {
        // must be at least 2 characters
        Assertions.assertFalse(PluginMetadata.ID_PATTERN.matcher("a").matches());

        Assertions.assertTrue(PluginMetadata.ID_PATTERN.matcher("ab").matches());
        Assertions.assertTrue(PluginMetadata.ID_PATTERN.matcher("a0").matches());

        // must start with a-z
        Assertions.assertFalse(PluginMetadata.ID_PATTERN.matcher("0a").matches());

        // cannot contain uppercase characters
        Assertions.assertFalse(PluginMetadata.ID_PATTERN.matcher("Ab").matches());
        Assertions.assertFalse(PluginMetadata.ID_PATTERN.matcher("aB").matches());

        // up to 64 characters
        Assertions.assertTrue(PluginMetadata.ID_PATTERN.matcher(String.join("", Collections.nCopies(64, "a"))).matches());
        // but no longer
        Assertions.assertFalse(PluginMetadata.ID_PATTERN.matcher(String.join("", Collections.nCopies(65, "a"))).matches());
    }
}
