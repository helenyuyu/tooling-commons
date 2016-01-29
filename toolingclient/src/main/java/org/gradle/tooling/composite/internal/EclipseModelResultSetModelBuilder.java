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

package org.gradle.tooling.composite.internal;

import org.gradle.api.Transformer;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.event.ListenerNotificationException;
import org.gradle.tooling.*;
import org.gradle.tooling.composite.ModelResult;
import org.gradle.tooling.exceptions.UnsupportedBuildArgumentException;
import org.gradle.tooling.exceptions.UnsupportedOperationConfigurationException;
import org.gradle.tooling.internal.consumer.AbstractLongRunningOperation;
import org.gradle.tooling.internal.consumer.ConnectionParameters;
import org.gradle.tooling.internal.consumer.async.AsyncConsumerActionExecutor;
import org.gradle.tooling.internal.consumer.connection.ConsumerAction;
import org.gradle.tooling.internal.consumer.connection.ConsumerConnection;
import org.gradle.tooling.internal.consumer.parameters.ConsumerOperationParameters;
import org.gradle.tooling.internal.protocol.BuildExceptionVersion1;
import org.gradle.tooling.internal.protocol.InternalBuildCancelledException;
import org.gradle.tooling.internal.protocol.ResultHandlerVersion1;
import org.gradle.tooling.internal.protocol.exceptions.InternalUnsupportedBuildArgumentException;
import org.gradle.tooling.internal.protocol.test.InternalTestExecutionException;
import org.gradle.tooling.model.UnsupportedMethodException;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.internal.Exceptions;
import org.gradle.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Model builder for a Set of Eclipse ModelResults.
 *
 * @param <T>
 * @author Benjamin Muschko
 */
public class EclipseModelResultSetModelBuilder<T> extends AbstractLongRunningOperation<EclipseModelResultSetModelBuilder<T>> implements ModelBuilder<Set<ModelResult<T>>> {
    private final Class<T> modelType;
    private final AsyncConsumerActionExecutor connection;
    private final CompositeModelProducer<EclipseProject> compositeModelProducer;

    public EclipseModelResultSetModelBuilder(Class<T> modelType, AsyncConsumerActionExecutor connection,
                                             ConnectionParameters parameters, Set<ProjectConnection> participants) {
        super(parameters);
        this.modelType = modelType;
        this.connection = connection;
        this.compositeModelProducer = new EclipseProjectCompositeModelProducer(participants);
        this.operationParamsBuilder.setEntryPoint("Eclipse ModelBuilder API");
    }

    @Override
    protected EclipseModelResultSetModelBuilder<T> getThis() {
        return this;
    }

    @Override
    public ModelBuilder<Set<ModelResult<T>>> forTasks(String... tasks) {
        List<String> rationalizedTasks = rationalizeInput(tasks);
        this.operationParamsBuilder.setTasks(rationalizedTasks);
        return this;
    }

    @Override
    public ModelBuilder<Set<ModelResult<T>>> forTasks(Iterable<String> tasks) {
        this.operationParamsBuilder.setTasks(rationalizeInput(tasks));
        return this;
    }

    @Override
    public Set<ModelResult<T>> get() throws GradleConnectionException, IllegalStateException {
        BlockingResultHandler<T> handler = new BlockingResultHandler<T>();
        get(handler);
        return handler.getResult();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void get(final ResultHandler<? super Set<ModelResult<T>>> handler) throws IllegalStateException {
        final ConsumerOperationParameters operationParameters = getConsumerOperationParameters();
        this.connection.run(new ConsumerAction<T>() {
            @Override
            public ConsumerOperationParameters getParameters() {
                return operationParameters;
            }

            @Override
            public T run(ConsumerConnection connection) {
                return (T) toModelResults(EclipseModelResultSetModelBuilder.this.compositeModelProducer.getModel());
            }
        }, new DefaultResultHandler(handler));
    }

    private <S> Set<ModelResult<S>> toModelResults(Set<EclipseProject> eclipseProjects) {
        return CollectionUtils.collect(eclipseProjects, new Transformer<ModelResult<S>, EclipseProject>() {
            @SuppressWarnings("unchecked")
            @Override
            public ModelResult<S> transform(EclipseProject eclipseProject) {
                return new DefaultModelResult<S>((S) eclipseProject);
            }
        });
    }

    /**
     * The default implementation of a model result.
     *
     * @author Benjamin Muschko
     */
    private static final class DefaultModelResult<T> implements ModelResult<T> {
        private final T model;

        private DefaultModelResult(T model) {
            this.model = model;
        }

        @Override
        public T getModel() {
            return this.model;
        }
    }

    /**
     * Implementation of a result handler that blocks until request is fully processed.
     *
     * @param <T> type
     */
    private static class BlockingResultHandler<T> implements ResultHandler<Set<ModelResult<T>>> {
        private final BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(1);
        private final Object NULL = new Object();

        @SuppressWarnings("unchecked")
        public Set<ModelResult<T>> getResult() {
            Object result;
            try {
                result = this.queue.take();
            } catch (InterruptedException e) {
                throw UncheckedException.throwAsUncheckedException(e);
            }

            if (result instanceof Throwable) {
                throw UncheckedException.throwAsUncheckedException(attachCallerThreadStackTrace((Throwable) result));
            }
            if (result == this.NULL) {
                return null;
            }
            return (Set<ModelResult<T>>)result;
        }

        private Throwable attachCallerThreadStackTrace(Throwable failure) {
            List<StackTraceElement> adjusted = new ArrayList<StackTraceElement>();
            adjusted.addAll(Arrays.asList(failure.getStackTrace()));
            List<StackTraceElement> currentThreadStack = Arrays.asList(Thread.currentThread().getStackTrace());
            if (!currentThreadStack.isEmpty()) {
                adjusted.addAll(currentThreadStack.subList(2, currentThreadStack.size()));
            }
            failure.setStackTrace(adjusted.toArray(new StackTraceElement[adjusted.size()]));
            return failure;
        }

        @Override
        public void onComplete(Set<ModelResult<T>> result) {
            this.queue.add(result == null ? this.NULL : result);
        }

        @Override
        public void onFailure(GradleConnectionException failure) {
            this.queue.add(failure);
        }
    }

    /**
     * Default implementation of a result handler.
     *
     * @param <S> type
     */
    public class DefaultResultHandler<S> implements ResultHandlerVersion1<Set<ModelResult<S>>> {
        private final ResultHandler<? super Set<ModelResult<S>>> handler;

        public DefaultResultHandler(ResultHandler<? super Set<ModelResult<S>>> handler) {
            this.handler = handler;
        }

        @Override
        public void onComplete(Set<ModelResult<S>> result) {
            this.handler.onComplete(result);
        }

        @Override
        public void onFailure(Throwable failure) {
            if (failure instanceof InternalUnsupportedBuildArgumentException) {
                this.handler.onFailure(new UnsupportedBuildArgumentException(connectionFailureMessage(failure)
                        + "\n" + failure.getMessage(), failure));
            } else if (failure instanceof UnsupportedOperationConfigurationException) {
                this.handler.onFailure(new UnsupportedOperationConfigurationException(connectionFailureMessage(failure)
                        + "\n" + failure.getMessage(), failure.getCause()));
            } else if (failure instanceof GradleConnectionException) {
                this.handler.onFailure((GradleConnectionException) failure);
            } else if (failure instanceof InternalBuildCancelledException) {
                this.handler.onFailure(new BuildCancelledException(connectionFailureMessage(failure), failure.getCause()));
            } else if (failure instanceof InternalTestExecutionException) {
                this.handler.onFailure(new TestExecutionException(connectionFailureMessage(failure), failure.getCause()));
            } else if (failure instanceof BuildExceptionVersion1) {
                this.handler.onFailure(new BuildException(connectionFailureMessage(failure), failure.getCause()));
            } else if (failure instanceof ListenerNotificationException) {
                this.handler.onFailure(new ListenerFailedException(connectionFailureMessage(failure), ((ListenerNotificationException) failure).getCauses()));
            } else {
                this.handler.onFailure(new GradleConnectionException(connectionFailureMessage(failure), failure));
            }
        }

        private String connectionFailureMessage(Throwable failure) {
            String message = String.format("Could not fetch model of type '%s' using %s.", EclipseModelResultSetModelBuilder.this.modelType.getSimpleName(), EclipseModelResultSetModelBuilder.this.connection.getDisplayName());
            if (!(failure instanceof UnsupportedMethodException) && failure instanceof UnsupportedOperationException) {
                message += "\n" + Exceptions.INCOMPATIBLE_VERSION_HINT;
            }
            return message;
        }
    }
}
