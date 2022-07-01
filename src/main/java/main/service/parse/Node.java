package main.service.parse;

import main.model.Field;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

public interface Node {

    List<String> parsePage();
    void parseTagField(List<Field> extractFromField, Document doc) throws IOException;
}
