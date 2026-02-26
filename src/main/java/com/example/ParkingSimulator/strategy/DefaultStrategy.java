package com.example.ParkingSimulator.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DefaultStrategy implements IStrategy {

	@Override
	public int trouverPlace(List<Long> tempPlace, List<Boolean> placeOccupe, int nbrPlaces) {

		// Collect all free slots
		List<Integer> freeSlots = new ArrayList<>();

		for (int i = 0; i < nbrPlaces; i++) {
			if (!placeOccupe.get(i)) {
				freeSlots.add(i);
			}
		}

		// No free slot
		if (freeSlots.isEmpty()) {
			return -1;
		}

		// Pick a random free slot
		Random r = new Random();
		return freeSlots.get(r.nextInt(freeSlots.size()));
	}
}
