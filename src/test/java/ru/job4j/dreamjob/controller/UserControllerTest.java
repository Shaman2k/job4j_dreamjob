package ru.job4j.dreamjob.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ui.ConcurrentModel;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private UserService userService;

    private UserController userController;

    @BeforeEach
    public void initServices() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    public void whenRequestRegistrationPageThenGetRegisterPage() {
        var view = userController.getRegistrationPage();
        assertThat(view).isEqualTo("users/register");
    }

    @Test
    public void whenPostUserRegisterAndSuccessThenRedirectToVacanciesPage() {
        var user = new User(1, "user@test.ru", "name", "password");
        var userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        when(userService.save(userArgumentCaptor.capture())).thenReturn(Optional.of(user));

        var model = new ConcurrentModel();
        var view = userController.register(model, user);
        var actualUser = userArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualUser).isEqualTo(user);
    }

    @Test
    public void whenPostUserRegistrationAndEmailExistsThenGetRegisterPage() {
        var user = new User(1, "user@test.ru", "name", "password");
        when(userService.save(user)).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = userController.register(model, user);

        assertThat(view).isEqualTo("users/register");
        assertThat(model.getAttribute("error")).isEqualTo("Пользователь с таким email уже сужествует");
    }

    @Test
    public void whenRequestUserLoginPageThenGetLoginPage() {
        var view = userController.getLoginPage();
        assertThat(view).isEqualTo("users/login");
    }

    @Test
    public void whenPostUserLoginAndSuccessThenRedirectToVacancies() {
        var user = new User(1, "user@test.ru", "name", "password");
        var foundUser = new User(1, "user@test.ru", "name", "password");
        when(userService.findByEmailAndPassword(user.getEmail(), user.getPassword()))
                .thenReturn(Optional.of(foundUser));

        var model = new ConcurrentModel();
        var request = mock(HttpServletRequest.class);
        var session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        var view = userController.loginUser(user, model, request);

        assertThat(view).isEqualTo("redirect:/vacancies");
    }

    @Test
    public void whenPostUserLoginAndFailThenReturnLoginPageWithError() {
        var user = new User(1, "user@mail.ru", "name", "password");
        when(userService.findByEmailAndPassword(user.getEmail(), user.getPassword()))
                .thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var request = mock(HttpServletRequest.class);

        var view = userController.loginUser(user, model, request);

        assertThat(view).isEqualTo("users/login");
        assertThat(model.getAttribute("error")).isEqualTo("Почта или пароль введены неверно");
    }

    @Test
    public void whenRequestUserLogoutThenRedirectToLogin() {
        var session = mock(HttpSession.class);
        var view = userController.logout(session);

        assertThat(view).isEqualTo("redirect:/users/login");
    }
}