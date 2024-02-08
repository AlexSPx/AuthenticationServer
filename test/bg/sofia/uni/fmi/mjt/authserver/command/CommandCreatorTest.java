package bg.sofia.uni.fmi.mjt.authserver.command;

import bg.sofia.uni.fmi.mjt.authserver.command.handlers.AddAdminCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.DeleteUserCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.LoginCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.LoginWithSessionCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.LogoutCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.RegisterCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.RemoveAdminCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.ResetPasswordCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.UpdateUserCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandCreatorTest {
    @Test
    void testCreateLoginWithSessionCommand_Success() {
        String input = "login --session-id random0sid";

        Command command = CommandCreator.create(input, "0.0.0.0");

        assertTrue(command instanceof LoginWithSessionCommand);
        LoginWithSessionCommand loginCommand = (LoginWithSessionCommand) command;
        assertEquals("random0sid", loginCommand.getSid());
        assertEquals("0.0.0.0", loginCommand.getCallerIp());
    }

    @Test
    void testCreateRegisterCommand_Success() {
        String input = "register --username user1 --password pass123 --first-name John --last-name Doe --email john@example.com";
        Command command = CommandCreator.create(input, "0.0.0.0");

        assertTrue(command instanceof RegisterCommand);
        RegisterCommand registerCommand = (RegisterCommand) command;
        assertEquals("user1", registerCommand.getUsername());
        assertEquals("pass123", registerCommand.getPassword());
        assertEquals("John", registerCommand.getFirstname());
        assertEquals("Doe", registerCommand.getLastname());
        assertEquals("john@example.com", registerCommand.getEmail());
    }

    @Test
    void testCreateLoginCommand_ValidArguments_ReturnsLoginCommand() {
        String input = "login --username user1 --password pass123";
        Command command = CommandCreator.create(input, "0.0.0.0");

        assertTrue(command instanceof LoginCommand);
        LoginCommand loginCommand = (LoginCommand) command;
        assertEquals("user1", loginCommand.getUsername());
        assertEquals("pass123", loginCommand.getPassword());
        assertEquals("0.0.0.0", loginCommand.getCallerIp());
    }

    @Test
    void testCreateUpdateUserCommand_ValidArguments_ReturnsUpdateUserCommand() {
        String input = "update-user --session-id session123 --new-username newuser --new-first-name New --new-last-name User --new-email newuser@example.com";
        Command command = CommandCreator.create(input, "0.0.0.0");

        assertTrue(command instanceof UpdateUserCommand);
        UpdateUserCommand updateUserCommand = (UpdateUserCommand) command;
        assertEquals("session123", updateUserCommand.getSid());
        assertEquals("newuser", updateUserCommand.getNewUsername().get());
        assertEquals("New", updateUserCommand.getNewFirstname().get());
        assertEquals("User", updateUserCommand.getNewLastname().get());
        assertEquals("newuser@example.com", updateUserCommand.getNewEmail().get());
        assertEquals("0.0.0.0", updateUserCommand.getCallerIp());
    }

    @Test
    void testCreateResetPasswordCommand_ValidArguments_ReturnsResetPasswordCommand() {
        String input = "reset-password --session-id session123 --username user1 --old-password oldpass --new-password newpass";
        Command command = CommandCreator.create(input, "0.0.0.0");

        assertTrue(command instanceof ResetPasswordCommand);
        ResetPasswordCommand resetPasswordCommand = (ResetPasswordCommand) command;
        assertEquals("session123", resetPasswordCommand.getSid());
        assertEquals("user1", resetPasswordCommand.getUsername());
        assertEquals("oldpass", resetPasswordCommand.getOldPassword());
        assertEquals("newpass", resetPasswordCommand.getNewPassword());
        assertEquals("0.0.0.0", resetPasswordCommand.getCallerIp());
    }

    @Test
    void testCreateLogoutCommand_ValidArguments_ReturnsLogoutCommand() {
        String input = "logout --session-id session123";
        Command command = CommandCreator.create(input, "0.0.0.0");

        assertTrue(command instanceof LogoutCommand);
        LogoutCommand logoutCommand = (LogoutCommand) command;
        assertEquals("session123", logoutCommand.getSid());
    }

    @Test
    void testCreateAddAdminCommand_ValidArguments_ReturnsAddAdminCommand() {
        String input = "add-admin-user --session-id session123 --username admin1";
        Command command = CommandCreator.create(input, "0.0.0.0");

        assertTrue(command instanceof AddAdminCommand);
        AddAdminCommand addAdminCommand = (AddAdminCommand) command;
        assertEquals("session123", addAdminCommand.getSid());
        assertEquals("admin1", addAdminCommand.getUsername());
        assertEquals("0.0.0.0", addAdminCommand.getCallerIp());
    }

    @Test
    void testCreateRemoveAdminCommand_ValidArguments_ReturnsRemoveAdminCommand() {
        String input = "remove-admin-user --session-id session123 --username admin1";
        Command command = CommandCreator.create(input, "0.0.0.0");

        assertTrue(command instanceof RemoveAdminCommand);
        RemoveAdminCommand removeAdminCommand = (RemoveAdminCommand) command;
        assertEquals("session123", removeAdminCommand.getSid());
        assertEquals("admin1", removeAdminCommand.getUsername());
        assertEquals("0.0.0.0", removeAdminCommand.getCallerIp());
    }

    @Test
    void testCreateDeleteUserCommand_ValidArguments_ReturnsDeleteUserCommand() {
        String input = "delete-user --session-id session123 --username user1";
        Command command = CommandCreator.create(input, "0.0.0.0");

        assertTrue(command instanceof DeleteUserCommand);
        DeleteUserCommand deleteUserCommand = (DeleteUserCommand) command;
        assertEquals("session123", deleteUserCommand.getSid());
        assertEquals("user1", deleteUserCommand.getUsername());
        assertEquals("0.0.0.0", deleteUserCommand.getCallerIp());
    }

    @Test
    void testCreateLoginWithSessionCommand_NoSession() {
        String input = "login --session-id";

        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateLoginCommand_MissingUsername_ThrowsIllegalArgumentException() {
        String input = "login --password pass123";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateLoginCommand_MissingPassword_ThrowsIllegalArgumentException() {
        String input = "login --username user1";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateLoginCommand_MissingArguments_ThrowsIllegalArgumentException() {
        String input = "login";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateLoginCommand_TooManyArguments_ThrowsIllegalArgumentException() {
        String input = "login --username user1 --password pass123 extraArg1 extraArg2";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateRegisterCommand_MissingUsername_ThrowsIllegalArgumentException() {
        String input = "register --password pass123 --first-name John --last-name Doe --email john@example.com";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateRegisterCommand_MissingPassword_ThrowsIllegalArgumentException() {
        String input = "register --username user1 --first-name John --last-name Doe --email john@example.com";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateRegisterCommand_MissingFirstname_ThrowsIllegalArgumentException() {
        String input = "register --username user1 --password pass123 --last-name Doe --email john@example.com";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateRegisterCommand_MissingLastname_ThrowsIllegalArgumentException() {
        String input = "register --username user1 --password pass123 --first-name John --email john@example.com";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateRegisterCommand_MissingEmail_ThrowsIllegalArgumentException() {
        String input = "register --username user1 --password pass123 --first-name John --last-name Doe";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateRegisterCommand_MissingArguments_ThrowsIllegalArgumentException() {
        String input = "register";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateRegisterCommand_TooManyArguments_ThrowsIllegalArgumentException() {
        String input = "register --username user1 --password pass123 --first-name John --last-name Doe " +
                "--email john@example.com extraArg1 extraArg2";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateAddAdminCommand_MissingSessionId_ThrowsIllegalArgumentException() {
        String input = "add-admin-user --username adminUser";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateAddAdminCommand_MissingUsername_ThrowsIllegalArgumentException() {
        String input = "add-admin-user --session-id sessionId";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateAddAdminCommand_MissingArguments_ThrowsIllegalArgumentException() {
        String input = "add-admin-user";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateAddAdminCommand_TooManyArguments_ThrowsIllegalArgumentException() {
        String input = "add-admin-user --session-id sessionId --username adminUser extraArg1 extraArg2";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateRemoveAdminCommand_MissingSessionId_ThrowsIllegalArgumentException() {
        String input = "remove-admin-user --username adminUser";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateRemoveAdminCommand_MissingUsername_ThrowsIllegalArgumentException() {
        String input = "remove-admin-user --session-id sessionId";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateRemoveAdminCommand_MissingArguments_ThrowsIllegalArgumentException() {
        String input = "remove-admin-user";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateRemoveAdminCommand_TooManyArguments_ThrowsIllegalArgumentException() {
        String input = "remove-admin-user --session-id sessionId --username adminUser extraArg1 extraArg2";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateDeleteUserCommand_MissingSessionId_ThrowsIllegalArgumentException() {
        String input = "delete-user --username userToDelete";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateDeleteUserCommand_MissingUsername_ThrowsIllegalArgumentException() {
        String input = "delete-user --session-id sessionId";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateDeleteUserCommand_MissingArguments_ThrowsIllegalArgumentException() {
        String input = "delete-user";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateDeleteUserCommand_TooManyArguments_ThrowsIllegalArgumentException() {
        String input = "delete-user --session-id sessionId --username userToDelete extraArg1 extraArg2";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateLogoutCommand_MissingSessionId_ThrowsIllegalArgumentException() {
        String input = "logout";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateLogoutCommand_TooManyArguments_ThrowsIllegalArgumentException() {
        String input = "logout --session-id sessionId extraArg1 extraArg2";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateResetPasswordCommand_MissingSessionId_ThrowsIllegalArgumentException() {
        String input = "reset-password --username user1 --old-password oldPass --new-password newPass";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateResetPasswordCommand_MissingUsername_ThrowsIllegalArgumentException() {
        String input = "reset-password --session-id sessionId --old-password oldPass --new-password newPass";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateResetPasswordCommand_MissingOldPassword_ThrowsIllegalArgumentException() {
        String input = "reset-password --session-id sessionId --username user1 --new-password newPass";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateResetPasswordCommand_MissingNewPassword_ThrowsIllegalArgumentException() {
        String input = "reset-password --session-id sessionId --username user1 --old-password oldPass";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateResetPasswordCommand_TooManyArguments_ThrowsIllegalArgumentException() {
        String input = "reset-password --session-id sessionId --username user1 --old-password oldPass --new-password newPass extraArg";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateUpdateUserCommand_MissingSessionId_ThrowsIllegalArgumentException() {
        String input = "update-user --new-username newUser --new-first-name John --new-last-name Doe --new-email john@example.com";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateUpdateUserCommand_TooManyArguments_ThrowsIllegalArgumentException() {
        String input = "update-user --session-id sessionId --new-username newUser --new-first-name John --new-last-name Doe --new-email john@example.com extraArg";
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }

    @Test
    void testCreateNoSuchCommand_ThrowsRuntimeException() {
        String input = "logii";
        assertThrows(RuntimeException.class, () -> CommandCreator.create(input, "0.0.0.0"));
    }
}
