/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates.  All rights reserved.
 *
 * This software is dual-licensed to you under the MIT License (MIT) and 
 * the Universal Permissive License (UPL).  See the LICENSE file in the root
 * directory for license terms.  You may choose either license, or both.
 */

package test;

/**
 * A simulated humidity sensor for use in the samples. The sensor has humidity
 * and maximum threshold attributes.
 */
public class HumiditySensor
{

	// The min range for values is 0% humidity
	// private final int hmin = 0;

	// The hmin range for values is 100% humidity
	private final int hmax = 100;

	// Maximum humidity threshold, writable
	private int maxThreshold;

	// A unique identifier for this sensor
	private final String hardwareId;

	// The HumiditySensor simulates humidity data by generating values that
	// fluctuate around the 'set point', arbitrarily set to 75% humidity.
	private final int setPoint = 75;

	// Used to generate fluctuation around the 'set point'
	private float angle = 0f;

	// amplitude is the maximum deviation from the 'set point'
	private int amplitude = hmax - setPoint;

	/**
	 * Create a HumiditySensor
	 * 
	 * @param id
	 * a unique identifier for this sensor
	 */
	public HumiditySensor(String id)
	{
		hardwareId = id;

		// set initial max threshold to 80%
		maxThreshold = 80;
	}

	/**
	 * Get the current humidity value. The value returned is between 0 and 100,
	 * representing a percent humidity.
	 * 
	 * @return the current humidity value.
	 */
	public synchronized int getHumidity()
	{
		final double delta = amplitude * Math.sin(Math.toRadians(angle));
		angle += 15;
		return (int) Math.round(setPoint + delta);
	}

	/**
	 * Set the maximum percent humidity threshold for alerts. The value is
	 * clamped to the range [60..100] and represents a whole percentage.
	 * 
	 * @param threshold
	 *            a value between 60 and 100
	 */
	public synchronized void setMaxThreshold(int threshold)
	{
		if (threshold < 60)
			maxThreshold = 60;
		else if (threshold > 100)
			maxThreshold = 100;
		else
			maxThreshold = threshold;
	}

	/**
	 * Get the maximum threshold value.
	 * 
	 * @return the maximum threshold
	 */
	public synchronized int getMaxThreshold()
	{
		return maxThreshold;
	}

	/**
	 * Get the manufacturer name, which can be used as part of the device
	 * meta-data.
	 * 
	 * @return the manufacturer name
	 */
	public String getManufacturer()
	{
		return "Sample";
	}

	/**
	 * Get the model number, which can be used as part of the device meta-data.
	 * 
	 * @return the model number
	 */
	public String getModelNumber()
	{
		return "MN-" + hardwareId;
	}

	/**
	 * Get the serial number, which can be used as part of the device meta-data.
	 * 
	 * @return the serial number
	 */
	public String getSerialNumber()
	{
		return "SN-" + hardwareId;
	}

	/**
	 * Get the hardware id, which can be used as part of the device meta-data.
	 * 
	 * @return the hardware id
	 */
	public String getHardwareId()
	{
		return hardwareId;
	}

}
