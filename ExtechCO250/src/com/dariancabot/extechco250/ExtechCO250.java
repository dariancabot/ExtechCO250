/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Darian Cabot
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.dariancabot.extechco250;

import jssc.SerialPort;
import jssc.SerialPortException;


/**
 * Extech CO250 Object.
 *
 * <p>
 * Models the attributes, commands, and measurement of the Extech CO250 indoor air quality meter.
 * <p>
 * Connection is established using the meter's RS-232 port and a host system Serial Port using the jSSC library.
 *
 * @author Darian Cabot
 */
public final class ExtechCO250
{
    private Communications communications;
    private SerialPort serialPort;
    private final Decoder decoder;

    private String[] portNames;
    private boolean isConnected;

    /**
     * Stores all of the readings data, both most recent and statistical.
     */
    public Data data;


    //-----------------------------------------------------------------------
    /**
     * Constructor.
     *
     */
    public ExtechCO250()
    {
        data = new Data();
        decoder = new Decoder(data);
    }


    //-----------------------------------------------------------------------
    /**
     * Gets the found available Serial Port on the host system.
     *
     * @return a String array of available Serial Ports, empty array if none found.
     */
    public String[] getPortNames()
    {
        // For more com port details (jssc can only give name), see: http://stackoverflow.com/q/6362775
        portNames = jssc.SerialPortList.getPortNames();

        return portNames;
    }


    //-----------------------------------------------------------------------
    private void serialWrite(Byte data)
    {
        if (serialPort.isOpened())
        {
            try
            {
                serialPort.writeByte(data);
            }
            catch (SerialPortException e)
            {
                System.out.println("Failed to write.");
            }
        }
    }


    //-----------------------------------------------------------------------
    /**
     * Connects/opens the Serial Port connection.
     *
     * @param port The String representation of the Serial Port (i.e. "COM3")
     *
     * @return true if connection successful, otherwise false.
     */
    public boolean connectSerialPort(String port)
    {
        if ( ! isConnected)
        {
            if ( ! port.isEmpty())
            {
                serialPort = new SerialPort(port);
                initialiseSerialReader();

                return connectSerialPort();
            }
        }

        return false;
    }


    //-----------------------------------------------------------------------
    /**
     * Disconnects/closes the Serial Port connection.
     */
    public void disconnectSerialPort()
    {
        if (isConnected)
        {
            try
            {
                serialPort.closePort();
                isConnected = false;
                data.co2Value.statistics.setEnabled(false);
                data.dbtValue.statistics.setEnabled(false);
                data.rhValue.statistics.setEnabled(false);
                data.dptValue.statistics.setEnabled(false);
                data.wbtValue.statistics.setEnabled(false);
            }
            catch (SerialPortException spe)
            {
                System.err.println("Error closing Serial Port: " + spe.getMessage());
            }
        }
    }


    //-----------------------------------------------------------------------
    private void initialiseSerialReader()
    {
        if (serialPort == null)
        {
            System.err.println("SerialPort must be set before SerialPortReader is initialised!");
            return;
        }

        communications = new Communications(serialPort, decoder);
    }


    //-----------------------------------------------------------------------
    private boolean connectSerialPort()
    {
        try
        {
            serialPort.openPort(); // Open port
            serialPort.setParams(9600, 8, 1, 0); // Set params
            int mask = SerialPort.MASK_RXCHAR; // Prepare mask
            serialPort.setEventsMask(mask); // Set mask
            serialPort.addEventListener(communications); // Add SerialPortEventListener

            System.out.println("Connected to serial port: " + serialPort.getPortName() + ".");

            isConnected = true;
            data.co2Value.statistics.setEnabled(true);
            data.dbtValue.statistics.setEnabled(true);
            data.rhValue.statistics.setEnabled(true);
            data.dptValue.statistics.setEnabled(true);
            data.wbtValue.statistics.setEnabled(true);

            return true;
        }
        catch (SerialPortException ex)
        {
            System.err.println(ex);
            return false;
        }
    }


    //-----------------------------------------------------------------------
    /**
     * Sets an EventListener to be notified when data is received over the Serial Port.
     *
     * @param eventListener An EventListener Object to be notified when data is received
     */
    public void setEventListener(EventListener eventListener)
    {
        this.decoder.setEventListener(eventListener);
    }

}
