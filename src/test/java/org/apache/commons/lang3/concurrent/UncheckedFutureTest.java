/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.lang3.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.UncheckedInterruptedException;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link UncheckedFuture}.
 */
public class UncheckedFutureTest {

    private static class TestFuture<V> implements Future<V> {

        private final V get;
        private final Exception executionException;

        TestFuture(final Exception throwable) {
            this.get = null;
            this.executionException = throwable;
        }

        TestFuture(final V get) {
            this.get = get;
            this.executionException = null;
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            return false;
        }

        @SuppressWarnings("unchecked") // Programming error if call site blows up at runtime.
        private <T extends Exception> void checkExecutionException() throws T {
            if (executionException != null) {
                throw (T) executionException;
            }
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            checkExecutionException();
            return get;
        }

        @Override
        public V get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            checkExecutionException();
            return get;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return false;
        }

    }

    @Test
    public void testGetExecutionException() {
        final ExecutionException e = new ExecutionException(new Exception());
        assertThrows(UncheckedExecutionException.class, () -> UncheckedFuture.on(new TestFuture<>(e)).get());
    }

    @Test
    public void testGetInterruptedException() {
        final InterruptedException e = new InterruptedException();
        assertThrows(UncheckedInterruptedException.class, () -> UncheckedFuture.on(new TestFuture<>(e)).get());
    }

    @Test
    public void testGetLongExecutionException() {
        final ExecutionException e = new ExecutionException(new Exception());
        assertThrows(UncheckedExecutionException.class, () -> UncheckedFuture.on(new TestFuture<>(e)).get(1, TimeUnit.MICROSECONDS));
    }

    @Test
    public void testGetLongInterruptedException() {
        final InterruptedException e = new InterruptedException();
        assertThrows(UncheckedInterruptedException.class, () -> UncheckedFuture.on(new TestFuture<>(e)).get(1, TimeUnit.MICROSECONDS));
    }

    @Test
    public void testGetLongTimeoutException() {
        final TimeoutException e = new TimeoutException();
        assertThrows(UncheckedTimeoutException.class, () -> UncheckedFuture.on(new TestFuture<>(e)).get(1, TimeUnit.MICROSECONDS));
    }

    @Test
    public void testOnCollection() {
        final List<String> expected = Arrays.asList("Y", "Z");
        final List<Future<String>> input = Arrays.asList(new TestFuture<>("Y"), new TestFuture<>("Z"));
        assertEquals(expected, UncheckedFuture.on(input).stream().map(UncheckedFuture::get).collect(Collectors.toList()));
    }

    @Test
    public void testOnFuture() {
        assertEquals("Z", UncheckedFuture.on(new TestFuture<>("Z")).get());
    }

}
