package org.reujdon.jtp.shared;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        MessageTest.class,
        ErrorTest.class,
        RequestTest.class,
        ResponseTest.class,
})
public class MessageTestSuite {
    // This class serves as a test suite container
}