package org.reujdon.jtp.shared.messaging;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        MessageTest.class,
        ErrorTest.class,
        RequestTest.class,
        ResponseTest.class,
        ParseTest.class,
})
public class MessageTestSuite {
    // This class serves as a test suite container
}