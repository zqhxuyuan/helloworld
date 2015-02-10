/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zqh.tez.examples;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.Set;

import org.apache.hadoop.util.ProgramDriver;
import org.apache.tez.common.counters.TezCounters;
import org.apache.tez.dag.api.TezException;
import org.apache.tez.dag.api.client.DAGClient;
import org.apache.tez.dag.api.client.DAGStatus;
import org.apache.tez.dag.api.client.Progress;
import org.apache.tez.dag.api.client.StatusGetOpts;
import org.apache.tez.dag.api.client.VertexStatus;

/**
 * A description of an example program based on its class and a
 * human-readable description.
 */
public class ExampleDriver {

  private static final DecimalFormat formatter = new DecimalFormat("###.##%");

  public static void main(String argv[]){
    int exitCode = -1;
    ProgramDriver pgd = new ProgramDriver();
    try {
      pgd.addClass("wordcount", WordCount.class,
          "A native Tez wordcount program that counts the words in the input files.");
      pgd.addClass("orderedwordcount", OrderedWordCount.class,
          "Word Count with words sorted on frequency");
      pgd.addClass("simplesessionexample", SimpleSessionExample.class,
          "Example to run multiple dags in a session");
      pgd.addClass("hashjoin", HashJoinExample.class,
          "Identify all occurences of lines in file1 which also occur in file2 using hash join");
      pgd.addClass("sortmergejoin", SortMergeJoinExample.class,
          "Identify all occurences of lines in file1 which also occur in file2 using sort merge join");
      pgd.addClass("joindatagen", JoinDataGen.class,
          "Generate data to run the joinexample");
      pgd.addClass("joinvalidate", JoinValidate.class,
          "Validate data generated by joinexample and joindatagen");
      exitCode = pgd.run(argv);
    } catch(Throwable e){
      e.printStackTrace();
    }

    System.exit(exitCode);
  }

}
	
