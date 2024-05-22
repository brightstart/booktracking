package com.tracking.booktracking.repo;

import com.tracking.booktracking.model.Author;
import com.tracking.booktracking.model.Book;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends CassandraRepository<Book, String> {
}
