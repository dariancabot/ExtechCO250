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

import com.dariancabot.extechco250.exceptions.ProtocolException;
import java.util.Arrays;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;


/**
 * Communications Object.
 *
 * @author Darian Cabot
 */
public final class Communications implements SerialPortEventListener
{
    private SerialPort serialPort;
    private final Decoder decoder;

    private static final byte PACKET_END_BYTE_1 = 0x0d;
    private static final byte PACKET_END_BYTE_2 = 0x0a;
    private static final int PACKET_LENGTH = 45;

    private final byte[] packetBuffer = new byte[PACKET_LENGTH + 1];
    private boolean packetEndByte1 = false;
    private int packetBufferPosition = 0;

    /**
     * Used by {@link #bytesToHex(byte[])}
     */
    final protected static char[] HEX_ARRAY = "0123456789abcdef".toCharArray();


    //-----------------------------------------------------------------------
    /**
     * Creates a new Communications instance.
     *
     * @param serialPort the SerialPort to be used
     * @param decoder    the Decoder to be used
     */
    public Communications(SerialPort serialPort, Decoder decoder)
    {
        this.serialPort = serialPort;
        this.decoder = decoder;
    }


    //-----------------------------------------------------------------------
    /**
     * Gets the SerialPort used for communications.
     *
     * @return the SerialPort used for communications
     */
    protected SerialPort getSerialPort()
    {
        return serialPort;
    }


    //-----------------------------------------------------------------------
    /**
     * Sets the SerialPort to be used for communications.
     *
     * @param serialPort the new SerialPort
     */
    protected void setSerialPort(SerialPort serialPort)
    {
        this.serialPort = serialPort;
    }


    //-----------------------------------------------------------------------
    /**
     * Implementation of the serialEvent method to see events that happened to the port. This only report those events that are set in the SerialPort
     * mask.
     *
     * @param event the new SerialPort
     */
    @Override
    public void serialEvent(SerialPortEvent event)
    {

        switch (event.getEventType())
        {
            case SerialPortEvent.RXCHAR: // Data has been received.

                try
                {
                    byte[] rxBuffer = serialPort.readBytes();

                    if (rxBuffer == null)
                    {
                        // Serial input buffer is empty.
                        return;
                    }

                    for (int byteCount = 0; byteCount < rxBuffer.length; byteCount ++)
                    {
                        packetBuffer[packetBufferPosition] = rxBuffer[byteCount];

                        // Buffer overflow protection.
                        if (packetBufferPosition >= PACKET_LENGTH)
                        {
                            // Reset for next packet
                            Arrays.fill(packetBuffer, (byte) 0); // Clear the packet buffer.
                            packetBufferPosition = 0;
                            continue;
                        }

                        // First end byte already detected.
                        if (packetEndByte1)
                        {
                            if (packetBuffer[packetBufferPosition] == PACKET_END_BYTE_2)
                            {

                                // We have a valid packet - get the relevent section.
                                byte[] packet = Arrays.copyOfRange(packetBuffer, 0, packetBufferPosition + 1);

                                // TODO: Comment this out for production...
                                // Print valid packet in hex (debugging)...
                                // System.out.println(bytesToHex(packet)); // Hex format.
                                // System.out.println(new String(packet, 0, packet.length)); // ASCII format.
                                //
                                // Decode the packet.
                                decoder.decodePacket(packet);

                                // Reset buffer ready for the next packet...
                                Arrays.fill(packetBuffer, (byte) 0); // Clear the packet buffer.
                                packetBufferPosition = 0;
                                continue;
                            }
                            else
                            {
                                packetEndByte1 = false;
                            }
                        }

                        if (packetBuffer[packetBufferPosition] == PACKET_END_BYTE_1)
                        {
                            packetEndByte1 = true;
                        }

                        packetBufferPosition ++;
                    }
                }
                catch (SerialPortException | ProtocolException e)
                {
                    ProtocolException pex = new ProtocolException("Error receiving serial data", e);
                    throw pex;
                }

                break;

            default:
                break;
        }
    }


    //-----------------------------------------------------------------------
    /**
     * Converts a byte array into a hex String.
     *
     * @param bytes A byte array to be converted to a HEX String
     *
     * @return A String of HEX representation of the passed byte array
     *
     * @see http://stackoverflow.com/a/9855338
     */
    private String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j ++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = HEX_ARRAY[v >>> 4];
            hexChars[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            hexChars[j * 3 + 2] = 0x20; // Space character.
        }

        return new String(hexChars);
    }

}
