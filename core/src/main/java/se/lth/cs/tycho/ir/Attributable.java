package se.lth.cs.tycho.ir;

import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface Attributable {

	/**
	 * Returns an Optional with the value attribute of the given name if present, otherwise an empty Optional.
	 * @param name attribute name
	 * @return the attribute, if present
	 */
	default Optional<ToolValueAttribute> getValueAttribute(String name) {
		return getAttributes().stream()
				.filter(attribute -> attribute.getName().equals(name))
				.filter(attribute -> attribute instanceof ToolValueAttribute)
				.map(attribute -> (ToolValueAttribute) attribute)
				.findFirst();
	}

	/**
	 * Returns an Optional with the type attribute of the given name if present, otherwise an empty Optional.
	 * @param name attribute name
	 * @return the attribute, if present
	 */
	default Optional<ToolTypeAttribute> getTypeAttribute(String name) {
		return getAttributes().stream()
				.filter(attribute -> attribute.getName().equals(name))
				.filter(attribute -> attribute instanceof ToolTypeAttribute)
				.map(attribute -> (ToolTypeAttribute) attribute)
				.findFirst();
	}

	/**
	 * Returns all attributes of this object.
	 * @return all attributes
	 */
	ImmutableList<ToolAttribute> getAttributes();

	/**
	 * returns an object with the specified attribute. existing attributes with the same name are replaced.
	 * @param attribute the attribute
	 * @return an object with the attribute replaced
	 */
	default Attributable withTypeAttribute(ToolTypeAttribute attribute) {
		List<ToolAttribute> result = new ArrayList<>(getAttributes());
		result.removeIf(a -> a instanceof ToolTypeAttribute && a.getName().equals(attribute.getName()));
		result.add(attribute);
		return withAttributes(result);
	}

	/**
	 * returns an object with the specified attribute. existing attributes with the same name are replaced.
	 * @param attribute the attribute
	 * @return an object with the attribute replaced
	 */
	default Attributable withValueAttribute(ToolValueAttribute attribute) {
		List<ToolAttribute> result = new ArrayList<>(getAttributes());
		result.removeIf(a -> a instanceof ToolValueAttribute && a.getName().equals(attribute.getName()));
		result.add(attribute);
		return withAttributes(result);
	}

	/**
	 * Returns an Attributable object representing this object except its attributes that are removed and replaced by the supplied attributes.
	 * @param attributes replacement attributes
	 * @return an object with its attributes replaced
	 */
	Attributable withAttributes(List<ToolAttribute> attributes);

}
