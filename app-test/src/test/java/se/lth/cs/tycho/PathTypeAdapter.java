package se.lth.cs.tycho;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathTypeAdapter extends TypeAdapter<Path> {
	@Override
	public void write(JsonWriter jsonWriter, Path path) throws IOException {
		jsonWriter.value(path.toString());
	}

	@Override
	public Path read(JsonReader jsonReader) throws IOException {
		if (jsonReader.peek() == JsonToken.NULL) {
			return Paths.get("");
		} else {
			return Paths.get(jsonReader.nextString());
		}
	}
}
