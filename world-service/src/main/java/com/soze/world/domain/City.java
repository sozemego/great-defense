package com.soze.world.domain;

public class City {

	public String id;
	public String name;

	public int factorySlots;

	public float latitude;
	public float longitude;

	@Override
	public String toString() {
		return "City{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", factorySlots=" + factorySlots + ", latitude=" + latitude + ", longitude=" + longitude + '}';
	}
}
