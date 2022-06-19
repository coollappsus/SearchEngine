package main.model;

import javax.persistence.*;

@Entity
@Table(name = "page")
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "path")
    private String path;

    @Column(name = "code")
    private int statusCode;

    @Column(name = "content", columnDefinition= "MEDIUMTEXT")
    private String content;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "site_id", referencedColumnName = "id")
    private Site site;

    public Page() {
    }

    public Page(String path, int statusCode, String content, Site site) {
        this.path = path;
        this.statusCode = statusCode;
        this.content = content;
        this.site = site;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
