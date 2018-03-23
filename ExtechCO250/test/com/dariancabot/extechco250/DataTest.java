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

import static org.hamcrest.Matchers.*;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 *
 * @author Darian Cabot
 */
public class DataTest
{

    public DataTest()
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


    @Test
    public void testBasicSetAndGet()
    {
        Data data = new Data();

        // Test text value
        data.co2Value.setValue("  A b c d ");
        assertThat(data.co2Value.getValueVerbatim(), equalTo("  A b c d "));
        assertThat(data.co2Value.getValue(), equalTo("A b c d"));
        assertThat(data.co2Value.getValueDouble(), equalTo(null));

        // Test numerical value
        data.co2Value.setValue("  -123.456 ");
        data.dbtValue.setValue("  50.00 ");

        assertThat(data.co2Value.getValueVerbatim(), equalTo("  -123.456 "));
        assertThat(data.co2Value.getValue(), equalTo("-123.456"));
        assertThat(data.co2Value.getValueDouble(), equalTo( - 123.456));

        assertThat(data.dbtValue.getValue(), equalTo("50.00"));

        // Test toString() methods
        assertThat(data.co2Value.toString(), equalTo("-123.456"));
        assertThat(data.co2Value.toString(false), equalTo("-123.456"));
        assertThat(data.co2Value.toString(true), equalTo("-123.456"));

        // Test unit
        data.co2Value.unit.setMeasurement(Data.Value.Unit.Measurement.PPM);
        assertThat(data.co2Value.unit.toString(), equalTo("ppm"));

        // Test toString() methods
        assertThat(data.co2Value.toString(), equalTo("-123.456"));
        assertThat(data.co2Value.toString(false), equalTo("-123.456"));
        assertThat(data.co2Value.toString(true), equalTo("-123.456 ppm"));

    }


    @Test
    public void testStatistics()
    {
        Data data = new Data();

        // Statistics not enabled yet, check null/default...
        assertThat(data.co2Value.statistics.getMinimum(), equalTo(null));
        assertThat(data.co2Value.statistics.getMaximum(), equalTo(null));
        assertThat(data.co2Value.statistics.getAverage(), equalTo(null));
        assertThat(data.co2Value.statistics.getSamples(), equalTo(0L));

        // Enable statistics.
        data.co2Value.statistics.setEnabled(true);

        // Loop in a bunch of values...
        for (int i = 1000; i <= 3000; i ++)
        {
            data.co2Value.setValue(String.valueOf(i));
        }

        assertThat(data.co2Value.statistics.getMinimum(), equalTo(1000d));
        assertThat(data.co2Value.statistics.getMaximum(), equalTo(3000d));
        assertThat(data.co2Value.statistics.getAverage(), equalTo(2000d));
        assertThat(data.co2Value.statistics.getSamples(), equalTo(2001L));

        // Reset statistics.
        data.co2Value.statistics.reset();

        // Statistics reset, check null/default...
        assertThat(data.co2Value.statistics.getMinimum(), equalTo(null));
        assertThat(data.co2Value.statistics.getMaximum(), equalTo(null));
        assertThat(data.co2Value.statistics.getAverage(), equalTo(null));
        assertThat(data.co2Value.statistics.getSamples(), equalTo(0L));

        // Statistics should still be enabled. Loop in a bunch of values...
        for (int i = 1000; i <= 3000; i ++)
        {
            data.co2Value.setValue(String.valueOf(i));
        }

        assertThat(data.co2Value.statistics.getMinimum(), equalTo(1000d));
        assertThat(data.co2Value.statistics.getMaximum(), equalTo(3000d));
        assertThat(data.co2Value.statistics.getAverage(), equalTo(2000d));
        assertThat(data.co2Value.statistics.getSamples(), equalTo(2001L));

        // Disable statistics.
        data.co2Value.statistics.setEnabled(false);

        // Set value, this should not be counted on statistics.
        data.co2Value.setValue("-50");
        data.co2Value.setValue("50000");

        // Check that nothing changed...
        assertThat(data.co2Value.statistics.getMinimum(), equalTo(1000d));
        assertThat(data.co2Value.statistics.getMaximum(), equalTo(3000d));
        assertThat(data.co2Value.statistics.getAverage(), equalTo(2000d));
        assertThat(data.co2Value.statistics.getSamples(), equalTo(2001L));

        // Enable statistics.
        data.co2Value.statistics.setEnabled(true);

        // Set value, this should be counted on statistics.
        data.co2Value.setValue("-50");
        data.co2Value.setValue("50000");

        // Check that the new values were applied...
        assertThat(data.co2Value.statistics.getMinimum(), equalTo( - 50d));
        assertThat(data.co2Value.statistics.getMaximum(), equalTo(50000d));
        assertEquals(data.co2Value.statistics.getAverage(), 2022.94058, 0.00001);
        assertThat(data.co2Value.statistics.getSamples(), equalTo(2003L));

    }

}
