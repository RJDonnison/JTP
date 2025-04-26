package org.reujdon.jtp.server;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.reujdon.jtp.server.handlers.CommandRegistryTest;

@Suite
@SelectClasses({
        CommandRegistryTest.class
})
public class ServerTestSuite {
    // This class serves as a test suite container
}
