package pns.si3.ihm.birder.exceptions;

/**
 * Document not found exception.
 */
public class DocumentNotFoundException extends Exception {
	public DocumentNotFoundException() {
		super("The requested document was not found in the database.");
	}
}
