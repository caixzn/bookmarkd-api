package bookmarkd.api.entity;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class Book extends PanacheEntity {
    public String title;
    public String author;
    public String publishedYear;
    public String openLibraryKey;
    public String openLibraryAuthorKey;

    @OneToMany(mappedBy = "book")
    public List<Review> reviews = new ArrayList<>();
}
