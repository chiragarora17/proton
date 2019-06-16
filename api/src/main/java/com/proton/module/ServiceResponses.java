package com.proton.module;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/** Factories for {@link ServiceResponse ServiceResponse}. */
public class ServiceResponses {

  /** Creates a ServiceResponse for an existing result value **/
  public static <A> ServiceResponse<A> now(A value) {
    return ServiceResponse$.MODULE$.now(value);
  }

  /** Creates a ServiceResponse wrapping the provided CompletableFuture **/
  public static <A> ServiceResponse<A> wrap(CompletableFuture<A> cf) {
    return ServiceResponse$.MODULE$.wrap(cf);
  }

  /** Creates a ServiceResponse for a result that is eventually supplied by the provided Supplier **/
  public static <A> ServiceResponse<A> async(Supplier<A> supplier) {
    return ServiceResponse$.MODULE$.async(supplier);
  }

  /** Creates a failed ServiceResponse from an exception */
  public static <A> ServiceResponse<A> failure(Throwable ex) {
    return ServiceResponse$.MODULE$.failure(ex);
  }
}
