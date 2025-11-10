package bookmarkd.api.entity;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends PanacheEntity {
    public String username;

    @OneToMany(mappedBy = "author")
    public List<Review> reviews = new ArrayList<>();
}
