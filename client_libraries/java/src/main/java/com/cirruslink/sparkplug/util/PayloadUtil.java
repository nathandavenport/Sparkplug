/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2017 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.cirruslink.sparkplug.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cirruslink.sparkplug.message.SparkplugBPayloadDecoder;
import com.cirruslink.sparkplug.message.SparkplugBPayloadEncoder;
import com.cirruslink.sparkplug.message.model.SparkplugBPayload;
import com.cirruslink.sparkplug.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;

/**
 * Utilities for Sparkplug Payload handling.
 */
public class PayloadUtil {
	
	private static Logger logger = LogManager.getLogger(PayloadUtil.class.getName());
	
	public static final String UUID_COMPRESSED = "SPBV1.0_COMPRESSED";
	
	/**
	 * Returns a decompressed {@link SparkplugBPayload} instance from an existing payload.  Will return the original
	 * payload if not compressed payload exists.
	 * 
	 * @param payload
	 * @return
	 * @throws Exception
	 */
	public static SparkplugBPayload decompress(SparkplugBPayload payload) throws Exception {
		if (UUID_COMPRESSED.equals(payload.getUuid())) {
			logger.trace("Decompressing payload");
			SparkplugBPayloadDecoder decoder = new SparkplugBPayloadDecoder();
			byte [] decompressedBytes;

			// TODO: Check decompression algorithm
			// Default to DEFLATE
			decompressedBytes = inflateBytes(payload.getBody());
			
			// Decode bytes and return
			return decoder.buildFromByteArray(decompressedBytes);
		} else {
			logger.trace("Not decompressing payload");
			return payload;
		}
	}

	/**
	 * 
	 * @param payload
	 * @return
	 * @throws IOException
	 */
	public static SparkplugBPayload compress(SparkplugBPayload payload) throws IOException {
		logger.trace("Compressing payload");
		SparkplugBPayloadEncoder encoder = new SparkplugBPayloadEncoder();
		// Encode bytes
		byte [] encoded = encoder.getBytes(payload);

		// TODO: Check compression algorithm
		// Default to DEFLATE
		byte [] compressedBytes = deflateBytes(encoded);
		
		// Create new payload, add the bytes as the body, and return.
		return new SparkplugBPayloadBuilder(payload.getSeq())
				.setBody(compressedBytes)
				.setUuid(UUID_COMPRESSED)
				.createPayload();
	}

	/**
	 * Compresses a byte array using DEFLATE compression algorithm.
	 * 
	 * @param bytes the byte array to compress.
	 * @return the compressed byte array.
	 * @throws IOException
	 */
	protected static byte [] deflateBytes(byte [] bytes) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
		Deflater deflater = new Deflater();
		deflater.setInput(bytes);
		deflater.finish();

		byte [] buffer = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer);
			baos.write(buffer, 0, count);
		}
		baos.close();

		return baos.toByteArray();
	}

	/**
	 * Decompresses a byte array using DEFLATE compression algorithm.
	 * 
	 * @param bytes the byte array to decompress.
	 * @return the decompressed byte array.
	 * @throws IOException
	 * @throws DataFormatException
	 */
	protected static byte [] inflateBytes(byte [] bytes) throws IOException, DataFormatException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
		Inflater inflater = new Inflater();
		inflater.setInput(bytes);

		byte [] buffer = new byte[1024];
		while (!inflater.finished()) {
			int count = inflater.inflate(buffer);
			baos.write(buffer, 0, count);
		}
		baos.close();
		return baos.toByteArray();
	}
}
