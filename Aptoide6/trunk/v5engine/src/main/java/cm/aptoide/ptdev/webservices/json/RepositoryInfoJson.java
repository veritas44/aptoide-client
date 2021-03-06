
package cm.aptoide.ptdev.webservices.json;

import cm.aptoide.ptdev.model.*;
import cm.aptoide.ptdev.model.Error;


import java.util.List;

public class RepositoryInfoJson{

   	public RepositoryInfoListing listing;

   	public String status;


    public List<Error> errors;

    public List<cm.aptoide.ptdev.model.Error> getErrors() {
        return errors;
    }

 	public RepositoryInfoListing getListing(){
		return this.listing;
	}
	public void setListing(RepositoryInfoListing listing){
		this.listing = listing;
	}
 	public String getStatus(){
		return this.status;
	}
	public void setStatus(String status){
		this.status = status;
	}


}
