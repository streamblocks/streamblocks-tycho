package se.lth.cs.tycho.backend.ir

final class Attributes {
  private var map: Map[Attribute[_], _] = Map.empty
  def replaceWith(attributes: Attributes): Unit = map = attributes.map
  def apply[V](attribute: Attribute[V]): Option[V] = map.lift(attribute).map(_.asInstanceOf[V])
  def update[V](attribute: Attribute[V], value: V): Unit = map += attribute -> value
}

trait Attributable {
  val attributes: Attributes = new Attributes
}

trait Attribute[V]

