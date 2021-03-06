/**
 * 
 */
package cm.aptoide.pt.utils.webservices;

/**
 * 
 * @author rafael
 * @since summerinternship2011
 * 
 * Defines the return status of a requested web service.
 */
public enum EnumResponseStatus {
	
	OK, FAIL;
	
	public static EnumResponseStatus valueOfToUpper(String name) {
		return EnumResponseStatus.valueOf(name.toUpperCase());
	}
	
}
