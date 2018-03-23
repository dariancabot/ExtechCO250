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

import com.dariancabot.extechco250.Data.Value.Unit.Measurement;
import com.dariancabot.extechco250.exceptions.ProtocolException;
import java.nio.charset.Charset;
import java.util.Arrays;


/**
 * The Decoder class is used to decode data packets from a Extech CO250 meter and update a provided {@link Data} Object with the acquired data.
 *
 * @author Darian Cabot
 */
public final class Decoder
{
    private final Data data;
    private EventListener eventListener;

    private static final byte PACKET_PREAMBLE_START_BYTE = 0x24; // Dollar sign.
    private static final int PACKET_PREAMBLE_LENGTH = 20; // Preamble length excluding line break bytes.
    private static final byte PACKET_END_BYTE_1 = 0x0D;
    private static final byte PACKET_END_BYTE_2 = 0x0A;


    //-----------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param data the Data object to be used
     */
    public Decoder(Data data)
    {
        this.data = data;
    }


    //-----------------------------------------------------------------------
    /**
     * Decodes a complete serial packet from the Extech CO250 DMM. The decoded data will populate the provided Data object, and notifies when complete
     * using the EventListener.
     *
     * @param packet The packet as a byte array. Must be 43 bytes long.
     *
     * @throws ProtocolException If the packet is invalid or unable to decode.
     */
    public void decodePacket(byte[] packet) throws ProtocolException
    {
        // Remove line break charcters (if found).
        if ((packet.length >= 2)
                && (packet[packet.length - 2] == PACKET_END_BYTE_1)
                && (packet[packet.length - 1] == PACKET_END_BYTE_2))
        {
            // Overwrite packet excluding the last 2 bytes.
            packet = Arrays.copyOfRange(packet, 0, packet.length - 2);
        }

        // Verfiy the checksum.
        if ( ! checksumValid(packet))
        {
            ProtocolException ex = new ProtocolException("Decode error: Packet checksum is invalid.");
            throw ex;
        }

        // Check for start byte of packet.
        if (packet[0] == PACKET_PREAMBLE_START_BYTE)
        {
            // This is the preamble/description line.
            // Check that is matches what is expected from the Extech CO250 (i.e. determine correct device is being read).
            // Note: The EventListener will not be called from this line as it has no useful values!

            // Check packet length.
            if (packet.length != PACKET_PREAMBLE_LENGTH)
            {
                ProtocolException ex = new ProtocolException("Decode error: Packet preamble length is " + packet.length + ", but should be " + PACKET_PREAMBLE_LENGTH + ".");
                throw ex;
            }

            // Check this line is as expected for CO250.
            String preamble = new String(packet, Charset.forName("US-ASCII"));

            if ( ! preamble.contains("CO2:Air:RH:DP:WBT"))
            {
                ProtocolException ex = new ProtocolException("Decode error: Packet preamble (" + preamble + ") does not match compatible Extech CO250 device.");
                throw ex;
            }
        }
        else
        {
            // This is the live values line.
            data.packet = packet; // Set the packet value.

            // Convert to String - excluding last two checksum characters.
            String dataPacket = new String(packet, 0, packet.length - 2);

            // Split into sections at each ':' colon character.
            String[] vals = dataPacket.split(":");

            // Check each section by leading character, then parse to nuerical value, and update data model.
            for (String val : vals)
            {
                switch (val.substring(0, 1))
                {
                    case "C": // CO2.
                        data.co2Value.setValue(val.substring(1, val.length() - 3));

                        if (val.contains("ppm"))
                        {
                            data.co2Value.unit.setMeasurement(Measurement.PPM);
                        }

                        break;

                    case "T": // Air / dry-bulb temperature.
                        data.dbtValue.setValue(val.substring(1, val.length() - 1));

                        if (val.substring(val.length() - 1).equals("C"))
                        {
                            data.dbtValue.unit.setMeasurement(Measurement.CELCIUS);
                        }
                        else if (val.substring(val.length() - 1).equals("F"))
                        {
                            data.dbtValue.unit.setMeasurement(Measurement.FARENHEIT);
                        }

                        break;

                    case "H": // Relative humidity.
                        data.rhValue.setValue(val.substring(1, val.length() - 1));

                        if (val.substring(val.length() - 1).equals("%"))
                        {
                            data.rhValue.unit.setMeasurement(Measurement.PERCENT);
                        }

                        break;

                    case "d": // Dew-point temperature.
                        data.dptValue.setValue(val.substring(1, val.length() - 1));

                        if (val.substring(val.length() - 1).equals("C"))
                        {
                            data.dptValue.unit.setMeasurement(Measurement.CELCIUS);
                        }
                        else if (val.substring(val.length() - 1).equals("F"))
                        {
                            data.dptValue.unit.setMeasurement(Measurement.FARENHEIT);
                        }

                        break;

                    case "w": // Wet-bulb temperature.
                        data.wbtValue.setValue(val.substring(1, val.length() - 1));

                        if (val.substring(val.length() - 1).equals("C"))
                        {
                            data.wbtValue.unit.setMeasurement(Measurement.CELCIUS);
                        }
                        else if (val.substring(val.length() - 1).equals("F"))
                        {
                            data.wbtValue.unit.setMeasurement(Measurement.FARENHEIT);
                        }

                        break;

                    default:
                        ProtocolException ex = new ProtocolException("Decode error: Unknown value designator '" + val.substring(0, 1) + "'.");
                        throw ex;
                }
            }

            // Notify using the event listener if one is set.
            if (eventListener != null)
            {
                eventListener.dataUpdateEvent();
            }
        }
    }


    //-----------------------------------------------------------------------
    /**
     * Checks a line of data (including checksum bytes, excluding line break bytes) and determines if the checksum is valid.
     *
     * @param data The line data excluding line break bytes (the last 2 bytes should be checksum).
     *
     * @return true if the checksum is correct, otherwise false.
     */
    private boolean checksumValid(byte[] data)
    {
        int intChecksum = 0;

        // The checksum in the data is an ASCII representation of a Hex value (2 characters).
        // Get these two characters/bytes.
        String strChecksum = new String(Arrays.copyOfRange(data, data.length - 2, data.length), Charset.forName("US-ASCII"));

        // Now convert them to get the actual checksum byte value. This is used for comparison later.
        byte byteChecksum = (byte) ((Character.digit(strChecksum.charAt(0), 16) << 4)
                + Character.digit(strChecksum.charAt(1), 16));

        // Calculate the checksum (8-bit sum 2s complement)...
        for (int i = 0; i < data.length - 2; i ++)
        {
            intChecksum += data[i];
        }

        byte checksum = (byte) (intChecksum & 0xff); // Get low byte.
        checksum = (byte) ( ~ checksum); // Invert / discard high bit.
        checksum += 1; // Add 1 for 2s compliment.

        // Compare and return result.
        return (checksum == byteChecksum);
    }


    //-----------------------------------------------------------------------
    /**
     * Sets an EventListener to be notified when data is received over the Serial Port.
     *
     * @param eventListener An EventListener Object to be notified when data is received
     */
    public void setEventListener(EventListener eventListener)
    {
        this.eventListener = eventListener;
    }

}
