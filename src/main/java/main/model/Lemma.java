package main.model;


import javax.persistence.*;

@Entity
@Table(name = "lemma")
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "lemma")
    private String lemma;

    @Column(name = "frequency")
    private float frequency;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "site_id", referencedColumnName = "id")
    private Site site;

    public Lemma() {
    }

    public Lemma(String lemma, float frequency, Site site) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.site = site;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public int getSiteId() {
        return site.getId();
    }

    public void setSiteId(Integer siteId) {
        site.setId(siteId);
    }


}
