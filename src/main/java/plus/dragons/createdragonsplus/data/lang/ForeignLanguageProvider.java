/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.data.lang;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class ForeignLanguageProvider implements DataProvider {
    private final String modId;
    private final String templateLocale;
    private final PackOutput.PathProvider langPathProvider;
    private final Path translationDirectory;

    public ForeignLanguageProvider(String modId, String templateLocale, PackOutput output, Path translationRoot) {
        this.modId = modId;
        this.templateLocale = templateLocale;
        this.langPathProvider = output.createPathProvider(Target.RESOURCE_PACK, "lang");
        this.translationDirectory = translationRoot.resolve("assets").resolve(modId).resolve("lang");
    }

    private JsonObject readLocalization(Path path) {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return GsonHelper.parse(reader);
        } catch (IOException exception) {
            throw new JsonIOException("Failed to read localization file " + path, exception);
        }
    }

    private List<Path> getForeignLocalizationFiles() {
        try (Stream<Path> paths = Files.list(translationDirectory)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .filter(path -> !path.getFileName().toString().equals(templateLocale + ".json"))
                    .sorted()
                    .toList();
        } catch (IOException exception) {
            throw new CompletionException("Failed to list localization files in " + translationDirectory, exception);
        }
    }

    private JsonObject combine(JsonObject template, JsonObject foreign) {
        JsonObject result = new JsonObject();
        Map<String, JsonElement> unlocalized = new LinkedHashMap<>();
        for (var entry : template.entrySet()) {
            String key = entry.getKey();
            if (foreign.has(key))
                result.add(key, foreign.get(key));
            else
                unlocalized.put(key, entry.getValue());
        }
        if (!unlocalized.isEmpty()) {
            result.addProperty("_comment.unlocalized", "Remove this line after finishing localization.");
            unlocalized.forEach(result::add);
        }
        return result;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Path templatePath = langPathProvider.json(new ResourceLocation(modId, templateLocale));
        return CompletableFuture.supplyAsync(() -> readLocalization(templatePath), Util.backgroundExecutor())
                .thenCompose(template -> {
                    CompletableFuture<?>[] writes = getForeignLocalizationFiles().stream()
                            .map(path -> CompletableFuture
                                    .supplyAsync(() -> readLocalization(path), Util.backgroundExecutor())
                                    .thenCompose(foreign -> {
                                        String fileName = path.getFileName().toString();
                                        String locale = fileName.substring(0, fileName.length() - ".json".length());
                                        Path outputPath = langPathProvider.json(new ResourceLocation(modId, locale));
                                        return DataProvider.saveStable(output, combine(template, foreign), outputPath);
                                    }))
                            .toArray(CompletableFuture[]::new);
                    return CompletableFuture.allOf(writes);
                });
    }

    @Override
    public String getName() {
        return "Foreign Languages: " + modId;
    }
}
