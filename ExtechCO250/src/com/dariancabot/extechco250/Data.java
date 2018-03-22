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

import java.util.ArrayList;
import java.util.Date;


/**
 * Data Object.
 *
 * @author Darian Cabot
 */
public final class Data
{

    /**
     * The CO2 value of the reading on the meter.
     */
    public Value co2Value = new Value();

    /**
     * The dry-bulb temperature value of the reading on the meter.
     */
    public Value dbtValue = new Value();

    /**
     * The relative humidity temperature value of the reading on the meter.
     */
    public Value rhValue = new Value();

    /**
     * The dry-point temperature value of the reading on the meter.
     */
    public Value dptValue = new Value();

    /**
     * The wet-bulb temperature value of the reading on the meter.
     */
    public Value wbtValue = new Value();

    /**
     * The packet data direct from the communications buffer.
     */
    public byte[] packetRaw = null;

    /**
     * The packet data without the start/end bytes.
     */
    public byte[] packetTidy = null;


    /**
     * Value representation of the DMM.
     * <p>
     * This includes the value, measurement unit, and statistics.
     */
    public static class Value
    {

        private String value;
        private String valueVerbatim;

        /**
         * The measurement unit.
         */
        public Unit unit = new Unit();

        /**
         * Statistics for this value like minimum, maximum, average.
         */
        public Statistics statistics = new Statistics();


        /**
         * The measurement unit.
         */
        public static class Unit
        {
            private Measurement measurement = Measurement.NONE;


            /**
             * The unit measurement.
             * <p>
             * Measurements that can be used:
             * <ul>
             * <li>{@link #NONE}
             * <li>{@link #PPM}
             * <li>{@link #CELCIUS}
             * <li>{@link #FARENHEIT}
             * <li>{@link #PERCENT}
             * </ul>
             */
            public enum Measurement
            {
                NONE(null, null),
                PPM("ppm", "Parts Per Million"),
                CELCIUS("C", "Celcius"),
                FARENHEIT("F", "Farenheit"),
                PERCENT("%", "Percent");

                private final String abbreviation;
                private final String name;


                Measurement(String abbreviation, String name)
                {
                    this.abbreviation = abbreviation;
                    this.name = name;
                }


                /**
                 * Gets the abbreviation of the measurement unit.
                 *
                 * @return the measurement unit abbreviation.
                 */
                public String getAbbreviation()
                {
                    return this.abbreviation;
                }


                /**
                 * Gets the name of the measurement unit.
                 *
                 * @return the measurement unit name.
                 */
                public String getName()
                {
                    return this.name;
                }

            }


            /**
             * Gets the measurement type.
             *
             * @return the measurement type.
             */
            public Measurement getMeasurement()
            {
                return measurement;
            }


            /**
             * Sets the measurement type.
             *
             * @param measurement the measurement type.
             */
            public void setMeasurement(Measurement measurement)
            {
                this.measurement = measurement;
            }


            /**
             * Gets a String representation of the unit in a concise, readable format.
             *
             * <p>
             * Format: [prefix][measurement] [type]
             *
             * @return A representation of the unit.
             */
            @Override
            public String toString()
            {
                String unit = "";

                if (this.measurement.getAbbreviation() != null)
                {
                    unit += this.measurement.getAbbreviation();
                }

                unit = unit.trim();

                if (unit.isEmpty())
                {
                    unit = null;
                }

                return unit;
            }

        }


        public static class Statistics
        {

            private boolean isEnabled;

            private long samples;
            private Date durationStart = new Date();
            private long duration;
            private Double minimum;
            private Double maximum;
            private final ArrayList<Double> averageValues = new ArrayList<>();
            private Double average;


            /**
             * Enable or disable the accumulation of statistical data.
             * <p>
             * Disabling does not reset statistics data. To also clear data, call the {@link reset()} function.
             *
             * @param isEnabled true to enable, false to disable.
             */
            public void setEnabled(boolean isEnabled)
            {
                this.isEnabled = isEnabled;
            }


            /**
             * Gets the enabled status of the statistics.
             *
             * @return true if the statistics are enabled.
             */
            public boolean isEnabled()
            {
                return isEnabled;
            }


            /**
             * Resets all statistics by clearing all counters, averages, and other values. Dues not change the "enabled" parameter.
             */
            public void reset()
            {
                samples = 0;

                durationStart = new Date();
                duration = 0;

                minimum = null;
                maximum = null;
                average = null;
                averageValues.clear();
            }


            /**
             * Updates the statistics with a new value.
             *
             * @param value the reading/measurement value.
             */
            protected void update(double value)
            {
                if ( ! isEnabled)
                {
                    return;
                }

                if (samples < 1)
                {
                    // Statistics need to be initialised...
                    samples = 1;
                    durationStart = new Date();
                    duration = 500; // Start at half a second (refresh rate is 2Hz).
                    minimum = value;
                    maximum = value;
                    average = value;
                    averageValues.clear();
                    averageValues.add(value);
                }
                else
                {
                    samples += 1L;

                    Date now = new Date();
                    duration = (now.getTime() - durationStart.getTime()) / 1000L;

                    minimum = Math.min(minimum, value);
                    maximum = Math.max(maximum, value);

                    averageValues.add(value);
                    Double valueSum = 0d;

                    for (Double avgValue : averageValues)
                    {
                        valueSum += avgValue;
                    }

                    average = valueSum / averageValues.size();
                }
            }


            /**
             * Gets the number of samples used to calculate statistics.
             *
             * @return the number of samples.
             */
            public long getSamples()
            {
                return samples;
            }


            /**
             * Gets the date-time of when the statistics began (i.e. date-time of the first sample).
             *
             * @return the date-time when statistics began.
             */
            public Date getDurationStart()
            {
                return durationStart;
            }


            /**
             * Gets the duration of statistics gathering in seconds.
             *
             * @return the length of time in seconds.
             */
            public Long getDuration()
            {
                return duration;
            }


            /**
             * Gets the minimum value of all samples.
             *
             * @return the minimum value.
             */
            public Double getMinimum()
            {
                return minimum;
            }


            /**
             * Gets the maximum value of all samples.
             *
             * @return the maximum value.
             */
            public Double getMaximum()
            {
                return maximum;
            }


            /**
             * Gets the average of all samples.
             *
             * @return the average of all samples.
             */
            public Double getAverage()
            {
                return average;
            }

        }


        /**
         * The value as a string not including the measurement unit.
         * <p>
         * To include the measurement unit use the {@link #toString(boolean)} function.
         *
         * @return a String representation of the value.
         */
        @Override
        public String toString()
        {
            return toString(false);
        }


        /**
         * The value as a string not including the measurement unit.
         *
         * @param includeUnit true to include the measurement unit.
         *
         * @return a String representation of the value.
         */
        public String toString(boolean includeUnit)
        {
            String valueStr = value;

            if ((includeUnit) && (unit.toString() != null))
            {
                valueStr += " " + unit.toString();
            }

            return valueStr;
        }


        /**
         * Sets the value as a String.
         * <p>
         * If numerical it will also used in the statistics if enabled.
         *
         * @param value String of the value.
         */
        public void setValue(String value)
        {
            // Update the value.
            this.valueVerbatim = value;
            this.value = value.trim();

            // Update statistics if value is numeric...
            if (isNumeric(this.value))
            {
                statistics.update(Double.parseDouble(this.value));
            }
        }


        /**
         * Gets the value represented as a String that resembles what is displayed on the LCD.
         *
         * This also includes non-numerical values.
         * <p>
         * For an exact representation (including whitespace), use the {@link #getValueVerbatim() getValueVerbatim} method.
         * <p>
         * For a numerical value, {@link #getValueDouble() getValueDouble} use the method.
         *
         * @return
         */
        public String getValue()
        {
            return value;
        }


        /**
         * Gets a more accurate representation what the value looks like on the ExtechCO250 LCD including whitespace padding, etc.
         *
         * This also includes non-numerical values.
         *
         * @return String value that accurately represents Extech LCD.
         */
        public String getValueVerbatim()
        {
            return valueVerbatim;
        }


        /**
         * Gets the value as a Double if numerical, otherwise returns null.
         *
         * @return a Double value if numerical, or null if not-numerical.
         */
        public Double getValueDouble()
        {
            if (isNumeric(value))
            {
                return Double.parseDouble(value);
            }
            else
            {
                return null;
            }
        }


        /**
         * Match a number with optional '-' and decimal.
         *
         * Note: Will fail if non-latin (i.e. 0 to 9) digits used (for example, Arabic digits).
         *
         * @see http://stackoverflow.com/a/1102916
         *
         * @param string The String to check.
         *
         * @return boolean True if numeric, false if not.
         */
        private boolean isNumeric(String string)
        {
            return string.matches("-?\\d+(\\.\\d+)?");
        }

    }
}
