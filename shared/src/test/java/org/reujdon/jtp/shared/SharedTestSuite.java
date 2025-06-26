package org.reujdon.jtp.shared;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.reujdon.jtp.shared.json.JsonTestSuite;
import org.reujdon.jtp.shared.messaging.MessagingTestSuite;
import org.reujdon.jtp.shared.parse.ParseTestSuite;

@Suite
@SelectClasses({
    JsonTestSuite.class,
    MessagingTestSuite.class,
    ParseTestSuite.class,
    PropertiesUtilTest.class,
    TokenUtilTest.class,
    PermissionTest.class,
})
public class SharedTestSuite {
    // This class serves as a test suite container
}