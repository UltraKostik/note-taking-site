package com.example.note_taking_site.service;

import com.example.note_taking_site.dto.RegisterRequest;
import com.example.note_taking_site.exception.UserAlreadyExistsException;
import com.example.note_taking_site.model.User;
import com.example.note_taking_site.repository.NotesRepository;
import com.example.note_taking_site.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotesRepository notesRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user(Long id, String email, User.Role role) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setPassword("encoded");
        u.setRole(role);
        return u;
    }

    private RegisterRequest registerRequest(String email, String password) {
        RegisterRequest r = new RegisterRequest();
        r.setEmail(email);
        r.setPassword(password);
        return r;
    }

    @Test
    void register_encodesPasswordAndAssignsUserRole() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register(registerRequest("new@example.com", "secret1"));

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getPassword()).isEqualTo("$2a$hashed");
        assertThat(result.getRole()).isEqualTo(User.Role.USER);
        verify(passwordEncoder).encode("secret1");
        verify(userRepository).save(result);
    }

    @Test
    void register_duplicateEmail_throwsUserAlreadyExists() {
        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(registerRequest("dup@example.com", "secret1")))
                .isInstanceOf(UserAlreadyExistsException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changeRole_updatesRole() {
        User existing = user(2L, "u@example.com", User.Role.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(existing));

        userService.changeRole(2L, User.Role.ADMIN);

        assertThat(existing.getRole()).isEqualTo(User.Role.ADMIN);
        verify(userRepository).save(existing);
    }

    @Test
    void getById_missing_throws() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(42L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void deleteUser_removesNotesThenUser() {
        User existing = user(9L, "gone@example.com", User.Role.USER);
        when(userRepository.findById(9L)).thenReturn(Optional.of(existing));

        userService.deleteUser(9L);

        InOrder order = inOrder(notesRepository, userRepository);
        order.verify(notesRepository).deleteAllByAuthor(existing);
        order.verify(userRepository).delete(existing);
    }

    @Test
    void getAllUsers_returnsRepositoryResult() {
        List<User> users = List.of(
                user(1L, "a@example.com", User.Role.USER),
                user(2L, "b@example.com", User.Role.ADMIN));
        when(userRepository.findAll()).thenReturn(users);

        assertThat(userService.getAllUsers()).isEqualTo(users);
    }
}
