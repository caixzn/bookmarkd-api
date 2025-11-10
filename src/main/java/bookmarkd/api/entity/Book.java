package bookmarkd.api.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Book extends PanacheEntity {
    public String title;
    public String author;
    public String year;
    public String openLibraryKey;
    public String openLibraryAuthorKey;
}
