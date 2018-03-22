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
// TODO: DJC 20180321: Convert from Protek 608 to Extech CO250.
package com.dariancabot.extechco250;

import com.dariancabot.extechco250.exceptions.ProtocolException;


/**
 * The Decoder class is used to decode data packets from a Extech CO250 meter and update a provided {@link Data} Object with the acquired data.
 *
 * @author Darian Cabot
 */
public final class Decoder
{
    private final Data data;
    private EventListener eventListener;

    private static final byte PACKET_START_BYTE = 0x24; // Dollar sign.
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
     * Decodes a Extech CO250 packet, updates the Data object, and notifies when complete using the EventListener.
     *
     * @param buffer The packet as a byte array. Must be 43 bytes long.
     *
     * @throws ProtocolException If the packet is invalid or unable to decode.
     */
    public void decodeSerialData(byte[] buffer) throws ProtocolException
    {

    }


    //-----------------------------------------------------------------------
    /**
     * Decodes a complete serial packet from the Extech CO250 DMM. The decoded data will populate the provided Data object.
     *
     * @param packet
     *
     */
    private void decodePacket(byte[] packet)
    {

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
