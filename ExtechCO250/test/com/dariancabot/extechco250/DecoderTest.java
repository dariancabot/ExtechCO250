/*
 * The MIT License
 *
 * Copyright 2018 Darian.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dariancabot.extechco250;

import com.dariancabot.extechco250.exceptions.ProtocolException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 *
 * @author Darian Cabot
 */
public class DecoderTest
{

    public DecoderTest()
    {
    }


    @BeforeClass
    public static void setUpClass()
    {
    }


    @AfterClass
    public static void tearDownClass()
    {
    }


    @Before
    public void setUp()
    {
    }


    @After
    public void tearDown()
    {
    }

    //-----------------------------------------------------------------------
    /**
     * Rule for testing of correctly thrown Exceptions.
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();


    //-----------------------------------------------------------------------
    /**
     * Test of decodePacket method, of class Decoder.
     *
     * Invalid checksum, should throw ProtocolException.
     */
    @Test
    public void testDecodePacket01()
    {
        Data data = new Data();
        Decoder decoder = new Decoder(data);

        // Preamble line (invalid checksum).
        String line = "$CO2:Air:RH:DP:WBTff";

        thrown.expect(ProtocolException.class);
        thrown.expectMessage("Decode error: Packet checksum is invalid.");
        decoder.decodePacket(line.getBytes());
    }


    //-----------------------------------------------------------------------
    /**
     * Test of decodePacket method, of class Decoder.
     *
     * Invalid checksum, should throw ProtocolException.
     */
    @Test
    public void testDecodePacket02()
    {
        Data data = new Data();
        Decoder decoder = new Decoder(data);

        // Preamble line (invalid checksum).
        String line = "C1115ppm:T26.3C:H52.9%:d15.9C:w19.4C2c";

        thrown.expect(ProtocolException.class);
        thrown.expectMessage("Decode error: Packet checksum is invalid.");
        decoder.decodePacket(line.getBytes());
    }


    //-----------------------------------------------------------------------
    /**
     * Test of decodePacket method, of class Decoder.
     *
     * Incompatible preamble, should throw ProtocolException.
     */
    @Test
    public void testDecodePacket03()
    {
        Data data = new Data();
        Decoder decoder = new Decoder(data);

        // Preamble line (not Extech CO250 compatible).
        String line = "$CO2:Abc:RH:DP:WBT0f";

        thrown.expect(ProtocolException.class);
        thrown.expectMessage("Decode error: Packet preamble ($CO2:Abc:RH:DP:WBT0f) does not match compatible Extech CO250 device.");
        decoder.decodePacket(line.getBytes());
    }

}
