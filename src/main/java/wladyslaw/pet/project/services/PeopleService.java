package wladyslaw.pet.project.services;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wladyslaw.pet.project.models.Book;
import wladyslaw.pet.project.models.Person;
import wladyslaw.pet.project.repositories.PeopleRepository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PeopleService {

    private final PeopleRepository peopleRepository;

    @Autowired
    public PeopleService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    public List<Person> findAll() {
        return peopleRepository.findAll();
    }

    public Person findOne(int id) {
        Optional<Person> foundPerson = peopleRepository.findById(id);
        return foundPerson.orElse(null); // возвращается null если человека с таким ID не существует
    }

    @Transactional
    public void save(Person person) {
        peopleRepository.save(person);
    }

    @Transactional
    public void update(int id, Person updatedPerson) {
        updatedPerson.setId(id);
        peopleRepository.save(updatedPerson);
    }

    @Transactional
    public void delete(int id) {
        peopleRepository.deleteById(id);
    }


    public Optional<Person> getPersonByFullName(String fullName) {
        return peopleRepository.findByFullName(fullName); // метод добавлен в PeopleRepository
    }

    // метод возвращает список книг человека по его ID или пустой лист если человек с таким ID не существует
    public List<Book> getBooksByPersonId(int id) {
        Optional<Person> person = peopleRepository.findById(id);

        if (person.isPresent()) { // isPresent() - метод класса Optional
            Hibernate.initialize(person.get().getBooks());
            // хоть книги и точно загрузятся после вызова геттера, на случай изменений в коде вызван Hibernate.initialize
            // например если будет удалена или вынесена в другой метод проверка просроченности книг

            // Проверка просроченности книг
            person.get().getBooks().forEach(book -> {
                long differenceInMillies = Math.abs(book.getTakenAt().getTime() - new Date().getTime());
                // модуль разности времени когда была взята книга и настоящего времени
                if (differenceInMillies > 864000000) // 864000000 милисекунд - это 10 суток
                    book.setExpired(true); // значит книга просрочена
            });

            return person.get().getBooks();
        }
        else {
            return Collections.emptyList();
        }
    }
}
