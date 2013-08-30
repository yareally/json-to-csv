/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Wes Lanning
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

package com.cc.json2csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Example of how json2csv works in Java
 * This uses the prebuilt json2csv.jar
 */
public class Main
{
    public static void main(String... args)
    {
        Map<String, LinkedHashSet<String>> needles = new LinkedHashMap<>();
        LinkedHashSet<String> headers = new LinkedHashSet<>();
        headers.add("calories_burned");
        headers.add("steps");
        headers.add("timestamp");
        headers.add("uri");
        needles.put("background_activities", headers);

        needles.put("diabetes", null);

        LinkedHashSet<String> headers2 = new LinkedHashSet<>();
        headers2.add("duration");
        headers2.add("entry_mode");
        headers2.add("has_path");
        headers2.add("source");
        headers2.add("start_time");
        headers2.add("total_calories");
        headers2.add("total_distance");
        headers2.add("type");
        headers2.add("uri");

        needles.put("fitness_activities", headers2);

        Json2Csv json2Csv = new Json2Csv();
        String json = readFile();
        System.out.print(json2Csv.toCsvStr(needles, json));
    }

    /**
     * Read in a json file for the example
     * @return the json file contents
     */
    private static String readFile()
    {
        Charset charset = Charset.forName("UTF-8");
        Path file = FileSystems.getDefault().getPath("assets", "example.json");
        StringBuilder strBuilder = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuilder.append(line);
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
        return strBuilder.toString();
    }
}
