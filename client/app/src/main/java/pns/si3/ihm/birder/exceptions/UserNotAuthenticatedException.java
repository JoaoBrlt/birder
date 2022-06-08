package pns.si3.ihm.birder.exceptions;

/**
 * User not authenticated exception.
 */
public class UserNotAuthenticatedException extends Exception {
	public UserNotAuthenticatedException() {
		super("The user was not authenticated.");
	}
}
