package wladyslaw.pet.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wladyslaw.pet.project.models.Book;
import wladyslaw.pet.project.models.Person;
import wladyslaw.pet.project.repositories.BooksRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BooksService {

    private final BooksRepository booksRepository;

    @Autowired
    public BooksService(BooksRepository booksRepository) {
        this.booksRepository = booksRepository;
    }

    public List<Book> findAll(boolean sortByYear) {
        if (sortByYear) // если в адресной строке есть sort_by_year
            return booksRepository.findAll(Sort.by("year")); // передаем в параметр критерий сортировки (колонку year)
        else
            return booksRepository.findAll();
    }

    // поиск книг с пагинацией
    public List<Book> findWithPagination(Integer page, Integer booksPerPage, boolean sortByYear) {
        if (sortByYear) // если в адресной строке есть sort_by_year
            return booksRepository.findAll(PageRequest.of(page, booksPerPage, Sort.by("year"))).getContent();
        else
            return booksRepository.findAll(PageRequest.of(page, booksPerPage)).getContent();
    }

    public Book findOne(int id) {
        Optional<Book> foundBook = booksRepository.findById(id);
        return foundBook.orElse(null);
    }

    // метод добавлен в BooksRepository
    public List<Book> searchByTitle(String query) {
        return booksRepository.findByTitleStartingWith(query);
    }

    @Transactional
    public void save(Book book) {
        booksRepository.save(book);
    }

    @Transactional
    public void update(int id, Book updatedBook) {
        Book bookToBeUpdated = booksRepository.findById(id).get();
        updatedBook.setId(id);
        updatedBook.setOwner(bookToBeUpdated.getOwner()); // чтобы не терялась связь с владельцем при обновлении
        booksRepository.save(updatedBook); // updatedBook не находится в Persistence context, поэтому нужен save()
    }

    @Transactional
    public void delete(int id) {
        booksRepository.deleteById(id);
    }

    // Returns null if book has no owner
    public Person getBookOwner(int id) {
        return booksRepository.findById(id).map(Book::getOwner).orElse(null);
        // return booksRepository.findById(id).map(book -> book.getOwner()).orElse(null); - эта запись сокращена method reference
    }

    // Метод освобождает книгу от владельца
    @Transactional
    public void release(int id) {
        booksRepository.findById(id).ifPresent(
                book -> {
                    book.setOwner(null);
                    book.setTakenAt(null);
                });
    }

    // Назначает книгу человеку (этот метод вызывается, когда человек забирает книгу из библиотеки)
    @Transactional
    public void assign(int id, Person selectedPerson) {
        booksRepository.findById(id).ifPresent(
                book -> {
                    book.setOwner(selectedPerson);
                    book.setTakenAt(new Date()); // текущее время
                }
        );
    }
}
