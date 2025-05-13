package org.reujdon.jtp.shared.messaging.messages;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;


@Suite
@SelectClasses({
        ErrorTest.class,
        RequestTest.class,
        ResponseTest.class,
})
public class MessagesTestSuite {
}
