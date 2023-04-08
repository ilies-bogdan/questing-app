package repository;

import domain.Entity;

public interface Repository<E extends Entity<Id>, Id> {
    /**
     * Adds an entity to the repository.
     * @param e - the entitity to be added
     * @throws RepositoryException if the entity has already been added
     */
    void add(E e) throws RepositoryException;

    /**
     * Deletes an entity from the repository.
     * @param e - the entity to be removed
     */
    void delete(E e);

    /**
     * Updates an entity in the repository.
     * @param e - the entity to be updated
     * @throws RepositoryException if the entitydoes not exist
     */
    void update(E e, Id id) throws RepositoryException;

    /**
     * Finds an entity in the repository by its ID.
     * @param id - the ID by which to find the entity
     * @return the entity with the given ID
     */
    E findById(Id id);

    /**
     * Gets all the entities in the repository.
     * @return iterable object containing all the entities
     */
    Iterable<E> getAll();
}
