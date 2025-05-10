package org.reujdon.jtp.shared;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.reujdon.jtp.shared.messaging.MessageTestSuite;

@Suite
@SelectClasses({
    MessageTestSuite.class,
    PropertiesUtilTest.class,
    TokenUtilTest.class,
})
public class SharedTestSuite {
    // This class serves as a test suite container
}