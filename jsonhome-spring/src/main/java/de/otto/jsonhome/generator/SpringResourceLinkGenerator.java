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

package de.otto.jsonhome.generator;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.net.URI;

import static de.otto.jsonhome.generator.UriBuilder.normalized;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

/**
 * A Spring-based ResourceLinkGenerator.
 *
 * Used to analyse Spring MVC controllers.
 *
 * @author Guido Steinacker
 * @since 17.10.12
 */
public class SpringResourceLinkGenerator extends ResourceLinkGenerator {

    private final URI applicationBaseUri;

    /**
     * Creates a SpringResourceLinkGenerator.
     *
     * @param applicationBaseUri the base URI used to create href and href-template URIs.
     * @param relationTypeBaseUri the base URI used to create absolute relation-type URIs.
     * @param varTypeBaseUri the base URI used to create var-types for href-vars. May be null.
     * @param docRootDir the root classpath directory containing Markdown documents. May be null.
     */
    public SpringResourceLinkGenerator(final URI applicationBaseUri,
                                       final URI relationTypeBaseUri,
                                       final URI varTypeBaseUri,
                                       final String docRootDir) {
        super(
                applicationBaseUri,
                relationTypeBaseUri,
                varTypeBaseUri,
                new SpringHintsGenerator(relationTypeBaseUri, docRootDir),
                new SpringHrefVarsGenerator(relationTypeBaseUri, docRootDir)
        );
        this.applicationBaseUri = applicationBaseUri;
    }

    /**
     * {@inheritDoc}
     *
     * A method is a candidate if there is a RequestMapping annotation.
     *
     * @param method the current method of the controller.
     * @return true if method should be analysed
     */
    @Override
    public boolean isCandidateForAnalysis(final Method method) {
        return AnnotationUtils.findAnnotation(method, RequestMapping.class) != null;
    }

    @Override
    protected String resourcePathFor(final Method method) {
        if (isCandidateForAnalysis(method)) {
            final RequestMapping methodRequestMapping = findAnnotation(method, RequestMapping.class);
            final String resourcePathPrefix = parentResourcePathsFrom(method.getDeclaringClass());
            final String resourcePathSuffix = methodRequestMapping.value().length > 0
                    ? methodRequestMapping.value()[0]
                    : "";

            return normalized(applicationBaseUri)
                    .withPathSegments(resourcePathPrefix, resourcePathSuffix)
                    .toString() + queryTemplateFrom(method);
        } else {
            return null;
        }
    }

    /**
     * Analyses the controller (possibly annotated with RequestMapping) and returns the list of resource paths defined by the mapping.
     *
     * @param controller the controller.
     * @return list of resource paths.
     */
    protected String parentResourcePathsFrom(final Class<?> controller) {
        final RequestMapping controllerRequestMapping = findAnnotation(controller, RequestMapping.class);
        final String firstResourcePathPrefix;
        if (controllerRequestMapping != null && controllerRequestMapping.value().length > 0) {
            firstResourcePathPrefix = controllerRequestMapping.value()[0];
        } else {
            firstResourcePathPrefix = "";
        }
        return firstResourcePathPrefix;
    }

}
