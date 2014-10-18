package se.lth.cs.tycho.analysis.types;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProcType implements Type {
	private static final LeastUpperBound leastUpperBound = new LeastUpperBound();
	private static final GreatestLowerBound greatestLowerBound = new GreatestLowerBound();

	private final List<Type> parameterTypes;

	public ProcType(List<Type> parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	@Override
	public Type leastUpperBound(Type that) {
		return this.accept(leastUpperBound, this);
	}

	@Override
	public Type greatestLowerBound(Type that) {
		return that.accept(greatestLowerBound, this);
	}

	public List<Type> getParameterTypes() {
		return parameterTypes;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> visitor, P param) {
		return visitor.visitProcType(this, param);
	}

	private static class LeastUpperBound implements LeastUpperBoundVisitor<ProcType> {
		@Override
		public Type visitProcType(ProcType b, ProcType a) {
			Iterator<Type> aParam = a.parameterTypes.iterator();
			Iterator<Type> bParam = b.parameterTypes.iterator();
			List<Type> param = new ArrayList<>();
			while (aParam.hasNext() && bParam.hasNext()) {
				param.add(aParam.next().greatestLowerBound(bParam.next()));
			}
			if (aParam.hasNext() || bParam.hasNext()) {
				return new BottomType();
			} else {
				return new ProcType(param);
			}
		}
	}

	private static class GreatestLowerBound implements GreatestLowerBoundVisitor<ProcType> {
		@Override
		public Type visitProcType(ProcType b, ProcType a) {
			Iterator<Type> aParam = a.parameterTypes.iterator();
			Iterator<Type> bParam = b.parameterTypes.iterator();
			List<Type> param = new ArrayList<>();
			while (aParam.hasNext() && bParam.hasNext()) {
				param.add(aParam.next().leastUpperBound(bParam.next()));
			}
			if (aParam.hasNext() || bParam.hasNext()) {
				return new BottomType();
			} else {
				return new ProcType(param);
			}
		}
	}

}
