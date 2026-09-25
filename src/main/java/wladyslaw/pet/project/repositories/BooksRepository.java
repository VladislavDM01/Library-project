package wladyslaw.pet.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wladyslaw.pet.project.models.Book;

import java.util.List;

@Repository
public interface BooksRepository extends JpaRepository<Book, Integer> {
    List<Book> findByTitleStartingWith(String title); // метод для поиска книг по начальным буквам
}
