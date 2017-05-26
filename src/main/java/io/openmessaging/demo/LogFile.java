package io.openmessaging.demo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class LogFile {
	private static AtomicInteger offset = new AtomicInteger(0);
	private String path;
	private String fileName;
	private RandomAccessFile file;
	private FileChannel fileChannel;
	private MappedByteBuffer writeMappedByteBuffer;

	public LogFile(String path, String fileName) {
		this.path = path;
		this.fileName = fileName;
		File file = new File(path, fileName);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			this.file = new RandomAccessFile(file, "rw");
			this.fileChannel = this.file.getChannel();
			this.writeMappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, Constants.BUFFER_SIZE);
		} catch (IOException e) {
			throw new ClientOMSException("LogFile create failure", e);
		}
	}

	public void doAppend(byte[] bytes) {
		try {
			if (offset.get() > 4) {
				offset.set(0);
			}
			writeMappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, offset.get(),
					Constants.BUFFER_SIZE);
			offset.incrementAndGet();
		} catch (IOException e) {
			e.printStackTrace();
		}
		writeMappedByteBuffer.put(bytes);
		writeMappedByteBuffer.clear();
	}

	public String getFileName() {
		return this.fileName;
	}

	public void flush() {
		try {
			fileChannel.force(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
