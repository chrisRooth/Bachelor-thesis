package Applications;

/**
 * Verification message inteface for Volvo CE Verification tool 
 * 
 * @author Christoffer Roth (roth.christoffer@gmail.com)
 * @version 1.0 (2018-04-03) 
 */
public interface VerificationMessageInterface {
	
	public String getVerificationResult();
	
	public int getVerificationFailureResult();

}
