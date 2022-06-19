package main.model;

import javax.persistence.*;

@Entity
@Table(name = "field")
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "selector")
    private String selector;

    @Column(name = "weight")
    private float weight;

    public Field() {
    }

    public Field(String name, String selector, float weight) {
        this.name = name;
        this.selector = selector;
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public String getSelector() {
        return selector;
    }

    public float getWeight() {
        return weight;
    }
}
