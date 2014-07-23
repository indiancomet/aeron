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

import uk.co.real_logic.aeron.common.concurrent.AtomicBuffer;

/**
 * .
 */
public class BufferPositionIndicator implements PositionIndicator
{
    private final AtomicBuffer buffer;
    private final int couunterId;
    private final CountersManager countersManager;
    private final int offset;

    public BufferPositionIndicator(final AtomicBuffer buffer, final int counterId)
    {
        this(buffer, counterId, null);
    }

    public BufferPositionIndicator(final AtomicBuffer buffer, final int counterId,
                                   final CountersManager countersManager)
    {
        this.buffer = buffer;
        this.couunterId = counterId;
        this.countersManager = countersManager;
        this.offset = CountersManager.counterOffset(counterId);
    }

    public long position()
    {
        return buffer.getLongVolatile(offset);
    }

    public void close()
    {
        countersManager.deregisterCounter(couunterId);
    }
}
