package bg.sofia.uni.fmi.mjt.authserver.command;

import bg.sofia.uni.fmi.mjt.authserver.UserManager;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.AddAdminCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.DeleteUserCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.LoginCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.LoginWithSessionCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.LogoutCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.RegisterCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.RemoveAdminCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.ResetPasswordCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.UpdateUserCommand;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserAlreadyExists;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserNotFound;
import bg.sofia.uni.fmi.mjt.authserver.logger.AuditLogger;
import bg.sofia.uni.fmi.mjt.authserver.ratelimiter.LoginRateLimiter;
import bg.sofia.uni.fmi.mjt.authserver.response.Response;
import bg.sofia.uni.fmi.mjt.authserver.response.StatusCode;
import bg.sofia.uni.fmi.mjt.authserver.session.Session;
import bg.sofia.uni.fmi.mjt.authserver.session.SessionStoreAPI;
import bg.sofia.uni.fmi.mjt.authserver.user.PasswordHasher;
import bg.sofia.uni.fmi.mjt.authserver.user.Role;
import bg.sofia.uni.fmi.mjt.authserver.user.User;
import bg.sofia.uni.fmi.mjt.authserver.user.UserStoreAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class CommandExecutorTest {
    @Mock
    private SessionStoreAPI sessionStore;
    @Mock
    private UserStoreAPI userStore;
    @Mock
    private AuditLogger auditLogger;
    @Mock
    private LoginRateLimiter loginRateLimiter;
    @InjectMocks
    private UserManager userManager;

    private CommandExecutor commandExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        commandExecutor = new CommandExecutor(userManager);
    }

    @Test
    void testLoginWithSessionCommand_Success() {
        String sid = "test-sid";
        String callerIp = "0.0.0.0";
        Command loginCommand = new LoginWithSessionCommand(sid, callerIp);

        Session mockedSession = new Session(sid, LocalDateTime.now().plusHours(1), "someuid");
        when(userManager.getSessionStore().getById(sid)).thenReturn(mockedSession);

        Response response = commandExecutor.execute(loginCommand);

        assertEquals(StatusCode.SUCCESS.getCode(), response.status());
    }

    @Test
    void testLoginWithSessionCommand_NoSession() {
        String sid = "test-sid";
        String callerIp = "0.0.0.0";
        LoginWithSessionCommand loginCommand = new LoginWithSessionCommand(sid, callerIp);

        Session mockedSession = new Session(sid, LocalDateTime.now().plusHours(1), "someuid");
        when(userManager.getSessionStore().getById(sid)).thenReturn(null);

        Response response = commandExecutor.execute(loginCommand);

        verify(auditLogger).writeFailLogin(eq(loginCommand.getId()), eq("0.0.0.0"));
        assertEquals(StatusCode.UNAUTHORIZED.getCode(), response.status());
    }

    @Test
    void testAddAdminCommand_Success() throws UserNotFound, IOException {
        String sid = "test-sid";
        String username = "topromote";
        String callerIp = "0.0.0.0";
        AddAdminCommand addAdminCommand = new AddAdminCommand(sid, username, callerIp);

        User toPromoteUser =
                new User("user-id-1", "topromote", "password", "John", "Doe", "john@example.com", Role.NORMAL);

        when(sessionStore.getById(sid))
                .thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "user-id"));
        when(userStore.getById("user-id"))
                .thenReturn(new User("user-id", "username", "password", "John", "Doe", "john@example.com", Role.ADMIN));
        when(userStore.getByUsername("topromote"))
                .thenReturn(toPromoteUser);

        Response response = commandExecutor.execute(addAdminCommand);

        verify(auditLogger, times(2))
                .writeChangeLog(eq(addAdminCommand.getId()), eq(callerIp), anyString());
        verify(sessionStore).getById(sid);
        verify(userStore).getById(anyString());
        verify(userStore).getByUsername("topromote");
        verify(userStore).updateUser(any(User.class));
        assertEquals(Role.ADMIN, toPromoteUser.getRole());
        assertEquals(
                new Response("topromote was successfully promoted to an Admin", StatusCode.SUCCESS.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testAddAdminCommand_InvalidSession_ThrowsException() {
        String sid = "test-sid";
        String username = "topromote";
        String callerIp = "0.0.0.0";
        AddAdminCommand addAdminCommand = new AddAdminCommand(sid, username, callerIp);

        when(sessionStore.getById(sid)).thenReturn(null);

        Response response = commandExecutor.execute(addAdminCommand);
        assertEquals(
                new Response("Invalid or expired session", StatusCode.SUCCESS.getCode()).toString(),
                response.toString()
        );

        verify(auditLogger, times(2)).writeChangeLog(anyString(), eq(callerIp), anyString());
        verify(sessionStore).getById(sid);
        verifyNoMoreInteractions(userStore);
    }

    @Test
    void testAddAdminCommand_NoPermissions_ThrowsException() throws UserNotFound {
        String sid = "test-sid";
        String username = "topromote";
        String callerIp = "0.0.0.0";
        AddAdminCommand addAdminCommand = new AddAdminCommand(sid, username, callerIp);

        when(sessionStore.getById(sid))
                .thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "user-id"));
        when(userStore.getById("user-id"))
                .thenReturn(new User("user-id", "username", "password", "John", "Doe", "john@example.com", Role.NORMAL));

        Response response = commandExecutor.execute(addAdminCommand);
        assertEquals(
                new Response("You don't have permissions to perform this action", StatusCode.SUCCESS.getCode()).toString(),
                response.toString()
        );

        verify(auditLogger, times(2)).writeChangeLog(anyString(), eq(callerIp), anyString());
        verify(sessionStore).getById(sid);
        verify(userStore).getById("user-id");
    }

    @Test
    void testAddAdminCommand_UserNotFound_ThrowsException() throws UserNotFound {
        String sid = "test-sid";
        String username = "topromote";
        String callerIp = "0.0.0.0";
        AddAdminCommand addAdminCommand = new AddAdminCommand(sid, username, callerIp);

        when(sessionStore.getById(sid)).thenReturn(new Session("session-id", LocalDateTime.now().plusHours(1), "user-id"));
        when(userStore.getById("user-id")).thenReturn(new User("user-id", "username", "password", "John", "Doe", "john@example.com", Role.ADMIN));
        when(userStore.getByUsername("topromote")).thenThrow(new UserNotFound("User was not found"));

        Response response = commandExecutor.execute(addAdminCommand);
        assertEquals(
                new Response("User was not found", StatusCode.NOT_FOUND.getCode()).toString(),
                response.toString()
        );

        verify(auditLogger, times(2)).writeChangeLog(anyString(), eq(callerIp), anyString());
        verify(sessionStore).getById(sid);
        verify(userStore).getById(anyString());
        verify(userStore).getByUsername("topromote");
        verifyNoMoreInteractions(userStore);
    }

    @Test
    void testDeleteUserCommand_Success() throws UserNotFound, IOException {
        String sid = "test-sid";
        String username = "todelete";
        String callerIp = "0.0.0.0";
        DeleteUserCommand deleteUserCommand = new DeleteUserCommand(sid, username, callerIp);

        User toDeleteUser =
                new User("user-id-1", "todelete", "password", "John", "Doe", "john@example.com", Role.ADMIN);
        Session toDeleteSession =
                new Session("to-delete-sid", LocalDateTime.now(), "user-id-1");

        when(sessionStore.getById(sid))
                .thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "user-id"));
        when(userStore.getById("user-id"))
                .thenReturn(new User("user-id", "admin", "adminpass", "Admin", "User", "admin@example.com", Role.ADMIN));
        when(userStore.getByUsername(username))
                .thenReturn(toDeleteUser);
        when(sessionStore.getByUid("user-id-1")).thenReturn(toDeleteSession);

        Response response = commandExecutor.execute(deleteUserCommand);

        verify(auditLogger, times(2))
                .writeChangeLog(eq(deleteUserCommand.getId()), eq(callerIp), anyString());
        verify(sessionStore).getById(sid);
        verify(userStore).getById(anyString());
        verify(userStore).getByUsername(username);
        verify(sessionStore).destroy(anyString());
        verify(userStore).deleteUser(toDeleteUser);
        assertEquals(
                new Response("Successfully deleted user", StatusCode.SUCCESS.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testDeleteUserCommand_InvalidSession_ThrowsException() {
        String sid = "test-sid";
        String username = "todelete";
        String callerIp = "0.0.0.0";
        DeleteUserCommand deleteUserCommand = new DeleteUserCommand(sid, username, callerIp);

        when(sessionStore.getById(sid)).thenReturn(null);

        Response response = commandExecutor.execute(deleteUserCommand);
        assertEquals(
                new Response("Invalid or expired session", StatusCode.UNAUTHORIZED.getCode()).toString(),
                response.toString()
        );

        verify(auditLogger, times(2)).writeChangeLog(anyString(), eq(callerIp), anyString());
        verify(sessionStore).getById(sid);
        verifyNoMoreInteractions(userStore);
    }

    @Test
    void testDeleteUserCommand_NoPermissions_ThrowsException() throws UserNotFound {
        String sid = "test-sid";
        String username = "todelete";
        String callerIp = "0.0.0.0";
        DeleteUserCommand deleteUserCommand = new DeleteUserCommand(sid, username, callerIp);

        when(sessionStore.getById(sid))
                .thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "user-id"));
        when(userStore.getById("user-id"))
                .thenReturn(new User("user-id", "normal", "normalpass", "Normal", "User", "normal@example.com", Role.NORMAL));

        Response response = commandExecutor.execute(deleteUserCommand);
        assertEquals(
                new Response("You don't have permissions to perform this action", StatusCode.UNAUTHORIZED.getCode()).toString(),
                response.toString()
        );

        verify(auditLogger, times(2)).writeChangeLog(anyString(), eq(callerIp), anyString());
        verify(sessionStore).getById(sid);
        verify(userStore).getById("user-id");
        verifyNoMoreInteractions(userStore);
        verifyNoMoreInteractions(sessionStore);
    }

    @Test
    void testDeleteUserCommand_UserNotFound_ThrowsException() throws UserNotFound {
        String sid = "test-sid";
        String username = "todelete";
        String callerIp = "0.0.0.0";
        DeleteUserCommand deleteUserCommand = new DeleteUserCommand(sid, username, callerIp);

        when(sessionStore.getById(sid)).thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "user-id"));
        when(userStore.getById("user-id")).thenReturn(new User("user-id", "admin", "adminpass", "Admin", "User", "admin@example.com", Role.ADMIN));
        when(userStore.getByUsername(username)).thenThrow(new UserNotFound("User was not found"));

        Response response = commandExecutor.execute(deleteUserCommand);
        assertEquals(
                new Response("User was not found", StatusCode.NOT_FOUND.getCode()).toString(),
                response.toString()
        );

        verify(auditLogger, times(2)).writeChangeLog(anyString(), eq(callerIp), anyString());
        verify(sessionStore).getById(sid);
        verify(userStore).getById(anyString());
        verify(userStore).getByUsername(username);
        verifyNoMoreInteractions(userStore);
        verifyNoMoreInteractions(sessionStore);
    }

    @Test
    void testLoginCommand_Success() throws UserNotFound {
        User testUser = new User("user-id", "testUser", PasswordHasher.hash("testPassword"), "John", "Doe", "john@example.com", Role.NORMAL);
        when(userStore.getByUsername("testUser")).thenReturn(testUser);
        when(sessionStore.getByUid("user-id")).thenReturn(null);
        when(loginRateLimiter.allowLogin("0.0.0.0")).thenReturn(true);

        LoginCommand loginCommand = new LoginCommand(testUser.getUsername(), "testPassword", "0.0.0.0");
        Response response = loginCommand.execute(userManager);

        verify(loginRateLimiter).allowLogin("0.0.0.0");
        verify(loginRateLimiter, times(0)).incrementAttempt(anyString());
        verify(userStore).getByUsername("testUser");
        verify(sessionStore).getByUid("user-id");
        verify(sessionStore, times(1)).save(any(Session.class));
        verify(sessionStore, times(0)).destroy(anyString());
        verify(auditLogger)
                .writeChangeLog(eq(loginCommand.getId()), anyString(), anyString());
        assertEquals(
                StatusCode.SUCCESS.getCode(),
                response.status()
        );
    }

    @Test
    void testLoginCommand_SuccessNewSession() throws UserNotFound {
        User testUser = new User("user-id", "testUser", PasswordHasher.hash("testPassword"), "John", "Doe", "john@example.com", Role.NORMAL);
        when(userStore.getByUsername("testUser")).thenReturn(testUser);
        when(sessionStore.getByUid("user-id")).thenReturn(
                new Session("existing-sid", LocalDateTime.now().plusHours(1), "test-id")
        );
        when(loginRateLimiter.allowLogin("0.0.0.0")).thenReturn(true);

        LoginCommand loginCommand = new LoginCommand(testUser.getUsername(), "testPassword", "0.0.0.0");
        Response response = loginCommand.execute(userManager);

        verify(loginRateLimiter).allowLogin("0.0.0.0");
        verify(loginRateLimiter, times(0)).incrementAttempt(anyString());
        verify(userStore).getByUsername("testUser");
        verify(sessionStore).getByUid("user-id");
        verify(sessionStore, times(1)).save(any(Session.class));
        verify(sessionStore, times(1)).destroy("existing-sid");
        verify(auditLogger).writeChangeLog(eq(loginCommand.getId()), anyString(), anyString());
        assertEquals(
                StatusCode.SUCCESS.getCode(),
                response.status()
        );
    }
    @Test
    void testLoginCommand_IncorrectPassword_ThrowsException() throws UserNotFound {
        User testUser = new User("user-id", "testUser", PasswordHasher.hash("testPassword"), "John", "Doe", "john@example.com", Role.NORMAL);
        when(userStore.getByUsername("testUser")).thenReturn(testUser);
        when(loginRateLimiter.allowLogin("0.0.0.0")).thenReturn(true);

        LoginCommand loginCommand = new LoginCommand("testUser", "wrongPassword", "0.0.0.0");
        Response response = loginCommand.execute(userManager);

        verify(loginRateLimiter).allowLogin("0.0.0.0");
        verify(loginRateLimiter).incrementAttempt("0.0.0.0");
        verify(userStore).getByUsername("testUser");
        verify(sessionStore, times(0)).save(any(Session.class));
        verify(sessionStore, times(0)).destroy(anyString());
        verify(auditLogger).writeFailLogin(anyString(), anyString());
        assertEquals(
                new Response("Incorrect password", StatusCode.UNAUTHORIZED.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testLoginCommand_TooManyLoginAttempts_ThrowsException() throws UserNotFound {
        when(loginRateLimiter.allowLogin("0.0.0.0")).thenReturn(false);

        LoginCommand loginCommand = new LoginCommand("test-user", "testPassword", "0.0.0.0");
        Response response = loginCommand.execute(userManager);

        verify(loginRateLimiter).allowLogin("0.0.0.0");
        verify(loginRateLimiter, times(0)).incrementAttempt(anyString());
        verify(userStore, times(0)).getByUsername(anyString());
        verify(sessionStore, times(0)).getByUid(anyString());
        verify(sessionStore, times(0)).save(any(Session.class));
        verify(sessionStore, times(0)).destroy(anyString());
        verify(auditLogger).writeFailLogin(anyString(), anyString());
        assertEquals(
                new Response("Too many login requests, try again later", StatusCode.TOO_MANY_REQUESTS.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testLoginCommand_UserNotFound_ThrowsException() throws UserNotFound {
        when(loginRateLimiter.allowLogin("0.0.0.0")).thenReturn(true);
        when(userStore.getByUsername("testUser")).thenThrow(new UserNotFound("User not found"));

        LoginCommand loginCommand = new LoginCommand("testUser", "dno", "0.0.0.0");
        Response response = loginCommand.execute(userManager);

        verify(loginRateLimiter).allowLogin(anyString());
        verify(loginRateLimiter).incrementAttempt(anyString());
        verify(userStore).getByUsername("testUser");
        verify(sessionStore, times(0)).getByUid(anyString());
        verify(sessionStore, times(0)).save(any(Session.class));
        verify(sessionStore, times(0)).destroy(anyString());
        verify(auditLogger).writeFailLogin(anyString(), anyString());
        assertEquals(
                new Response("User not found", StatusCode.NOT_FOUND.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testLogoutCommand_Success() {
        LogoutCommand logoutCommand = new LogoutCommand("test-sid");
        Response response = logoutCommand.execute(userManager);

        verify(sessionStore).destroy("test-sid");
        assertEquals(
                new Response("Logged out successfully", StatusCode.SUCCESS.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testRegisterCommand_Success() throws IOException, UserAlreadyExists {
        RegisterCommand registerCommand = new RegisterCommand("testUser", "testPassword", "Jhon", "Doe", "jhon@example.com", "0.0.0.0");
        Response response = registerCommand.execute(userManager);

        verify(auditLogger)
                .writeChangeLog(eq(registerCommand.getId()), eq("0.0.0.0"),
                        eq("User testUser registered successfully"));
        verify(userStore).save(any(User.class));
        verify(sessionStore).save(any(Session.class));
        assertEquals(
                StatusCode.SUCCESS.getCode(),
                response.status()
        );
    }

    @Test
    void testRegisterCommand_UserAlreadyExists_ThrowsException() throws IOException, UserAlreadyExists {
        doThrow(new UserAlreadyExists("User already exists"))
                .when(userStore).save(any(User.class));

        RegisterCommand registerCommand = new RegisterCommand("testUser", "testPassword", "Jhon", "Doe", "jhon@example.com", "0.0.0.0");
        Response response = registerCommand.execute(userManager);

        verify(auditLogger, times(2)).writeChangeLog(any(String.class), eq("0.0.0.0"), anyString());
        verify(userStore).save(any(User.class));
        verify(sessionStore, times(0)).save(any(Session.class));
        assertEquals(
                new Response("User already exists", StatusCode.ALREADY_EXISTS.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testRemoveAdminCommand_Success() throws UserNotFound, IOException {
        User adminUser = new User("admin-id", "adminUser", "adminPassword", "Admin", "User", "admin@example.com", Role.ADMIN);
        User toDemoteUser = new User("user-id", "testUser", "testPassword", "John", "Doe", "john@example.com", Role.ADMIN);

        when(sessionStore.getById("test-sid")).thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "admin-id"));
        when(userStore.getById("admin-id")).thenReturn(adminUser);
        when(userStore.getByUsername("testUser")).thenReturn(toDemoteUser);

        RemoveAdminCommand removeAdminCommand =
                new RemoveAdminCommand("test-sid", "testUser", "0.0.0.0");
        Response response = removeAdminCommand.execute(userManager);

        verify(auditLogger, times(2))
                .writeChangeLog(eq(removeAdminCommand.getId()),
                        eq("0.0.0.0"), anyString());
        verify(sessionStore).getById("test-sid");
        verify(userStore).getById("admin-id");
        verify(userStore).getByUsername("testUser");
        verify(userStore).updateUser(any(User.class));
        assertEquals(Role.NORMAL, toDemoteUser.getRole());
        assertEquals(
                new Response("testUser was successfully demoted", StatusCode.SUCCESS.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testRemoveAdminCommand_InvalidSession_ThrowsException() {
        when(sessionStore.getById("test-sid")).thenReturn(null);

        RemoveAdminCommand removeAdminCommand =
                new RemoveAdminCommand("test-sid", "testUser", "0.0.0.0");
        Response response = removeAdminCommand.execute(userManager);

        verify(auditLogger, times(2)).writeChangeLog(anyString(), eq("0.0.0.0"), anyString());
        verify(sessionStore).getById("test-sid");
        verifyNoMoreInteractions(userStore);
        assertEquals(
                new Response("Invalid or expired session", StatusCode.SUCCESS.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testRemoveAdminCommand_NoPermissions_ThrowsException() throws UserNotFound {
        User nonAdminUser = new User("non-admin-id", "nonAdminUser", "nonAdminPassword", "Non", "Admin", "nonadmin@example.com", Role.NORMAL);

        when(sessionStore.getById("test-sid")).thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "non-admin-id"));
        when(userStore.getById("non-admin-id")).thenReturn(nonAdminUser);

        RemoveAdminCommand removeAdminCommand =
                new RemoveAdminCommand("test-sid", "testUser", "0.0.0.0");
        Response response = removeAdminCommand.execute(userManager);

        verify(auditLogger, times(2)).writeChangeLog(anyString(), eq("0.0.0.0"), anyString());
        verify(sessionStore).getById("test-sid");
        verify(userStore).getById("non-admin-id");
        verifyNoMoreInteractions(userStore);
        assertEquals(
                new Response("You don't have permissions to perform this action", StatusCode.SUCCESS.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testRemoveAdminCommand_UserNotFound_ThrowsException() throws UserNotFound {
        when(sessionStore.getById("test-sid")).thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "admin-id"));
        when(userStore.getById("admin-id")).thenReturn(new User("admin-id", "adminUser", "adminPassword", "Admin", "User", "admin@example.com", Role.ADMIN));
        when(userStore.getByUsername("testUser")).thenThrow(new UserNotFound("User not found"));

        RemoveAdminCommand removeAdminCommand =
                new RemoveAdminCommand("test-sid", "testUser", "0.0.0.0");
        Response response = removeAdminCommand.execute(userManager);

        verify(auditLogger, times(2)).writeChangeLog(anyString(), eq("0.0.0.0"), anyString());
        verify(sessionStore).getById("test-sid");
        verify(userStore).getById("admin-id");
        verify(userStore).getByUsername("testUser");
        verifyNoMoreInteractions(userStore);
        assertEquals(
                new Response("User not found", StatusCode.NOT_FOUND.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testResetPasswordCommand_Success() throws UserNotFound, IOException {
        User testUser = new User("user-id", "testUser", PasswordHasher.hash("oldPassword"), "John", "Doe", "john@example.com", Role.NORMAL);

        when(sessionStore.getById("test-sid")).thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "user-id"));
        when(userStore.getById("user-id")).thenReturn(testUser);

        ResetPasswordCommand resetPasswordCommand =
                new ResetPasswordCommand("test-sid", "testUser", "oldPassword", "newPassword", "0.0.0.0");
        Response response = resetPasswordCommand.execute(userManager);

        verify(auditLogger, times(2))
                .writeChangeLog(eq(resetPasswordCommand.getId()), eq("0.0.0.0"), anyString());
        verify(sessionStore).getById("test-sid");
        verify(userStore).getById("user-id");
        verify(userStore).updateUser(any(User.class));
        assertEquals(
                PasswordHasher.hash("newPassword"), testUser.getPassword()
        );
        assertEquals(
                new Response("Password successfully updated", StatusCode.SUCCESS.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testResetPasswordCommand_InvalidSession_ThrowsException() {
        when(sessionStore.getById("test-sid")).thenReturn(null);

        ResetPasswordCommand resetPasswordCommand =
                new ResetPasswordCommand("test-sid", "testUser", "oldPassword", "newPassword", "0.0.0.0");
        Response response = resetPasswordCommand.execute(userManager);

        verify(auditLogger, times(2)).writeChangeLog(anyString(), eq("0.0.0.0"), anyString());
        verify(sessionStore).getById("test-sid");
        verifyNoMoreInteractions(userStore);
        assertEquals(
                new Response("Invalid or expired session", StatusCode.UNAUTHORIZED.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testResetPasswordCommand_IncorrectPassword_ThrowsException() throws UserNotFound {
        User testUser = new User("user-id", "testUser", PasswordHasher.hash("wrongPassword"), "John", "Doe", "john@example.com", Role.NORMAL);

        when(sessionStore.getById("test-sid")).thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "user-id"));
        when(userStore.getById("user-id")).thenReturn(testUser);

        ResetPasswordCommand resetPasswordCommand =
                new ResetPasswordCommand("test-sid", "testUser", "oldPassword", "newPassword", "0.0.0.0");
        Response response = resetPasswordCommand.execute(userManager);

        verify(auditLogger, times(2)).writeChangeLog(anyString(), eq("0.0.0.0"), anyString());
        verify(sessionStore).getById("test-sid");
        verify(userStore).getById("user-id");
        verifyNoMoreInteractions(userStore);
        assertEquals(
                new Response("Invalid password", StatusCode.UNAUTHORIZED.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testResetPasswordCommand_UserNotFound_ThrowsException() throws UserNotFound {
        when(sessionStore.getById("test-sid")).thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "user-id"));
        when(userStore.getById("user-id")).thenThrow(new UserNotFound("User not found"));

        ResetPasswordCommand resetPasswordCommand =
                new ResetPasswordCommand("test-sid", "testUser", "oldPassword", "newPassword", "0.0.0.0");
        Response response = resetPasswordCommand.execute(userManager);

        verify(auditLogger, times(2)).writeChangeLog(anyString(), eq("0.0.0.0"), anyString());
        verify(sessionStore).getById("test-sid");
        verify(userStore).getById("user-id");
        verifyNoMoreInteractions(userStore);
        assertEquals(
                new Response("User not found", StatusCode.UNAUTHORIZED.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testUpdateUserCommand_Success() throws UserNotFound, IOException {
        User testUser = new User("user-id", "testUser", "password", "John", "Doe", "john@example.com", Role.NORMAL);

        when(sessionStore.getById("test-sid")).thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "user-id"));
        when(userStore.getById("user-id")).thenReturn(testUser);

        UpdateUserCommand updateUserCommand =
                new UpdateUserCommand("test-sid", Optional.of("NotJhonDoeAnymoreAnylonger"),
                        Optional.of("Found"),
                Optional.of("Doe"), Optional.of("mail"), "0.0.0.0");
        Response response = updateUserCommand.execute(userManager);

        verify(auditLogger, times(2))
                .writeChangeLog(eq(updateUserCommand.getId()), eq("0.0.0.0"), anyString());
        verify(sessionStore).getById("test-sid");
        verify(userStore).getById("user-id");
        verify(userStore).updateUser(any(User.class));
        assertEquals(testUser.getUsername(), "NotJhonDoeAnymoreAnylonger");
        assertEquals(testUser.getFirstname(), "Found");
        assertEquals(testUser.getLastname(), "Doe");
        assertEquals(testUser.getEmail(), "mail");
        assertEquals(
                new Response("Successfully updated user", StatusCode.SUCCESS.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testUpdateUserCommand_InvalidSession_ThrowsException() {
        when(sessionStore.getById("test-sid")).thenReturn(null);

        UpdateUserCommand updateUserCommand =
                new UpdateUserCommand("test-sid", Optional.of("newUsername"), Optional.of("newFirstname"),
                        Optional.of("newLastname"), Optional.of("newEmail"), "0.0.0.0");
        Response response = updateUserCommand.execute(userManager);

        verify(auditLogger, times(2)).writeChangeLog(anyString(), eq("0.0.0.0"), anyString());
        verify(sessionStore).getById("test-sid");
        verifyNoMoreInteractions(userStore);
        assertEquals(
                new Response("Invalid or expired session", StatusCode.UNAUTHORIZED.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testUpdateUserCommand_UserNotFound_ThrowsException() throws UserNotFound {
        when(sessionStore.getById("test-sid")).thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "user-id"));
        when(userStore.getById("user-id")).thenThrow(new UserNotFound("User not found"));

        UpdateUserCommand updateUserCommand =
                new UpdateUserCommand("test-sid", Optional.of("newUsername"), Optional.of("newFirstname"),
                        Optional.of("newLastname"), Optional.of("newEmail"), "0.0.0.0");
        Response response = updateUserCommand.execute(userManager);

        verify(auditLogger, times(2)).writeChangeLog(anyString(), eq("0.0.0.0"), anyString());
        verify(sessionStore).getById("test-sid");
        verify(userStore).getById("user-id");
        verifyNoMoreInteractions(userStore);
        assertEquals(
                new Response("User not found", StatusCode.UNAUTHORIZED.getCode()).toString(),
                response.toString()
        );
    }

    @Test
    void testAddAdminCommand_IOException() throws UserNotFound, IOException {
        String sid = "test-sid";
        String username = "topromote";
        String callerIp = "0.0.0.0";
        AddAdminCommand addAdminCommand = new AddAdminCommand(sid, username, callerIp);

        User toPromoteUser =
                new User("user-id-1", "topromote", "password", "John", "Doe", "john@example.com", Role.NORMAL);

        when(sessionStore.getById(sid))
                .thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "user-id"));
        when(userStore.getById("user-id"))
                .thenReturn(new User("user-id", "username", "password", "John", "Doe", "john@example.com", Role.ADMIN));
        when(userStore.getByUsername("topromote"))
                .thenReturn(toPromoteUser);

        doThrow(new IOException("Test IOException")).when(userStore).updateUser(any(User.class));

        assertThrows(RuntimeException.class, () -> commandExecutor.execute(addAdminCommand));
        verify(auditLogger).writeChangeLog(any(), eq("0.0.0.0"), eq("Test IOException"));
    }

    @Test
    void testDeleteUserCommand_IOException() throws UserNotFound, IOException {
        String sid = "test-sid";
        String username = "todelete";
        String callerIp = "0.0.0.0";
        DeleteUserCommand deleteUserCommand = new DeleteUserCommand(sid, username, callerIp);

        User toDeleteUser =
                new User("user-id-1", "todelete", "password", "John", "Doe", "john@example.com", Role.ADMIN);
        Session toDeleteSession =
                new Session("to-delete-sid", LocalDateTime.now(), "user-id-1");

        when(sessionStore.getById(sid))
                .thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "user-id"));
        when(userStore.getById("user-id"))
                .thenReturn(new User("user-id", "admin", "adminpass", "Admin", "User", "admin@example.com", Role.ADMIN));
        when(userStore.getByUsername(username))
                .thenReturn(toDeleteUser);
        when(sessionStore.getByUid("user-id-1")).thenReturn(toDeleteSession);

        doThrow(new IOException("Test IOException")).when(userStore).deleteUser(any(User.class));

        assertThrows(RuntimeException.class, () -> commandExecutor.execute(deleteUserCommand));
        verify(auditLogger).writeChangeLog(any(), eq("0.0.0.0"), eq("Test IOException"));
    }


    @Test
    void testRegisterCommand_IOException() throws IOException, UserAlreadyExists {
        RegisterCommand registerCommand = new RegisterCommand("testUser", "testPassword", "Jhon", "Doe", "jhon@example.com", "0.0.0.0");

        doThrow(new IOException("Test IOException")).when(userStore).save(any(User.class));

        assertThrows(RuntimeException.class, () -> commandExecutor.execute(registerCommand));
        verify(auditLogger).writeChangeLog(any(), eq("0.0.0.0"), eq("Test IOException"));
    }

    @Test
    void testRemoveAdminCommand_IOException() throws UserNotFound, IOException {
        User adminUser = new User("admin-id", "adminUser", "adminPassword", "Admin", "User", "admin@example.com", Role.ADMIN);
        User toDemoteUser = new User("user-id", "testUser", "testPassword", "John", "Doe", "john@example.com", Role.ADMIN);

        when(sessionStore.getById("test-sid")).thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "admin-id"));
        when(userStore.getById("admin-id")).thenReturn(adminUser);
        when(userStore.getByUsername("testUser")).thenReturn(toDemoteUser);

        RemoveAdminCommand removeAdminCommand =
                new RemoveAdminCommand("test-sid", "testUser", "0.0.0.0");

        doThrow(new IOException("Test IOException")).when(userStore).updateUser(any(User.class));

        assertThrows(RuntimeException.class, () -> commandExecutor.execute(removeAdminCommand));
        verify(auditLogger).writeChangeLog(any(), eq("0.0.0.0"), eq("Test IOException"));
    }


    @Test
    void testResetPasswordCommand_IOException() throws UserNotFound, IOException {
        User testUser = new User("user-id", "testUser", PasswordHasher.hash("oldPassword"), "John", "Doe", "john@example.com", Role.NORMAL);

        when(sessionStore.getById("test-sid")).thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "user-id"));
        when(userStore.getById("user-id")).thenReturn(testUser);

        ResetPasswordCommand resetPasswordCommand =
                new ResetPasswordCommand("test-sid", "testUser", "oldPassword", "newPassword", "0.0.0.0");

        doThrow(new IOException("Test IOException")).when(userStore).updateUser(any(User.class));

        assertThrows(RuntimeException.class, () -> commandExecutor.execute(resetPasswordCommand));
        verify(auditLogger).writeChangeLog(any(), eq("0.0.0.0"), eq("Test IOException"));
    }

    @Test
    void testUpdateUserCommand_IOException() throws UserNotFound, IOException {
        User testUser = new User("user-id", "testUser", "password", "John", "Doe", "john@example.com", Role.NORMAL);

        when(sessionStore.getById("test-sid")).thenReturn(new Session("test-sid", LocalDateTime.now().plusHours(1), "user-id"));
        when(userStore.getById("user-id")).thenReturn(testUser);

        UpdateUserCommand updateUserCommand =
                new UpdateUserCommand("test-sid", Optional.of("NotJhonDoeAnymoreAnylonger"),
                        Optional.of("Found"),
                        Optional.of("Doe"), Optional.of("mail"), "0.0.0.0");

        doThrow(new IOException("Test IOException")).when(userStore).updateUser(any(User.class));

        assertThrows(RuntimeException.class, () -> commandExecutor.execute(updateUserCommand));
        verify(auditLogger).writeChangeLog(any(), eq("0.0.0.0"), eq("Test IOException"));
    }
}