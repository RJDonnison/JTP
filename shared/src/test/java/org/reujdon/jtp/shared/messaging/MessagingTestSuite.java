package org.reujdon.jtp.shared.messaging;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.reujdon.jtp.shared.messaging.messages.MessagesTestSuite;

@Suite
@SelectClasses({
        MessageTest.class,
        MessageTypeTest.class,
        MessagesTestSuite.class,
})
public class MessagingTestSuite {
    // This class serves as a test suite container
}