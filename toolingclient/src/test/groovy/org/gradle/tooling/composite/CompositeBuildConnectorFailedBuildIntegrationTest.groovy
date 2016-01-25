/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.tooling.composite

import org.gradle.tooling.composite.fixtures.ExternalDependencies

class CompositeBuildConnectorFailedBuildIntegrationTest extends AbstractCompositeBuildConnectorIntegrationTest {

    def "cannot create composite with no participating projects"() {
        when:
        createComposite()

        then:
        Throwable t = thrown(IllegalStateException)
        t.message == "A composite build requires at least one participating project."
    }

    def "cannot request model that is not an interface"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        createBuildFileWithDependency(projectDir, ExternalDependencies.COMMONS_LANG)

        when:
        CompositeBuildConnection compositeBuildConnection = createComposite(projectDir)
        compositeBuildConnection.getModels(String)

        then:
        Throwable t = thrown(IllegalArgumentException)
        t.message == "Cannot fetch a model of type 'java.lang.String' as this type is not an interface."
    }

    def "cannot request model for unknown model"() {
        given:
        File projectDir = directoryProvider.createDir('project')
        createBuildFileWithDependency(projectDir, ExternalDependencies.COMMONS_LANG)

        when:
        CompositeBuildConnection compositeBuildConnection = createComposite(projectDir)
        compositeBuildConnection.getModels(List)

        then:
        Throwable t = thrown(IllegalArgumentException)
        t.message == "The only supported model for a Gradle composite is EclipseProject.class."
    }
}
