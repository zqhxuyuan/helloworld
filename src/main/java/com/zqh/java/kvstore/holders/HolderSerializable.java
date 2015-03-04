/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.zqh.java.kvstore.holders;

import java.nio.ByteBuffer;

/**
 * Interface for Serializable Holders
 * 
 * @author Guillermo Grandes / guillermo.grandes[at]gmail.com
 */
public interface HolderSerializable<T> {

	// ========= Serialization =========

	public int byteLength();

	public void serialize(final ByteBuffer buf);

	public T deserialize(final ByteBuffer buf);

}
