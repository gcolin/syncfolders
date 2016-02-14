package application.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Duplicate {

	private Consumer<Item[]> duplicateConsumer;
	private Consumer<Integer> percentConsumer;
	
	public Duplicate() {
	}
	
	public void enableRemove() {
		Consumer<Item[]> prec = duplicateConsumer;
		duplicateConsumer = new Consumer<Item[]>() {

			@Override
			public void accept(Item[] x) {
				Map<Item, List<Item>> byParent = new HashMap<>();
				for (int i = 0; i < x.length; i++) {
					List<Item> list = byParent.get(x[i].getParent());
					if (list == null) {
						list = new ArrayList<>();
						byParent.put(x[i].getParent(), list);
					}
					list.add(x[i]);
				}
				List<Item> keeped = new ArrayList<>();

				try {
					for (List<Item> list : new ArrayList<>(byParent.values())) {
						if (list.size() == 1) {
							keeped.add(list.get(0));
						} else {
							Item min = list.get(0);
							for (int i = 1; i < list.size(); i++) {
								if (list.get(i).getName().length() < min.getName().length()) {
									min.remove();
									min = list.get(i);
								} else {
									list.get(i).remove();
								}
							}
							keeped.add(min);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(0);
				}
				
				if(keeped.size()>1) {
					prec.accept(keeped.toArray(new Item[keeped.size()]));
				}
				
			}
		};
	}
	
	public void setDuplicateConsumer(Consumer<Item[]> duplicateConsumer) {
		this.duplicateConsumer = duplicateConsumer;
	}
	
	public void setPercentConsumer(Consumer<Integer> percentConsumer) {
		this.percentConsumer = percentConsumer;
	}
	
	public Consumer<Integer> getPercentConsumer() {
		return percentConsumer;
	}
	
	public Consumer<Item[]> getDuplicateConsumer() {
		return duplicateConsumer;
	}
	
	public void duplicate(Sync c, String src, boolean checkSize, boolean checkContent) throws IOException {
		List<Check> allChecks = new ArrayList<>();
		if (checkSize) {
			allChecks.add(Check.SIZE);
		}
		if (checkContent) {
			allChecks.add(Check.HASH);
		}
		Check check = allChecks.isEmpty() ? Check.NOCHECK : Check.all(allChecks.toArray(new Check[allChecks.size()]));
		
		Connection co = c.build(src);
		List<Item> all = new ArrayList<>();
		try {
			do {
				for (Item item : co.list()) {
					if (!item.isDirectory()) {
						all.add(item);
					}
				}
			} while (co.enter());
			List<Item> list = new ArrayList<>();
			int size = all.size();
			int prec = -1;
			HashSet<Item> duplicates = new HashSet<>();
			for(int i=0;i<size;i++) {
				Item e = all.get(i);
				if(duplicates.contains(e)) {
					continue;
				}
				for(int j=i+1;j<size;j++) {
					if(!check.test(e, all.get(j), null)) {
						list.add(all.get(j));
						duplicates.add(all.get(j));
					}
				}
				if(!list.isEmpty()) {
					list.add(0, e);
					duplicateConsumer.accept(list.toArray(new Item[list.size()]));
					list.clear();
				}
				if(i*100/size!=prec) {
					prec = i*100/size;
					percentConsumer.accept(prec);
				}
			}
		} finally {
			c.close(co);
		}
	}

}
