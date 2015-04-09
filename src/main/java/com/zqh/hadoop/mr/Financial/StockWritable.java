package com.zqh.hadoop.mr.Financial; /**
 * Copyright 2013 Jesse Anderson
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Writable;

/**
 * Writable representing a stock's price on a given day
 *
 */
public class StockWritable implements Writable {
	/** The stock's price */
	public BigDecimal price;
	
	/** The date of the stock price */
	public String date;

	/**
	 * Empty constructor - required for serialization.
	 */
	public StockWritable() {

	}

	/**
	 * Constructor
	 */
	public StockWritable(String date, BigDecimal price) {
		this.date = date;
		this.price = price;
	}

	/**
	 * Serializes the fields of this object to out.
	 */
	public void write(DataOutput out) throws IOException {
		out.writeUTF(date);
		
		byte[] priceArray = Bytes.toBytes(price);
		
		out.writeInt(priceArray.length);
		out.write(priceArray);
	}

	/**
	 * Deserializes the fields of this object from in.
	 */
	public void readFields(DataInput in) throws IOException {
		date = in.readUTF();
		
		int byteSize = in.readInt();
		byte[] priceArray = new byte[byteSize];
		in.readFully(priceArray);
		
		price = Bytes.toBigDecimal(priceArray);
	}
	
	@Override
	public String toString() {
		return date + " " + price;
	}
	
	@Override
	public Object clone() {
		return new StockWritable(date, price);
	}

	/**
	 * The hashCode method generates a hash code for a com.zqh.hadoop.mr.Financial.StockWritable object. The equals and hashCode methods have been
	 * automatically generated by Eclipse by right-clicking on an empty line, selecting Source, and then selecting the
	 * Generate hashCode() and equals() option.
	 */
	@Override
	public int hashCode() {
		return date.hashCode();
	}
}
