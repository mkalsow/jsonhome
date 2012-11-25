/**
 Copyright 2012 Guido Steinacker

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package de.otto.jsonhome.registry;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.testng.Assert.*;

/**
 * @author Guido Steinacker
 * @since 14.11.12
 */
public class FileSystemRegistryTest {

    private File file = null;

    @BeforeMethod
    public void beforeMethod() throws IOException {
        file = new File(System.getProperty("user.home") + "/jsonhome/jsonhome" + System.currentTimeMillis(), ".registry");
        file.deleteOnExit();
    }

    @Test
    public void shouldCreateJsonHomeDirOnStartup() throws IOException {
        // given
        // when
        final FileSystemRegistry registry = new FileSystemRegistry(file);
        // then
        assertTrue(file.exists());
    }

    @Test
    public void shouldCreateRegistryFileOnStartup() throws IOException {
        // given
        // when
        final FileSystemRegistry registry = new FileSystemRegistry(file);
        // then
        final Map map = readRegistryAsMap();
        assertTrue(map.containsKey("registry"));
        assertEquals(map.get("registry"), emptyMap());
    }

    @Test
    public void shouldRegisterNewRegistryEntry() throws IOException {
        // given
        final FileSystemRegistry registry = new FileSystemRegistry(file);
        final RegistryEntry entry = new RegistryEntry(
                URI.create("http://example.org/registry/42"),
                "foo",
                URI.create("http://example.org/foo/json-home")
        );
        // when
        registry.put(entry);
        // then
        assertEquals(registry.findBy(entry.getSelf()), entry);
        final Map map = readRegistryAsMap();
        final Map registryMap = (Map) map.get("registry");
        final Map entryMap = (Map) registryMap.get("http://example.org/registry/42");
        assertNotNull(registryMap);
        assertNotNull(entryMap);
        assertEquals(entryMap.size(), 2);
        assertEquals(entryMap.get("title"), entry.getTitle());
        assertEquals(entryMap.get("href"), entry.getHref().toString());
    }

    @Test
    public void shouldUnregisterRegistryEntry() throws IOException {
        // given
        final FileSystemRegistry registry = new FileSystemRegistry(file);
        final RegistryEntry entry = new RegistryEntry(
                URI.create("http://example.org/registry/42"),
                "foo",
                URI.create("http://example.org/foo/json-home")
        );
        registry.put(entry);
        // when
        registry.remove(entry.getSelf());
        // then
        assertNull(registry.findBy(entry.getSelf()));
        assertTrue(registry.getAll().isEmpty());
    }

    private Map readRegistryAsMap() throws IOException {
        final JsonFactory factory = new JsonFactory(new ObjectMapper());
        final JsonParser jsonParser = factory.createJsonParser(file);
        try {
            return jsonParser.readValueAs(Map.class);
        } finally {
            jsonParser.close();
        }
    }
}