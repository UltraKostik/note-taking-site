package com.example.note_taking_site.repository;

import com.example.note_taking_site.model.Notes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Notes, Long> {
}
