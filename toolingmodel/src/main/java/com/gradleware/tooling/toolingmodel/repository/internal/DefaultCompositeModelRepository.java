/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradleware.tooling.toolingmodel.repository.internal;

import java.util.List;

import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

import com.gradleware.tooling.toolingclient.Consumer;
import com.gradleware.tooling.toolingclient.ModelRequest;
import com.gradleware.tooling.toolingclient.ToolingClient;
import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace;
import com.gradleware.tooling.toolingmodel.repository.CompositeModelRepository;
import com.gradleware.tooling.toolingmodel.repository.EclipseWorkspaceUpdateEvent;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

public class DefaultCompositeModelRepository extends BaseCachingSimpleModelRepository implements CompositeModelRepository {

    private ImmutableList<FixedRequestAttributes> requestAttributes;

    public DefaultCompositeModelRepository(List<FixedRequestAttributes> requestAttributes, ToolingClient toolingClient, EventBus eventBus) {
        super(toolingClient, eventBus);
        Preconditions.checkArgument(requestAttributes.size() == 1, "Composite builds can currently contain exactly one project");
        this.requestAttributes = ImmutableList.copyOf(requestAttributes);
    }

    @Override
    public OmniEclipseWorkspace fetchEclipseWorkspace(TransientRequestAttributes transientAttributes, FetchStrategy fetchStrategy) {
        ModelRequest<EclipseProject> modelRequest = createModelRequest(EclipseProject.class, this.requestAttributes.get(0), transientAttributes);
        Consumer<OmniEclipseWorkspace> successHandler = new Consumer<OmniEclipseWorkspace>() {

            @Override
            public void accept(OmniEclipseWorkspace result) {
                postEvent(new EclipseWorkspaceUpdateEvent(result));
            }
        };
        Converter<EclipseProject, OmniEclipseWorkspace> converter = new BaseConverter<EclipseProject, OmniEclipseWorkspace>() {

            @Override
            public OmniEclipseWorkspace apply(EclipseProject eclipseProject) {
                return DefaultOmniEclipseWorkspace.from(DefaultOmniEclipseProject.from(eclipseProject, false));
            }
        };
        return executeRequest(modelRequest, successHandler, fetchStrategy, OmniEclipseWorkspace.class, converter);
    }

    private <T> ModelRequest<T> createModelRequest(Class<T> model, FixedRequestAttributes fixedAttributes, TransientRequestAttributes transientAttributes) {
        ModelRequest<T> request = getToolingClient().newModelRequest(model);
        fixedAttributes.apply(request);
        transientAttributes.apply(request);
        return request;
    }

}
