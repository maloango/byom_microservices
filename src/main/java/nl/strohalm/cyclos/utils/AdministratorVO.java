package nl.strohalm.cyclos.utils;

/**
 *
 * @author Lue Infoservices
 */
public class AdministratorVO {
    
    private long     id;
     private String  name;
    private String adminGroup;
    
    private String fullName;
    
    private String email;
    
    public AdministratorVO(){
        
    }

    public AdministratorVO(String adminGroup, String loginName, String fullName, String email, Long id) {
        
       
        this.id=id;
        this.name=loginName;
        this.adminGroup = adminGroup;
        
        this.fullName = fullName;
        this.email = email;
    }

    public String getAdminGroup() {
        return adminGroup;
    }

    public void setAdminGroup(String adminGroup) {
        this.adminGroup = adminGroup;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
      public long getId() {
        return id;
    }
     public long setId(long id) {
        return this.id=id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
   
    
}
