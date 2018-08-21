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
package org.spongepowered.plugin.meta.velocity.gson;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.spongepowered.plugin.meta.PluginDependency;
import org.spongepowered.plugin.meta.PluginMetadata;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class VelocityPluginMetadataAdapter extends TypeAdapter<PluginMetadata> {

    public static final VelocityPluginMetadataAdapter DEFAULT = new VelocityPluginMetadataAdapter(new Gson(), ImmutableMap.of());

    private final Gson gson;
    private final ImmutableMap<String, Class<?>> extensions;

    public VelocityPluginMetadataAdapter(Gson gson, ImmutableMap<String, Class<?>> extensions) {
        this.gson = gson;
        this.extensions = extensions;
    }

    public Gson getGson() {
        return this.gson;
    }

    public ImmutableMap<String, Class<?>> getExtensions() {
        return this.extensions;
    }

    public Class<?> getExtension(String key) {
        Class<?> result = this.extensions.get(key);
        return result != null ? result : Object.class;
    }

    @Override
    public PluginMetadata read(JsonReader in) throws IOException {
        in.beginObject();

        final Set<String> processedKeys = new HashSet<>();

        final PluginMetadata result = new PluginMetadata("unknown");
        String id = null;

        while (in.hasNext()) {
            final String name = in.nextName();
            if (!processedKeys.add(name)) {
                throw new JsonParseException("Duplicate key '" + name + "' in " + in);
            }

            switch (name) {
                case "id":
                    id = in.nextString();
                    result.setId(id);
                    break;
                case "name":
                    result.setName(in.nextString());
                    break;
                case "version":
                    result.setVersion(in.nextString());
                    break;
                case "description":
                    result.setDescription(in.nextString());
                    break;
                case "url":
                    result.setUrl(in.nextString());
                    break;
                case "authors":
                    in.beginArray();
                    while (in.hasNext()) {
                        result.addAuthor(in.nextString());
                    }
                    in.endArray();
                    break;
                case "dependencies":
                    in.beginArray();

                    while (in.hasNext()) {
                        result.addDependency(readDependency(in));
                    }
                    in.endArray();
                    break;
                case "main":
                    // TODO: Consider exposing this directly on PluginMetadata
                    // or using a proper extension
                default:
                    result.setExtension(name, this.gson.fromJson(in, getExtension(name)));
            }
        }

        in.endObject();

        if (id == null) {
            throw new JsonParseException("Plugin metadata is missing required element 'id'");
        }

        return result;
    }

    private static PluginDependency readDependency(JsonReader in) throws IOException {
        in.beginObject();

        String id = null;
        boolean optional = false;

        while (in.hasNext()) {
            switch (in.nextName()) {
                case "id":
                    id = in.nextString();
                    break;
                case "optional":
                    optional = in.nextBoolean();
                    break;
                default:
                    in.skipValue();
            }
        }

        in.endObject();

        if (id == null) {
            throw new JsonParseException("Plugin dependency is missing required element 'id'");
        }

        return new PluginDependency(PluginDependency.LoadOrder.AFTER, id, null, optional);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void write(JsonWriter out, PluginMetadata meta) throws IOException {
        out.beginObject();
        out.name("id").value(meta.getId());
        writeIfPresent(out, "name", meta.getName());
        writeIfPresent(out, "version", meta.getVersion());
        writeIfPresent(out, "description", meta.getDescription());
        writeIfPresent(out, "url", meta.getUrl());

        if (!meta.getAuthors().isEmpty()) {
            out.name("authors").beginArray();
            for (String author : meta.getAuthors()) {
                out.value(author);
            }
            out.endArray();
        }

        if (!meta.getDependencies().isEmpty()) {
            out.name("dependencies").beginArray();
            for (PluginDependency dependency : meta.getDependencies()) {
                if (dependency.getLoadOrder() != PluginDependency.LoadOrder.BEFORE) {
                    out.beginObject();
                    out.name("id").value(dependency.getId());
                    out.name("optional").value(dependency.isOptional());
                    out.endObject();
                }
            }
            out.endArray();
        }

        for (Map.Entry<String, Object> entry : meta.getExtensions().entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            out.name(key);
            this.gson.toJson(value, getExtension(key), out);
        }

        out.endObject();
    }

    private static void writeIfPresent(JsonWriter out, String key, @Nullable String value) throws IOException {
        if (value != null) {
            out.name(key).value(value);
        }
    }

}
