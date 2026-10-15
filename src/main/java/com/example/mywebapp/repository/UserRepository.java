
package com.example.mywebapp.repository;

import com.example.mywebapp.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
