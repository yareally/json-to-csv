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

import com.cc.json2csv.Json2Csv
import scala.Predef._
import java.util
import util.{LinkedHashMap ⇒ Lmap, LinkedHashSet ⇒ Lset}

/**
 * Example using the json2csv class
 *
 * @author Wes Lanning
 * @version 2013-07-06
 */
object Json2CsvTest
{
    /**
     * Order matters, as that is what they will be returned as in CSV
     * (also why linked sets/maps are used)
     */
    val needles = new Lmap[String, Lset[String]]()
    {
        put("background_activities",
            new Lset[String]()
            {
                add("calories_burned")
                add("steps")
                add("timestamp")
                add("uri")
            }
        )
        put("diabetes", null)
        put("fitness_activities",
            new Lset[String]()
            {
                add("duration")
                add("entry_mode")
                add("has_path")
                add("source")
                add("start_time")
                add("total_calories")
                add("total_distance")
                add("type")
                add("uri")
            }
        )
    }


    val source = scala.io.Source.fromFile("example.json")
    val lines  = source.mkString
    source.close()

    def main(args: Array[String])
    {
        val json2csv = new Json2Csv()

        // loop over headerData outer
        val csvLines = json2csv.toCsvStr(needles, lines)

        print(csvLines)
    }
}

