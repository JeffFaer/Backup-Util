package falgout.backup.guice;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Guice;
import com.google.inject.Inject;

import falgout.backup.app.Configuration;
import falgout.backup.app.History;

@RunWith(JukitoRunner.class)
public class DeviceModuleTest {
    @Inject private Configuration conf;
    
    @Before
    public void init() throws IOException {
        Path dir = Files.createTempDirectory(getClass().getName());
        when(conf.getRoot()).thenReturn(dir);
        when(conf.getFileStore()).thenReturn(Files.getFileStore(dir));
        when(conf.getID()).thenReturn(UUID.randomUUID());
    }
    
    @Test
    public void AddsAliasToHistoryWhenInjected() throws IOException {
        History h = Guice.createInjector(new DeviceModule(conf)).getInstance(DeviceModule.HISTORY_PROVIDER).get();
        assertThat(h.getAliases(), contains(conf.getRoot()));
    }
}
