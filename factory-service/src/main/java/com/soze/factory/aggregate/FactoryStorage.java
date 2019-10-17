package com.soze.factory.aggregate;

import com.soze.common.dto.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FactoryStorage {

	private final Map<Resource, StorageSlot> resources;

	public FactoryStorage(Map<Resource, StorageSlot> resources) {
		Objects.requireNonNull(this.resources = resources);
		validateSlots(this.resources);
		calculatePrices();
	}

	public void addResource(Resource resource) {
		addResource(resource, 1);
	}

	public void addResource(Resource resource, final int count) {
		if (!canFit(resource, count)) {
			return;
		}
		StorageSlot slot = getSlot(resource);
		slot.setCount(slot.getCount() + count);
		calculatePrices();
	}

	public boolean canFit(Resource resource) {
		return canFit(resource, 1);
	}

	public boolean canFit(Resource resource, int count) {
		return getRemainingCapacity(resource) >= count;
	}

	public void removeResource(Resource resource) {
		removeResource(resource, 1);
	}

	public void removeResource(Resource resource, final int count) {
		if (!hasResource(resource, count)) {
			return;
		}

		StorageSlot slot = getSlot(resource);
		slot.setCount(slot.getCount() - count);
		calculatePrices();
	}

	public boolean hasResource(Resource resource) {
		return hasResource(resource, 1);
	}

	public boolean hasResource(Resource resource, int count) {
		StorageSlot slot = resources.get(resource);
		return slot != null && slot.getCount() >= count;
	}

	public int getRemainingCapacity(Resource resource) {
		int capacityTaken = getCapacityTaken(resource);
		int capacity = getCapacity(resource);
		return capacity - capacityTaken;
	}

	public int getCapacityTaken(Resource resource) {
		return getSlot(resource).getCount();
	}

	public int getCapacity(Resource resource) {
		return getSlot(resource).getCapacity();
	}

	void transferFrom(FactoryStorage otherStorage) {
		otherStorage.getResources().forEach((resource, slot) -> {
			int transferCount = Math.min(slot.getCount(), getRemainingCapacity(resource));
			addResource(resource, transferCount);
		});
	}

	/**
	 * Checks if any of the capacities are below zero.
	 */
	private void validateSlots(Map<Resource, StorageSlot> resources) {
		resources.forEach((resource, slot) -> {
			if (slot.getCapacity() < 0) {
				throw new IllegalArgumentException("Capacity cannot be negative: " + resource + ":" + slot.getCapacity());
			}
			if (slot.getCount() < 0) {
				throw new IllegalArgumentException("Capacity cannot be negative: " + resource + ":" + slot.getCount());
			}
			if (slot.getPrice() < 0) {
				throw new IllegalArgumentException("Capacity cannot be negative: " + resource + ":" + slot.getPrice());
			}
		});
	}

	/**
	 * This method does not remove any resources. It simply removes resources that have 0 capacity or less.
	 */
	public void clean() {
		Map<Resource, StorageSlot> newResources = new HashMap<>();
		this.resources.forEach((resource, slot) -> {
			if (slot.getCapacity() > 0) {
				newResources.put(resource, slot);
			}
		});
		this.resources.clear();
		this.resources.putAll(newResources);
	}

	public boolean isFull(Resource resource) {
		return getRemainingCapacity(resource) == 0;
	}

	private void calculatePrices() {
		resources.forEach((resource, slot) -> {
			if (slot.getCapacity() == 0) {
				return;
			}
			float percentTaken = slot.getCount() / (float) slot.getCapacity();
			float percentFree = 1f - percentTaken;
			float priceRange = resource.getMaxPrice() - resource.getMinPrice();
			int price = resource.getMinPrice() + (int) (priceRange * percentFree);
			slot.setPrice(price);
		});
		clean();
	}

	public FactoryStorage copy() {
		Map<Resource, StorageSlot> newResources = new HashMap<>();
		resources.forEach((resource, slot) -> {
			newResources.put(resource, new StorageSlot(slot));
		});
		return new FactoryStorage(newResources);
	}

	public void setCapacity(Resource resource, int capacity) {
		getSlot(resource).setCapacity(capacity);
	}

	public void changeCapacities(Map<Resource, Integer> capacityChanges) {
		capacityChanges.forEach((resource, change) -> {
			StorageSlot slot = getSlot(resource);
			slot.setCapacity(slot.getCapacity() + change);
		});
		clean();
	}

	public Map<Resource, StorageSlot> getResources() {
		return resources;
	}

	public Map<Resource, Integer> getPrices() {
		Map<Resource, Integer> prices = new HashMap<>();
		resources.forEach((resource, slot) -> {
			prices.put(resource, slot.getPrice());
		});
		return prices;
	}

	public Map<Resource, Integer> getCapacities() {
		Map<Resource, Integer> capacities = new HashMap<>();
		resources.forEach((resource, slot) -> {
			capacities.put(resource, slot.getCapacity());
		});
		return capacities;
	}

	private StorageSlot getSlot(Resource resource) {
		return resources.compute(resource, (r, slot) -> {
			if (slot == null) {
				return new StorageSlot(0, 0, 0);
			}
			return slot;
		});
	}

	@Override
	public String toString() {
		return "FactoryStorage{" + "resources=" + resources + '}';
	}
}
