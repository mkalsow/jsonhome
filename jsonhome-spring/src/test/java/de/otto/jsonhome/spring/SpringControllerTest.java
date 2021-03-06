/*
 * Copyright 2012 Guido Steinacker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.otto.jsonhome.spring;

import de.otto.jsonhome.controller.JsonHomeController;
import de.otto.jsonhome.fixtures.spring.TestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Guido Steinacker
 * @since 18.12.12
 */
@ContextConfiguration(locations = "classpath:/testSpringContext.xml")
public class SpringControllerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private TestController controller;

    @Autowired
    private JsonHomeController jsonHomeController;

    @Test
    public void controllerUsedForTestShouldExistAndWork() throws Exception {
        assertEquals(controller.getAFoo("42"), "42");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldFindJsonHomeWithAspects() {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final Map<String,?> json = jsonHomeController.getAsApplicationJson(response);
        final Map<String, Map<String, ?>> resources = (Map<String, Map<String, ?>>) json.get("resources");
        assertNotNull(resources);
        final Map<String, ?> fooResource = resources.get("http://specs.example.org/rel/foo");
        assertNotNull(fooResource);
        final Map<String, String> hrefVars = (Map<String, String>) fooResource.get("href-vars");
        assertEquals(hrefVars.get("fooId"), "http://specs.example.org/rel/foo#fooId");
        assertEquals(fooResource.get("href-template"), "http://example.org/{fooId}");
    }


}
