package com.proton.module;

import org.junit.Assert;
import org.junit.Test;

public class ServiceNameTest {

  @Test
  public void testCreateServiceName() throws Exception {
    ServiceName name = ServiceName.of("test");

    Assert.assertEquals("test", name.toString());
  }

  @Test
  public void testServiceNamesEqual() throws Exception {
    ServiceName name1 = ServiceName.of("test");
    ServiceName name2 = ServiceName.of("test");
    ServiceName name3 = ServiceName.of("test2");

    Assert.assertEquals(name1, name1);
    Assert.assertEquals(name1, name2);
    Assert.assertNotEquals(null, name3);
    Assert.assertNotEquals(name1, name3);
    Assert.assertNotEquals(name2, name3);
  }

  @Test
  public void testHash() throws Exception {
    ServiceName name1 = ServiceName.of("test");
    ServiceName name2 = ServiceName.of("test");
    ServiceName name3 = ServiceName.of("test2");

    Assert.assertEquals(name1.hashCode(), name2.hashCode());
    Assert.assertNotEquals(name1.hashCode(), name3.hashCode());
    Assert.assertNotEquals(name2.hashCode(), name3.hashCode());
  }

  @Test
  public void testCleansupPath() throws Exception {
    ServiceName name1 = ServiceName.of("test first");
    ServiceName name2 = ServiceName.of("/test Second");
    ServiceName name3 = ServiceName.of("\\test3");

    Assert.assertEquals("testfirst", name1.getPath());
    Assert.assertEquals("testsecond", name2.getPath());
    Assert.assertEquals("test3", name3.getPath());
  }
}
