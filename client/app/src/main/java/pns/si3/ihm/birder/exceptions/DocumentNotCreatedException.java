package pns.si3.ihm.birder.exceptions;

/**
 * Document not created exception.
 */
public class DocumentNotCreatedException extends Exception {
	public DocumentNotCreatedException() {
		super("The document was not created in the database.");
	}
}
