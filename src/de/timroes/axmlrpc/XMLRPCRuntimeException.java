package de.timroes.axmlrpc;

/**
 *
 * @author Tim Roes
 */
@SuppressWarnings("serial")
public class XMLRPCRuntimeException extends RuntimeException {

	public XMLRPCRuntimeException(String ex) {
		super(ex);
	}

	public XMLRPCRuntimeException(Exception ex) {
		super(ex);
	}
	
}