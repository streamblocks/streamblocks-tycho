package se.lth.cs.tycho.type;

import java.util.Objects;
import java.util.OptionalInt;

/*public final class ComplexType implements NumberType {

    public static final ComplexType defaultComplex = new ComplexType(RealType.f64);
    private final Type internalType;

    public ComplexType(){
        internalType = defaultComplex;
    }

    public ComplexType(Type internalType) {
        this.internalType = internalType;
    }

    public Type getInternalType() {
        return internalType;
    }

    @Override
    public String toString() {
        return "complex(type: "+internalType+")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplexType complexType = (ComplexType) o;
        return Objects.equals(complexType.internalType, internalType);
    }

    public int hashCode() {
        return Objects.hash(internalType);
    }
}*/

public final class ComplexType implements NumberType {

    private final Type elementType;

    public ComplexType(Type elementType) {
        this.elementType = elementType;
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplexType setType = (ComplexType) o;
        return getElementType().equals(setType.getElementType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getElementType());
    }

    @Override
    public String toString() {
        return "complex(type:" + getElementType() + ")";
    }

    public boolean isIntComplex(){
        return elementType instanceof IntType;
    }
}

