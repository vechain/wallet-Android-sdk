/*
 * Vechain Wallet SDK is licensed under the MIT LICENSE, also included in LICENSE file in the repository.
 *
 * Copyright (c) 2019 VeChain support@vechain.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.vechain.wallet.network.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

/**
 * GSON util
 * 
 */
public class GsonUtils {
	public static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

	public static final Gson GsonSerializeNull = new GsonBuilder().serializeNulls().create();

	/*
	 * Parse the object data into json
	 */
	public static String toJson(final Object object) {
		// TODO Auto-generated method stub
		return toString(object);
	}

	public static String toJson(final Object object, Type typeOfT) {
		// TODO Auto-generated method stub
		return toString(object, typeOfT);
	}

	public static String toString(Object object, Type typeOfT) {
		String result = "";
		try {
			if (typeOfT == null)
				result = GSON.toJson(object);
			else
				result = GSON.toJson(object, typeOfT);
		} catch (Exception e) {
			result = "";
		}catch (Error error){
			error.printStackTrace();
		}

		return result;
	}

	public static String toString(final Object object) {
		return toString(object, null);
	}

	/**
	 * JSON to object
	 * 
	 * @param json
	 * @param classOfT
	 * @return object
	 */
	public static <T> T fromJson(final String json, Class<T> classOfT) {
		// TODO Auto-generated method stub
		T result = null;
		try {
			result = GSON.fromJson(json, classOfT);
		} catch (Exception e) {
			result = null;
		}catch (Error error){
			error.printStackTrace();
		}

		return result;
	}

	/**
	 * java.lang.reflect.Type type = new TypeToken<T>() { }.getType();
	 * 
	 * @param json
	 * @param typeOfT
	 * @return object
	 */

	public static <T> T fromJson(final String json, Type typeOfT) {
		// TODO Auto-generated method stub
		T result = null;
		try {
			result = GSON.fromJson(json, typeOfT);
		} catch (Exception e) {
			result = null;
		}catch (Error error){
			error.printStackTrace();
		}

		return result;
	}

	public static String toSerializeNullString(final Object object){

		String result = "";
		try {

			result = GsonSerializeNull.toJson(object);

		} catch (Exception e) {
			result = "";
		}catch (Error error){
			error.printStackTrace();
		}

		return result;
	}
}
