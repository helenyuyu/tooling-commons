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

package com.gradleware.tooling.toolingclient;
/**
 * A {@code CompositeModelRequest} allows you to fetch a snapshot of some model for a composite build. Instances of {@code CompositeModelRequest} are not thread-safe. <p> You use a {@code
 * CompositeModelRequest} as follows: <ul> <li>Create an instance of {@code CompositeModelRequest} by calling {@link ToolingClient#newCompositeModelRequest(Class)}. <li>Configure the request as appropriate.
 * <li>Call either {@link #executeAndWait()} or {@link #execute()} to fetch the model. <li>Optionally, you can reuse the request to fetch the model multiple times. </ul>
 *
 * @author Stefan Oehme
 * @param <T> the type of model to fetch
 */
public interface CompositeModelRequest<T> {
    ConnectionDescriptor addProject();

    /**
     * Fetches the requested model synchronously. Calling this method will block until the model has been fetched or a failure has occurred.
     *
     * @return the requested model
     * @see org.gradle.tooling.ModelBuilder#get()
     */
    T executeAndWait();

    /**
     * Fetches the requested model asynchronously. Calling this method will return immediately. The returned promise is used to configure the success and failure behavior.
     *
     * @return the promise of the requested model
     * @see org.gradle.tooling.ModelBuilder#get(org.gradle.tooling.ResultHandler)
     */
    LongRunningOperationPromise<T> execute();
}
