package se.lth.cs.tycho;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import se.lth.cs.tycho.ir.QID;

import java.io.IOException;

public class QIDTypeAdapter extends TypeAdapter<QID> {
	@Override
	public void write(JsonWriter jsonWriter, QID qid) throws IOException {
		jsonWriter.value(qid.toString());
	}

	@Override
	public QID read(JsonReader jsonReader) throws IOException {
		if (jsonReader.peek() == JsonToken.NULL) {
			return QID.empty();
		} else {
			return QID.parse(jsonReader.nextString());
		}
	}
}
