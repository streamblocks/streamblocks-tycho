package se.lth.cs.tycho.transformation.cal2am;

import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.Setting;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class KnowledgeRemoval {
	public static final Setting<EnumSet<KnowledgeKind>> forgetOnWait = new RemovalPolicy() {
		@Override public String getKey() { return "forget-on-wait"; }
		@Override public String getDescription() {
			return "A ,-separated list of things that should be forgotten on wait-transitions in the " +
					"translation from Cal to Actor Machines. There are three kinds: input, output and guards. Use none for an empty list.";
		}
	};
	public static final Setting<EnumSet<KnowledgeKind>> forgetOnExec = new RemovalPolicy() {
		@Override public String getKey() { return "forget-on-exec"; }
		@Override public String getDescription() {
			return "A ,-separated list of things that should be forgotten on exec-transitions in the " +
					"translation from Cal to Actor Machines. There are three kinds: input, output and guards. Use none for an empty list.";
		}
	};

	public enum KnowledgeKind {
		INPUT("input"),
		OUTPUT("output"),
		GUARDS("guards");

		private final String tag;
		KnowledgeKind(String tag) {
			this.tag = tag;
		}

		public String getTag() {
			return tag;
		}

		public static Optional<KnowledgeKind> fromTag(String tag) {
			for (KnowledgeKind knowledgeKind : values()) {
				if (tag.equals(knowledgeKind.getTag())) {
					return Optional.of(knowledgeKind);
				}
			}
			return Optional.empty();
		}

	}

	public static abstract class RemovalPolicy implements Setting<EnumSet<KnowledgeKind>> {

		@Override
		public String getType() {
			return "knowledge-kind-list";
		}

		@Override
		public Optional<EnumSet<KnowledgeKind>> read(String string) {
			if (string.equals("none")) {
				return Optional.of(EnumSet.noneOf(KnowledgeKind.class));
			}
			String[] tags = string.split(" *, *");
			List<Optional<KnowledgeKind>> kindList = Stream.of(tags).map(KnowledgeKind::fromTag).collect(Collectors.toList());
			if (kindList.stream().allMatch(Optional::isPresent)) {
				EnumSet<KnowledgeKind> policy = kindList.stream()
						.map(Optional::get)
						.collect(() -> EnumSet.noneOf(KnowledgeKind.class), EnumSet::add, EnumSet::addAll);
				return Optional.of(policy);
			} else {
				return Optional.empty();
			}
		}

		@Override
		public EnumSet<KnowledgeKind> defaultValue(Configuration configuration) {
			return EnumSet.allOf(KnowledgeKind.class);
		}
	}
}
