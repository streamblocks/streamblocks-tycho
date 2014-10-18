package se.lth.cs.tycho.messages.util;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import se.lth.cs.tycho.messages.Message;

public final class Result<T> {
	private final T result;
	private final Message message;
	private final boolean success;

	public static <T> Result<T> success(T result) {
		return new Result<>(true, result, null);
	}

	public static <T> Result<T> failure(Message message) {
		return new Result<>(false, null, message);
	}

	private Result(boolean success, T result, Message message) {
		this.success = success;
		this.result = result;
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public boolean isFailure() {
		return !success;
	}

	public T get() {
		if (success) {
			return result;
		} else {
			throw new CompilationException(message);
		}
	}
	
	public T orElse(T other) {
		if (success) {
			return result;
		} else {
			return other;
		}
	}
	
	public T orElseGet(Supplier<? extends T> other) {
		if (success) {
			return result;
		} else {
			return other.get();
		}
	}
	
	public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
		if (success) {
			return result;
		} else {
			throw exceptionSupplier.get();
		}
	}

	public Message getMessage() {
		if (success) {
			throw new IllegalStateException();
		} else {
			return message;
		}
	}
	
	public <U> Result<U> map(Function<? super T, U> mapper) {
		if (success) {
			return Result.success(mapper.apply(result));
		} else {
			return Result.failure(message);
		}
	}
	
	public <U> Result<U> flatMap(Function<? super T, Result<U>> mapper) {
		if (success) {
			return mapper.apply(result);
		} else {
			return Result.failure(message);
		}
	}

	public boolean equals(Object obj) {
		if (obj instanceof Result<?>) {
			Result<?> that = (Result<?>) obj;
			return this.success == that.success && Objects.equals(this.result, that.result)
					&& Objects.equals(this.message, that.message);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Objects.hash(success, result, message);
	}
	
	public String toString() {
		if (success) {
			return String.format("Result.success(%s)", result.toString());
		} else {
			return String.format("Result.failure(%s)", message.toString());
		}
	}
}
