package falgout.backup.guice;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;

import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;

@RunWith(JukitoRunner.class)
public class BackupModuleTest {
    public static class A extends JukitoModule {
        @Override
        protected void configureTest() {
            bindMock(Properties.class);
        }
    }
    
    @Inject private Properties properties;
    private BackupModule module;
    
    @Before
    public void init() {
        module = new BackupModule(properties);
    }
    
    @Test(expected = CreationException.class)
    public void ErrorIfNoLocation() {
        when(properties.propertyNames()).then(returnEmpty());
        
        Injector i = Guice.createInjector(module);
        i.getProvider(BackupModule.LOCATION);
    }
    
    private Answer<Enumeration<String>> returnEmpty() {
        return new Answer<Enumeration<String>>() {
            @Override
            public Enumeration<String> answer(InvocationOnMock invocation) throws Throwable {
                return Collections.enumeration(Collections.<String> emptyList());
            }
        };
    }
    
    @Test
    public void InjectsLocationForBackupLocation() {
        when(properties.propertyNames()).then(returnLocation());
        when(properties.getProperty("location")).thenReturn("/home/jeffrey/foo");
        
        Injector i = Guice.createInjector(module);
        assertEquals(Paths.get("/home/jeffrey/foo"), i.getInstance(Key.get(Path.class, BackupLocation.class)));
    }
    
    private Answer<?> returnLocation() {
        return new Answer<Enumeration<String>>() {
            @Override
            public Enumeration<String> answer(InvocationOnMock invocation) throws Throwable {
                return Collections.enumeration(Arrays.asList("location"));
            }
        };
    }
}
