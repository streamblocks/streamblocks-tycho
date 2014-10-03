package net.opendf.transform.reduction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class PriorityListSelector implements Selector<Integer> {
	private final int[] priorities;

	public PriorityListSelector(int[] priorities) {
		this.priorities = priorities;
	}

	public static int[] readIntsFromFile(File file) throws FileNotFoundException {
		List<Integer> numbers = new ArrayList<>();
		try (Scanner s = new Scanner(file)) {
			while (s.hasNextInt()) {
				numbers.add(s.nextInt());
			}
		}
		int[] priorities = new int[numbers.size()];
		int i = 0;
		for (Integer n : numbers) {
			priorities[i++] = n;
		}
		return priorities;
	}

	@Override
	public Integer select(Collection<Integer> collection) {
		Integer selected = null;
		int index = Integer.MAX_VALUE;
		for (Integer candidate : collection) {
			int i = 0;
			while (i < priorities.length && priorities[i] != candidate && i < index) {
				i++;
			}
			if (i < index) {
				index = i;
				selected = candidate;
			}
		}
		return selected;
	}

}
