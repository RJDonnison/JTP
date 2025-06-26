package org.reujdon.jtp.shared.json;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    GsonAdapterTest.class,
    JsonExceptionTest.class
})
public class JsonTestSuite {
}
