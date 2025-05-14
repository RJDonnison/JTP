package org.reujdon.jtp.server;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.reujdon.jtp.server.handlers.HelpCommandHandler;

@Suite
@SelectClasses({
        CommandRegistryTest.class,
        HelpCommandHandler.class,
        JTPServerConfig.class
})
public class JTPServerTestSuite {
    // This class serves as a test suite container
}
