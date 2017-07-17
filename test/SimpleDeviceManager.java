package test;

import oracle.iot.client.DeviceModel;
import oracle.iot.client.device.Alert;
import oracle.iot.client.device.DirectlyConnectedDevice;
import oracle.iot.client.device.VirtualDevice;

import java.io.IOException;
import java.util.Date;

/*
 * This sample presents a simple sensor as virtual
 * device to the IoT server.
 *
 * The simple sensor is polled every 30 second and the virtual device is
 * updated. An alert is generated when the value from the sensor exceeds
 * the maximum threshold of the sensor's data model.
 *
 */

public class SimpleDeviceManager
{
	/*
	 * 0. Register the Device in IoT CS - download file with credentials
	 * 
	 */

	/*
	 * 1. Define the URN of the Device Model to use (attributes, commands) the
	 * Device Model has been defined in IoT CS
	 */
	private static final String HUMIDITY_SENSOR_MODEL_URN = "urn:com:oracle:iot:device:humidity_sensor";

	// define name of attribute for the
	// the Device model
	private static final String HUMIDITY_ATTRIBUTE = "humidity";
	private static final String MAX_THRESHOLD_ATTRIBUTE = "maxThreshold";
	private static final String TOO_HUMID_ALERT = HUMIDITY_SENSOR_MODEL_URN + ":too_humid";

	/**
	 * sensor polling interval before sending next readings. Could be configured
	 * using {@code com.oracle.iot.sample.sensor_polling_interval} property
	 * 
	 * set to 30 secs (L.S.)
	 */
	private static final long SENSOR_POLLING_INTERVAL = Long.getLong("com.oracle.iot.sample.sensor_polling_interval",
			5000);

	// The following calculations of number_of_loops and sleepTime break the
	// SENSOR_POLLING_INTERVAL into a number of smaller intervals approximately
	// SLEEP_TIME milliseconds long. This just makes the sample a little more
	// responsive to keyboard input and has nothing to do with the client
	// library.
	private static final int SLEEP_TIME = 100;
	private static long number_of_loops = (SLEEP_TIME > SENSOR_POLLING_INTERVAL ? 1
			: SENSOR_POLLING_INTERVAL / SLEEP_TIME);
	private static long sleepTime = (SLEEP_TIME > SENSOR_POLLING_INTERVAL ? SENSOR_POLLING_INTERVAL
			: SLEEP_TIME + (SENSOR_POLLING_INTERVAL - number_of_loops * SLEEP_TIME) / number_of_loops);

	public static boolean exiting = false;

	/**
	 * 
	 * Main
	 * 
	 * @param args:
	 *            nome file pwd
	 */
	public static void main(String[] args)
	{
		SimpleDeviceManager deviceClass = new SimpleDeviceManager();

		DirectlyConnectedDevice directlyConnectedDevice = null;

		try
		{
			if (args.length != 2)
			{
				display("\nIncorrect number of arguments.\n");
				throw new IllegalArgumentException("");
			}

			/*
			 * 2. Initialize the device client
			 * 
			 * parameters: a. name of configuration file (downloaded from IoT CS)
			 *  		   b. pwd to protect it
			 */
			directlyConnectedDevice = new DirectlyConnectedDevice(args[0], args[1]);

			/*
			 * 3. if the device is not yet activated, Activate it
			 */
			if (!directlyConnectedDevice.isActivated())
			{
				display("\nActivating...");

				directlyConnectedDevice.activate(HUMIDITY_SENSOR_MODEL_URN);
			}

			/*
			 * The Java class that simulate the sensor (in the same package) the
			 * simulator generate a Humidity value fluctuating around 75%
			 */
			final HumiditySensor sensor = new HumiditySensor(directlyConnectedDevice.getEndpointId() + "_Sample_HS");

			/*
			 * 4. Create a virtual device implementing the device model
			 * 
			 * here it attaches a Device Model to the Device
			 */
			final DeviceModel deviceModel = directlyConnectedDevice.getDeviceModel(HUMIDITY_SENSOR_MODEL_URN);

			final VirtualDevice virtualHumiditySensor = directlyConnectedDevice
					.createVirtualDevice(directlyConnectedDevice.getEndpointId(), deviceModel);

			//
			// shows header for displayed data
			//
			displayHeader(deviceModel, virtualHumiditySensor);

			// alert if HUM > threshold
			final Alert tooHumidAlert = virtualHumiditySensor.createAlert(TOO_HUMID_ALERT);

			/*
			 * Callbacks Definition
			 * 
			 * Monitor the virtual device for requested attribute changes and
			 * errors.
			 *
			 * Since there is only one attribute, maxThreshold, this could have
			 * done with using an attribute specific on change handler.
			 */
			virtualHumiditySensor.setOnError(new VirtualDevice.ErrorCallback<VirtualDevice>() {
				public void onError(VirtualDevice.ErrorEvent<VirtualDevice> event)
				{
					VirtualDevice device = event.getVirtualDevice();
					display(new Date().toString() + " : onError : " + device.getEndpointId() + " : \""
							+ event.getMessage() + "\"");
				}
			});

			display("\nPress enter to exit !!!\n\n");

			StringBuilder consoleMessage = new StringBuilder(">>> Readings").append("( every ")
					.append(SENSOR_POLLING_INTERVAL).append(" msec.)\n");

			display(consoleMessage.toString());

			/*
			 * A flag to make sure alerts are only sent when crossing the
			 * threshold.
			 */
			boolean humidityAlerted = false;

			/**
			 * 
			 * Main Loop
			 * 
			 */
			while (true)
			{
				/**
				 * Simulate a reading from "real sensor" here you get the value
				 * "from the sensor", that is sent to IoT CS
				 * 
				 */
				int humidity = sensor.getHumidity();

				consoleMessage = new StringBuilder(new Date().toString()).append(" :" + " Set : \"")
						.append(HUMIDITY_ATTRIBUTE).append("\"=").append(humidity);

				// Set the sensor value in an 'update'. If
				// the humidity maxThreshold has changed, that maxThreshold
				// attribute
				// will be added to the update (see the block below). A call to
				// 'finish' will commit the update.

				/*
				 * 5. send update to IoT CS (together with finish)
				 */
				virtualHumiditySensor.update().set(HUMIDITY_ATTRIBUTE, humidity);

				int humidityThreshold = sensor.getMaxThreshold();

				display(consoleMessage.toString());

				/*
				 * 6. commit changes
				 */
				virtualHumiditySensor.finish();

				if (humidity > humidityThreshold)
				{
					if (!humidityAlerted)
					{
						humidityAlerted = true;
						consoleMessage = new StringBuilder(new Date().toString()).append(" : Alert : \"")
								.append(HUMIDITY_ATTRIBUTE).append("\"=").append(humidity).append(",\"")
								.append(MAX_THRESHOLD_ATTRIBUTE).append("\"=").append(humidityThreshold);
						display(consoleMessage.toString());

						//
						// send the alert...
						//
						tooHumidAlert.set(HUMIDITY_ATTRIBUTE, humidity).raise();
					}
				} else
				{
					humidityAlerted = false;
				}

				//
				// Wait SENSOR_POLLING_INTERVAL seconds before sending the next
				// reading.
				deviceClass.delay();
			}
		} catch (Throwable e)
		{
			// catching Throwable, not Exception:
			// could be java.lang.NoClassDefFoundError
			// which is not Exception

			displayException(e);

		} finally
		{
			// Dispose of the device client
			try
			{
				if (directlyConnectedDevice != null)
					directlyConnectedDevice.close();
			} catch (IOException ignored)
			{
			}
		}
	}

	/**
	 * showUsage: here you see parameters to be passed on the CMD line
	 */
	private static void showUsage()
	{
		Class<?> thisClass = new Object() {
		}.getClass().getEnclosingClass();
		display("Usage: \n" + "java " + thisClass.getName() + " <trusted assets file> <trusted assets password>\n");
	}

	private static void display(String string)
	{
		System.out.println(string);
	}

	private static void displayException(Throwable e)
	{
		StringBuilder sb = new StringBuilder(e.getMessage() == null ? e.getClass().getName() : e.getMessage());
		if (e.getCause() != null)
		{
			sb.append(".\n\tCaused by: ");
			sb.append(e.getCause());
		}
		System.out.println('\n' + sb.toString() + '\n');
		showUsage();
	}

	private static void displayHeader(DeviceModel deviceModel, VirtualDevice virtualHumiditySensor)
	{
		display("");
		display(">>> Beginning of simulation...\n");
		display("Information on the Device: \n");
		display("Device Model: " + deviceModel.getName());
		display("\nCreated Virtual Device Sensor, device ID = " + virtualHumiditySensor.getEndpointId() + "\n");
	}

	// The SENSOR_POLLING_INTERVAL is broken into a number of
	// smaller increments
	// to make the application more responsive to a keypress, which
	// will cause
	// the application to exit.
	private void delay()
	{
		for (int i = 0; i < number_of_loops; i++)
		{
			try
			{
				Thread.sleep(sleepTime);

				// User pressed the enter key while sleeping, exit.
				if (System.in.available() > 0)
				{
					System.exit(0);
				}
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
