/*
 * Copyright 2014 Real Logic Ltd.
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
package uk.co.real_logic.aeron.common.status;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import uk.co.real_logic.aeron.common.concurrent.AtomicBuffer;

import java.util.function.BiConsumer;

import static java.nio.ByteBuffer.allocate;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.co.real_logic.aeron.common.BitUtil.SIZE_OF_LONG;

@SuppressWarnings("unchecked")
public class CountersManagerTest
{
    private static final int NUMBER_OF_COUNTERS = 4;

    private AtomicBuffer labelsBuffer = new AtomicBuffer(allocate(NUMBER_OF_COUNTERS * CountersManager.LABEL_SIZE));
    private AtomicBuffer counterBuffer = new AtomicBuffer(allocate(NUMBER_OF_COUNTERS * CountersManager.COUNTER_SIZE));
    private CountersManager manager = new CountersManager(labelsBuffer, counterBuffer);
    private CountersManager otherManager = new CountersManager(labelsBuffer, counterBuffer);

    @Test
    public void managerShouldStoreLabels()
    {
        int counterId = manager.registerCounter("abc");
        BiConsumer<Integer, String> consumer = mock(BiConsumer.class);
        otherManager.forEachLabel(consumer);
        verify(consumer).accept(counterId, "abc");
    }

    @Test
    public void managerShouldStoreMultipleLabels()
    {
        int abc = manager.registerCounter("abc");
        int def = manager.registerCounter("def");
        int ghi = manager.registerCounter("ghi");

        BiConsumer<Integer, String> consumer = mock(BiConsumer.class);
        otherManager.forEachLabel(consumer);

        InOrder inOrder = Mockito.inOrder(consumer);
        inOrder.verify(consumer).accept(abc, "abc");
        inOrder.verify(consumer).accept(def, "def");
        inOrder.verify(consumer).accept(ghi, "ghi");
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void shouldDeregisterAndReuseCounters()
    {
        int abc = manager.registerCounter("abc");
        int def = manager.registerCounter("def");
        int ghi = manager.registerCounter("ghi");

        manager.deregisterCounter(def);

        BiConsumer<Integer, String> consumer = mock(BiConsumer.class);
        otherManager.forEachLabel(consumer);

        InOrder inOrder = Mockito.inOrder(consumer);
        inOrder.verify(consumer).accept(abc, "abc");
        inOrder.verify(consumer).accept(ghi, "ghi");
        inOrder.verifyNoMoreInteractions();

        assertThat(manager.registerCounter("the next label"), is(def));
    }

    @Test(expected = IllegalArgumentException.class)
    public void managerShouldNotOverAllocateCounters()
    {
        manager.registerCounter("abc");
        manager.registerCounter("def");
        manager.registerCounter("ghi");
        manager.registerCounter("jkl");
        manager.registerCounter("mno");
    }

    @Test
    public void registeredCountersCanBeMapped()
    {
        manager.registerCounter("def");

        int id = manager.registerCounter("abc");
        BufferPositionIndicator reader = new BufferPositionIndicator(counterBuffer, id);
        BufferPositionReporter writer = new BufferPositionReporter(counterBuffer, id);
        writer.position(0xFFFFFFFFFL);
        assertThat(reader.position(), is(0xFFFFFFFFFL));
    }
}
