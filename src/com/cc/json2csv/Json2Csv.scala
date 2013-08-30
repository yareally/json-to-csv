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

package com.cc.json2csv

import play.api.libs.json._
import scala.Predef._
import java.util
import scala.collection.JavaConversions._
import util.{Map ⇒ Jmap, LinkedHashMap ⇒ Lmap, Set ⇒ Jset, LinkedHashSet ⇒ Lset}

/**
 * Converts a JSON object string into one of CSV.
 * Assumptions: JSON is properly formatted and is of non-zero length
 */
class Json2Csv()
{
    private var maxFound = -1 // how many values found for each line so far

    /**
     * Converts Json values automatically to strings when needed
     * @param jsVal - json value to convert
     * @return
     */
    implicit def jsVal2Str(jsVal: JsValue) = jsVal.toString()

    /**
     * Takes all the found json data and converts to a huge string
     *
     * @param needles - the values to find while parsing the json
     * @param jsonHaystack - the json string to parse and search
     * @throws java.lang.RuntimeException - thrown if an invalid/invalid json value is encountered
     * @throws play.api.libs.json.JsResultException - thrown if there's an error initially converting the
     *                                              json string to a json object.
     * @return the formatted data in csv
     */
    @throws(classOf[RuntimeException])
    @throws(classOf[JsResultException])
    def toCsvStr(needles: Jmap[String, Lset[String]],
                    jsonHaystack: String): String =
    {
        // TODO: find a way to make it static, stupid maxValues variable getting in the way of that...
        val csvLines = new StringBuffer()
        val csvData = parse(needles, jsonHaystack)

        for ((csvCollectionKey, headerSet) ← needles) {
            if (csvData contains csvCollectionKey) {
                val data = csvData(csvCollectionKey)

                if (!data.isEmpty) {
                    for (i ← 0 to maxFound) {
                        val csvLine = new StringBuffer()

                        for (needle ← headerSet) {
                            val output = if (data(needle).get(i) == null) "\"\"" else data(needle).get(i)
                            csvLine.append(s"$output,")
                        }
                        csvLines.append(csvLine.replace(csvLine.length - 1, csvLine.length, "\n"))
                        //print(csvLine.replace(csvLine.length - 1, csvLine.length - 1, "\n"))
                    }
                }
            }
        }
        csvLines.toString
    }

    /**
     * Parses a json object and looks for predefined
     * values to be reformatted into comma separated values
     *
     * @param needles - the values to find while parsing the json
     * @param jsonHaystack - the json string to parse and search
     * @throws java.lang.RuntimeException - thrown if an invalid/invalid json value is encountered
     * @throws play.api.libs.json.JsResultException - thrown if there's an error initially converting the
     *                                              json string to a json object.
     * @return the matching data from the json object
     */
    @throws(classOf[RuntimeException])
    @throws(classOf[JsResultException])
    def parse(needles: Jmap[String, Lset[String]],
              jsonHaystack: String
             ): Jmap[String, Jmap[String, Jmap[Int, String]]] =
    {
        parse(needles, str2Json(jsonHaystack))
    }

    /**
     * Recursively iterates over the given Json object to find any values
     * that were defined to be parsed and stored for conversion to csv later.
     *
     * @param needles - values to find in the json data
     * @param haystack - the json data we are searching
     * @param lastFoundNeedle - the last valid json value that was found
     * @param foundNeedles - holds all the data parsed from the json so far.
     * @param found - Used to keep track of how many array objects for the current json array have been found. Used
     *              basically as a workaround for json objects that are missing while found in others to keep them in sync
     *              when reformatting to csv
     * @throws java.lang.RuntimeException - if invalid json data is encountered
     * @return
     */
    @throws(classOf[RuntimeException])
    private def parseJson(needles: Jset[String],
                  haystack: JsValue,
                  lastFoundNeedle: String = "",
                  foundNeedles: Jmap[String, Jmap[Int, String]],
                  found: Int = -1
                 ): Jmap[String, Jmap[Int, String]] =
    {
        var newFound = found
        haystack match {
            case array: JsArray ⇒
                array.as[List[JsValue]] foreach {
                    case unsearchedHaystack: JsObject ⇒ newFound += 1
                        parseJson(needles, unsearchedHaystack, lastFoundNeedle, foundNeedles, newFound)
                    case unsearchedHaystack: JsValue ⇒
                        parseJson(needles, unsearchedHaystack, lastFoundNeedle, foundNeedles, found)
                }
            case jsObj: JsObject ⇒
                jsObj.fields foreach {
                    case (needleHeader: String, unsearchedHaystack: JsValue) ⇒
                        parseJson(needles, unsearchedHaystack, needleHeader, foundNeedles, found)
                }
            case jsNull: JsNull.type ⇒
                if (needles contains lastFoundNeedle) {
                    foundNeedles.get(lastFoundNeedle).put(found, "")
                }
                ""
            case jsUndef: JsUndefined ⇒
                sys.error(s"Missing a json value. Last good value found was: $lastFoundNeedle")
            case jsVal: JsValue ⇒
                if (needles contains lastFoundNeedle) {
                    foundNeedles.get(lastFoundNeedle).put(found, jsVal)
                }
                ""
        }
        if (found > maxFound) maxFound = found // keep track for iteration later
        foundNeedles
    }

    /**
     * Converts the json string into a json object.
     * Also strips off excess json to get to the first named object.
     *
     * @param jsonStr - the json string to parse into a json object
     * @throws play.api.libs.json.JsResultException - thrown if there's a parse error
     * @return the created json object
     */
    @throws(classOf[JsResultException])
    private def str2Json(jsonStr: String): JsValue =
    {
        Json.parse(jsonStr).as[JsArray].apply(0)
    }

    /**
     * Creates the map of maps that will hold all the collected values from the json
     * that need to be reformatted into csv.
     *
     * @param needles - values need to be collected
     * @return the created map of maps
     */
    private def initNeedlesToFind(needles: Lset[String]): Jmap[String, Jmap[Int, String]] =
    {
        val needlesToFind = new Lmap[String, Jmap[Int, String]]()

        for (k ← needles) {
            needlesToFind.put(k, new Lmap[Int, String]())
        }
        needlesToFind
    }

    /**
     * Parses a json object and looks for predefined
     * values to be reformatted into comma separated values
     *
     * @param needles - values to find while parsing the json object
     * @param haystack - the json object to search
     * @throws java.lang.RuntimeException - thrown if an invalid/invalid json value is encountered
     * @throws play.api.libs.json.JsResultException - thrown if there's a parse error
     * @return the matching data from the json object
     */
    @throws(classOf[RuntimeException])
    @throws(classOf[JsResultException])
    private def parse(needles: Jmap[String, Lset[String]],
                      haystack: JsValue
                     ): Jmap[String, Jmap[String, Jmap[Int, String]]] =
    {
        val parsedData = new Lmap[String, Jmap[String, Jmap[Int, String]]]

        if (!needles.isEmpty) {
            for ((k, v) ← needles) {
                if (v != null && !v.isEmpty) {
                    val parsed = parseJson(v, haystack \ k, foundNeedles = initNeedlesToFind(v))
                    parsedData.put(k, parsed)
                }
                else {
                    parsedData.put(k, new Lmap[String, Jmap[Int, String]])
                }
            }
        }
        parsedData
    }
}



