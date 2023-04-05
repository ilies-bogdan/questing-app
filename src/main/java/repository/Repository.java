package repository;

import domain.Entity;

public interface Repository<E extends Entity<Id>, Id> {
    void add(E e) throws RepositoryException;
    void delete(E e);
    void update(E e, Id id);
    E findById(Id id);
    Iterable<E> getAll();
}
