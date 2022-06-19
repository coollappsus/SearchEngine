package main.service;

import main.model.Field;
import main.repositories.FieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FieldService {

    private final FieldRepository fieldRepository;

    @Autowired
    public FieldService(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
    }

    public void create() {
        ArrayList<Field> fields = new ArrayList<>();
        Iterable<Field> fieldIterable = fieldRepository.findAll();
        fieldIterable.forEach(fields::add);
        if (fields.size() != 2) {
            Field field1 = new Field("title", "title", 1);
            fieldRepository.save(field1);
            Field field2 = new Field("body", "body", 0.8F);
            fieldRepository.save(field2);
        }
    }

    public Field findById (int id) {
        return fieldRepository.findById(id).get();
    }

    public List<Field> findAll() {
        ArrayList<Field> result = new ArrayList<>();
        Iterable<Field> fieldIterable = fieldRepository.findAll();
        fieldIterable.forEach(result::add);
        return result;
    }
}
