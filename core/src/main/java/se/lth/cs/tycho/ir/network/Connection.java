package se.lth.cs.tycho.ir.network;

import se.lth.cs.tycho.ir.AttributableIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.ToolAttribute;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class Connection extends AttributableIRNode {
	private final End source;
	private final End target;

	public Connection(End source, End target) {
		this(null, source, target);
	}

	private Connection(Connection original, End source, End target) {
		super(original);
		this.source = source;
		this.target = target;
	}

	public Connection copy(End source, End target) {
		if (Objects.equals(this.source, source) && Objects.equals(this.target, target)) {
			return this;
		} else {
			return new Connection(this, source, target);
		}
	}

	public End getSource() {
		return source;
	}

	public Connection withSource(End source) {
		return copy(source, target);
	}

	public End getTarget() {
		return target;
	}

	public Connection withTarget(End target) {
		return copy(source, target);
	}

	@Override
	public Connection withAttributes(List<ToolAttribute> attributes) {
		return (Connection) super.withAttributes(attributes);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Connection transformChildren(Transformation transformation) {
		return withAttributes((List) getAttributes().map(transformation));
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		getAttributes().forEach(action);
	}

	@Override
	public Connection deepClone() {
		return (Connection) super.deepClone();
	}

	@Override
	public Connection clone() {
		return (Connection) super.clone();
	}

	public static final class End {
		private final Optional<String> instance;
		private final String port;

		public End(Optional<String> instance, String port) {
			this.instance = instance;
			this.port = port;
		}

		public Optional<String> getInstance() {
			return instance;
		}

		public End withInstance(Optional<String> instance) {
			if (this.instance.equals(instance)) {
				return this;
			} else {
				return new End(instance, port);
			}
		}

		public String getPort() {
			return port;
		}

		public End withPort(String port) {
			if (this.port.equals(port)) {
				return this;
			} else {
				return new End(instance, port);
			}
		}
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			End end = (End) o;
			return Objects.equals(instance, end.instance) &&
					Objects.equals(port, end.port);
		}

		@Override
		public int hashCode() {
			return Objects.hash(instance, port);
		}
	}
}
