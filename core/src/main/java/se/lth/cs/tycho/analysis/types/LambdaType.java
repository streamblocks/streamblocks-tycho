package se.lth.cs.tycho.analysis.types;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LambdaType implements Type {
	private static final LeastUpperBound leastUpperBound = new LeastUpperBound();
	private static final GreatestLowerBound greatestLowerBound = new GreatestLowerBound();
	
	private final Type returnType;
	private final List<Type> parameterTypes;

	public LambdaType(Type returnType, List<Type> parameterTypes) {
		this.returnType = returnType;
		this.parameterTypes = parameterTypes;
	}

	@Override
	public Type leastUpperBound(Type that) {
		return that.accept(leastUpperBound, this);
	}
	
	@Override
	public Type greatestLowerBound(Type that) {
		return that.accept(greatestLowerBound, this);
	}

	public Type getReturnType() {
		return returnType;
	}

	public List<Type> getParameterTypes() {
		return parameterTypes;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> visitor, P param) {
		return visitor.visitLambdaType(this, param);
	}
	
	private static class LeastUpperBound implements LeastUpperBoundVisitor<LambdaType> {
		@Override
		public Type visitLambdaType(LambdaType b, LambdaType a) {
			Type returnType = a.returnType.leastUpperBound(b.returnType);
			Iterator<Type> aParam = a.parameterTypes.iterator();
			Iterator<Type> bParam = b.parameterTypes.iterator();
			List<Type> param = new ArrayList<>();
			while (aParam.hasNext() && bParam.hasNext()) {
				param.add(aParam.next().greatestLowerBound(bParam.next()));
			}
			if (aParam.hasNext() || bParam.hasNext()) {
				return new BottomType();
			} else {
				return new LambdaType(returnType, param);
			}
		}
	}
	
	private static class GreatestLowerBound implements GreatestLowerBoundVisitor<LambdaType> {
		@Override
		public Type visitLambdaType(LambdaType b, LambdaType a) {
			Type returnType = a.returnType.greatestLowerBound(b.returnType);
			Iterator<Type> aParam = a.parameterTypes.iterator();
			Iterator<Type> bParam = b.parameterTypes.iterator();
			List<Type> param = new ArrayList<>();
			while (aParam.hasNext() && bParam.hasNext()) {
				param.add(aParam.next().leastUpperBound(bParam.next()));
			}
			if (aParam.hasNext() || bParam.hasNext()) {
				return new BottomType();
			} else {
				return new LambdaType(returnType, param);
			}
		}
	}
}
