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
package org.spongepowered.plugin.metadata.parser;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.spongepowered.plugin.metadata.PluginContributor;
import org.spongepowered.plugin.metadata.PluginDependency;
import org.spongepowered.plugin.metadata.PluginLinks;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class PluginMetadataAdapter extends TypeAdapter<PluginMetadata> {

    private final Gson gson;

    public PluginMetadataAdapter(final Gson gson) {
        this.gson = Objects.requireNonNull(gson);
    }

    @Override
    public void write(final JsonWriter out, final PluginMetadata value) throws IOException {
        Objects.requireNonNull(out);
        Objects.requireNonNull(value);

        out.beginObject();
        out.name("loader").value(value.loader());
        out.name("id").value(value.id());
        this.writeStringIfPresent(out, "name", value.name());
        out.name("version").value(value.version());
        out.name("main-class").value(value.mainClass());
        this.writeStringIfPresent(out, "description", value.description());
        this.writeLinks(out, value.links());
        this.writeContributors(out, value.contributors());
        this.writeDependencies(out, value.dependencies());
        this.writeExtraMetadata(out, value.extraMetadata());
        out.endObject();
    }

    @Override
    public PluginMetadata read(final JsonReader in) throws IOException {
        Objects.requireNonNull(in);

        in.beginObject();
        final Set<String> processedKeys = new HashSet<>();
        final PluginMetadata.Builder builder = PluginMetadata.builder();
        while (in.hasNext()) {
            final String key = in.nextName();
            if (!processedKeys.add(key)) {
                throw new JsonParseException("Duplicate key '" + key + "' in " + in);
            }

            switch (key) {
                case "loader":
                    builder.loader(in.nextString());
                    break;
                case "id":
                    builder.id(in.nextString());
                    break;
                case "name":
                    builder.name(in.nextString());
                    break;
                case "version":
                    builder.version(in.nextString());
                    break;
                case "main-class":
                    builder.mainClass(in.nextString());
                    break;
                case "description":
                    builder.description(in.nextString());
                    break;
                case "links":
                    this.readLinks(in, builder);
                    break;
                case "contributors":
                    this.readContributors(in, builder);
                    break;
                case "dependencies":
                    this.readDependencies(in, builder);
                    break;
                case "extra":
                    in.beginObject();
                    final Map<String, String> extraMetadata = new HashMap<>();
                    while (in.hasNext()) {
                        final String eKey = in.nextName();
                        final String eValue = in.nextString();
                        extraMetadata.put(eKey, eValue);
                    }
                    in.endObject();
                    // TODO Move Extra Metadata to String -> String
                    builder.extraMetadata((Map<String, Object>) (Object) extraMetadata);
            }
        }
        in.endObject();
        return builder.build();
    }

    private void readLinks(final JsonReader in, final PluginMetadata.Builder builder) throws IOException {
        Objects.requireNonNull(in);
        Objects.requireNonNull(builder);

        in.beginObject();
        final Set<String> processedKeys = new HashSet<>();
        final PluginLinks.Builder linksBuilder = PluginLinks.builder();
        while (in.hasNext()) {
            final String key = in.nextName();
            if (!processedKeys.add(key)) {
                throw new JsonParseException("Duplicate key '" + key + "' in " + in);
            }
            switch (key) {
                case "homepage":
                    linksBuilder.homepage(new URL(in.nextString()));
                    break;
                case "source":
                    linksBuilder.source(new URL(in.nextString()));
                    break;
                case "issues":
                    linksBuilder.issues(new URL(in.nextString()));
                    break;
            }
        }
        builder.links(linksBuilder.build());
        in.endObject();
    }

    private void readContributors(final JsonReader in, final PluginMetadata.Builder builder) throws IOException {
        Objects.requireNonNull(in);
        Objects.requireNonNull(builder);

        in.beginArray();
        while (in.hasNext()) {
            builder.addContributor(this.readContributor(in));
        }
        in.endArray();
    }

    private PluginContributor readContributor(final JsonReader in) throws IOException {
        Objects.requireNonNull(in);

        in.beginObject();
        final Set<String> processedKeys = new HashSet<>();
        final PluginContributor.Builder builder = PluginContributor.builder();
        while (in.hasNext()) {
            final String key = in.nextName();
            if (!processedKeys.add(key)) {
                throw new JsonParseException("Duplicate key '" + key + "' in " + in);
            }
            switch (key) {
                case "name":
                    builder.name(in.nextString());
                    break;
                case "description":
                    builder.description(in.nextString());
                    break;
            }
        }
        in.endObject();
        return builder.build();
    }

    private void readDependencies(final JsonReader in, final PluginMetadata.Builder builder) throws IOException {
        Objects.requireNonNull(in);
        Objects.requireNonNull(builder);

        in.beginArray();
        while (in.hasNext()) {
            builder.addDependency(this.readDependency(in));
        }
        in.endArray();
    }

    private PluginDependency readDependency(final JsonReader in) throws IOException {
        Objects.requireNonNull(in);

        in.beginObject();
        final Set<String> processedKeys = new HashSet<>();
        final PluginDependency.Builder builder = PluginDependency.builder();
        while (in.hasNext()) {
            final String key = in.nextName();
            if (!processedKeys.add(key)) {
                throw new JsonParseException("Duplicate key '" + key + "' in " + in);
            }
            switch (key) {
                case "id":
                    builder.id(in.nextString());
                    break;
                case "version":
                    builder.version(in.nextString());
                    break;
                case "optional":
                    builder.optional(in.nextBoolean());
                    break;
                case "load-order":
                    try {
                        builder.loadOrder(PluginDependency.LoadOrder.valueOf(in.nextString().toUpperCase()));
                    } catch (final Exception ex) {
                        throw new JsonParseException("Invalid load order found in " + in, ex);
                    }
                    break;
            }
        }
        in.endObject();
        return builder.build();
    }

    private void writeLinks(final JsonWriter out, final PluginLinks links) throws IOException {
        Objects.requireNonNull(out);
        Objects.requireNonNull(links);

        if (!links.homepage().isPresent() && !links.source().isPresent() && !links.issues().isPresent()) {
            return;
        }
        out.name("links").beginObject();
        this.writeURLIfPresent(out, "homepage", links.homepage());
        this.writeURLIfPresent(out, "source", links.source());
        this.writeURLIfPresent(out, "issues", links.issues());
        out.endObject();
    }

    private void writeContributors(final JsonWriter out, final List<PluginContributor> contributors) throws IOException {
        Objects.requireNonNull(out);
        Objects.requireNonNull(contributors);

        if (contributors.isEmpty()) {
            return;
        }
        out.name("contributors").beginArray();
        for (final PluginContributor contributor : contributors) {
            out.beginObject();
            out.name("name").value(contributor.name());
            this.writeStringIfPresent(out, "description", contributor.description());
            out.endObject();
        }
        out.endArray();
    }

    private void writeDependencies(final JsonWriter out, final List<PluginDependency> dependencies) throws IOException {
        Objects.requireNonNull(out);
        Objects.requireNonNull(dependencies);

        if (dependencies.isEmpty()) {
            return;
        }

        out.name("dependencies").beginArray();
        for (final PluginDependency dependency : dependencies) {
            out.beginObject();
            out.name("id").value(dependency.id());
            out.name("version").value(dependency.version());
            out.name("load-order").value(dependency.loadOrder().name());
            out.name("optional").value(dependency.optional());
            out.endObject();
        }

        out.endArray();
    }

    private void writeExtraMetadata(final JsonWriter out, final Map<String, Object> extraMetadata) throws IOException {
        Objects.requireNonNull(out);
        Objects.requireNonNull(extraMetadata);

        if (extraMetadata.isEmpty()) {
            return;
        }

        out.name("extra").beginObject();
        for (final Map.Entry<String, Object> entry : extraMetadata.entrySet()) {
            out.beginObject();
            // TODO Figure out how to serialize properly
            out.name(entry.getKey()).value(entry.getValue().toString());
            out.endObject();
        }
        out.endObject();
    }

    private void writeStringIfPresent(final JsonWriter out, final String name, final Optional<String> value) throws IOException {
        Objects.requireNonNull(out);
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);

        if (value.isPresent()) {
            out.name(name).value(value.get());
        }
    }

    private void writeURLIfPresent(final JsonWriter out, final String name, final Optional<URL> value) throws IOException {
        Objects.requireNonNull(out);
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);

        if (value.isPresent()) {
            out.name(name).value(value.get().toString());
        }
    }
}
