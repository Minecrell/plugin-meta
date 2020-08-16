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

import static java.util.Arrays.asList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.plugin.meta.gson.ModMetadataAdapter;
import org.spongepowered.plugin.meta.gson.ModMetadataCollectionAdapter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a serializer for {@link PluginMetadata} for the
 * {@code mcmod.info} file format.
 */
public final class McModInfo {

    /**
     * The file name the metadata is usually saved in.
     */
    public static final String STANDARD_FILENAME = "mcmod.info";

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final String INDENT = "    ";

    /**
     * The default serializer that converts all additional properties into
     * standard Java types.
     */
    public static final McModInfo DEFAULT = new McModInfo(ModMetadataCollectionAdapter.DEFAULT);

    private final ModMetadataCollectionAdapter adapter;

    private McModInfo(ModMetadataCollectionAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Deserializes the specified JSON string into a {@link List} of
     * {@link PluginMetadata}.
     *
     * @param json The JSON string
     * @return The deserialized metadata list
     */
    public List<PluginMetadata> fromJson(String json) {
        try {
            return this.adapter.fromJson(json);
        } catch (IOException e) {
            throw new JsonIOException(e);
        }
    }

    /**
     * Reads a {@link List} of {@link PluginMetadata} from the file represented
     * by the specified {@link Path}.
     *
     * @param path The path to the file
     * @return The deserialized metadata list
     * @throws IOException If an error occurs while reading
     */
    public List<PluginMetadata> read(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, CHARSET)) {
            return read(reader);
        }
    }

    /**
     * Reads a {@link List} of {@link PluginMetadata} from the given
     * {@link InputStream}.
     *
     * @param in The input stream
     * @return The deserialized metadata list
     * @throws IOException If an error occurs while reading
     */
    public List<PluginMetadata> read(InputStream in) throws IOException {
        return this.adapter.fromJson(new BufferedReader(new InputStreamReader(in, CHARSET)));
    }

    /**
     * Reads a {@link List} of {@link PluginMetadata} from the given
     * {@link Reader}.
     *
     * @param reader The reader
     * @return The deserialized metadata list
     * @throws IOException If an error occurs while reading
     */
    public List<PluginMetadata> read(Reader reader) throws IOException {
        return this.adapter.fromJson(reader);
    }

    /**
     * Reads a {@link List} of {@link PluginMetadata} from the given
     * {@link JsonReader}.
     *
     * @param reader The JSON reader
     * @return The deserialized metadata list
     * @throws IOException If an error occurs while reading
     */
    public List<PluginMetadata> read(JsonReader reader) throws IOException {
        return this.adapter.read(reader);
    }

    /**
     * Serializes the specified {@link PluginMetadata} to a JSON string.
     *
     * @param meta The plugin metadata to serialize
     * @return The serialized JSON string
     */
    public String toJson(PluginMetadata... meta) {
        return toJson(asList(meta));
    }

    /**
     * Serializes the specified {@link List} of {@link PluginMetadata} to a
     * JSON string.
     *
     * @param meta The plugin metadata to serialize
     * @return The serialized JSON string
     */
    public String toJson(List<PluginMetadata> meta) {
        StringWriter writer = new StringWriter();
        try {
            write(writer, meta);
        } catch (IOException e) {
            throw new JsonIOException(e);
        }
        return writer.toString();
    }

    /**
     * Writes the specified {@link PluginMetadata} to the file represented by
     * the {@link Path}.
     *
     * @param path The path to the file to write to
     * @param meta The plugin metadata to serialize
     * @throws IOException If an error occurs while writing
     */
    public void write(Path path, PluginMetadata... meta) throws IOException {
        write(path, asList(meta));
    }

    /**
     * Writes the specified {@link List} of {@link PluginMetadata} to the file
     * represented by the {@link Path}.
     *
     * @param path The path to the file to write to
     * @param meta The plugin metadata to serialize
     * @throws IOException If an error occurs while writing
     */
    public void write(Path path, List<PluginMetadata> meta) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, CHARSET)) {
            write(writer, meta);
        }
    }

    /**
     * Writes the specified {@link PluginMetadata} to the {@link Writer}.
     *
     * @param writer The writer
     * @param meta The plugin metadata to serialize
     * @throws IOException If an error occurs while writing
     */
    public void write(Writer writer, PluginMetadata... meta) throws IOException {
        write(writer, asList(meta));
    }

    /**
     * Writes the specified {@link List} of {@link PluginMetadata} to the
     * {@link Writer}.
     *
     * @param writer The writer
     * @param meta The plugin metadata to serialize
     * @throws IOException If an error occurs while writing
     */
    public void write(Writer writer, List<PluginMetadata> meta) throws IOException {
        try (JsonWriter json = new JsonWriter(writer)) {
            json.setIndent(INDENT);
            write(json, meta);
            writer.write('\n'); // Add new line at the end of the file
        }
    }

    /**
     * Writes the specified {@link PluginMetadata} to the {@link JsonWriter}.
     *
     * @param writer The JSON writer
     * @param meta The plugin metadata to serialize
     * @throws IOException If an error occurs while writing
     */
    public void write(JsonWriter writer, PluginMetadata... meta) throws IOException {
        write(writer, asList(meta));
    }

    /**
     * Writes the specified {@link List} of {@link PluginMetadata} to the
     * {@link JsonWriter}.
     *
     * @param writer The JSON writer
     * @param meta The plugin metadata to serialize
     * @throws IOException If an error occurs while writing
     */
    public void write(JsonWriter writer, List<PluginMetadata> meta) throws IOException {
        this.adapter.write(writer, meta);
    }

    /**
     * Creates a new {@link Builder} that can be used to construct a
     * {@link McModInfo} serializer that serializes specific extension keys
     * to their Java object representation.
     *
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Represents a builder for {@link McModInfo} serializes that can be used
     * to construct a serializer that serializes specific extension keys to
     * their Java object representation.
     */
    public static final class Builder {

        private final GsonBuilder gson = new GsonBuilder();
        private final Map<String, Class<?>> extensions = new HashMap<>();

        private Builder() {
        }

        /**
         * Returns the {@link GsonBuilder} that will be used to build this
         * serializer's {@link Gson} instance. Modify the builder if you
         * need to set custom options to make your extension serializer
         * work properly.
         *
         * @return The GSON builder
         */
        public GsonBuilder gson() {
            return this.gson;
        }

        /**
         * Registers an extension with the given key and the specified
         * extension class. By default, GSON will serializer the extension
         * class using a representation of the public fields.
         *
         * @param key The key of the extension
         * @param extensionClass The class to serialize the extension to
         * @return This builder instance
         */
        public Builder registerExtension(String key, Class<?> extensionClass) {
            this.extensions.put(key, extensionClass);
            return this;
        }

        /**
         * Registers an extension with the given key, extension class and a
         * custom type adapter to use for serializing the extension class.
         *
         * @param key The key of the extension
         * @param extensionClass The class to serialize the extension to
         * @param typeAdapter The type adapter to use for serializing
         * @return This builder instance
         *
         * @see GsonBuilder#registerTypeAdapter(Type, Object)
         */
        public Builder registerExtension(String key, Class<?> extensionClass, Object typeAdapter) {
            registerExtension(key, extensionClass);
            this.gson.registerTypeAdapter(extensionClass, typeAdapter);
            return this;
        }

        /**
         * Builds the {@link McModInfo} serializer using the specified options.
         *
         * @return The built serializer
         */
        public McModInfo build() {
            return new McModInfo(new ModMetadataCollectionAdapter(new ModMetadataAdapter(this.gson.create(), this.extensions)));
        }

    }

}
