package com.proton.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

public class ServiceResponseTest {

  @Test
  public void testCreateFromSupplier() {
    ServiceResponse<String> rtn = ServiceResponses.async(() -> "test_supplier");
    assertEquals("test_supplier", rtn.get());
  }

  @Test
  public void testCreateFromCompletableFuture() {
    ServiceResponse<String> rtn = ServiceResponses.wrap(CompletableFuture.completedFuture("test_future"));
    assertEquals("test_future", rtn.get());
  }

  @Test
  public void testCreateFromValue() {
    ServiceResponse<String> rtn = ServiceResponses.now("test_value");
    assertEquals("test_value", rtn.get());
  }

  @Test(expected = Exception.class)
  public void testCreateFromException() throws Exception {
    ServiceResponse<String> rtn = ServiceResponses.failure(new Exception("bad"));
    rtn.get();
  }

  @Test
  public void testMap() throws Exception {
    ServiceResponse<String> rtn = ServiceResponses.now("test").map(s -> s + "_map");
    assertEquals("test_map", rtn.get());
  }

  @Test
  public void testFlatMap() throws Exception {
    ServiceResponse<String> rtn = ServiceResponses.now("test").flatMap(s -> ServiceResponses.now(s + "_flatmap"));
    assertEquals("test_flatmap", rtn.get());
  }

  @Test
  public void testOnSuccess() throws Exception {
    AtomicBoolean ranOnSuccess = new AtomicBoolean(false);
    String expected = "test_on_success";
    ServiceResponse<String> rtn = ServiceResponses.now(expected);
    rtn.onSuccess(s -> {
      if (expected.equals(s)) ranOnSuccess.getAndSet(true);
    });

    rtn.get();

    assertTrue(ranOnSuccess.get());
  }

  @Test
  public void testOnComplete() throws Exception {
    ServiceResponse<String> good = ServiceResponses.now("good").onComplete((s, t) -> s);
    assertEquals("good", good.get());

    ServiceResponse<Throwable> bad = ServiceResponses.async(() -> {
      throw new RuntimeException("bad");
    }).onComplete((s, e) -> e);
    Throwable exception = bad.get();

    assertNotNull(exception);
    assertEquals("java.lang.RuntimeException: bad", exception.getMessage());

  }

  @Test
  public void testRecover() throws Exception {
    ServiceResponse<String> good = ServiceResponses.now("good").recover((Throwable ex) -> ex.getMessage());
    assertEquals("good", good.get());

    ServiceResponse<String> bad = ServiceResponses.<String>failure(new RuntimeException("bad"))
      .recover((Throwable ex) -> ex.getMessage());

    assertEquals("java.lang.RuntimeException: bad", bad.get());
  }

  @Test
  public void testRecoverWith() throws Exception {
    ServiceResponse<String> good = ServiceResponses.now("good")
      .recoverWith((Throwable ex) -> ServiceResponses.now(ex.getMessage()));
    assertEquals("good", good.get());

    ServiceResponse<String> bad = ServiceResponses.<String>failure(new RuntimeException("bad"))
      .recoverWith((Throwable ex) -> ServiceResponses.now(ex.getMessage()));

    assertEquals("java.lang.RuntimeException: bad", bad.get());
  }

  @Test
  public void testToCF() throws Exception {
    CompletableFuture<String> rtn = ServiceResponses.now("test_cf").toCompletableFuture();

    Assert.assertEquals("test_cf", rtn.get());
  }
}
