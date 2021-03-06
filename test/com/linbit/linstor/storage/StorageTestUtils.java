package com.linbit.linstor.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.powermock.api.mockito.PowerMockito;
import com.linbit.extproc.ExtCmd;
import com.linbit.extproc.utils.TestExtCmd;
import com.linbit.extproc.utils.TestExtCmd.Command;
import com.linbit.fsevent.EntryGroupObserver;
import com.linbit.fsevent.FileObserver;
import com.linbit.fsevent.FileSystemWatch;
import com.linbit.fsevent.FileSystemWatch.Event;
import com.linbit.fsevent.FileSystemWatch.FileEntry;
import com.linbit.fsevent.FileSystemWatch.FileEntryGroup;
import com.linbit.fsevent.FileSystemWatch.FileEntryGroupBuilder;
import com.linbit.linstor.SatelliteCoreServices;
import com.linbit.linstor.core.DeviceManager;
import com.linbit.linstor.drbdstate.DrbdStateTracker;
import com.linbit.linstor.logging.ErrorReporter;
import com.linbit.linstor.testutils.EmptyErrorReporter;
import com.linbit.timer.Action;
import com.linbit.timer.GenericTimer;
import com.linbit.timer.Timer;

public class StorageTestUtils
{
    protected static interface DriverFactory
    {
        StorageDriver createDriver() throws StorageException;
    }

    protected final FileObserver emptyFileObserver;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    protected StorageDriver driver;
    protected TestExtCmd ec;

    private DriverFactory driverFactory;

    public StorageTestUtils(DriverFactory factory) throws Exception
    {
        this.driverFactory = factory;
        emptyFileObserver = new FileObserver()
        {
            @Override
            public void fileEvent(FileEntry watchEntry)
            {
            }
        };
        ec = new TestExtCmd();
        PowerMockito
            .whenNew(ExtCmd.class)
            .withAnyArguments()
            .thenReturn(ec);
    }

    @Before
    public void setUp() throws Exception
    {
        ec.clearBehaviors();
        driver = driverFactory.createDriver();
        driver.initialize(new DummySatelliteCoreServices());
    }

    @After
    public void tearDown() throws Exception
    {
        StringBuilder sb = new StringBuilder();
        HashSet<Command> uncalledCommands = ec.getUncalledCommands();
        if (!uncalledCommands.isEmpty())
        {
            for (Command cmd : uncalledCommands)
            {
                sb.append(cmd).append("\n");
            }
            sb.setLength(sb.length()-1);
            fail("Not all expected commands were called: \n"+sb.toString());
        }
    }

    protected void expectException(Map<String, String> config)
    {
        try
        {
            driver.setConfiguration(config);
            fail(StorageException.class.getName() + " expected");
        }
        catch (StorageException e)
        {
            // expected
        }
    }

    protected static Map<String, String> createMap(String... strings)
    {
        HashMap<String, String> map = new HashMap<>();
        int idx = 0;
        while (idx < strings.length)
        {
            map.put(strings[idx], strings[idx+1]);
            idx += 2;
        }
        return map;
    }

    protected static Path findCommand(String command)
    {
        Path[] pathFolders = getPathFolders();
        Path path = null;

        if (pathFolders != null)
        {
            for (Path folder : pathFolders)
            {
                Path commandPath = folder.resolve(command);
                if (Files.exists(commandPath) && Files.isExecutable(commandPath))
                {
                    path = commandPath;
                    break;
                }
            }
        }
        return path;
    }

    protected static Path[] getPathFolders()
    {
        String path = System.getenv("PATH");
        if (path == null)
        {
            path = System.getenv("path");
        }
        if (path == null)
        {
            path = System.getenv("Path");
        }
        Path[] folders = null;
        if (path != null)
        {
            String[] split = path.split(File.pathSeparator);

            folders = new Path[split.length];
            for (int i = 0; i < split.length; i++)
            {
                folders[i] = Paths.get(split[i]);
            }
        }
        return folders;
    }

    protected static <T> T getInstance(Class<T> clazz, Object... parameters) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Class<?>[] parameterClasses = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++)
        {
            parameterClasses[i] = parameters[i].getClass();
        }
        Constructor<T> constructor = clazz.getDeclaredConstructor(parameterClasses);
        boolean accessible = constructor.isAccessible();
        constructor.setAccessible(true);
        T ret = constructor.newInstance(parameters);
        constructor.setAccessible(accessible);
        return ret;
    }

    protected static FileEntryGroupBuilder getTestFileEntryGroupBuilder(final String expectedFilePath, final Event expectedEvent, final FileEntryGroup testFileEntryGroup)
    {
        return new FileEntryGroupBuilder()
        {
            @Override
            public void newEntry(String filePath, Event event)
            {
                assertEquals(expectedFilePath, filePath);
                assertEquals(expectedEvent, event);
            }

            @Override
            public FileEntryGroup create(FileSystemWatch watch, EntryGroupObserver observer) throws IOException
            {
                return testFileEntryGroup;
            }
        };
    }

    private class DummySatelliteCoreServices implements SatelliteCoreServices
    {
        private ErrorReporter errRep = new EmptyErrorReporter();
        private Timer<String, Action<String>> timer = new GenericTimer<>();

        @Override
        public ErrorReporter getErrorReporter()
        {
            return errRep;
        }

        @Override
        public Timer<String, Action<String>> getTimer()
        {
            return timer;
        }

        @Override
        public FileSystemWatch getFsWatch()
        {
            return null;
        }

        @Override
        public DeviceManager getDeviceManager()
        {
            return null;
        }

        @Override
        public DrbdStateTracker getDrbdStateTracker()
        {
            return null;
        }
    }
}
