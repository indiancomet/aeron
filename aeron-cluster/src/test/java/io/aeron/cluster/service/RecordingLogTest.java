/*
 * Copyright 2017 Real Logic Ltd.
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
package io.aeron.cluster.service;

import org.agrona.IoUtil;
import org.junit.After;
import org.junit.Test;

import java.io.File;

import static io.aeron.cluster.service.RecordingLog.ENTRY_TYPE_SNAPSHOT;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class RecordingLogTest
{
    private static final File TEMP_DIR = new File(IoUtil.tmpDirName());

    @After
    public void after()
    {
        IoUtil.delete(new File(TEMP_DIR, RecordingLog.RECORDING_INDEX_FILE_NAME), false);
    }

    @Test
    public void shouldCreateNewIndex()
    {
        final RecordingLog recordingLog = new RecordingLog(TEMP_DIR);

        assertThat(recordingLog.entries().size(), is(0));
    }

    @Test
    public void shouldAppendAndThenReloadLatestSnapshot()
    {
        final RecordingLog recordingLog = new RecordingLog(TEMP_DIR);

        final RecordingLog.Entry entry = new RecordingLog.Entry(1, 2, 3, 4, ENTRY_TYPE_SNAPSHOT);

        recordingLog.appendSnapshot(entry.recordingId, entry.logPosition, entry.leadershipTermId, entry.timestamp);

        final RecordingLog recordingLogTwo = new RecordingLog(TEMP_DIR);
        assertThat(recordingLogTwo.entries().size(), is(1));

        final RecordingLog.Entry snapshot = recordingLogTwo.getLatestSnapshot();
        assertEquals(entry.toString(), snapshot.toString());
    }
}