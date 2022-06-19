package main.model;

import javax.persistence.*;


@Entity
@Table(name = "`index`")
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "page_id", referencedColumnName = "id")
    private Page page;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "lemma_id", referencedColumnName = "id")
    private Lemma lemma;

    @Column(name = "`rank`")
    private float rank;

    public Index() {
    }

    public Index(Page page, Lemma lemma, float rank) {
        this.page = page;
        this.lemma = lemma;
        this.rank = rank;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIdPage(int idPage) {
        page.setId(idPage);
    }

    public void setIdLemma(int idLemma) {
        lemma.setId(idLemma);
    }

    public void setRank(float rank) {
        this.rank = rank;
    }

    public int getIdPage() {
        return page.getId();
    }

    public int getIdLemma() {
        return lemma.getId();
    }

    public float getRank() {
        return rank;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public Lemma getLemma() {
        return lemma;
    }

    public void setLemma(Lemma lemma) {
        this.lemma = lemma;
    }
}
