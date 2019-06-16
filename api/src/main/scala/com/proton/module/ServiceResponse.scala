package com.proton.module

import java.util.concurrent.CompletableFuture
import java.util.function.{BiFunction, Consumer, Function, Supplier}

import com.proton.module

/** `ServiceResponse` wraps the result of a computation, which may be performed asynchronously.
  *
  * Users of `ServiceResponse` should not get the result from it. Instead, they should use its
  * methods to manipulate the result when that becomes available, and assign tasks to be performed
  * at that time.
  *
  * A `ServiceResponse` may also be a failure, in which case any attempt to retrieve its value with
  * `get` will result in an exception being thrown. The other methods, excluding the ones designed
  * to handle failure, will never be called in that case (but won't thrown an exception either).
  *
  * @tparam T Type of the result of the computation.
  */
class ServiceResponse[T] private(internalFuture: CompletableFuture[T]) {

  /** Apply a function to the result of the `ServiceResponse`. */
  def map[U](func: Function[T, U]): ServiceResponse[U] = {
    new ServiceResponse(internalFuture.thenApply[U](func))
  }

  /** Apply a function returning a new `ServiceResponse` to the result of this `ServiceResponse`. */
  def flatMap[K](func: Function[T, ServiceResponse[K]]): ServiceResponse[K] = {
    new ServiceResponse[K](internalFuture.thenCompose(new Function[T, CompletableFuture[K]] {
      override def apply(t: T): CompletableFuture[K] = func(t).toCompletableFuture
    }))
  }

  /** Return a Java's [[java.util.concurrent.CompletableFuture CompletableFuture]] based on this `ServiceResponse`. */
  def toCompletableFuture: CompletableFuture[T] = internalFuture

  /** Apply a function to the result of the `ServiceResponse`. */
  def map[K](func: T => K): ServiceResponse[K] = {
    new ServiceResponse(internalFuture.thenApply[K](new Function[T, K] {
      override def apply(t: T): K = func(t)
    }))
  }

  /** Apply a function returning a new `ServiceResponse` to the result of this `ServiceResponse`. */
  def flatMap[K](func: T => ServiceResponse[K]): ServiceResponse[K] = {
    new ServiceResponse[K](internalFuture.thenCompose(new Function[T, CompletableFuture[K]] {
      override def apply(t: T): CompletableFuture[K] = func(t).toCompletableFuture
    }))
  }

  /** Add listener to be called with the result of this `ServiceResponse` once it becomes available. */
  def onSuccess(func: Consumer[T]): Unit = {
    internalFuture.thenAccept(func)
  }

  /** Add listener to be called with the result of this `ServiceResponse` once it becomes available. */
  def onSuccess(func: T => Unit): Unit = {
    internalFuture.thenAccept(new Consumer[T] {
      override def accept(t: T): Unit = func(t)
    })
  }

  /** Add a listener to be called once a result becomes available, or the computation fails. */
  def onComplete[N](func: BiFunction[T, Throwable, N]): ServiceResponse[N] = {
    new ServiceResponse(internalFuture.handle[N](func))
  }

  /** Add a listener to be called once a result becomes available, or the computation fails. */
  def onComplete[N](func: (T, Throwable) => N): ServiceResponse[N] = {
    new ServiceResponse(internalFuture.handle[N](new BiFunction[T, Throwable, N] {
      override def apply(t: T, u: Throwable): N = func(t, u)
    }))
  }

  /** Recover from a computation failure with the given function. This is like `map` but for
    * failures, though it turns failed computations into successful ones.
    *
    * If the computation does not fail, that result will be returned as normal.
    */
  def recover(func: Throwable => T): ServiceResponse[T] = {
    new ServiceResponse(internalFuture.exceptionally(new Function[Throwable, T] {
      override def apply(t: Throwable): T = func(t)
    }))
  }

  /** Recover from a computation failure with the given function. This is like `map` but for
    * failures, though it turns failed computations into successful ones.
    *
    * If the computation does not fail, that result will be returned as normal.
    */
  def recover(func: Function[Throwable, T]): ServiceResponse[T] = {
    new ServiceResponse(internalFuture.exceptionally(func))
  }

  /** Recover from a computation failure with the given function. This is like `flatMap` but
    * for failures, though it turns failed computations into successful ones.
    *
    * If the computation does not fail, that result will be returned as normal.
    */
  def recoverWith(func: Throwable => ServiceResponse[T]): ServiceResponse[T] = {
    new ServiceResponse(internalFuture.handle[CompletableFuture[T]](new BiFunction[T, Throwable, CompletableFuture[T]] {
      override def apply(t: T, u: Throwable): CompletableFuture[T] = {
        if (t != null) {
          CompletableFuture.completedFuture(t)
        } else {
          func(u).toCompletableFuture
        }
      }
    }).thenCompose(Function.identity()))
  }

  /** Recover from a computation failure with the given function. This is like `flatMap` but
    * for failures, though it turns failed computations into successful ones.
    *
    * If the computation does not fail, that result will be returned as normal.
    */
  def recoverWith(func: Function[Throwable, ServiceResponse[T]]): ServiceResponse[T] = {
    new ServiceResponse(internalFuture.handle[CompletableFuture[T]](new BiFunction[T, Throwable, CompletableFuture[T]] {
      override def apply(t: T, u: Throwable): CompletableFuture[T] = {
        if (t != null) {
          CompletableFuture.completedFuture(t)
        } else {
          func(u).toCompletableFuture
        }
      }
    }).thenCompose(Function.identity()))
  }

  /** Synchronously wait until the computation is completed and then returns its result.
    *
    * This will throw an exception if the computation fails.
    *
    * AVOID USING THIS METHOD for anything but tests. Instead, use a method like `onSuccess`
    * or `onComplete` to pass the remaining of the computation that uses the result.
    */
  def get(): T = internalFuture.get()
}

/** Factories for [[module.ServiceResponse ServiceResponse]]. */
object ServiceResponse {
  /** Creates a ServiceResponse wrapping the provided CompletableFuture **/
  def wrap[T](internalFuture: CompletableFuture[T]): ServiceResponse[T] = new ServiceResponse(internalFuture)

  /** Creates a ServiceResponse for an existing result value **/
  def now[T](obj: T): ServiceResponse[T] = wrap(CompletableFuture.completedFuture(obj))

  /** Creates a ServiceResponse for a result that is eventually supplied by the provided Supplier **/
  def async[T](sup: Supplier[T]): ServiceResponse[T] = wrap(CompletableFuture.supplyAsync(sup))

  /** Creates a ServiceResponse for a result that is eventually supplied by the provided function **/
  def apply[T](t: => T): ServiceResponse[T] = async(new Supplier[T] {
    override def get(): T = t
  })

  /** Creates a failed ServiceResponse from an exception */
  def failure[T](ex: Throwable): ServiceResponse[T] = apply[T](throw ex)
}
